# Rework roadmap (post-build)

Backlog carried out of the `build(green)` + `test(device)` work. Phase 6 user feature
requests also land here.

## P1 — correctness defects found on device

- **Desktop icon click doesn't launch apps.** Touch reaches the workspace (long-press
  sheet works) but a tap on an item is absorbed by the page-level `CellLayout`
  (`Workspace.addExtraEmptyScreen`/`bindAddScreens` marks pages clickable for overview
  exit). Verify item z-order inside `ShortcutAndWidgetContainer`, the launcher's
  `mWorkspace.isFinishedSwitchingState()` gate, and whether the item view is actually
  the click target after my restored `loadWorkspace()` binding. Likely a
  `mDefaultPage`/temp-screen interplay: on a fresh profile the workspace keeps a temp
  empty screen at the front and pages may be off-panned (`mDefaultPage = 1`).

- **`mDefaultPage` / screen-order on load.** `bindAddScreens()` logs an empty
  `orderedScreenIds` on a fresh profile and inserts screen id `-201`; the workspace then
  sits on a temp page until the first user interaction. The restore should snap to the
  first loaded screen.

- **Widget case of `loadWorkspace()`** was reconstructed conservatively (provider-cached
  map + restore-status math per the dex). Verify with a widget-bearing backup restore.

- **Release lint** is disabled (`checkReleaseBuilds = false`) because the decompile shipped
  the whole merged (app + libraries) resource table; lint trips on the duplicates/cycles.
  The resource table should be de-duplicated properly (drop lib-owned styles/layouts) and
  vital checks re-enabled.

## P2 — decompile-quality debt

- Several loader/provider methods still carry "REWORK: jadx lost an exception-flow local"
  annotations; the flows were made compilable (swallow + log). Re-derive their exact dex
  semantics if a crash shows up around `LauncherProvider` upgrade/migrate paths.
- `loadWorkspaceScreensDb()` and per-row icon-queue behavior (Evie deltas) should be
  cross-checked against a real Evie database with pinned shortcuts.
- `R.styleable` re-creations (Workspace, BubbleTextView, PagedView, PageIndicator,
  CellLayout, PreloadIconDrawable, Black/Mask/RoundedFrameLayout, SpringFadingEdge..., cpv/dsb)
  were reconstructed from attr indexes the dex reads; cross-check each styled attribute's
  ORDER against the dex before changing any of them.
- The vendored `SectionedRecyclerViewAdapter`'s index-based add/get implemented on
  LinkedHashMap; if a screen ever uses indexed sections, verify ordering.

## P3 — packaging / infra

- ProGuard/R8 still off for release (`isMinifyEnabled = false`). Enable + add keep rules
  for the decompiled reflectively-called classes (ButterKnife targets, Gson models).
- Baseline-profile task (`baselineProfiles/`) exists in the release output dir; consider
  shipping the donor profile or removing the dependency.
- The multidex dependency is unused for minSdk 21+ (gradle warns); remove it.
- The upgrade card re-shows on every process start; the `_last_version` write path
  should be re-verified.
- Emulator stability: host emulator process can be torn down between long adb commands;
  batch device interactions, or run with `-no-window -no-audio` + a persistent snapshot.

## P4 — deferred feature scope (was cut, may return)

- Side-screen feed (ExoPlayer-based) — cut with the cloud deps; the launcher's feed
  hook points remain (search for `sideScreenEnabled`).
- Web/remote search backends — replaced by `ACTION_WEB_SEARCH` forwarding.
- Launcher settings importer — cut; the trimmed `ImportHelper.importItems` shell remains.
- Gson-based cloud models — the gson deserializer slot in the Dagger graph is now an
  empty set; re-add when the remote models return.
## P5 — Cloudflare-post optimization round (2026-11-19)

Inspired by the 1.1.1.1 DNS-cache memory post (bench-first, carry only
what the read needs). Landed, committed:

- `5aa6a1e` search hot path: SELECT * (12 cols) -> the 7 read columns,
  per-row getColumnIndexOrThrow hoisted, O(n^2) indexOf dedup -> link
  maps. Robolectric A/B (80-item corpus, 15-query mix): 12.7 -> 11.8ms
  median, identical 47 rows; gains scale with DB size.
- `a0fa1d6` + `d7c66df` accents: fast-scroll thumb/popup/letters rail,
  NEW-section dot, list/grid toggles now follow SettingsPalette accent
  and refresh on drawer resume (no restart).

On-device (R5CT60JX7FM) A/B old-vs-new, same gestures: jank ~14% both,
p50 9->7ms, slow-UI-thread identical (14/165). The 69ms p99 outlier was
a one-off first-open bitmap upload after install (0 GC pauses logged).

Parked candidates (profiled-out or cold):
- ContactManager line ~175 queries Data.CONTENT_URI with a null
  projection (all columns) — cold path, one row per lookup; trim when
  contacts next get touched.
- Cold start wallpaper/blur pipeline: already async (decode + 25%
  downscale + fastBlur off-main); main-thread first-frame is model-load
  bound, not wallpaper-bound.
- Unlock wedge (2026-11-18, phone): first unlock after screen-off left
  the launcher unusable until lock+unlock again. No dropbox entry (no
  crash). Non-reproducible on demand 2026-11-19. Watch: live logcat
  stream to /tmp/opencode/live_unlock.log whenever the phone is
  plugged in; Samsung third-party-launcher translucent-window class is
  the suspected culprit.
- Emulator-only: workspace search-bar tap resolves to quicksearchbox
  (external WEB_SEARCH fallback); the in-app overlay opens via drawer
  pull-down as designed. Harmless; note for future search-UI work.
