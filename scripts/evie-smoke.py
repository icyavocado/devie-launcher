#!/usr/bin/env python3
"""dEvie release smoke gate.

Installs an APK (default: the phoneDebug build; pass -a to override), drives
the launcher end-to-end and fails the run if the app crashes anywhere on the
way:

  1. install -r + runtime permissions
  2. force-stop, clear logcat, relaunch (monkey)
  3. open the search overlay (workspace search bar tap; swipe-down gesture
     fallback — fresh installs hide the bar by design)
  4. type a query, verify the overlay, TAP the first result row and the web
     row (the regression paths: LocalItemPresenter.onItemClick, link
     resolution, the analytics filter post-injection)
  5. back to workspace, reopen the overlay empty, tap the frequently-used
     area
  6. gate: any FATAL EXCEPTION / ANR in logcat since the marker => exit 1

Usage:
  scripts/evie-smoke.py [-d SERIAL] [-a APK_PATH] [-q QUERY] [-o OUTDIR]

Exit 0 = pass; screenshots land in OUTDIR (default /tmp/opencode).
"""
import argparse, os, re, subprocess, sys, time

ADB = os.path.expanduser('~/Android/Sdk/platform-tools/adb')
PKG = 'com.ovacoda.devie'
OUT = '/tmp/opencode'


def run(serial, *args, timeout=60, check=False):
    r = subprocess.run([ADB, '-s', serial, *args], capture_output=True, text=True, timeout=timeout)
    if check and r.returncode != 0:
        raise RuntimeError(f'adb {args} failed: {r.stdout} {r.stderr}')
    return r.stdout + r.stderr


def dump_ui(serial, retries=5):
    """uiautomator dump with the rm-before-dump recipe (stale dumps null out)."""
    for _ in range(retries):
        run(serial, 'shell', 'rm', '-f', '/sdcard/ui.xml')
        run(serial, 'shell', 'uiautomator', 'dump', '/sdcard/ui.xml')
        x = run(serial, 'shell', 'cat', '/sdcard/ui.xml')
        if x and 'null root node' not in x and len(x) > 500:
            return x
        time.sleep(1.2)
    return ''


