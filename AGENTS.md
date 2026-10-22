# AGENTS.md — environment + workflow (do not re-derive this)

## Where things are

| Thing | Path |
|---|---|
| Repo (this project) | `/home/dai/projects/evie` |
| Original APK (donor) | `./evie-launcher-2.14.8.12_arm64.apk` |
| Decompiled donor tree (jadx) | `/tmp/opencode/evie-src/sources` (session-temp; re-decompile `~/tools` not needed — `jadx` at `/tmp/opencode/jadx/bin/jadx`) |
| apktool jar | `/tmp/opencode/apktool.jar` (2.12.1) |
| apktool resource decode | `/tmp/opencode/apktool-out` |
| smali dump | `/tmp/opencode/apktool-smali` (for cross-checking decompile damage) |
| Plan file | `/home/dai/.opencode/plan/evie-launcher-rework.md` |
| Roadmap (post-build backlog) | `docs/roadmap.md` |
| Smoke-test log | `docs/smoke-test.md` |

## Builds — Docker only (host has no gradle SDK)

```sh
./scripts/evie-build.sh :app:assembleDebug      # or assembleRelease
```
- Image `evie-build` (built from `Dockerfile`: gradle 9.8.1 + jdk17 + platform-tools).
- Host has NO gradle SDK; do not try host-side gradle.
- `.docker-gradle/` = persistent gradle cache volume; `.docker-home/` = pinned debug keystore.
- The image's default user is `gradle` (uid 1000). `evie-build.sh` passes `-u $(id -u):$(id -g)` so files written into `app/` stay user-owned.

## Emulator — HOST side, not docker

- **Host Android SDK: `~/Android/Sdk`** (emulator binary, platform-tools/adb, cmdline-tools, system-images). Use it.
- **adb on host: `~/Android/Sdk/platform-tools/adb`** — not on PATH. Use the script:
  `./scripts/evie-emulator.sh [boot|wait|status|adb|shell|install|screencap|logcat|kill]`
- **AVD: `evie-test`** (pixel_4 profile, API 33, x86_64, headless: `-no-window -no-audio`), log at `~/.local/share/evie-emu/emulator.log`.
- Device name once up: `emulator-5554`.
- First-run smoke checklist lives in `docs/smoke-test.md`.

### Verify loop (every fix, in this order)

```sh
./scripts/evie-build.sh :app:assembleDebug
./scripts/evie-emulator.sh install app/build/outputs/apk/debug/app-debug.apk
./scripts/evie-emulator.sh shell am force-stop app.dai.evie
./scripts/evie-emulator.sh shell monkey -p app.dai.evie 1        # relaunch home
./scripts/evie-emulator.sh logcat -d | tail -100                  # crash check
./scripts/evie-emulator.sh screencap /tmp/opencode/emu.png
```

### Emulator quirks

- Host emulator process may be torn down between long adb calls — run adb commands
  promptly (or `-no-window -no-audio` keeps it stable).
- Set default home once if needed:
  `cmd role add-role-holder android.app.role.HOME app.dai.evie`
- Reset app state for a clean onboarding run:
  `adb shell pm clear app.dai.evie` (grants are revoked; expect the onboarding flow).
- Screen is **1080×2280** (`wm size`) — not 2340; blind taps at y≈2235 hit the nav
  bar. For button coords use uiautomator dump via `scripts/drive_onboarding.py`
  (exact-text priority matching); `rm -f /sdcard/ui.xml` first — stale pulls when
  uiautomator null-roots.
- `run-as` fails on release builds (not debuggable): inspect app DBs with
  `su 0 sh -c "... | sqlite3 /data/data/app.dai.evie/databases/launcher.db"`.
- Never `pkill` with a pattern matching the shell's own cmdline (kills the agent
  shell); use `./scripts/evie-emulator.sh kill`.
- jadx decode traps to check against smali (`/tmp/opencode/apktool-smali`) when
  logic misbehaves: int-to-float division dropped (mock preview scale 0 — fixed
  in 5 SettingsFragments), flattened dex flow (`LocalSearchManager.search`
  far-distance case must `continue`; trailing `get(-1)`), hardcoded constants
  (`BaseApplication.isDebugBuild` was `false` — must read `BuildConfig.DEBUG`).

## Git discipline

- One commit per todo/fix. Conventional-commit style, subject ≤ 50 chars.
- Working dir is `/home/dai/projects/evie`; repo is NOT at any other path.

## Releases

Upon release:

1. **Increase dEvie's version** — `versionCode` +1 and `versionName` → next
   `rw<N>` suffix (e.g. `2.14.8-rw2`) in `app/build.gradle.kts`; commit.
2. **Push `main`**, then **push a version tag** `v<versionName>` (e.g.
   `v2.14.8-rw2`) — the tag push runs the `release` CI workflow, which builds
   the signed APK and publishes the release at that tag
   (`devie-launcher-<tag>.apk`). No rolling `latest` releases.
3. **Edit the release notes** with the changelog:
   `gh release edit <tag> -R icyavocado/devie-launcher --notes "..."`.
4. Ship the same build to the user's phone (Samsung SM-S901W, USB serial
   `R5CT60JX7FM`; poll `adb devices` first) — standing instruction.

NEVER cut a release (version bump + tag) without the user's explicit
go-ahead; plain fix-builds to the phone are fine otherwise.

NEVER commit: `keystore/`, `.docker-*/`, `.gradle/`, `app/build/`, donor
dumps (`.aapt-dump.txt`). Never use `git add -f` (it sweeps ignored secrets
in — this leaked once and required a full squash + purge).

## Rework scope (locked)

- Package `app.dai.evie` (was `is.shortcut`), pure Java, minSdk 21 / target+compile 35.
- CUT: side-screen feed, Jigsaw cards, remote/web search backends, analytics
  (crashlytics/fabric/events), Firebase/GMS extras, launcher-settings importers,
  debug activities (MemoryDump/ToggleWeightWatcher).
- KEEP: local search + web-intent row, notifications/badges, app lock, backup/restore
  (local/SAF), feedback/upgrade card, contacts onboarding permission.