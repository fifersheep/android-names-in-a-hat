# NAM-10 research — the Draw screen

Baseline: `main` at ff34628 (NAM-7 merged). The spec is `docs/design/3.0-spec.md`. The NAM-10 ticket comments (2026-11-12 spec deltas, 2026-11-14 NAM-9 hand-off) override the ticket description where they differ.

## Findings

### Spec (what to build)
- **Screen** `3.0-spec.md:151-172`.
  - Medium flexible top bar. Row 1: Up, the sort pill, Reset (icon) and the overflow (Edit list). Row 2: the list name in headlineLarge, display face.
  - Below it: the sticky select-all header, a vertically scrolling `FlowRow` of chips (16 dp padding, 8 dp gaps) and a docked draw bar.
  - Landscape (compact height): a single-row bar with a titleLarge title, and the draw bar as a ~200 dp trailing vertical panel.
- **Select-all header** `:174-193`.
  - A 56 dp row whose `TriStateCheckbox` is labelled by the counter.
  - The box state is computed over **not-drawn** names. Its "some" state is a tonal dash: `primaryContainer` fill, a 2 dp `primary` border and a `primary` dash, so it is a custom box rather than the stock TriStateCheckbox colours.
  - The box is disabled when every name is drawn.
  - Tapping it selects or clears every name, drawn ones included.
  - TalkBack: "Select all, 15 of 24 in the hat".
- **Chips** `:195-211`.
  - Three visuals for four flag combinations: In (check icon, `secondaryContainer`), Out (1 dp `outline`, no fill) and Drawn (ticket icon, strikethrough, `surfaceContainerHigh`, tap opens a "Put back" menu).
  - At least 40 dp tall with a 48 dp touch target; labelLarge; up to 2 lines.
  - Toggleable role plus a state description; a 0.96 press scale on the fast spatial spring (`:445`).
- **Draw bar** `:213-227`.
  - With 1 or more in the hat: a connected stepper [− n +] with 48×56 dp segments, plus a filled **Draw n** pill (56 dp).
  - With none in the hat and nothing drawn: "Tap names to put them in the hat" and a disabled Draw.
  - With none in the hat and some selected-and-drawn: "Everyone's been drawn" and a filled Reset.
  - n starts at 1, is clamped to 1..in-the-hat and is kept per list in saved state. The live value is announced politely.
- **Reveal** `:244-268`.
  - Order: compute → persist drawn flags → animate.
  - A full-window takeover that covers both panes on large screens. Backdrop `primary` (light) or `primaryContainer` (dark).
  - Hat shake ±6°, 3 cycles, ≤600 ms. Cards pop with the expressive spatial spring, staggered `min(250, 900/(n−1))` ms.
  - End layout: cards (`surfaceContainerLowest`, displaySmall) in a column on phones, a grid on large screens and a 2-column grid in landscape (hat leading, buttons trailing).
  - Buttons: Done (outlined) and **Draw n more** (`min(n, left)`), or the line "Everyone's been drawn".
  - Dismiss with Done, back (predictive preview) or Esc.
  - The result is in saved state. TalkBack announces "Drawn: Ada, Sam and Tom" at once.
- **Reduced motion** `:450-458`. Read `Settings.Global.ANIMATOR_DURATION_SCALE == 0`: no shake, no stagger, one crossfade, the same announcement.
- **Empty state** `:297` plus the NAM-9 hand-off. The hat illustration, `draw_empty_title`, `draw_empty_body` and a filled **Add names** → `backStack.editList(id, fromDraw = true)`. No header and no bar.
- **Copy** `:603-633`. Every `draw_*`, `chip_*` and `reveal_*` key. Plurals: `draw_counter` (on %2$d), `draw_counter_drawn` and `reveal_draw_more`. Retired: the grid toggle strings and `title_activity_selection`.
- **Mock-up sources**
  - `docs/design/3.0-mockups/project/DrawScreen.dc.html` has the exact geometry, the colour roles, the icon SVG paths (sort, reset, more, check, ticket, minus, plus, put-back) and the empty-state hat SVG.
  - `Reveal.dc.html` has the reveal layouts per size, card padding and type sizes (64/52 px for one name, 40/30 px for several) and colour roles.

