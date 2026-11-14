# Plan — Build the draw screen: select names, draw, reveal

## Intent
Replace NAM-7's Draw placeholder with the real 3.0 Draw screen from the design spec. It has chips, the tri-state select-all and counter, the sort pill, the stepper and Draw button, the full-window reveal (expressive, plus a reduced-motion crossfade) and the empty state. `:shared` moves to the 3.0 rules: Unsorted → A–Z → Z–A → Unsorted, minimum 1, the in-the-hat pool, and the grid preference retired.

Per the decision log, the screen is **drawn-aware but never marks names drawn**: NAM-12 adds `markDrawn`, Reset and Put back.

Sequencing: implementation starts only after NAM-8 (`flow/groups-screen`) merges. Rebase onto `main` first and reuse what NAM-8 adds: `ui/ListStatus.kt`, `ui/components/{UpButton,HatIllustration}.kt`, `ic_more_vert`/`ic_add`, `action_more`, the Draw entry's `singlePane` parameter and the title's shared-bounds key.

## Acceptance criteria
The ticket's ACs, as amended by the 2026-11-12 spec comment and the 2026-11-14 NAM-9 hand-off.

- [ ] **AC1. Upgrade.** Install over a 2.7.0-migrated install (a release-signed build on `api36_niah`; never a debug install there).
  - A list opens with the same names selected and the same sort order, including a list stored as Z–A. This is the sort half of NAM-6 AC6 that NAM-8 handed over.
  - The column clause is dropped.
  - The expected values come from what NAM-5, NAM-6 and NAM-8 recorded for `api36_niah` (selection per list, `group_sort_type_for_group_<id>`). Task 9 reads them before installing.
- [ ] **AC2. Selection persists.** Toggle a chip, go back to Lists and reopen the list: the selection is the same. Also after force-stop and relaunch.
- [ ] **AC3. Counter.** "n of t in the hat", plus "· d drawn" when d > 0, with plurals. Unit tests cover 0, 1 and many selected, a list of 1, and drawn names present. Seen on device.
- [ ] **AC4. Select all.** Unit tests cover the box state over not-drawn names: All, Some, None, and Disabled when every name is drawn. Tapping it clears all when the box is All, and otherwise selects all. Drawn flags are never changed. Seen on device, including Disabled with `drawn` seeded via sqlite3.
- [ ] **AC5. Sort.**
  - The cycle is NONE → ASC → DESC → NONE (`NameSortTypeTest`, `SortOrderTest`), case-insensitive (`SortNamesTest`) and per list.
  - On device, the pill shows Unsorted/A–Z/Z–A, and the choice survives force-stop and relaunch.
- [ ] **AC6. Stepper.**
  - n starts at 1. A restored ViewModel keeps the stored n: no clamp write-back runs before the names have loaded (unit test).
  - − is disabled at 1 and + at the in-the-hat count.
  - Deselecting below n clamps n down, and n stays clamped when names are reselected.
  - Covered by unit tests and seen on device.
- [ ] **AC7. Draw.**
  - A draw of k returns k distinct names, all selected and not drawn. A seeded `RandomNumberGenerator(Random(seed))` gives the same result twice (unit tests on `NameDraw` and `DrawViewModel`).
  - Production binds `RandomNumberGenerator()` (`Random.Default`).
  - The draw never calls `markDrawn` (unit test: the fake records no `markDrawn` call).
  - Toggling a name off and tapping Draw straight away never draws that name (unit test with a fake that delays the re-emit). A double tap draws once.
- [ ] **AC8. Minimum to draw.** `MIN_NAMES_TO_DRAW == 1`. With 0 in the hat:
  - the stepper is hidden;
  - the bar reads "Tap names to put them in the hat" (or "Everyone's been drawn" when every selected name is drawn);
  - Draw is disabled.
  Covered by unit tests and seen on device.
- [ ] **AC9. Result survives.** With the reveal showing:
  - rotating shows the same names;
  - backgrounding, then `adb shell am kill uk.lobsterdoodle.namepicker`, then relaunching shows the same names;
  - a unit test builds a second ViewModel from the same `SavedStateHandle` and gets the same result;
  - after rotation or restore, the reveal shows its end layout without replaying the animation.
