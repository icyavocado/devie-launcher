# Evie Launcher Rework — Build Plan

## Decisions locked (from planning session)

| Decision | Choice |
|---|---|
| Strategy | Gradle rebuild from decompiled source |
| Scope | Working build first; feature rework after |
| Package | NEW name — default `app.dai.evie` (not `is.shortcut`) |
| Cloud features | Remove (side-screen feed, Jigsaw, remote/web search, analytics, GMS extras) |
| Kept | Local search, notifications/badges, app lock, backup/restore, feedback, upgrade card |

## Key references

- Decompiled source donor tree: `/tmp/opencode/evie-src`
- APK: `/home/dai/projects/evie/evie-launcher-2.14.8.12_arm64.apk` (Evie 2.14.8-12, pkg `is.shortcut`)
- jadx 1.5.6 standalone: `/tmp/opencode/jadx`
- Toolchain (**Docker build env** as requested): `Dockerfile` at repo root — base `gradle:9.8.1-jdk17-noble` + cmdline-tools + platform `android-35` + build-tools `36.0.0`; builds run as `docker run` with project + persistent gradle-cache volume; wrapper pins Gradle 9.8.1
- AGP 9.4.1 (requires Gradle ≥ 9.6.0, build-tools ≥ 36.0.0, JDK 17)
- Known broken method: `LauncherModel.LoaderTask.loadWorkspace()` (jadx type-inference overflow) — restore from AOSP Launcher3 (Apache-2.0), cross-check jadx smali dump with `--comments-level debug`
- App-side decompile quality: only 1 broken file; 7 other errors are in replaceable Google/ad SDK classes

## Final feature scope

**KEEP:** desktop grid, folders, dock, drag & drop, widgets + picker + resize, wallpaper picker,
icon packs, app shortcuts/deep shortcuts, backup/restore, onboarding + intro video,
search overlay (local apps/contacts/settings only) + `ACTION_WEB_SEARCH` intent,
hide apps, app lock (fingerprint + timeout), notification listener/badges/extractors,
custom gestures, feedback, all settings screens, upgrade/what's-new card,
vendored `com.voxel.recyclerview` fork.

**CUT:** side-screen feed (`com.evie.sidescreen`, ExoPlayer, AOL-Oath player, web viewer,
personalize), Jigsaw (`com.evie.jigsaw`), remote+web search managers (`com.evie.search.remote`,
`com.evie.search.web`, `api.LocationHandler`), launcher settings importer
(`com.voxel.simplesearchlauncher.importer`), Crashlytics/Fabric/Firebase/events/AB-tests,
GMS ads/maps/auth/install-referrer, `MemoryDumpActivity`, `ToggleWeightWatcher`,
`assets/mocktopicdetail.txt`, `res/raw/upgrade_feed.mp4`.

---

## TODO list — one commit per todo

Phase order matters; commits land in this sequence.

