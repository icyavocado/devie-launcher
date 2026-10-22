#!/usr/bin/env python3
"""Drive dEvie's first-run onboarding on the emulator with per-step screenshots."""
import re, subprocess, sys, time, os

ADB = os.path.expanduser('~/Android/Sdk/platform-tools/adb')
DEV = 'emulator-5554'
OUT = '/tmp/opencode'

def adb(*args, timeout=30):
    return subprocess.run([ADB, '-s', DEV, *args], capture_output=True, text=True, timeout=timeout)

def dump_ui(retries=5):
    for _ in range(retries):
        adb('shell', 'rm', '/sdcard/ui.xml')
        adb('shell', 'uiautomator', 'dump', '/sdcard/ui.xml')
        r = adb('shell', 'cat', '/sdcard/ui.xml')
        x = r.stdout
        if x and 'null root node' not in x and len(x) > 500:
            return x
        time.sleep(1.2)
    return ''

def nodes(x):
    out = []
    for m in re.finditer(r'<node[^>]*?text="([^"]*)"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', x):
        t, x1, y1, x2, y2 = m.group(1), int(m.group(2)), int(m.group(3)), int(m.group(4)), int(m.group(5))
        out.append((t, (x1 + x2) // 2, (y1 + y2) // 2))
    return out

def shot(name):
    os.makedirs(OUT, exist_ok=True)
    subprocess.run([ADB, '-s', DEV, 'exec-out', 'screencap', '-p'], timeout=30, stdout=open(f'{OUT}/{name}.png', 'wb'))

def find(x, *texts):
    res = nodes(x)
    for want in texts:              # exact action-label matches first
        for t, cx, cy in res:
            if t.strip().lower() == want.lower():
                return (t, cx, cy)
    for want in texts:              # then substring fallback
        for t, cx, cy in res:
            if want.lower() in t.lower():
                return (t, cx, cy)
    return None

def main():
    step = 0
    log = []
    # fresh state + go home
    adb('shell', 'pm', 'clear', 'com.ovacoda.devie')
    time.sleep(1)
    adb('shell', 'input', 'keyevent', 'KEYCODE_HOME')
    time.sleep(5)
    last_texts = ''
    for step in range(1, 26):
        x = dump_ui()
        texts = [t for t, _, _ in nodes(x) if t.strip()]
        shot(f'drv{step}')
        log.append((step, texts[:8]))
        if not texts:
            print(f'step {step}: no ui dump (screen empty?), aborting')
            break
        nxt = find(x, 'Allow', 'WHILE USING THE APP', 'Only this time', 'Allow all', 'Don’t allow', 'Grant Access', 'Next', 'Continue', 'OK', 'Accept', 'Set as default', 'Skip', 'Done', 'Start')
        if not nxt:
            print(f'step {step}: no known button; texts={texts[:8]}; stopping')
            break
        t, cx, cy = nxt
        print(f'step {step}: tap "{t}" @({cx},{cy})')
        adb('shell', 'input', 'tap', str(cx), str(cy))
        time.sleep(2.2)
    else:
        pass
    # final state
    shot('drvFinal')
    print('steps:', log)
    r = adb('logcat', '-d', '-t', '2000')
    fatal = [l for l in r.stdout.split('\n') if 'FATAL' in l or 'ANR in' in l]
    print('FATAL/ANR lines:', fatal[:6])

if __name__ == '__main__':
    main()