- [ ] **AC10. Reduced motion.** With `settings put global animator_duration_scale 0`, the reveal appears with no shake and no stagger (a screen recording shows one frame or one crossfade). The result and the announcement are the same.
- [ ] **AC11. Empty state.** A list with no names shows the hat, "This hat is empty", the body line and **Add names**. There is no header and no bar. Add names opens Edit list for this list, and back returns to Draw. `BackStackTest` covers `editList(id, fromDraw = true)`, which already exists.
- [ ] **AC12. Matches the spec.** Screenshots in `evidence/`:
  - phone portrait and landscape, and the tablet (list-detail; the reveal covers both panes), in light and dark;
  - the chip states (with drawn seeded) and the bar states;
  - the reveal with 1 and 3 names.
  - TalkBack announces "Drawn: …" when the reveal opens, chips read "Ada, in the hat" / "Ben, out of the hat" / "Chloe, drawn", the box reads "Select all, 15 of 24 in the hat", and the stepper value is announced.
  - The status-bar icons are readable over the reveal's backdrop, in light and dark.
  - The reveal dismisses with Done, with back (with a predictive-back preview on API 36 gesture nav, or the recorded fallback) and with Escape on a hardware keyboard (`adb shell input keyevent 111`).
  - In single pane, the Lists → Draw title morph runs.
- [ ] **AC13. ViewModel tests.** `DrawViewModelTest` covers selection, select all, sort, count clamping, draw, draw more, dismiss, the saved-state restore, and the existing 6 status and active-list tests.
- [ ] **AC14. Grid retired.** `grep -rnE "GridColumns|GridLayout|setGridColumns|\.gridColumns|DEFAULT_GRID_COLUMNS" shared/src app/src` finds nothing. Legacy grid, selection and dialog strings are removed from `strings.xml`.
- [ ] **AC15. No hard-coded copy.** `grep -rnE '(Text\((text = )?|contentDescription = |stateDescription = |paneTitle = )"' app/src/main/kotlin/uk/lobsterdoodle/namepicker/ui/draw/` finds nothing. Every count uses plurals, and the announced name list is joined with `android.icu.text.ListFormatter` (API 26+), not a hard-coded " and ".

## Approach

### `:shared` (rules; all unit-tested)
- **`NameSortType.next()`:** DESC → NONE. Update the KDoc in `NameSortType`/`SortOrder` and the tests.
- **`DrawState`:** rebuilt around the spec vocabulary.
  - Counts: `inHat` (selected && !drawn), `total` and `drawnCount`.
  - `box: SelectAllBox` (ALL/SOME/NONE/DISABLED) over not-drawn names.
  - `bar: DrawBar` (`Stepper(max = inHat)` / `NothingInHat` / `AllDrawn`) and `hasNames`.
  - `MIN_NAMES_TO_DRAW = 1` gates Stepper.
  - `fun clampCount(n, inHat) = n.coerceIn(1, maxOf(1, inHat))`.
- **`NameDraw.drawFrom`:** draws from the in-the-hat pool. Its KDoc names the pool.
- **Grid removal:** delete `GridColumns`, `GridLayout` and their tests; `PreferencesRepository.gridColumns`/`setGridColumns`; the DataStore implementation; both fakes; and the grid assertions in `PreferencesRepositoryTest`, `PreferencesThreadingTest` and `LegacyNameListMigrationTest`.

### `:app`

**`ui/draw/DrawViewModel`** grows the placeholder ViewModel (assisted `listId`) and keeps `status` and `onShown()` unchanged.
- **Inputs:** a `SavedStateHandle`, `NameRepository`, `NameEditor`, `SortOrder` and `NameDraw`.
- **`uiState`:** `combine(status, observeNames, sortOrder.observe, count, result)` → `DrawUiState`, which holds:
  - the title;
  - the sort type;
  - chips: `id`, `text`, visual In/Out/Drawn, keyed by id;
  - the `DrawState`;
  - the effective n;
  - `reveal: RevealState?`, holding the names, `more = min(n, inHat)` and `noneLeft = inHat == 0`. `more` is live, so NAM-12's marking flows through.
