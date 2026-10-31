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

## rw6 hotfix: recents-click NPE + release smoke gate (2026-10-24)

- `44f2378` fix(analytics): set injectable log filter post-init — fefb949
  regression: FrequentlyUsedAnalyticsFilter was built in preDaggerInit with a
  not-yet-injected FrequentlyUsedModel, so the first logged click (recents /
  frequently-used row) NPE'd and killed the app mid-launch on the R8 phone
  build. AnalyticsHandler now starts with a pass-through filter and the real
  one is swapped in via static setAnalyticsFilter after dagger injection.
- `scripts/evie-smoke.py` = the pre-release gate: install -r + grants,
  force-stop/logcat-c/monkey, overlay open (bar tap by resource-id;
  swipe-down fallback for fresh installs where onUpgrade(-1) hides the bar —
  the phone's SpringySearchView hint is NOT in the uiautomator text attr),
  type query + click the result row + frequently-used area, then the
  FATAL/ANR logcat gate (exit 1 on crash; screenshots to /tmp/opencode).
  VERIFIED PASS on emulator-5554 (result row = Jenny Appleseed contact) and
  phone R5CT60JX7FM (fixed release installed in-place; versionCode 21408018).
- rw6 tag on GitHub still carries the pre-fix NPE for downloaders — rw7
  re-tag pending go-ahead.
## 2026-10-30 — recents long-press NPE (rw8, user's phone)

Phone crash 14:20:12 MDT: NPE Drawable.setBounds in BitmapUtil.getBitmap:85
← SearchShortcutHandler.createShortcut(:163) ← RecentItemPresenter.onItemLongClick:140.
Long-press a recents row BEFORE the async icon decode lands (or on failed
resolve) — Fresco used to always hold a hierarchy drawable; the rework's
plain ImageView leaves view.getDrawable() null. Donor has the same latent
unguarded code.

Fix (b4c43a4): BitmapUtil.getBitmap(Drawable,int) null-guards → null;
SearchShortcutHandler gets simpleDraweeView==null guards in LocalSearchSubitem
+ LocalSearchItem overloads and a bitmap null-check inside the
FrequentlyUsedItem try/catch (null = skip shortcut, not crash).

Phone note: rw8 on the phone is RELEASE-signed (cert = evie-release.jks) —
unobfuscated trace came from the old catch-all keepnames still present at
build time, NOT from a debug build. app-debug.apk install
→ INSTALL_FAILED_UPDATE_INCOMPATIBLE; app-phoneDebug.apk installs in-place.
Phone channel going forward = phoneDebug APK.

Verified: emulator-5554 (overlay recents long-press → deep-shortcut popup,
frequently-used long-press → App options popup, 0 FATAL) + phone R5CT60JX7FM
(in-place phoneDebug install, logcat -c → relaunch → 0 FATAL, pid alive).
DiscreteSeekBar WIP deliberately left uncommitted (not part of this fix).

## 2026-10-30 — code-review sweep of the 50 unpushed commits (9 fixes)

Reviewed origin/main..HEAD (50 commits, 122 files) with donor-smali ground
truth. Found + fixed, one commit each:

1. 4e42f91 fix(widgets): map provider resize modes — AppWidgetResizeFrame
   tested framework-unassigned bits 2/4; both-way widgets were
   horizontal-only, horizontal-only non-resizable. Framework 0/1/2/3
   already match the class encoding; donor hardcodes mode 3.
2. 18742f8 fix(badges): keep extractor widget id on success — the new
   finally deleted the appwidget id on every path (donor keeps it after
   createWidget+setAppEnabled); badge extractors died after first bind.
3. b22221f fix(desktop): restore drop snap and resize guards — two donor
   conditions inverted in Workspace.onDrop: frame only for j != -101
   (desktop); snap only when page != target && !hotseat (donor smali
   :2967/:2992 verified).
4. d4de946 fix(settings): rework slider touch gestures — DiscreteSeekBar
   now fires onStartTrackingTouch (grid-line previews), defers claiming
   the drag until sideways move > touch slop (vertical swipes scroll the
   page), tap-jumps on up, Math.round mapping (last step was half-width),
   setMin/setMax push the other bound instead of inverting.
5. 0e30c7e fix(settings): dismiss slider popup on detach — WindowLeaked.
6. 60fdac2 fix(r8): keep animated layout accessors — DragLayer$LayoutParams
   width/height/x/y + CellLayout backgroundAlpha (by-name animators were
   stripped in release).
7. 6c0df80 fix(search): refresh outline after resize — SpringySearchView
   outline provider re-installed on every size change (a33ba96 only
   re-indented; block still sat inside the background==null gate).
8. 27bc173 fix(search): snapshot db handle in readers — LocalSearchManager
   readers race restart() (backup restore): snapshot once + isOpen check.
9. da9680a fix(provider): end migration transaction on error — donor's
   catchall ends + rethrows; the empty swallow left the tx open and
   nested the workspaceScreens one inside it.

Verified: assembleDebug/assemblePhoneDebug green; phone R5CT60JX7FM
in-place phoneDebug install → relaunch → 0 FATAL, pid alive (phone went
off-USB afterwards; a stray `cmd notification post` testtag notification
may remain in its tray — dismissable). Emulator: Homescreen settings —
drag Columns → grid lines + pin bubble show during drag, preview reflows,
lines hide on release; swipe-up started on a slider row scrolls the
list without changing the value; 0 FATAL. Widget resize-frame path not
UI-verified on emulator (no widget on the desktop) — donor-smali verified.
DiscreteSeekBar setMin/setMax untested in UI (no current call site
depends on inverted bounds).

## 2026-10-30 (later): Xiaomi contact-action fix

Report (Portuguese Xiaomi 17 / HyperOS 16): contact rows fine, but tapping
CALL/TEXT actions offered unrelated apps; reporter suspected
`com.android.contacts`.

Cause (verified on Samsung): `com.android.contacts` in the link is the
contacts PROVIDER authority, present on Xiaomi too — not the bug. Real
cause = donor default-app probes: contacts VIEW probe lacked the contact
MIME type (resolver / generic messaging VIEW handler could win), phone
probe used raw ACTION_CALL (bypasses the dialer role → resolver or Zoiper),
text probe used SENDTO sms: which WhatsApp also registers (get(0)
nondeterministic), and resolver detection only matched the AOSP
ResolverActivity class name (MIUI's lives in package `android` with a
different class).

Fix (b844ee8, 2 files: LinkResolver + com.evie.search.local.LocalAppsManager):
- Contacts: VIEW + CONTENT_ITEM_TYPE probe → APP_CONTACTS category
  fallback (both with `android`-package guard); OPEN_CONTACT rows are
  filtered by the rework so this path is hardening only.
- Phone: TelecomManager.getDefaultDialerPackage() tier → donor probe.
- Text: Telephony.Sms.getDefaultSmsPackage(Context) (public API; consults
  the SMS role on Q+) → donor SENDTO probe. NOTE: first implementation
  used the Secure setting `sms_default_application`, which is ABSENT on
  the Samsung (`settings list secure` has no such key) even though
  `cmd role get-role-holders android.app.role.SMS` = Google Messages —
  role state is not mirrored into that Secure key there, hence the
  Telephony API.
- Resolver hardening in both LocalAppsManager classes: skip resolves
  whose package is `android` (OEM resolvers), in resolveActivity and in
  the queryIntentActivities fallback list.
- LinkResolver: tel: without CALL_PHONE → ACTION_DIAL targeted at the
  telecom default dialer, else bare ACTION_DIAL (no chooser); sms: →
  SENDTO targeted at the Telephony default, else bare SENDTO.

Verification: emulator chips cannot render (no recently-used contacts:
provider `last_time_contacted` returns 0 and ignores all writes — AOSP
gap), so contact-row tap path only: 0 FATAL. Phone R5CT60JX7FM: in-place
phoneDebug install → relaunch → 0 FATAL, pid 6193; roles verified
(SMS=Google Messages, DIALER=Google Dialer). HAND TEST ON PHONE STILL
PENDING (user): search a recently-contacted person, tap CALL → default
dialer prefilled, no chooser; tap TEXT → default SMS app.

Known environment artifact (not a regression): the evie-test AVD
(Android 13, sdk_phone64_x86_64) intermittently ANRs with "Application
does not have a focused window" under scripted tap/type input —
reproduced identically on a build without the fix; the launcher main
thread was IDLE in nativePollOnce in the traces. AVD display also went
black/wedged late in the session (black screencaps, empty dumps,
0 FATAL). Verify via FATAL grep only on this AVD.

## 2026-10-30 (later): contact-row tap opens unrelated apps — fixed on-device

User report ("It doesn't work that way") → drove the REAL phone
(R5CT60JX7FM): search overlay → "adam" → tap contact row.

Reproduced the Xiaomi reporter's bug exactly: tapping the contact row
fired `START u0 {act=VIEW dat=content://com.android.contacts/...}`
resolving to `android/com.android.internal.app.ResolverActivity` —
"Open with" offered **Google / Lichess / Messages** (generic content
handlers; screenshot /tmp/opencode/phone-tap1.png). No CALL/TEXT chip
rows exist on this device for a tapped contact — the contact row is the
only tap path (earlier chip claims were wrong).

Two-layer root cause in LinkResolver case "contact":
1. Index stores links as
   `contact:<URL-encoded FULL contacts URI>` (LocalSearch.db item.link,
   e.g. `contact:content%3A%2F%2Fcom.android.contacts%2Fcontacts%2F
   lookup%2F3312i...%2F1`); the code prepended
   `content://com.android.contacts/contacts/` onto the ALREADY-full
   decoded ssp → double-prefix garbage URI (contacts app opened, found
   nothing, auto-finished back to the launcher).
2. No MIME type + no package targeting → resolver with unrelated
   handlers.

Fix (commit 3e4f3e0): ssp already starting with `content://` is used
directly (bare-id legacy form still prefixed); VIEW + contact MIME
`vnd.android.cursor.item/contact`; queryIntentActivities(VIEW+mime)
→ prefer system app (skip `android`-package resolver) → setPackage so
no chooser ever appears; LocalAppsManager.getResolveInfoForIntent
fallback also prefers system apps (user-installed file managers claim
every MIME via generic content VIEW). Verified on phone by am start
probe (pkg=com.samsung.android.app.contacts → contact card) then
re-driving the tap: `cmp=com.samsung.android.app.contacts/
com.samsung.android.contacts.detail.ContactDetailActivity` directly,
Adam Lowell card stays open (screenshot /tmp/opencode/phone-tap5.png),
0 real FATAL (logcat "FATAL EXCEPTION" matches were adbd echo of the
grep commands themselves). assembleDebug + assemblePhoneDebug green.


## 2026-10-30 (late): tiered contact-app resolution (8499fc4)

Code-review follow-up on 3e4f3e0 (plan-mode review findings, user
approved; comments removed from code — rationale lives in the commit
message). LinkResolver case "contact" now resolves in tiers:
1) resolveActivity(VIEW+contact-mime, MATCH_DEFAULT_ONLY) honors the
   user's actual default ("android"-package = system resolver = none
   set → falls through);