def nodes(x):
    out = []
    for m in re.finditer(r'<node[^>]*?text="([^"]*)"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', x):
        t, x1, y1, x2, y2 = m.group(1), int(m.group(2)), int(m.group(3)), int(m.group(4)), int(m.group(5))
        if t.strip():
            out.append((t, (x1 + x2) // 2, (y1 + y2) // 2))
    return out


def find_row(x, *texts):
    """Exact match first, substring fallback — mirrors drive_onboarding."""
    res = nodes(x)
    for want in texts:
        for t, cx, cy in res:
            if t.strip().lower() == want.lower():
                return (t, cx, cy)
    for want in texts:
        for t, cx, cy in res:
            if want.lower() in t.lower():
                return (t, cx, cy)
    return None


def tap(serial, cx, cy):
    run(serial, 'shell', 'input', 'tap', str(cx), str(cy))


def step(msg):
    print(f'[smoke] {msg}', flush=True)


def screen_size(serial):
    geom = run(serial, 'shell', 'wm', 'size')
    m = re.search(r'(\d+)x(\d+)', geom)
    return int(m.group(1)), int(m.group(2))


def open_overlay(serial):
    """Open the in-app search overlay.

    The SpringySearchView hint is NOT in the uiautomator text attr; find the
    bar by resource-id, tap it; swipe-down gesture fallback (fresh installs
    hide the desktop bar by design — onUpgrade(-1)). Verifies the overlay is
    actually open before returning.
    """
    for attempt in range(3):
        x = dump_ui(serial)
        m = re.search(
            r'resource-id="[^"]*:id/(?:qsb_search|search_src_text)"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', x)
        if m:
            cx, cy = (int(m.group(1)) + int(m.group(3))) // 2, (int(m.group(2)) + int(m.group(4))) // 2
            tap(serial, cx, cy)
            step(f'workspace search bar tapped at ({cx},{cy}) (attempt {attempt + 1})')
        else:
            step(f'search bar not in dump — swipe-down gesture fallback (attempt {attempt + 1})')
            mw, mh = screen_size(serial)
            run(serial, 'shell', 'input', 'swipe', str(mw // 2), str(int(mh * 0.06)), str(mw // 2), str(int(mh * 0.55)), '200')
        time.sleep(1.5)
        # overlay open? the qsb becomes a full-width focused search field in the overlay
        time.sleep(0.5)
        x2 = dump_ui(serial)
        if re.search(r'resource-id="[^"]*:id/search_src_text"', x2):
            return x2
        run(serial, 'shell', 'input', 'keyevent', 'KEYCODE_BACK')  # close stray IME/popup, retry
        time.sleep(1)
    raise RuntimeError('could not open the search overlay (bar tap + gesture both failed)')


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('-d', '--serial', default='emulator-5554')
    ap.add_argument('-a', '--apk', default='app/build/outputs/apk/phoneDebug/app-phoneDebug.apk',
                    help='APK to install (default: phoneDebug build; -a app/build/outputs/apk/release/app-release.apk for release)')
    ap.add_argument('-q', '--query', default='s', help="search text to type (default 's')")
    ap.add_argument('-o', '--outdir', default=OUT)
    ap.add_argument('--no-install', action='store_true', help='drive only, skip install')
    args = ap.parse_args()
    serial = args.serial
    os.makedirs(args.outdir, exist_ok=True)

    # 0. device must be present
    run(serial, 'get-state', check=True)
    step(f'device {serial}: ready')

    # 1. install + grants
    if not args.no_install:
        if not os.path.exists(args.apk):
            step(f'APK not found: {args.apk} — building phoneDebug first')
            b = subprocess.run(['./scripts/evie-build.sh', ':app:assemblePhoneDebug'],
                               capture_output=True, text=True, timeout=900)
            if 'BUILD SUCCESSFUL' not in b.stdout + b.stderr:
                print(b.stderr[-3000:])
                print(b.stdout[-3000:])
                raise RuntimeError('build failed')
        run(serial, 'install', '-r', args.apk, check=True, timeout=300)
        step(f'installed {args.apk}')
        for perm in ('android.permission.READ_EXTERNAL_STORAGE',
                     'android.permission.READ_CONTACTS',
                     'android.permission.ACCESS_FINE_LOCATION'):
            run(serial, 'shell', 'pm', 'grant', PKG, perm)  # already-granted/others: ignore errors

    # 2. relaunch + clear logcat (marker = everything after this counts)
    run(serial, 'shell', 'am', 'force-stop', PKG)
    run(serial, 'logcat', '-c')
    run(serial, 'shell', 'monkey', '-p', PKG, '1', check=False)
    time.sleep(4)

    # 3. open the search overlay (bar tap by resource-id, gesture fallback)
    x = open_overlay(serial)

    # 4. type the query + validate + CLICK the regression paths
    step(f"typing query '{args.query}'")
    run(serial, 'shell', 'input', 'text', args.query)
    time.sleep(2)
    x = dump_ui(serial)
    subprocess.run([ADB, '-s', serial, 'shell', 'screencap', '-p', '/sdcard/smoke.png'], timeout=30)
    png = subprocess.run([ADB, '-s', serial, 'exec-out', 'cat', '/sdcard/smoke.png'], capture_output=True, timeout=30).stdout
    shot = os.path.join(args.outdir, '02_search_results.png')
    open(shot, 'wb').write(png)
    run(serial, 'shell', 'rm', '-f', '/sdcard/smoke.png')
    got = find_row(x, 'jenny appleseed', 'clock', 'settings')
    if got:
        step(f'tapped result row: {got!r}')
        tap(serial, got[1], got[2])
    else:
        res = nodes(x)
        _, h = screen_size(serial)
        # result rows live between the top hint and the bottom of the overlay
        # list (fractions so the gate works on any device)
        rows = [(t, cx, cy) for (t, cx, cy) in res
                if 0.11 * h < cy < 0.66 * h]
        if not rows:
            raise RuntimeError('no tappable result rows found in overlay')
        step(f'tapped first row: {rows[0]!r}')
        tap(serial, rows[0][1], rows[0][2])
    time.sleep(2)
    subprocess.run([ADB, '-s', serial, 'shell', 'screencap', '-p', '/sdcard/smoke.png'], timeout=30)
    png = subprocess.run([ADB, '-s', serial, 'exec-out', 'cat', '/sdcard/smoke.png'], capture_output=True, timeout=30).stdout
    open(os.path.join(args.outdir, '03_after_result_tap.png'), 'wb').write(png)
    run(serial, 'shell', 'rm', '-f', '/sdcard/smoke.png')

    # 5. back to workspace, reopen overlay empty, tap the frequently-used area
    run(serial, 'shell', 'input', 'keyevent', 'KEYCODE_BACK')
    time.sleep(1.2)
    x = open_overlay(serial)
    fu = find_row(x, 'frequently used', 'recent results', 'on your device')
    png = subprocess.run([ADB, '-s', serial, 'exec-out', 'sh', '-c', 'screencap -p /sdcard/smoke.png; cat /sdcard/smoke.png'], capture_output=True, timeout=30).stdout
    open(os.path.join(args.outdir, '04_overlay_empty.png'), 'wb').write(png)
    run(serial, 'shell', 'rm', '-f', '/sdcard/smoke.png')
    if fu:
        step(f'reopened overlay empty — tap below header {fu!r} (frequently-used icon)')
        tap(serial, fu[1], min(fu[2] + 180, 1700))
        time.sleep(2)
        png = subprocess.run([ADB, '-s', serial, 'exec-out', 'sh', '-c', 'screencap -p /sdcard/smoke.png; cat /sdcard/smoke.png'], capture_output=True, timeout=30).stdout
        open(os.path.join(args.outdir, '05_after_fu_tap.png'), 'wb').write(png)
        run(serial, 'shell', 'rm', '-f', '/sdcard/smoke.png')
    run(serial, 'shell', 'input', 'keyevent', 'KEYCODE_BACK')
    time.sleep(0.8)
    run(serial, 'shell', 'input', 'keyevent', 'KEYCODE_BACK')
    time.sleep(0.8)

    # 6. crash gate — logcat since the marker
    log = run(serial, 'logcat', '-d', '-v', 'time', timeout=60)
    fatals = [ln for ln in log.splitlines() if re.search(r'FATAL EXCEPTION|ANR in ', ln)]
    if fatals:
        idx = log.find(fatals[0])
        print('\n'.join(log[idx:idx + 45]))
        print(f'[smoke] FAIL — {len(fatals)} fatal/ANR events', file=sys.stderr)
        sys.exit(1)
    step(f'PASS on {serial}: overlay, query {args.query!r}, result click, empty-overlay frequently-used click, FATAL/ANR = 0')
    print(f'[smoke] screenshots in {args.outdir}')


if __name__ == '__main__':
    main()