- **Loading:** names start as `null` (not loaded), not `emptyList()`. `uiState` stays Loading until they arrive, so the empty state never flashes.
- **Saved state:** only platform-safe types, so the state can't crash at `onSaveInstanceState`.
  - `count: Int`.
  - The result as `resultIds: LongArray` + `resultTexts: ArrayList<String>`. The texts are kept so a later rename can't change what is shown.
  - `revealPlayed: Boolean`.
- **Clamp write-back:** runs only once names have loaded. Whenever in-the-hat drops below the stored count, the stored count is written down to the clamped value.
- **Writes and the draw are serialised:** one `Mutex` covers `toggle`, `toggleAll` and `draw`.
  - `draw()` takes the lock and re-reads `observeNames(id).first()` after any pending write.
  - It then runs `nameDraw.drawFrom(names, clamp(n))` and stores the result. There is no animation in the ViewModel.
  - A `drawing` flag, set synchronously before the coroutine launches, makes a double tap draw once. The flag stays correct once NAM-12 adds its suspending `markDrawn` inside the lock.
  - Draw does nothing while a result is showing, or when fewer than `MIN_NAMES_TO_DRAW` are in the hat.
- **`drawMore()`:** replaces the result with a new draw of `min(n, inHat)`.
- **`dismiss()`:** clears the result.
- **Other actions:** `toggle(id)`, `toggleAll()`, `cycleSort()`, `increment()` and `decrement()`.

**UI** in `ui/draw/`:
- **`DrawScreen`:**
  - `Scaffold`, with a medium flexible top bar in portrait and a single-row bar in compact height.
  - The sticky `SelectAllHeader`.
  - A `FlowRow` of `NameChip` in a vertical scroll.
  - `DrawBar`: bottom in portrait, a trailing 200 dp panel in compact height.
  - `DrawEmptyState`.
  - `RevealOverlay`.
- **The box:** a custom `TriStateCheckbox`-semantics box (toggleable, `ToggleableState`) drawn to the spec, with the tonal dash. Stock colours can't do the tonal dash with its border.
- **Chips:** `Modifier.toggleable` plus `stateDescription`. Drawn chips are not toggleable (Put back is NAM-12). There is a 0.96 press scale on `MaterialTheme.motionScheme.fastSpatialSpec()`.
- **The stepper:** an M3 1.5 connected `ButtonGroup`, with a `liveRegion = Polite` value.

**Reveal** = a `Dialog(properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false))`, so it covers the whole window, both panes included, from inside the detail entry.
- **Content:**
  - the `primary`/`primaryContainer` backdrop;
  - the hat: NAM-8's `ui/components/HatIllustration`, given optional `body`/`band`/`sparkle` colour parameters (defaulting to its `primary`/`tertiary`), so the empty state uses the defaults and the reveal passes `#F4F2FF` and the sparkle role (`Reveal.dc.html:38-44`);
  - the cards;
  - Done and Draw n more, or "Everyone's been drawn".
- **Motion:**
  - The shake is an `Animatable` rotation of ±6°, 3 cycles, 600 ms.
  - Each card is an `Animatable` (scale and offset) on the expressive default spatial spring, delayed by `600 + i × stagger` ms. `stagger` is 250 when n ≤ 4, and `min(250, 900 / (n − 1))` in float maths otherwise, so a 1-name draw never divides by zero.
  - The sequence plays only when `revealPlayed` is false; it then sets the flag. Rotation or restore shows the end layout without replaying.
  - Reduced motion (`Settings.Global.ANIMATOR_DURATION_SCALE == 0f`, read once per reveal) skips the shake and the stagger. The end layout fades in with a single `AnimatedVisibility(fadeIn)`, which is instant if the platform suppresses it.
- **Accessibility:**
  - On open, a `paneTitle`.
  - A `liveRegion = Assertive` node for "Drawn: Ada, Sam and Tom", built from `reveal_a11y` and a `ListFormatter` join. It is composed empty and its text is set after the first frame (`LaunchedEffect` + `withFrameNanos`), because a live region only announces a change; it isn't gated on the animation. A fresh reveal announces; a restore doesn't.
  - The status-bar icons: set the dialog window's `WindowInsetsControllerCompat.isAppearanceLightStatusBars`/`NavigationBars = false` (the backdrop is dark in both themes), via `(LocalView.current.parent as DialogWindowProvider).window`.
  - The cards are always in the semantics tree, in order, even while invisible mid-animation.
