# AGENTS.md — dEvie Launcher: complete build/test/release/runbook

Anyone joining this session cold: read this top-to-bottom once; it contains
every environment fact, trap and standing rule accumulated across the whole
rework. Do not re-derive any of it.

## 1. What this project is

dEvie = a ground-up Gradle rebuild of **Evie Launcher 2.14.8** from its donor
APK (GPL-3.0), with all cloud services stripped. Fully offline, no tracking.
Repo: `github.com/icyavocado/devie-launcher` (public), branch `main` = default.

| Fact | Value |
|---|---|
| Application id | `com.ovacoda.devie` (rebranded from original `is.shortcut`; was briefly `app.dai.evie`) |
| Launcher activity | `com.voxel.launcher3.Launcher` |
| Language / min / target | Pure Java, minSdk 21, target+compile 35 |
| versionName scheme | `2.14.8-rw<N>` (increment `rw<N>` every release) |
| Latest release | `v2.14.8-rw4` (signed, published by CI) |
| License | GPL-3.0 |

## 2. Where things are

| Thing | Path |
|---|---|
| Repo (always here) | `/home/dai/projects/evie` |
| Donor APK | `/home/dai/Downloads/evie-launcher-2.14.8.12_arm64.apk` (symlinked into repo as `donor/evie-launcher-2.14.8.12_arm64.apk`, `donor/` is git-ignored) |
| Donor jadx source tree | `/tmp/opencode/evie-src` (**session-temp — may vanish; see §3 to regenerate**) |
| Donor smali (dex truth) | `/tmp/opencode/apktool-smali-src` (**may vanish; regenerate, see §3**) |
| Donor res decode | `/tmp/opencode/apktool-out` |
| jadx binary | `/tmp/opencode/jadx/bin/jadx` |
| apktool jar | `/tmp/opencode/apktool.jar` (2.12.1) |
| Release signing | `keystore/evie-release.jks` + `keystore/evie-release.properties` (git-ignored, NEVER commit, NEVER echo values) |
| Build outputs | `app/build/outputs/apk/{debug,phoneDebug,release}/` |
| R8 map (symbolication) | `app/build/outputs/mapping/release/mapping.txt` |
| Roadmap backlog | `docs/roadmap.md` |
| Smoke log | `docs/smoke-test.md` (per-session verification record) |
| CI workflow | `.github/workflows/release.yml` |
| Screenshots for README | `docs/screenshots/` |

## 3. Regenerating donor truth (after `/tmp` is wiped)

```sh
cd /home/dai/projects/evie
APK=donor/evie-launcher-2.14.8.12_arm64.apk
# jadx (binary re-download if /tmp/opencode/jadx is gone: jadx-1.4.7 zip, unzip)
/tmp/opencode/jadx/bin/jadx -d /tmp/opencode/evie-src "$APK"
# smali + res (apktool jar: re-download 2.12.1 if missing; NO -s — that flag skips
# smali and yields no dex truth)
java -jar /tmp/opencode/apktool.jar d -f -o /tmp/opencode/apktool-smali-src "$APK"
java -jar /tmp/opencode/apktool.jar d -r -f -o /tmp/opencode/apktool-out "$APK"
```

## 4. Builds — Docker only (host has NO gradle)

```sh
./scripts/evie-build.sh :app:assembleDebug        # or assembleRelease, assemblePhoneDebug
```

- Image `evie-build` (from repo `Dockerfile`: gradle 9.8.1 + jdk17 + AGP 9.4 + SDK 35).
- `.docker-gradle/` = persistent gradle cache; `.docker-home/` = the container's
  `~/.android`, holds the **pinned debug keystore** `.docker-home/debug.keystore`
  (`signingConfigs.debug` pins it: alias `androiddebugkey`, passwords `android`).
  If deleted, regenerate:
  ```sh
  docker run --rm -v "$PWD:$PWD" -w "$PWD" -u "$(id -u):$(id -g)" evie-build \
    keytool -genkeypair -keystore .docker-home/debug.keystore -storepass android \
    -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US"
  ```
  (Without the pin each build signs with a fresh key and every reinstall needs an
  uninstall first — known-painful; keep the pin.)
- `phoneDebug` buildType = release-signed but debuggable (update-in-place; `run-as` works).
- Release = R8 minify + shrinkResources; signing only when
  `keystore/evie-release.properties` exists (CI builds unsigned without secrets).
