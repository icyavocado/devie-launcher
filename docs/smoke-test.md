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

## Play-Store survey removal + cloud/dead-code sweep (2026-10-24)

Full de-Play-Store + de-cloud pass across three commits (all verified on
AVD `evie-test`, fresh cold-boot state, onboarding driven via
`scripts/drive_onboarding.py`; no FATAL/ANR/Skipping-child in logcat):

1. `33d2a5c fix(search): remove Play Store rating survey` — the search
   overlay feedback chain deleted (SearchFeedbackPresenter, 9 slide/view
   files incl. AppFlash orphan variant, layout, 13 strings × 15 locales,
   dimen); Launcher branch reduced to set-default prompt only; kept
   OptionSlide/TextSlide base classes (shared with onboarding) — TextSlide
   itself then dropped as dead in the sweep below.
2. `307c7d2 fix(contacts): drop S3 cover fetch, remove INTERNET` — the
   real cloud leftover: `ContactManager.getHeaderImageUrl()` built a
   cleartext S3 URL (`http://s3-us-west-2.amazonaws.com/.../cover_<color>_
   <density>`) for photo-less contacts which Fresco fetched over the
   network at bind time. Replaced with the local initials/photo URI;
   dead HttpUrlConnection fetchers in BitmapUtil
   (getBitmapFromURL/getCircleBitmap/getTint) removed; R.array
   contact_headers + 4 contact_cover_*_url strings removed; manifest
   INTERNET permission + network_security_config dropped — the app now
   does no network I/O at the install level. Verified: seeded photo-less
   contact "Jenny Appleseed", full search → row renders LOCAL purple
   initials avatar, tapped row → recents written (CONTACT row, empty
   image_url); final `strings` scan of LocalSearch.db + APPS_INFO_DB:
   0 amazonaws/http rows.
3. `4794fe6 refactor: remove leftover dead code and persisted UUID` —
   TextSlide + its layout, utils/Constants (MILLIS_IN_*), DebugUtil
   .isAnalyticsEnabled; AnalyticsHandler no longer generates/persists the
   anonyous-user UUID (analytics_pref file gone from fresh installs).

Emulator notes: fresh-install migration `SettingsStorage.onUpgrade(-1)`
sets `search_bar_desktop_enabled=false` (bar hidden by design; search =
swipe-down gesture / custom gestures, prefs `custom_gesture.*`). For
testing I set the pref true via run-as so the workspace bar shows and the
TAP focus path opens the overlay. Two earlier "system QuickSearchBox
suggestions" appearances were the emulator's own com.android.quicksearchbox
app foregrounding, not dEvie. Icon-pack "browse icon packs" market:// rows
KEPT per owner decision (generic intent).

## Session: smaller-faster branch — size push (rw5 + de-Fresco)

Branch from main@f970532 (rw5 released; CI green, asset published).

1. `59a02c3 perf(size): arm64/v7a ABIs and English-only locales in packaging`
   — release 4.97 → 3.94 MB (arsc 1453→685 KB, lib 727→328 KB; localeFilters
   en + release ndk.abiFilters arm64-v8a/armeabi-v7a; x86 stays in debug).
2. `e4fa220 perf(size): convert drawable PNGs to lossless WebP` — 116 drawables
   via `magick -define webp:lossless=true -define webp:method=6` (kept .9.png
   + mipmap icons); release 3.94 → 3.85 MB.
3. `31eb1bb perf(size): remove Fresco for in-process IconLoader` — all three
   .so files are now GONE (zero lib/ in the APK); com.facebook drawee/pipeline
   classes, SoLoaderShim, MemoryRegistry(+module), OkHttpModule,
   ConnectionPoolKiller deleted; deps dropped: fresco, imagepipeline-okhttp3,
   okhttp3, relinker (rebound kept — spring UI). New
   com.ovacoda.devie.images.IconLoader = LruCache with dispatch over
   data:image/bmp;base64 (BitmapUtil.getUrlFromBitmap), content:// contact
   photos, and magic example.com URIs → IconCache (+icon-pack filter);
   anything else is refused+logged. IconCacheCleaner keeps the recents
   warm-up lifecycle. 9 layouts: SimpleDraweeView → ImageView (rating widgets
   in search_item.xml were dead → removed with their dimens/ids).

   Verified on emulator-5554 (debug): workspace bar + overlay (search
   "Jenny" → local purple JA initials avatar, web row), all-apps drawer
   rendered fully through the new loader (magic URIs → IconCache), recents
   row written, 0 FATAL/ANR, logcat clean of refusals. Release build green:
   **3.01 MB** (from 4.97 MB at rw5; −39%).