- **Dismiss:**
  - `onDismissRequest` = `dismiss()`.
  - `PredictiveBackHandler` inside the dialog scales and fades the content with progress, then dismisses.
  - `onPreviewKeyEvent(Escape)` dismisses.

**Wiring:**
- `AppNavigation`'s `Draw` entry calls `DrawScreen` with the same parameters NAM-8's `DrawPlaceholder` has once rebased (`key`, `onUp = back`, `onEdit = editList(fromDraw = true)`, `onGone = listGone`, plus NAM-8's `singlePane` and title shared key). Add names uses `onEdit`. Up is NAM-8's `ui/components/UpButton`, which keeps hiding in list-detail.
- Delete `DrawPlaceholder`/`DrawViewModel` from `ui/placeholder/` and move `DrawViewModelTest` to `ui/draw/`.
- DI: provide `NameEditor`, `SortOrder` and `NameDraw(RandomNumberGenerator())`.

### Alternatives (rejected)
- **Reveal hosted above `NavDisplay` via a CompositionLocal:** covers both panes too, but threads state through `AppNavigation`, a file NAM-8 and NAM-9 also edit. The Dialog keeps it local.
- **Clamping n only for display:** the old n would come back when names are reselected. The spec and the ticket say clamp.
- **Stock `FilterChip`:** can't do the three-visual mapping (ticket icon plus strikethrough plus no toggle for drawn) without fighting its colours.
- **Reveal state in `rememberSaveable`:** the ticket requires the ViewModel/saved-state path, and the tests assert on the ViewModel.

## Assumptions & open questions
- [answered 2026-11-14] Drawn-aware, no marking. NAM-10 builds the in-the-hat pool, the counter suffix, the box over not-drawn names, the Drawn chip visual, the bar states and the reveal endings. Draw never calls `markDrawn`. Source: decision log.
- [answered 2026-11-14] Implementation waits for NAM-8 to merge, then rebases. Source: decision log.
- [answered 2026-11-12] The spec deltas in the ticket comment (chips, the stepper, the counter, the box, the sort cycle, min 1, the reveal). Source: decision log.
- [answered 2026-11-14] The NAM-9 hand-off: NAM-10 owns the Draw empty state → `EditList(id)`. Source: decision log.
- [assumed] Reset (the top-bar icon, the AllDrawn bar button and the snackbar) and Put back (the drawn-chip menu) are NAM-12.
  - NAM-10's top bar has no Reset icon.
  - The AllDrawn bar shows only "Everyone's been drawn" and a disabled Draw.
  - Drawn chips aren't interactive.
  - Rationale: the decision-log line is "drawn-aware display only; no action that changes `drawn`". Spec `:229-242` and NAM-12's spec table own these.
  - NAM-12 deltas to name in the report: the AllDrawn bar's Reset button (spec `:221`, `:300`), the "Draw morphs into Reset" motion (`:445`) and the top-bar Reset.
- [assumed] The drawn-aware UI is verified on device by seeding `drawn = 1` with sqlite3 on `niah_debug_api36` (memory: sqlite3 is available in adb). Nothing in NAM-10 can set it. Force-stop the app before the write and relaunch after it: Room's invalidation tracker doesn't see outside writes.
- [assumed] The stepper's n lives in the entry's `SavedStateHandle`, so it is per visit to a list. Leaving and reopening starts again at 1. Spec `:225` says "kept per list in saved state", which saved state per entry satisfies.
- [assumed] "Draw n more" in NAM-10 draws again from the same pool (nothing is marked), so names can repeat between draws until NAM-12. This is acceptable because 2.7.0 behaves the same (NAM-12 context).
- [assumed] The result stores names as text plus id at draw time. A name renamed or deleted while the reveal is open still shows as drawn. That's the raffle case: a result never changes.
- [assumed] The full-window Dialog gets edge-to-edge from `decorFitsSystemWindows = false`, with the content padded by `safeDrawing` insets. The backdrop draws under the bars.
- [assumed] Predictive back on the reveal is a `PredictiveBackHandler` in the dialog content. Check on device (API 36, gesture nav) that the dialog window forwards progress. If not, fall back to plain dismiss-on-back and record it as a deviation.
- [assumed] The reduced-motion check reads `Settings.Global` once when the reveal opens. The spec says there is no Compose API, and Compose itself already zeroes other animations.
- [assumed] The hat is NAM-8's Compose `ui/components/HatIllustration` (NAM-8 `plan.md:150`), extended with colour parameters with defaults. NAM-8's call sites don't change. The Draw empty state uses the same `primary`/`tertiary` (`DrawScreen.dc.html:92-97`); only the reveal overrides them (`Reveal.dc.html:38-44`).
- [assumed] The predictive-back fallback is decided in task 9 on API 36 with gesture nav. If the dialog window doesn't give `PredictiveBackHandler` any progress, back dismisses without a preview, and that is logged as a deviation.
- [assumed] Lists → Draw title morph: NAM-8 tags the Draw placeholder's title with `sharedBounds("list-title-$id")` in single pane (NAM-8 plan). NAM-10 moves that modifier onto its real title row.
- [assumed] Compose UI tests are out of scope, as for NAM-8: `:app` has no androidTest or Robolectric set-up. The ACs use ViewModel tests and the device.
- [assumed] If NAM-9 lands first with its own `FakeNames`/`NameEditor` provider, NAM-10 reuses them at rebase instead of adding duplicates.

## Tasks
1. **Sort cycle.**
   - What: `NameSortType.next()` DESC → NONE; update the KDoc in `NameSortType` and `SortOrder`.
   - Files: `shared/…/model/NameSortType.kt`, `usecase/SortOrder.kt`, `commonTest/…/NameSortTypeTest.kt`, `SortOrderTest.kt`, and `jvmTest/…/PreferencesRepositoryTest.kt` (in "concurrent sort updates are atomic", 50 cycles now split 17/17/16 and end on ASC).
   - Verify: `:shared:allTests`.
2. **Draw rules.**
   - What: `MIN_NAMES_TO_DRAW = 1`; `DrawState` rebuilt (`inHat`, `total`, `drawnCount`, `box`, `bar`, `hasNames`, `clampCount`); `drawFrom` uses the in-the-hat pool.
   - Files: `shared/…/draw/DrawState.kt`, `NameDraw.kt`, `commonTest/…/DrawStateTest.kt`, `NameDrawTest.kt`.
   - Verify: `:shared:allTests`; the AC3, AC4, AC7 and AC8 cases.
3. **Retire the grid.**
   - What: remove `GridColumns`, `GridLayout`, `gridColumns`/`setGridColumns` and their tests and fakes.
   - Files: `shared/…/selection/GridColumns.kt`, `usecase/GridLayout.kt`, `repository/PreferencesRepository.kt`, `data/DataStorePreferencesRepository.kt`, `commonTest/…/GridColumnsTest.kt`, `GridLayoutTest.kt`, `fakes/FakePreferencesRepository.kt`, `jvmTest/…/PreferencesRepositoryTest.kt`, `PreferencesThreadingTest.kt`, `androidHostTest/…/LegacyNameListMigrationTest.kt`, `app/src/test/…/FakePreferences.kt`.
   - Verify: `:shared:allTests`; the AC14 grep.
4. **`DrawViewModel` + tests.**
   - What: move the ViewModel to `ui/draw/`; add the `uiState`, the actions, the saved-state count and result, and write-back clamping. Add the DI providers. Add `FakeNames` (it records calls), unless NAM-9 already has one.
   - Files: `app/…/ui/draw/DrawViewModel.kt`, `di/StorageModule.kt`, `ui/placeholder/PlaceholderViewModels.kt` (remove `DrawViewModel`), `test/…/ui/draw/DrawViewModelTest.kt` (moved and extended), `test/…/FakeNames.kt` (records calls, and can hold back a re-emit), `test/…/FakePreferences.kt` (`sortType`/`updateSortType` become a real per-group store).
   - Verify: `:app:testDebugUnitTest`; the AC6 (including restore), AC7 (including the toggle-then-draw race and the double tap), AC9 (unit) and AC13 cases.
5. **Copy + icons.**
   - What: add the copy-deck `draw_*` (except `draw_reset*`), `chip_in`/`chip_out`/`chip_drawn` and `reveal_*` strings, with plurals for `draw_counter`, `draw_counter_drawn` and `reveal_draw_more`. Remove `title_activity_selection`, `generated_names_dialog_*`, `selection_*`, `menu_item_action_grid_*` and `sort_alphabetically`. Add the vectors `ic_sort`, `ic_check`, `ic_ticket` and `ic_remove`, ported from `DrawScreen.dc.html`; reuse NAM-8's `ic_add`/`ic_more_vert`.
   - Files: `res/values/strings.xml`, `res/drawable/ic_*.xml`.
   - Verify: `:app:clean :app:assembleDebug`.
6. **Draw screen UI.**
   - What: `DrawScreen`, `SelectAllHeader`, `NameChip`, `DrawBar` (stepper, messages), `DrawEmptyState`; colour parameters on `HatIllustration`; portrait and compact-height layouts; the title's shared bounds.
   - Files: `app/…/ui/draw/DrawScreen.kt`, `DrawComponents.kt`, `ui/components/HatIllustration.kt` (NAM-8's).
   - Verify: `:app:assembleDebug :app:lintDebug`, then a quick install on `niah_debug_api36` for a portrait and landscape screenshot. Full checks are in task 9.
7. **Reveal.**
   - What: `RevealOverlay` (Dialog, layouts for phone, landscape and large, the motion sequence, reduced motion, announcement, dismiss paths).
   - Files: `app/…/ui/draw/Reveal.kt`.
   - Verify: `:app:assembleDebug :app:lintDebug`, then a quick install on `niah_debug_api36`: draw 1 and draw 3 (the 1-name path must not crash). Full checks are in task 9.
8. **Wire into the shell.**
   - What: the `Draw` entry → `DrawScreen`; delete `DrawPlaceholder`.
   - Files: `navigation/AppNavigation.kt`, `ui/placeholder/Placeholders.kt`.
   - Verify: full verify plus `:app:testDebugUnitTest :app:lintDebug`; `BackStackTest` unchanged and green.
9. **Device verification + evidence.**
   - What:
     - On `niah_debug_api36` and the NAM-7 tablet AVD: AC2–AC12. That includes rotation, `am kill`, `animator_duration_scale 0`, TalkBack, the status-bar icons over the reveal, Escape (`input keyevent 111`), the predictive-back preview (decide the fallback here), the single-pane title morph, and drawn states seeded via sqlite3 (force-stop, write, relaunch).
     - Then AC1 on `api36_niah`: write down the expected selection and sort per list from the NAM-5, NAM-6 and NAM-8 evidence, then install the `assembleRelease` build (check `versionName` first; `emu kill` afterwards).
   - Files: `docs/tasks/2026-11-14-draw-screen-select-draw-reveal/evidence/`.
   - Verify: the AC checklist in `report.md`.

## Out of scope
- Marking drawn on a draw, Reset with undo, Put back in the hat, and the per-name un-draw repository method: NAM-12.
- Lists, the Edit list header and the Lists half of the title morph: NAM-8. The names section of Edit list: NAM-9.
- Sharing or copying the result, a draw history, and teams (ticket "Out of scope").
- Compose UI or screenshot tests.

## Verification
- **Machine:**
  - `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug`.
  - Read the counts from `shared/build/test-results/*/*.xml` and `app/build/test-results/testDebugUnitTest/*.xml`.
  - Run the AC14 and AC15 greps.
- **Outcome:**
  - On `niah_debug_api36`: create a list of 6 with Edit list, then open Draw.
    - Toggle chips and watch the counter and box.
    - Cycle the sort, relaunch and check it persisted.
    - Step n to 4, deselect down to 2, check that n clamps to 2, then reselect.
    - Draw 2 and watch the reveal.
    - Rotate, then `am kill`, relaunch, and check that the result is the same.
    - Done, then draw again with `animator_duration_scale 0`.
    - Turn on TalkBack and listen for "Drawn: …".
  - Seed `drawn = 1` on some rows with sqlite3 and check the Drawn chips, the "· d drawn" suffix, the box, and the "Everyone's been drawn" bar.
  - Open an empty list → Add names → Edit list → back.
  - Repeat on the tablet: the reveal covers both panes.
  - Then the release install-over on `api36_niah` for AC1.
