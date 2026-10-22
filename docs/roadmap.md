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