- Crash lines from R8 builds: symbolicate with `mapping.txt` (e.g. `r7` ↔ class via mapping).
- R8 rules live in `proguard-rules.pro`: keepnames for Fragment subclasses (covers
  `TransparentSettingsActivity`'s `"show_fragment"` reflection), Room `_Impl` +
  `**_Impl` ctors, `-dontoptimize` (API-36 ART VerifyError on decompiled receiver
  classes). (The old catch-all keepnames for `com.voxel.**`/`com.evie.**`/
  `com.ovacoda.**` died with ButterKnife 8.x — removed when the dep was dropped.)

## 5. Devices

| Device | Serial | Screen | Notes |
|---|---|---|---|
| Emulator (AVD evie-test) | `emulator-5554` | 1080×2280 | **y≈2235 = nav bar — blind taps there press HOME** |
| Phone Samsung SM-S901W | `R5CT60JX7FM` (USB) | 1080×2340 | screencap imgs ≈ 900-wide → ×1.2/×1.17; wireless adb ports rotate (mdns empty on LAN) |
| Tablet acer A10-31P | `534014316917` (USB) | 800×1280@160 | Android 15 |

- adb (host): `~/Android/Sdk/platform-tools/adb` — NOT on PATH. Multi-device:
  **always `-s <serial>`**, and check `adb devices` first — never let `install`
  block waiting on an absent serial (it waits forever).
- screencap = `adb -s S shell screencap -p /sdcard/x.png` + `exec-out cat` (direct
  exec-out screencap corrupts on some devices).
- Live logging: `nohup adb -s S logcat -v time > /tmp/opencode/live.log 2>&1 &` —
  don't poll; grep it after actions.
- Never `pkill` with a pattern matching your own shell's cmdline (kills the agent
  shell). Use `./scripts/evie-emulator.sh kill` / `pgrep -f qemu | xargs -r kill`.
- OEM phone: starts of non-exported activities fail (`am start SettingsActivity` →
  SecurityException) — reach Settings via the search overlay: type "set" → tap the
  "dEvie Settings" row. Default home lives in the ROLE system (see §9).
- Don't tap/drive a device the user is actively using; ask first.

## 6. Emulator runbook (AVD `evie-test`, host-side)

```sh
./scripts/evie-emulator.sh [boot|wait|status|adb|shell|install|screencap|logcat|kill]
```

- **Cold boots wipe `/data`** (no snapshots): after each `boot` = reinstall APK +
  `cmd role add-role-holder android.app.role.HOME com.ovacoda.devie` + the app runs
  onboarding fresh (grants revoked).