## 2.14.8-rw5-branch: smaller-faster (class-design audit stream)

Branch `smaller-faster` off rw5 (f970532). Explore-agent audit of all
579 java files + res: reference-counted every class/member/resource.
Fixes landed one-by-one, each with assembleDebug + emulator smoke
(install -r → grant → force-stop → monkey → FATAL grep = 0):

- `59a02c3` arm64/v7a release ABIs + English-only locales (4.97→3.94)
- `e4fa220` 116 drawable PNGs → lossless WebP, kept .9.png (3.94→3.85)
- `31eb1bb` de-Fresco: in-house IconLoader (LruCache + bg decoder +
  IconCache magic-URI resolve; hard network refusal), IconCacheCleaner;
  fresco/relinker/okhttp deps gone (3.85→3.01, zero lib/)
- `528be47` commit the untracked footer layout (add -u lesson)
- `f561338` inert deps: viewpager/swiperefresh/legacy-core-ui/
  interpolator/sqlite/gson/multidex + @SerializedName pairs
- `af6af66` res prune −12,168 lines: 124 locale value dirs (en-only),
  381 unused ids, dead colors/dimens/strings/bools/integers (census
  = fixpoint refs over java+manifest+ALL value xml incl qualifiers —
  self-exclusion false-killed vpi__ alias colors: fixed; 9 non-locale
  qualifier dirs wrongly swept restored, incl values-nodpi/arrays.xml
  wallpapers array)
- `e339255` Launcher dead members −89: intro-screen chain (dead since
  hasDismissableIntroScreen=false), 12 decl-only getters/stubs, 7
  no-op requestLogsSync/stopLogsSync sites, AnalyticsHandler no-ops
- `041e7e1` Stats: dead per-launch disk-I/O/broadcast removed (−150,
  manifest permission + string)
- `e43eb33` MemoryCrashHandler no-op + fabricInit stub (−33)
- `cc2a0b5` DebugUtil/TraceUtil: 21 log wrappers → Timber.tag().d(),
  8 dead isDevDebugLogEnabled blocks, TraceUtil calls (−88)
- `fefb949` wrappers: MainThreadExecutor.getInstance() inline, chain
  classes deleted, AnalyticsHandler.init takes the one real filter
- `7c7da33` inert analytics/event services −461: AnalyticsService/
  BaseAnalyticsService/models×4/EventService/Link + dagger factories;
  LinkResolver keeps real schemes only (http/https/contact/market/tel/
  settingsdeeplink/websearch/app); local Timber shim KEPT
- `99187e5`+`ca28ace` settings version footer scrolls with the list
  under "Switch to another launcher": "dEvie <version> - <sha>" via
  BuildConfig.GIT_SHA (git rev-parse, unknown-fallback)

Release = **3.00 MB** (3,148,374 B; dex 1530 KB, arsc 667 KB STORED,
res 616, META 96; zero native). −40% vs rw5 (4.97), −67% vs donor
(9.07). All verified: workspace + search overlay (JA avatar + contact +
web rows) + drawer through IconLoader, 0 FATAL/ANR throughout.

Deliberately not done (audit findings kept as notes):
- styles.xml lib-table prune: ABANDONED after 3 regex-census passes
  ate live styles (SearchViewStyle/PreloadIcon family; android.R
  substring artifacts) — zero APK benefit (shrinkResources strips at
  link), repo-only weight; kept at HEAD for aapt fidelity
- icon-bitmap dedup (#13) + grid-settings-fragment dedup (#7): skipped
  (false-positive semantics / MockLauncherController risk per AGENTS §8)
- lint checkReleaseBuilds stays OFF (documented in build.gradle.kts:
  merged lib style table still trips release lint; shrinkResources
  covers the artifact)