2) flag-0 list query, prefer FLAG_SYSTEM non-"android" then any
   non-"android" — flag 0 kept deliberately: MATCH_DEFAULT_ONLY could
   drop OEM handlers lacking CATEGORY_DEFAULT (Samsung's
   ContactDetailActivity is launchable via setPackage, verified);
3) nothing claims the typed VIEW → setData drops the MIME type so the
   legacy untyped query still matches (launcher's
   ActivityNotFoundException path covers "neither").
Also removed the (overlapping) comment blocks this branch had added to
case "contact" and inlined the java.util.List import.

Verify:
- Phone R5CT60JX7FM (phoneDebug in-place install, 0 FATAL, pid alive):
  wake → HOME → search bar (540,180) → "adam" → Adam Lowell row
  [212,373][488,438] tap → START shows
  `dat=content://com.android.contacts/... typ=vnd.android.cursor.item/
  contact pkg=com.samsung.android.app.contacts
  cmp=...detail.ContactDetailActivity`, mCurrentFocus = contact card,
  stays open (screenshot /tmp/opencode/phone-contact-tier.png). No
  resolver/chooser.
- Emulator-5554: was gone from adb (died after the black-screencap
  wedge) → cold boot + reinstall + HOME role re-added; relaunch
  0 FATAL; overlay (540,146) → "ada" → Ada row [194,335][457,394] tap
  → resolved `pkg=com.android.contacts
  cmp=.quickcontact.QuickContactActivity` directly (tier 1/2 found the
  AOSP system handler; tier-3 untyped fallback not exercised here),
  0 FATAL. The "focused window" ANR under scripted input remains the
  known environment artifact — not seen this round.