- [ ] `chore(docker):` Docker build env — Dockerfile (gradle:9.8.1-jdk17-noble + cmdline-tools + platform android-35 + build-tools 36.0.0), `scripts/evie-build.sh` wrapper, `.gitignore`, `.dockerignore`; verify image builds
- [ ] `chore(skeleton):` Gradle project skeleton in `/home/dai/projects/evie` — root gradle files, `app/` module, gradle wrapper, git init, `.gitignore`, `gradle.properties`; Java-only, `minSdk 21`, `compileSdk 35`, `targetSdk 35`, pkg `app.dai.evie`, placeholder manifest + launcher activity stub
- [ ] `chore(res):` copy resources — `resources/res/*` → `app/src/main/res`, `assets/fonts/`, `res/raw/evie_intro.mp4` + `res/raw/upgrade_searchbar.mp4` (skip `upgrade_feed.mp4`; drop `mocktopicdetail.txt`), fix duplicate-resource collisions
- [ ] `chore(code):` copy app code — `com/voxel/launcher3`, `com/voxel/simplesearchlauncher`, `com/evie/{common,config,models,screen,search/local}`, `com/voxel/recyclerview` → `app/src/main/java/...` (cut features excluded; see next commits)
- [ ] `refactor(pkg):` rename package `is.shortcut` → `app.dai.evie` — sed all `is.shortcut.R` imports, rewrite manifest `package=`/`authorities`/custom permissions namespace, application class path
- [ ] `build(deps):` swap third-party for Maven — androidx/appcompat/recyclerview/material, Fresco, ButterKnife 8.8.1 runtime, Dagger 2 runtime (no processor), Gson, Timber, Rebound, LeakCanary (debug), Retrofit/OkHttp only if still referenced; wire `dependencies {}`
- [ ] `refactor(cut:sidescreen):` remove `com.evie.sidescreen` + ExoPlayer/Oath player refs, manifest activities/services/receivers/permissions, feed hooks in Launcher + simplesearchlauncher callers
- [ ] `refactor(cut:jigsaw):` remove `com.evie.jigsaw` + its manifest entry + deep-link/caller references
- [ ] `refactor(cut:searchcloud):` strip `com.evie.search.remote`, `com.evie.search.web`, `api.LocationHandler`; wire web queries to `ACTION_WEB_SEARCH` intent in search presenter
- [x] `refactor(cut:analytics):` remove Crashlytics/Fabric/Firebase/events/AB-test/analytics services imports+calls, GMS ads/maps/auth/referrer fragments + manifest entries, prune permissions 45 → lean set (keep listener/fingerprint/wallpaper/contacts/phone/boot) — committed `abd63b3` (AnalyticsHandler local stub; backup/restore reworked to SAF; includes `chore(cleanup)` jars `8138c71`, `docs(readme)` `4fcab37`)
- [x] `refactor(cut:importer):` remove `com.voxel.simplesearchlauncher.importer` + its settings entry point + legacy launcher READ_SETTINGS permissions — committed `2dafa97`; kept trimmed `LauncherImportHelper.importItems` + `ImportData` (default dial/sms/camera/browser row placement), deleted 15 scrapers + `ImportIntroSlide`/`ImportSlide`, `IntroSlide`/`OnboardingActivity` rewired, 22 manifest permissions remain
- [x] `refactor(cut:debug):` remove `MemoryDumpActivity`, `ToggleWeightWatcher` + manifest entries — committed `dd69ac2` (MemoryTracker service kept for weight-watcher infra); `build(app): re-enable BuildConfig generation` = `fdde821`
- [x] `fix(model):` restore `LauncherModel.LoaderTask.loadWorkspace()` from AOSP Launcher3 source, adapt to Evie's ItemInfo/Wave deltas, compile-verify — committed `f111a02` (transcribed from jadx `--comments-level debug` register dump of classes.dex; flags-head preserved, `getLauncherProviderSafe` + `CONTENT_URI_NO_NOTIFICATION`, boolean-return kept, `util/MutableInt` local re-impl of the hidden-API class)
- [x] `build(green):` first full compile loop — grind remaining decompile artifacts (casts, lambda classes, butterknife binder mismatches, R refs in vendored code) until `:app:assembleDebug` passes — committed `ae50e9d` (`chore(vendor)`: cpv/discrete-seekbar/sectioned-adapter vendored + styleables) + `6a4f53e` (`build(green)`: lambdas de-static'd, jadx exception-flow reworks, Dagger 2.22.1 hand-patched graph, Room 2.6.1 via annotationProcessor from annotated @Dao/@Entity, jetifier on, cross-package imports, res dedupe; assembleDebug green producing app-debug.apk)
- [x] `build(sign):` release signing — generate new keystore, `signingConfigs` in gradle, `assembleRelease` green — committed `6230c2d` (self-signed `keystore/evie-release.jks` (PKCS12), passwords in git-ignored `keystore/evie-release.properties`, release lint vitals relaxed until the merged res table is trimmed; app-release.apk built + signed)
- [x] `test(device):` install + smoke test per checklist below — record results in repo `docs/smoke-test.md` — committed `2a5420a` (emulator api33 round: install/home-role/loader/drawer/drawer-click/long-press-sheet pass, desktop-item click = open defect; parallel phone api36 round logged in `docs/smoke-test.md` `c3d5109` w/ compat fixes `7010a2e`)
- [ ] `docs(roadmap):` capture post-build rework backlog (Phase 6 user feature requests go here) — drafted `docs/roadmap.md` (P1 device defects: desktop-click, default-page/temp-screen; P2 decompile debt; P3 packaging; P4 cut-feature notes)

## Smoke-test checklist (for `test(device)` commit)

- [ ] Installs; prompt → set as home works
- [ ] Desktop: drag icon, reorder, create folder, drop to dock
- [ ] Widget: add, place, resize frame handle
- [ ] Search: swipe up, query apps/contacts/settings, web intent fallback fires
- [ ] Fingerprint lock prompt on locked app launch
- [ ] Badge counts on app icons appear (if enabled by default)
- [ ] Backup: export file → import on fresh install
- [ ] Icon pack: pick pack, icons remap
- [ ] No crash on rotation/kill-relaunch; no leftover calls into removed cloud code