# Smoke test — rebuild on device/emulator

Environment: Android emulator `evie-test` (pixel_5 profile, API 33, x86_64), host-side
SDK at `~/Android/Sdk`. Debug build from `./scripts/evie-build.sh :app:assembleDebug`
installed with `adb install` (package `app.dai.evie`, version `2.14.8-rw1`).

Run date: 2026-10-19.

| # | Check | Result | Notes |
|---|-------|--------|-------|
| 1 | Installs (both debug + signed release) | PASS | `INSTALL_FAILED_UPDATE_INCOMPATIBLE` appeared whenever the container's transient debug key changed; uninstall + reinstall clears it. Debug keystore is now pinned in `.docker-home/debug.keystore` so the signature is stable across builds. |
| 2 | HOME role + "set as home" | PASS | `cmd role add-role-holder android.app.role.HOME app.dai.evie` then KEYCODE_HOME focuses our `com.voxel.launcher3.Launcher`. On a real phone the first-run prompt path still needs verification. |
| 3 | Loader + model | PASS | `loadWorkspace()` (restored from the dex in `fix(model)`) runs, loads default favorites, builds screens, binds items; no crash, `Launcher` ends up topResumed. |
| 4 | App drawer | PASS | Swipe-up opens All Apps; alphabet scroller renders; list populated from the app model. |
| 5 | Drawer item click → launch | PASS | Tapping an item in the drawer starts the matching activity (verified with the Files app). |
| 6 | Desktop item renders | PASS | Default-workspace favorites persist (verified in `launcher.db` via run-as sqlite) and the Settings icon draws at its cell. |
| 7 | Desktop item click → launch | **FAIL (open defect)** | Click lands on the clickable `CellLayout` page instead of the item view. Long-press works (opens wallpapers/widgets/settings sheet), so touch dispatch reaches the workspace; the item's own click path is what breaks. Suspects: CellLayout/ShortcutAndWidgetContainer z-order + `clickable(true)` on pages, default-page handling, or the bind-time click binding. See roadmap. |
| 8 | Long-press desktop | PASS | Long-press opens the wallpaper/widgets/settings sheet with the preview panel. |
| 9 | Widget add/resize | NOT YET TESTED | Widget picker opens from the long-press sheet; end-to-end add/resize still to run. |
| 10 | Search overlay + web fallback | NOT YET TESTED | Swipe-down opens the search bar; query + ACTION_WEB_SEARCH fallback still to run. |
| 11 | Fingerprint app-lock | NOT YET TESTED | Needs fingerprint enrollment on the emulator. |
| 12 | Badges | NOT YET TESTED | Notification listener + badge extractor paths are wired but not yet exercised end-to-end. |
| 13 | Backup/restore via SAF | NOT YET TESTED | Requires a documents-provider picker run. |
| 14 | Icon pack remap | NOT YET TESTED | Needs an icon pack installed. |
| 15 | Rotation / kill / relaunch | PARTIAL | No crash on relaunch (killed + relaunched several times during signature churn); rotation pass still to run. |

## Known device-side fixes landed during testing

- `PendingIntent.FLAG_IMMUTABLE` on the usage-decay alarm (targetSdk 31+ requirement).
- Removed `android:permissionGroup="android.permission-group.SYSTEM_TOOLS"` from the
  custom permission declarations (the group no longer exists on P+; install failed with
  `-127` otherwise).
- `DeviceProfile`'s xml-ids ctor now wires `defaultLayoutId`/`defaultNoAllAppsLayoutId`
  again (jadx had dropped the params, defaulting every profile to `empty_workspace`).
- Default-workspace XMLs rewritten with modern package names; the launcher3 attribute
  namespace is restored to the full `res-auto/com.voxel.launcher3` form the dex
  attribute-lookup helpers still use.
- Debug keystore pinned to `.docker-home/debug.keystore` (host-side persistence), so the
  debug signature no longer changes per build run.# Smoke test log

## 2026-10-19 — emulator smoke (API 33, x86_64)

Build: `:app:assembleDebug` (HEAD `6230c2d` + working-tree fixes), installed and
launched via adb-over-Wi-Fi from the `evie-build` container (`--network host`).

### Session fixes (applied to reach green run)

1. `LocalSearchManager.setUsageDecayAlarm` — added `PendingIntent.FLAG_IMMUTABLE`
   (targetSdk 31+ requires an explicit mutability flag; crash at `Launcher.onCreate`).
2. jadx hoisted `BadgeDrawable`'s `dpToPx(…)` field initializers ahead of the
   `mContext` assignment → NPE during `BadgeableAppIcon` construction. Moved the
   four initializers into the constructor (original dex order).