### `:shared` today (NAM-4)
- `draw/DrawState.kt:10` `MIN_NAMES_TO_DRAW = 2` → 1 (D7).
  - `DrawState.of` counts **selected including drawn** (`:28-35`).
  - It has no in-the-hat or drawn counts, no tri-state and no bar-state.
  - Spec `:233` says NAM-10/NAM-12 move it onto the remaining pool.
- `draw/NameDraw.kt:22` `drawFrom` filters `selected` only (drawn names included).
- `draw/NumberGenerator.kt:12` `RandomNumberGenerator(Random.Default)`. Production-safe: no fixed seed.
- `model/NameSortType.kt:13-17` cycles DESC → ASC. D17 needs DESC → NONE. The KDoc is at `:12` and `usecase/SortOrder.kt:7`; tests are `NameSortTypeTest` and `SortOrderTest`.
- `selection/SortNames.kt` is case-insensitive and stable. It is already correct.
- Columns to delete:
  - `selection/GridColumns.kt`, `usecase/GridLayout.kt` and their tests (`GridColumnsTest`, `GridLayoutTest`);
  - `PreferencesRepository.gridColumns`/`setGridColumns` (`repository/PreferencesRepository.kt:24-31`), plus the DataStore implementation and both fakes (shared `FakePreferencesRepository`, app `FakePreferences`).
- `usecase/NameEditor.kt`: `setSelected`, `selectAll` and `clearAll` exist and never touch drawn.
- `NameRepository`: `observeNames` (insertion order), `markDrawn(ids)` and `clearDrawn(groupId)` exist (`repository/NameRepository.kt:30-34`). There is no per-name un-draw (NAM-12 adds it).
- `usecase/ActiveGroup.open` is already called by the shell's `DrawViewModel.onShown` (`app/.../ui/placeholder/PlaceholderViewModels.kt:77-81`).

### `:app` today (NAM-7)
- `DrawPlaceholder` (`ui/placeholder/Placeholders.kt:84-108`) and `DrawViewModel` (`PlaceholderViewModels.kt:59-87`).
  - The ViewModel is assisted-injected with `listId`. It exposes `status: ListStatus` (Loading/Present/Gone) and `onShown()`, which sets the active list once while the entry is STARTED.
  - The composable fires `onGone` → `listGone`.
  - Keep this behaviour, and its 6 tests in `test/.../ui/placeholder/DrawViewModelTest.kt`.
- Wiring is `navigation/AppNavigation.kt:101-108`. The `Draw` entry is in the detail pane, `onUp = back` and `onEdit = backStack.editList(id, fromDraw = true)`. Up is hidden in list-detail (`Placeholders.kt:170-178`).
- Saved state per entry:
  - `rememberSaveableStateHolderNavEntryDecorator` runs before `rememberViewModelStoreNavEntryDecorator` (`AppNavigation.kt:82-87`).
  - The VM decorator (lifecycle-viewmodel-navigation3 2.11.0) passes the entry's `SavedStateRegistryOwner` to `rememberViewModelStoreOwner` (checked in the bytecode), so a `SavedStateHandle` in an entry's ViewModel is per entry and survives process death.
  - NAM-7 verified process death on device for `rememberSaveable`. The `SavedStateHandle` path still needs a device check (AC9).
- DI (`di/StorageModule.kt`) provides `GroupRepository`, `NameRepository`, `PreferencesRepository`, `ActiveGroup` and `GroupEditor`. It does **not** provide `NameEditor`, `SortOrder`, `NameDraw` or a `NumberGenerator`.
- Resources:
  - Drawables are only `ic_arrow_back` and the launcher vectors. There is no hat illustration and no sort/reset/more/check/ticket icons. No material-icons dependency (NAM-8 plan: port the mock-up SVGs as vectors).
  - Strings: only `draw_back` and the legacy `selection_draw_button_label` exist (`res/values/strings.xml:15,55`).
- Theme (`ui/theme/`): the display face and the Top Hat fallback are in place from NAM-7. Check that `MotionScheme.expressive()` is set in `Theme.kt` during planning.
- Tests: `:app` uses plain JVM tests (`kotlin-test-junit`, coroutines-test, `unitTests.isReturnDefaultValues`). There are no Compose UI-test or Robolectric dependencies in `:app`. Fakes: `FakeGroups` and `FakePreferences`. There is no `FakeNames`, and shared `FakeNameRepository` is commonTest-only, so `:app` can't see it.