- Emulator-only gap: default-workspace favorites referencing missing packages
  (`com.android.dialer` etc.) throw NameNotFoundException during the default load —
  environmental, expected (the user's phone resolves them).
- Contacts provider starts empty; seed if needed:
  ```sh
 adb -s emulator-5554 shell 'content insert --user 0 --uri content://com.android.contacts/raw_contacts --bind display_name:s:Name'
  # then data rows on content://com.android.contacts/data:
  #   mimetype s:vnd.android.cursor.item/name, data1 s:"Full Name", raw_contact_id i:<id>
  #   mimetype s:vnd.android.cursor.item/phone_v2 (+ optional phone row)
  # verify: content query --user 0 --uri content://com.android.contacts/contacts --projection "_id:display_name:in_visible_group"
  ```

### Verify loop (every fix, in this order)

```sh
./scripts/evie-build.sh :app:assembleDebug
./scripts/evie-emulator.sh install app/build/outputs/apk/debug/app-debug.apk
./scripts/evie-emulator.sh shell am force-stop com.ovacoda.devie
./scripts/evie-emulator.sh shell monkey -p com.ovacoda.devie 1   # relaunch home
./scripts/evie-emulator.sh logcat -d | grep -E 'FATAL|ANR in|Skipping child'   # crash check
./scripts/evie-emulator.sh shell screencap -p /sdcard/x.png && ./scripts/evie-emulator.sh adb pull /sdcard/x.png /tmp/opencode/x.png
```

Signature mismatch on install (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`) happens after
keystore changes or cross-buildtype swaps: `pm uninstall com.ovacoda.devie` once,
reinstall, re-add the role.

### Onboarding-driving helper

`python3 scripts/drive_onboarding.py` = pm clear + HOME, then taps through
first-run by uiautomator-dump button matching (exact-text priority). Coordinates
(emulator): Grant Access @(540,2066); location dialog WHILE USING @(540,1380);
contacts ALLOW @(540,1181); set-default OK @(678,2006); dEvie in picker @(395,639).

- **uiautomator traps**: `rm -f /sdcard/ui.xml` BEFORE every dump+pull (stale file
  when the dump null-roots); DiscreteSeekBar thumbs don't respond to plain taps —
  **drag** them (`input swipe x1 y x2 y 500` on the track y).
- The emulator's first-run flows are enabled by rework tweaks in
  `Launcher.shouldRunFirstRunActivity/shouldRunFirstRunClings` (the
  `isRunningInTestHarness` clause removed — do not "restore" it).

### App DBs on real devices

Release/phoneDebug = not debuggable → run-as fails on RELEASE only:
emulator release: `su 0 sh -c 'cat /data/local/tmp/qN.sql | sqlite3 /data/data/com.ovacoda.devie/databases/launcher.db'`
(push SQL to /data/local/tmp first). Tablet has NO sqlite binary: on debug builds
`exec-out run-as com.ovacoda.devie cat databases/launcher.db` → host `python3 -c` with `sqlite3`.

| DB | Contents |
|---|---|
| `launcher.db` | `favorites` (container -100 ws / -101 dock, itemType 0 app/1 shortcut/2 folder/4 widget/6 deep-shortcut, cellX/cellY/spanX/spanY) + `workspaceScreens` (_id, screenRank) |
| `LocalSearch.db` | `item` + `search` keyword index (fuzzy match) + `recents` |
| `APPS_INFO_DB` | app metadata + icons |

## 7. Git + release discipline

- One commit per fix; Conventional-commit; subject ≤ 50 chars.
- **NEVER `git push` or cut a release (version bump + tag) without the user's
  explicit go-ahead.** Plain fix-builds installed on the phone without a release
  are fine (standing instruction: "after fixing the bug install on my phone").
- NEVER commit: `keystore/`, `.docker-*/`, `.gradle/`, `app/build/`, donor dumps
  (`.aapt-dump.txt`). **NEVER `git add -f`** (it sweeps ignored secrets in — this
  leaked once and forced a full squash + purge).
- History = a single squashed root on purpose (secret purge). Keep it that way.

### How to cut a release (user has approved)

1. `app/build.gradle.kts`: `versionCode` +1, `versionName` next `rw<N>`; commit
   `chore(release): bump 2.14.8-rw<N>`.
2. `git push origin main`.
3. `git tag v2.14.8-rw<N> && git push origin v2.14.8-rw<N>` — the tag push runs the
   `release` workflow (tag-only; no rolling `latest`): signed APK published at the
   tag as `devie-launcher-<tag>.apk`.
4. `gh run watch <run> -R icyavocado/devie-launcher --interval 20` (≈2 min).
5. Edit notes with the changelog: `gh release edit <tag> -R icyavocado/devie-launcher
   --title "dEvie <version>" --notes "..."` (shape: `## Changelog` / `### Fixed` /
   bullets, then "Install: download the APK below and allow installing from this
   source." — only the fixes of THAT release).
6. Install the same local build on the phone: poll `adb devices` for
   `R5CT60JX7FM`, then `adb -s R5CT60JX7FM install -r app/build/outputs/apk/release/app-release.apk`
   + force-stop/relaunch + FATAL check.

CI signing = repo secrets `RELEASE_KEYSTORE` (base64 .jks) + `RELEASE_PROPERTIES`;
those are set by the user locally (keystore material never enters chat). `gh` is
authenticated as `icyavocado` (HTTPS remote; ssh port 22 firewalled).

## 8. Code map (who lives where)

| Area | Location |
|---|---|
| Launcher core (4.4-era Launcher2/3) | `com.voxel.launcher3.*` — Launcher, Workspace, CellLayout, LauncherModel (+ LoaderTask, workspaceScreens reconcile), LauncherProvider (authority `com.ovacoda.devie.settings`), DeviceProfile/DynamicGrid, IconCache, dragndrop, Folder/FolderIcon, wallpaper-picker helpers (`com.android.gallery3d.*`) |
| Evie feature layer | `com.voxel.simplesearchlauncher.*` — search overlay (SearchPresenter/SearchUiHelper + `com.evie.search.*` local fuzzy engine), settings (SettingsActivity, *SettingsFragment, gridsettings.*, MockLauncherController previews), onboarding (PermissionsSlide; IntroSlide was cut), notification/badge (NotificationListener + NotificationReceiver), app lock (DeviceAdmin/LockOverlay), backup/restore (BackupRestoreManager + BackupRestoreSettingsFragment, SAF + ProgressDialog) |
| New code home | `com.ovacoda.devie.*` — DevieLinks (REPO_URL/ISSUES_URL → github.com/icyavocado/devie-launcher), widget.DiscreteSeekBar (in-house, own min/max range handling), view.LoadingSpinner (in-house spinner) |
| Vendored (donor-carried, in use) | `com.android.gallery3d.*` + `com.android.photos.*` — AOSP Gallery2 wallpaper-crop pipeline (BitmapCropTask, EXIF, glrenderer, tiled decoding) used by WallpaperPickerActivity et al. |
| ButterKnife/luizgrp/adw | REMOVED (branch review/vendor-removal): bindings inlined as findViewById, settings lists use flat RecyclerView adapters, seek bars are `com.ovacoda.devie.widget.DiscreteSeekBar` |
| Dagger | Hand-maintained generated tree under `com.voxel.simplesearchlauncher.dagger/`. Deleting an injected class = update `DaggerLauncherAppComponent` (imports/fields/inner builders/map.put/ctor-assign) + the module file. |

- **Workspace settings preview** = `MockLauncherController` over
  `layout/settings_mock_launcher`; reads `LauncherModel.getItemsForScreenPosition(0)`
  + `getHotseatItems()`. The folder preview = `Folder.fromXml` + a mocked 16-item
  FolderInfo; items pull REAL icons via
  `iconCache.getIcon(intent, UserHandleCompat.myUserHandle())` with the component
  from `LocalAppData`. Dark "phone screen" = `@color/mock_screen_background`
  (#18212c) on the mock panel (`mock_phone_screen` drawable for the folder grid).
- **Settings look**: opaque window, flat soft-gray `#d9dde3` top-to-bottom; main
  screen AND submenus = small muted section headers above one white rounded card
  per group with thin inset dividers (drawable `settings_group_divider`, inset
  wrapper `settings_row_divider`; SettingsFragment rebuilds the main screen
  programmatically — layouts `settings_activity_top_level_fragment/row`;
  sub-fragment rows come from the flattened `settings_activity_{switch,text,seek}_item`
  layouts inside per-fragment card wrappers); seek rows = label stacked above a
  full-width track. Preview submenus (desktop/dock/folder/app_drawer/icon_pack):
  the mock preview is pinned behind the activity root; keep the rows container
  background-free so the card slides over the preview instead of an opaque grey
  sheet, and do NOT re-add the parallax scroll listeners (removed deliberately).
  The divider include = spacer kept for ButterKnife's `R.id.list_divider` binding
  (ButterKnife gone — include kept only as legacy spacing) — do not remove it
  from row layouts. `settings_activity_app_drawer_fragment` MUST keep the donor
  mock block (`RoundedFrameLayout id=mock` + embedded AllAppsFragment
  `<fragment>`); removing it NPEs `AppDrawerSettingsFragment.setupMock`.
- **Set-default flow** = API 29+: `RoleManager.isRoleHeld(ROLE_HOME)` /
  `createRequestRoleIntent` (requestCode 47161) in `ResetLauncherActivity`; the
  2020 resolveActivity/clearHome dance only runs < Q.
- Package visibility (API 30+): manifest `<queries>` has MAIN/LAUNCHER intent +
  `QUERY_ALL_PACKAGES` — without it OEMs return an empty app universe (drawer +
  search dead). Do not trim these.

## 9. Decompile-trap catalog (all fixed; treat every oddity as a suspect)

The codebase = a jadx decompile rebuilt by hand. When logic misbehaves, diff
against donor smali (`/tmp/opencode/apktool-smali-src`) or the upstream 4.4 sources
(`android-4.4.4_r2` via googlesource, format=TEXT + base64 -d) BEFORE inventing a
patch. Known trap classes + the fixes:

1. **int-to-float dropped** → int division truncates: preview scale 0
   (5 SettingsFragments 688c945-era), icon-scale max 0 at Columns>4
   (`DeviceProfile.getMaxIconScale*` — donor `div-float`s both operands).
2. **Flattened dex flow** → missing edges: `LocalSearchManager.search` far-distance
   must `continue` (the trailing `get(-1)` AIOOBE was swallowed by
   `.onErrorReturnItem` = all contact search silently empty);
   `CellLayout.findNearestArea` (11-param) occupied-cell skip → infinite spin →
   ANR "icons disappear/cut off" (fixed by restoring the canonical 4.4 body
   verbatim); `Launcher.bindItems` double-increment (every other dock icon
   skipped); `DragController.drop/endDrag` re-entrancy NPEs (capture locals at
   entry, null-guard mDragObject); `Widget drop` null host-view guard.
3. **Hardcoded constants**: `BaseApplication.isDebugBuild` = was hardcoded false
   (Timber never planted → ALL app logs invisible) → read `BuildConfig.DEBUG`;
   `shouldRunFirstRunActivity/Clings` test-harness clause removed for the emulator;
   provider `insert()` rejecting workspaceScreens rowId 0; container check flipped
   (-100/-101).
4. **Generics erased on desugared lambdas**: raw `AsyncTask` param → javac passes
   `Object[]` varargs → `doInBackground(Integer...)` CCE (wallpaper picker crash).
5. **Styleable-index reads** (`getInt(2,1)` → home-page bug), blank registers /
   `this.f$0` lambda artifacts, WeakReference holder (`sLauncherProvider` — now a
   strong static; a GC'd ref silently disabled workspaceScreens reconcile).
6. **Donor default-app probes resolve the system resolver** (Xiaomi/HyperOS
   report): donor's `ACTION_CALL tel:` / `SENDTO sms:` / contact-VIEW-without-MIME
   probes match arbitrary handlers (WhatsApp registers sms:; messaging apps
   register tel:), and OEM resolvers aren't always class
   `com.android.internal.app.ResolverActivity` (any package `android` counts).
   Default-app detection now: dialer = `TelecomManager.getDefaultDialerPackage()`,
   SMS = `Telephony.Sms.getDefaultSmsPackage(Context)`, contacts = VIEW +
   `CONTENT_ITEM_TYPE` (NOT the Secure key `sms_default_application` — Samsung
   doesn't populate it; roles live in RoleManager: check via
   `cmd role get-role-holders android.app.role.SMS`). See
   `com.evie.search.local.LocalAppsManager` + `LinkResolver` (b844ee8).

## 10. Known quirks / open items (see docs/roadmap.md)

- Emulator-only: partial default-workspace loads (missing packages), occasional
  empty-page flicker on reload cycles; verify absence of `Skipping child, screenId`
  lines after loader changes. User devices resolve everything.
- The first-run flow flags live in `app.voxel.launcher3_preferences.xml`
  (`launcher.first_run_*`) — `pm clear` resets them (and ALL grants).
- `com.sec.android.diagmonagent` (Samsung) crashes harmlessly after our bloatware
  purge — unrelated to dEvie.

## 11. Spin-up checklist (fresh session)

1. Read this file (+ `docs/smoke-test.md` tail for the latest session state).
2. `./scripts/evie-emulator.sh status` → up? else `boot` + `wait`; reinstall the
   debug build; re-add the HOME role.
3. `./scripts/evie-build.sh :app:assembleDebug` → green?
4. Confirm the donor trees exist (§3), else regenerate.
5. Check `git status` — clean tree; `git log --oneline -5` for context.
6. Working rule: reproduce on the emulator (or the user's device per their
   instruction), fix donor-faithfully, verify, commit; push/release only on the
   user's explicit go-ahead; always ship plain fix-builds to the phone.

## Rework scope (locked)

- CUT stays cut: side-screen feed/jigsaw cards, remote/web search backends,
  analytics (crashlytics/fabric/events), Firebase/GMS extras, launcher-settings
  importers, debug activities (MemoryDump/ToggleWeightWatcher), MemoryTracker/
  WeightWatcher, What's-New popup + promo videos, Privacy Policy (replaced by the
  No-tracking dialog), dead Terms page + hidden 10-tap easter egg.
- KEEP: local search + web-intent row (opens in the user's browser), notifications/
  badges, app lock, backup/restore (local/SAF), feedback screen (GitHub Issues +
  star links), contacts onboarding permission.