Both variants green (assembleDebug 25s, assemblePhoneDebug 51s). Tree
clean after 8499fc4; branch fix/full-review-3, ~67 commits ahead of
origin/main; nothing pushed (push/release blocked on explicit
go-ahead).

## 2026-10-31 (gesture-strand fix build + settings card restyle)

### Gesture-strand fix (Launcher.java, UNCOMMITTED — pending user
phone re-test)
- Fix: main-layer router now settles the QSB/all-apps springy trackers
  when a custom-gesture claim starves the stream mid-gesture (inline
  CANCEL to each active tracker, springs settle to the nearest end) and
  onDown heals already-stranded drawer states (factor<0.5 with no end
  value locks to the drawer again).
- Emulator-5554 verification: drawer opens (chevron 539,1855); slow
  swipe follows and springs back FULLY open; fast fling leaves no
  strand; workspace pull-down still engages search; 0 FATAL/ANR.
- Phone R5CT60JX7FM: phoneDebug in-place install Success, relaunch
  0 FATAL. AWAITING the user's hand re-test of the drawer recipe.

### Settings card restyle (7a808b8)
- Gestures: one white card with all 8 rows + inset dividers
  (set-gestures2.png); first attempt drew no dividers — the inset
  divider drawable has no intrinsic width, so SettingsListDividerDecoration
  must setBounds(parent L/R) (set-gestures.png shows the missing-
  divider state).