### Parallel work (same files)
- **NAM-8** (`flow/groups-screen`, main checkout, plan written, `docs/tasks/2026-11-14-groups-screen/plan.md`) adds:
  - `GroupSummary.drawnCount`;
  - a Compose `ui/components/HatIllustration` and `ui/components/UpButton` (moved), plus the `ic_add`, `ic_more_vert` and `ic_app_badge` vectors (corrected after the plan critic: NAM-8's revised plan has no `ic_hat`);
  - `action_more`, `action_undo` and other strings;
  - a move of `ListStatus` to `ui/ListStatus.kt`, and possibly an app-scope DI module;
  - edits to `AppNavigation.kt`, `Placeholders.kt` and `PlaceholderViewModels.kt`.
- **NAM-9** (`flow/edit-names-screen`, worktree `android-names-in-a-hat-nam-9`, research only so far) plans to add:
  - a `FakeNames` in `:app` tests;
  - a `NameEditor` provider;
  - edits to `Placeholders.kt` and `PlaceholderViewModels.kt`.
- Conflict points with NAM-10: `HatIllustration`, `UpButton` and `ic_more_vert`, `action_more`, `FakeNames`, the `NameEditor` provider, the `ListStatus` location, and the Draw parts of `Placeholders.kt`/`PlaceholderViewModels.kt`/`AppNavigation.kt`.

## Constraints & prior art
- The 2026-11-12 ticket comment rewrites AC1 (column clause dropped), AC3 (counter wording), AC4 (tri-state), AC5 (cycle back to Unsorted), AC6 (stepper) and AC8 (min 1, so "says why" becomes the bar message "Tap names to put them in the hat").
- Key everything by name id, not position (ticket rabbit hole).
- The draw result goes in saved state. The draw is computed before any animation. Production uses `RandomNumberGenerator()`; tests use a seeded `Random`.
- `MaterialShapes` and `LoadingIndicator` are not used (experimental); the hat is a vector animation (`3.0-spec.md:486-487`).
- The reveal covers the whole window, both panes, from inside a detail-pane entry. That needs a full-window layer above `NavDisplay` (for example a `Dialog` with `usePlatformDefaultWidth = false` and edge-to-edge) rather than in-pane content. To be settled at planning.
- Build gotchas (memory): `--no-watch-fs`; no commas in KMP test names; Robolectric sdk=34; `:app:clean` after deleting res dirs.
- Device rules (memory): never debug-install or `pm clear` on `api36_niah` (2.7.0 data); use `niah_debug_api36` and the headless AVDs.
  - AC1 needs a 2.7.0-migrated list. NAM-5/NAM-6 already verified the migration path; `api36_niah` is read-only for us.

## Open questions
1. **NAM-10 / NAM-12 boundary (shape-changing).**
   - Does NAM-10's draw mark names drawn? NAM-12 is "blocked by NAM-10" and owns marking, the remaining pool, Reset, the Drawn chip and Put back.
   - The NAM-10 comment, though, asks for the "· n drawn" counter, a tri-state over not-drawn names, and "Draw n more" / "Everyone's been drawn" in the reveal.
2. **Parallel NAM-8/NAM-9 overlap (shape-changing for sequencing).** Build in parallel and rebase, or wait for NAM-8 to land first?
3. **Recorded as defaults; not asked:**
   - **Lists → Draw title morph** (Motion table): whichever of NAM-8 or NAM-10 lands second wires the shared bounds. Default: NAM-10 adds the Draw side's `sharedBounds` key if NAM-8 has landed; otherwise it's noted as a hand-off.
   - **"Draw n" stepper value across process death:** a `SavedStateHandle` key per list.
   - **The reveal's predictive-back preview:** use `PredictiveBackHandler` on the reveal layer and scale or fade the takeover.
   - **Esc:** `onPreviewKeyEvent` on the reveal.
   - **Hat illustration:** reuse NAM-8's `ui/components/HatIllustration` and add colour parameters (see plan.md).