3. jadx decompiled ButterKnife binding classes as non-static inner classes —
   the implicit enclosing-instance parameter broke the reflective
   `(target, View)` constructor lookup:
   - `UpgradeFragment$FeatureSection_ViewBinding` → made static
   - `SettingsFragment`: `HeaderViewHolder_ViewBinding`, `ViewHolder_ViewBinding` → static
   - `HideAppsSettingsFragment`: `HeaderViewHolder_ViewBinding`, `ItemViewHolder_ViewBinding` → static

### Result

- Install clean; launcher process stays alive; `topResumedActivity` =
  `app.dai.evie/com.voxel.launcher3.Launcher`; 0 runtime exceptions.
- Restored `loadWorkspace()` (dex dump) passes: workspace loads without crash.
- UpgradeFragment ("What's New" card) renders, incl. Fresco-loaded image.

### Notes / open items

- Samsung SM-S901W (API 36) install previously failed `-127` on the
  `permissionGroup="android.permission-group.SYSTEM_TOOLS"` references; now
  dropped. Phone round below covers the rest.

## 2026-10-19 — phone smoke (Samsung SM-S901W, API 36, adb-over-Wi-Fi)

Three more modern-API walls cleared:

1. `CONFLICTING_PROVIDER` — original Evie (`is.shortcut`) is installed on the
   phone and owns authority `com.voxel.launcher3.settings`. Renamed the
   rework's authority to `app.dai.evie.settings` (provider exported=false, so
   no external contract broken).
2. `SecurityException` on `registerReceiver` — API 34+ requires an explicit
   `RECEIVER_EXPORTED`/`RECEIVER_NOT_EXPORTED` flag. All dynamic
   registrations routed through `ContextCompat.registerReceiver` with
   `RECEIVER_NOT_EXPORTED` (all filters are system or same-app broadcasts;
   `LocalBroadcastManager` registrations exempt). NULL-receiver sticky peeks
   for `SYSTEM_READY` got the flags treatment too.
3. Ephemeral debug signing — see build-script fix above.

Result: install clean; launch shows `OnboardingActivity` (first-run flow);
0 runtime exceptions at this writing. Original Evie remains installed and
untouched alongside.
- Debug signing is generated per-container (`HOME=/tmp/evie` is ephemeral);
  `scripts/evie-build.sh` now mounts `.docker-home` → `/tmp/evie/.android` to
  persist it. Empty dir observed after one build — verify `install -r` survives
  a rebuild before trusting the fix.
- Full device checklist (plan `test(device)` item) still to run.
## Emulator session — 5 issue verification (2026-10-20)

All five 2.14.8-12 rework issues reproduced on AVD `evie-test` (emulator-5554,
screen 1080×2280) and verified fixed:

1. Set-default prompt OK → system default-app picker opens, flow completes.
   No crash (old release build had it; current tree is clean).
2. Permissions slide: location row registers (jadx had dropped it), legacy
   READ_EXTERNAL_STORAGE row dropped on API 33+; Location (while-using) and
   Contacts (allow) dialogs grant and slide advances (f19b8a0).
3. Contact search after grant: seeded contacts via `content insert`, search
   shows "ON YOUR DEVICE" row with initials-avatar fallback + web fallback.
   Root causes: contacts resync at search time (b1574e7) + dex-faithful
   contact ranking — jadx flattened the far-distance `continue`, leaving
   get(-1) AIOOBE swallowed into empty results by onErrorReturnItem (94af4b2).
4. Homescreen settings: feed + Fahrenheit rows removed (b75c8f1).
5. Previews: mock-launcher scaling was integer division (jadx dropped
   int-to-float), scale 0 → invisible icons; `/ (float)` in all 5 settings
   fragments (688c945). Icons render in Homescreen/Dock/Icon-pack previews.

Additionally fixed en route: `isDebugBuild` reads BuildConfig.DEBUG (Timber
logs restored), onboarding/clings run on emulators (isRunningInTestHarness
clause dropped), workspaceScreens reconcile with favorites (e2354b2) heals
the "Skipping child, screenId X not found" empty-desktop spiral.

## Drag-freeze round 2 (2026-10-20 11:0x)
- Phone `dEvie isn't responding` at icon-drop persisted on the d84e339 build → second flattened edge in the same function.
- `8ff5f51 fix(ws): restore canonical findNearestArea body`: the 11-param `findNearestArea` body restored verbatim from upstream Launcher2 4.4 (the donor dex is literally this algorithm — verified against `/tmp/opencode/fna11.smali`); terminating by construction (labeled `continue inner` + `while (!(hitMaxX && hitMaxY))` expansion).
- Release rebuilt (11,448,096B) and installed on the acer tablet (Success, 0 FATAL). Phone missed the install (cable unplugged) — ship on next connection.
- Related P1 (pre-existing): on some cycles the workspace view never re-lays-out (dump shows stale huge square bounds + `L` layout-requested flag) — empty desktop; tracked on roadmap.