- Gesture action-picker (Double tap, shared layout): same one-card
  style, rows None → Screen lock (set-picker.png).
- Hide apps: muted heading + single card (Hide from search + app rows),
  adapter header type removed / positions shifted (set-hideapps.png,
  set-hideapps-bottom.png).
- Badges: dark card (mock_screen_background #18212c) behind the demo
  icon strip, white labels readable (set-badges5.png). The "Unread
  count"/"Notification count" section headers remain green (out of
  scope as asked).
- Notification-listener access was granted on the emulator via the
  system page (row taps; dialog ALLOW @ (540,1693)).
- All screens 0 FATAL while driving; launcher restored to workspace.

Phone R5CT60JX7FM: assemblePhoneDebug green (53s), in-place install
Success, relaunch 0 FATAL (pid 6520). Launcher.java gesture fix still
UNCOMMITTED (tree has only that file modified). Branch main, 1 ahead
of the released 811fcd4; nothing new pushed.

## 2026-10-31 — Hide apps split tuning (7dc423d)

User tweak of the card restyle: remove the small in-content "Hide apps"
heading (the submenu title bar already names the screen) and split the
single card into two — "Hide from search" alone in the first, the app
list in the second.

- settings_activity_hide_apps_fragment.xml: heading include removed;
  card 1 wraps settings_activity_hide_apps_config (the switch row);
  card 2 keeps the RecyclerView. Cards at 12/10/12/10 margins like the
  badge screen's stacked cards.
- HideAppsSettingsFragment: switch binding moved from a RecyclerView
  row (TYPE_CONFIG) to onViewCreated via the predeclared
  R.id.hide_from_search; adapter is app rows only now (itemCount =
  size(), positions map 1:1). ConfigViewHolder removed; stale
  heading TextView bind dropped; unused hide_apps_heading id removed.
- Emulator (emulator-5554): screencap set-hideapps-split.png —
  title bar + switch card + app-list card, no in-content heading;
  scroll reaches the last rows (…/TMoble/WebView Browser Tester);
  checked-state and sort (selected first) unchanged. 0 FATAL.
- Phone R5CT60JX7FM: phoneDebug in-place install Success, relaunch
  0 FATAL (pid 10468). Launcher.java gesture fix still UNCOMMITTED,
  pending the user's phone re-test. Branch main at 7dc423d (2 ahead
  of released 811fcd4); nothing new pushed.

## 2026-10-31 — Badge demo card tightened (7133458)

User: the Unread badges dark card had too much space top and bottom
behind the demo icons — keep the spacing, only shrink the background.

- Cause: the demo strip kept the donor's fixed 150dip height while the
  BadgeableAppIcon content is ~61dip (8dip pads + 26dip icon + label);
  the restyle's dark card made that dead space visible.
- Fix: strip height 150dip -> 62dip (content measured via uiautomator:
  icon nodes 168px, density 2.75 px/dip). Icons, label and spacing
  untouched; card went 470px -> 227px (82dip incl. the 10dip paddings).
- Emulator: card [33,248][1047,475] vs icons [*,277][*,445] — 10dip
  dark above/below, icons unchanged (badges-tight.png); 0 FATAL.
- Phone R5CT60JX7FM: phoneDebug in-place install Success, relaunch
  0 FATAL (pid 11794). Launcher.java gesture fix still uncommitted
  (pending phone re-test). Branch main at 7133458; nothing pushed.

## 2026-10-31 — gesture strand fix committed (b9c8c94)

User re-test on the phone: "The drawer issue is not reproducible" —
re-tested on rw9.1, which does NOT contain the fix, so the strand is
intermittent rather than gone. The fix stays as protective hardening
(root cause code-verified: gesture claims, including unassigned ones,
close the router gate mid-stream; trackers never see UP/CANCEL; the
spring parks at the last fed factor) and adds the at-rest heal clause
in onDown.

- Committed the Launcher.java fix (22 insertions, 1 deletion) with the
  deviation rationale in the commit body.
- Emulator relaunch sanity: 0 FATAL (pid 4614).
