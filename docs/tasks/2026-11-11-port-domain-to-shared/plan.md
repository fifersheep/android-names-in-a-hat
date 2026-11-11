# Plan — Port the domain logic and its tests into `:shared`

## Intent
Move the shipped app's rules (draw, draw state, select/clear all, sort cycle, grid toggle, active group) out of EventBus subscribers on `master` into plain Kotlin in `:shared` `commonMain`. Add the 3.0 models, the repository interfaces that NAM-5 (Room) and NAM-6 (DataStore) will implement, and thin use cases that enforce label validation (GitHub #5/#6). Port every meaningful legacy test to `kotlin.test` with hand-written fakes.

## Acceptance criteria
- [ ] AC1: `commonMain` contains every rule in the ticket table, with no Android, EventBus, Dagger or `java.*` imports. Check: `grep -rnE 'import (android|org\.greenrobot|dagger|javax|java\.)' shared/src/commonMain` returns nothing.
- [ ] AC2: every non-analytics legacy test case in the **Test mapping** table below is either ported to the named `commonTest` test or dropped for the stated reason, and `./gradlew :shared:allTests` passes (jvm, android host and iosSimulatorArm64). Check: Gradle output, plus a reviewer walk of the table against the test names.
- [ ] AC3: `NameDraw` with `RandomNumberGenerator(Random(seed))` gives the same result on every run. Check: test `seeded draws are repeatable`.
- [ ] AC4: drawing more names than are available returns every pool element exactly once and does not throw; a count of 0 or a negative count returns an empty list. Check: tests `draw clamps to the pool`, `draw of zero is empty` and `draw of a negative count is empty`.
- [ ] AC5: group labels and name texts that are empty, or only spaces, tabs, `\n` or `\r` (GitHub #5/#6: "Spaces, tabs, and char return"), are rejected and nothing is written; `"  Ada  "` is stored as `"Ada"`. This holds for create group, rename group, add name and rename name. Check: `LabelsTest` covers each whitespace kind, plus `GroupEditorTest`/`NameEditorTest` reject-and-trim tests.
- [ ] AC6: `NameSortType` constants are exactly `NONE`, `ASC`, `DESC`; `fromStored` maps unknown and null values to `NONE`. Check: test `sort type names are a stored format`.
- [ ] AC7: the iOS targets compile. Check: `./gradlew :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64`; CI `ios-compile` is green on the PR.
- [ ] AC8 (new, from NAM-12): `NameEditor.selectAll`/`clearAll` call only `setAllSelected` and never `markDrawn`/`clearDrawn`, and the `NameRepository.setAllSelected` KDoc states that it must not change `drawn`, which NAM-5's Room `UPDATE` must honour. Check: test `select all and clear all leave drawn untouched` (the fake records calls), and read the KDoc.
- [ ] AC9: the draw pool is the selected names only. Check: test `drawFrom draws only selected names`.

## Approach
All new code lives in package `uk.lobsterdoodle.namepicker.shared`, in subpackages `model`, `draw`, `selection`, `repository` and `usecase`. There is no DI framework; everything is constructor-injected so Hilt can wire it in `:app` later.

- **Models** (`model/`):
  - `Group(id: Long, label: String)`
  - `GroupSummary(group: Group, nameCount: Int)`, the overview row NAM-5 must supply
  - `Name(id: Long, groupId: Long, text: String, selected: Boolean, drawn: Boolean = false)`
  - `NameSortType { NONE, ASC, DESC }` with `next()` and `companion fun fromStored(value: String?)`
- **Pure rules** (no I/O):
  - `draw/NumberGenerator`: a `fun interface` with `nextInt(until: Int): Int`.
  - `draw/RandomNumberGenerator(random: Random = Random.Default)`.
  - `draw/NameDraw(generator).draw(pool: List<T>, count: Int): List<T>`: removes each pick before the next, clamps count to `pool.size`, and returns an empty list for count ≤ 0.
  - `draw/NameDraw.drawFrom(names: List<Name>, count): List<Name>`: draws from the selected names only. Legacy filtered the pool in the UI (`SelectionActivity.kt:95-99`); NAM-12 extends this pool to exclude drawn names.
  - `draw/DrawState.of(names: List<Name>)`, which returns:
    - `selectedCount`, `totalCount`
    - `options: List<Int>` (1..selected)
    - `allSelected` (total > 0 && selected == total)
    - `hasNames`
    - `canDraw` (selected ≥ `MIN_NAMES_TO_DRAW`)
  - `const val MIN_NAMES_TO_DRAW = 2`.
  - `selection/sortNames(names, type)`: stable `sortedWith`, ASC/DESC use `compareTo(other, ignoreCase = true)`, NONE keeps input order.
  - `selection/GridColumns.of(stored)` returns `(current, next)`. `current` is `stored` if it is 1 or 2, otherwise `DEFAULT_GRID_COLUMNS = 2`, so a corrupt stored value can't reach the UI. `next` toggles 1↔2.
- **Repository interfaces** (`repository/`): reads as `Flow`, writes as `suspend`, covering the NAM-5/NAM-6 lists in research.md. The KDoc on each method states the contract NAM-5/NAM-6 must honour:
  - `observeGroups` returns newest first.
  - `observeNames` returns names in insertion order.
  - `delete(group)` cascades to its names.
  - `setAllSelected` never changes `drawn`.
  - `updateSortType` is atomic.
  - `GroupRepository`: `observeGroups(): Flow<List<GroupSummary>>`, `observeGroup(id): Flow<Group?>`, `create(label): Long`, `rename(id, label)`, `delete(id): Group?`.
  - `NameRepository`: `observeNames(groupId): Flow<List<Name>>`, `add(groupId, text, selected): Long`, `rename(id, text)`, `delete(id): Name?`, `setSelected(id, selected)`, `setAllSelected(groupId, selected)`, `markDrawn(ids)`, `clearDrawn(groupId)`.
  - `PreferencesRepository`: `activeGroupId: Flow<Long?>`, `setActiveGroup(id)`, `clearActiveGroup()`, `gridColumns: Flow<Int>`, `setGridColumns(n)`, `sortType(groupId): Flow<NameSortType>`, `updateSortType(groupId, transform: (NameSortType) -> NameSortType): NameSortType` (atomic read-modify-write; DataStore's `edit {}` gives this), `removeSortType(groupId)`.
- **Validation** (`usecase/Labels.kt`): `validateLabel(raw): String?` returns the trimmed value, or null if blank. `sealed interface WriteResult<out T>` with `Saved(value)` and `data object Blank`.
- **Thin use cases** (`usecase/`):
  - `GroupEditor(groups, prefs)`:
    - `create(raw): WriteResult<Long>`
    - `rename(id, raw): WriteResult<Unit>`
    - `delete(id): Group?`. Only when a group was actually deleted (non-null), it also calls `prefs.removeSortType(id)` (NAM-6 AC4's hook) and clears the active group if it pointed at this group, so no id is left dangling.
  - `NameEditor(names)`:
    - `add(groupId, raw): WriteResult<Long>`, created selected per legacy `ClassroomDbHelper.kt:105`
    - `rename(id, raw): WriteResult<Unit>`, `delete(id): Name?`, `setSelected(id, b)`
    - `selectAll(groupId)` and `clearAll(groupId)`, both via `setAllSelected`
  - `SortOrder(prefs)`: `observe(groupId)`, and `cycle(groupId): NameSortType`, which calls `prefs.updateSortType(groupId) { it.next() }` so two quick taps can't both read NONE.
  - `GridLayout(prefs)`: `observe(): Flow<GridColumns>` and `select(count)`, which stores it.
  - `ActiveGroup(prefs)`: `open(id)`, `close()`, `observe(): Flow<Long?>`.
- **Tests** use hand-written in-memory fakes in `commonTest` (`FakeGroupRepository`, `FakeNameRepository`, `FakePreferencesRepository`) backed by `MutableStateFlow`, plus `runTest`. The fakes also record calls, so the tests can assert which repository methods a use case invoked.
- **Dependencies:** `kotlinx-coroutines-core` 1.11.0 as **`api`** in commonMain (the public interfaces expose `Flow`), and `kotlinx-coroutines-test` 1.11.0 in commonTest. This bumps `:app`'s transitive coroutines from 1.10.x to 1.11.0, which `:app:assembleDebug` covers.

Alternatives considered:
- Bare interfaces plus validators, with ViewModels enforcing them: rejected by the user, because validation would be scattered.
- A failure result for over-draw: rejected by the user in favour of clamping.
- Use `kotlin.random.Random` directly as the injectable: the ticket asks to keep the `NumberGenerator` seam, and fakes for exact indices are simpler with it.
- Turbine for Flow tests: `first()` is enough for these tests.

## Assumptions & open questions
- [answered 2026-11-11] Thin use cases over the repositories hold the write rules; the `StorageUseCaseTest` cases port against fakes. (decision log)
- [answered 2026-11-11] Over-draw clamps to `min(count, available)`. (decision log)
- [assumed] The domain returns numbers and state, not the legacy English strings ("2 out of 4 names selected", "Select All"/"Clear All"). NAM-10/NAM-12 need plurals resources and design-spec wording (NAM-12 notes). The legacy cases assert the same numbers and flags instead.
- [assumed] 0 selected gives empty `options`. Legacy gave `["0"]` (`SelectionUseCase.kt:33`), which isn't a valid draw count, and draw is disabled at 0 anyway.
- [assumed] `allSelected` is false for an empty list. Legacy read "Clear All" for an empty group (`:35`, `0 == 0`), which was a quirk. `hasNames` lets the UI show the empty state instead.
- [assumed] `MIN_NAMES_TO_DRAW = 2` (the legacy value). NAM-2/NAM-12 own changing it; it is one constant.
- [assumed] `NameSortType.fromStored` lives here (unknown or null → `NONE`). That is NAM-6's AC3 rule, but the enum is this ticket's, and it stops a repeat of legacy's `valueOf` crash (`SelectionNamesUseCase.kt:45`).
- [assumed] AC2 is read as "ported or dropped for a stated reason". The ticket itself drops the bus, `AdapterDataWrapper`, the overview hint and analytics, so a literal reading is impossible. The drop reasons are limited to: bus registration, analytics, AdapterDataWrapper, overview hint, and list-holding/read pass-through (replaced by repository `Flow`s, which NAM-5 tests).
- [assumed] `AppInfo` and its test stay: `:app` uses it (`MainActivity.kt:20,45`), and removing it is NAM-7's call.
- [assumed] Model field names are `Group.label` and `Name.text`. That avoids `Name.name`, and "group label" is the ticket's wording.
- [assumed] Repository method names and signatures are the contract for NAM-5/NAM-6. Those tickets may add methods (e.g. ordering hints) but shouldn't need to change these.
- [assumed] The legacy draw result's `"\n"`-joined string and `multiple` flag (`SelectionUseCase.kt:27`, asserted in the legacy single/multiple draw tests) are dropped. `draw` returns a `List`, so joining and `size > 1` are the caller's concern. Disclosed as a behaviour drop.
- [assumed] `GridColumns.of` maps any stored value other than 1 or 2 to the default 2. Legacy only defined the next step (anything except 1 → 1, `SelectionGridUseCase.kt:29-32`) and showed the corrupt value as is.
- [assumed] Deleting the active group clears the active group. Legacy hid the dangling id by clearing it on every overview resume (`OverviewActivity.kt:82`); 3.0 may restore the last-open group, so the delete handles it at the source.
- [assumed] Sort stability on Native is proven by the local macOS `:shared:allTests` (`iosSimulatorArm64Test`) via a tie test (`"ada"`/`"Ada"` under ASC). CI runs commonTest on the JVM only (`android.yml:48`; `ios-compile` compiles main only). A follow-up to run Native tests in CI is a report item, not this ticket.
- [open — NAM-12] The remaining-pool draw rule and the transactional "mark drawn and show result" step; this ticket only provides `markDrawn`/`clearDrawn` and the `drawn` flag.

## Tasks
1. Add coroutines to `:shared`. Files: `gradle/libs.versions.toml`, `shared/build.gradle.kts`. Verify: `./gradlew :shared:allTests :shared:compileKotlinIosArm64`.
2. Models, sort type and validation, test-first. Files: `shared/src/commonMain/.../shared/model/{Group,GroupSummary,Name,NameSortType}.kt`, `usecase/Labels.kt`; tests `model/NameSortTypeTest.kt`, `usecase/LabelsTest.kt`. Verify: `./gradlew :shared:jvmTest`.
3. Draw rules, test-first: port the `SelectionUseCaseTest` cases, plus clamp, zero and seeded. Files: `draw/{NumberGenerator,RandomNumberGenerator,NameDraw,DrawState}.kt`; tests `draw/NameDrawTest.kt`, `draw/DrawStateTest.kt`. Verify: `./gradlew :shared:jvmTest`.
4. Sorting and grid pure rules, test-first. Files: `selection/{SortNames,GridColumns}.kt`; tests `selection/SortNamesTest.kt`, `selection/GridColumnsTest.kt`. Verify: `./gradlew :shared:jvmTest`.
5. Repository interfaces and test fakes. Files: `repository/{GroupRepository,NameRepository,PreferencesRepository}.kt`; `commonTest/.../fakes/{FakeGroupRepository,FakeNameRepository,FakePreferencesRepository}.kt`. Verify: compiles (`./gradlew :shared:compileTestKotlinJvm`).
6. Use cases, test-first: port the StorageUseCase, SelectionAdapterDataWrapper (non-plumbing), SelectionNamesUseCase/SelectionGridUseCase persist, and ActiveGroupUseCase cases, plus the AC5 and AC8 tests. Files: `usecase/{GroupEditor,NameEditor,SortOrder,GridLayout,ActiveGroup}.kt`; tests `usecase/{GroupEditorTest,NameEditorTest,SortOrderTest,GridLayoutTest,ActiveGroupTest}.kt`. Verify: `./gradlew :shared:allTests`.
7. Final checks. Files: none (fix-ups only). Verify: the AC1 grep is empty; `./gradlew :app:assembleDebug :shared:allTests :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64`; `grep -rF` every mapped test name from the table under `shared/src/commonTest` and confirm each one hits.

## Test mapping (AC2)
Legacy root is `core/src/test/java/uk/lobsterdoodle/namepicker/` on `origin/master`; the non-analytics test files hold 67 cases (including the two analytics-event cases dropped below). New tests are under `shared/src/commonTest/kotlin/uk/lobsterdoodle/namepicker/shared/`.

**Each phrase after the class name is the literal backtick test function name.** Cells with " / " name several tests. Task 7 greps each name. Beyond the mapping, there are new tests for AC3–AC9 (seeded draw, clamp, zero/negative, whitespace kinds, sort-type format, drawn untouched, selected-only pool) plus `ASC keeps ties in input order`, `out-of-range stored columns read as 2` and `deleting the active group clears it`.

| Legacy case | → New test (or drop reason) |
|---|---|
| **SelectionUseCaseTest** registers_on_bus | drop: bus |
| …get_random_int_for_draw_count | `NameDrawTest` asks the generator once per name drawn |
| …NamesGeneratedEvent_with_single_name | `NameDrawTest` draw one returns the name at the generated index |
| …NamesGeneratedEvent_with_multiple_names | `NameDrawTest` each pick is removed before the next |
| …UpdateDrawActions_with_some_selected | `DrawStateTest` some selected |
| …UpdateDrawActions_with_all_selected | `DrawStateTest` all selected |
| …UpdateDrawActions_without_selected | `DrawStateTest` none selected (options now empty, see Assumptions) |
| …DisableDrawActions_when_1_or_fewer | `DrawStateTest` cannot draw below the minimum |
| …EnableDrawActions_when_2_or_more | `DrawStateTest` can draw at the minimum |
| **SelectionNamesUseCaseTest** registers_on_bus | drop: bus |
| …when_sort_type_is_none / asc / desc (3) | `SortNamesTest` NONE keeps order / ASC ignores case / DESC ignores case |
| …SortMenuItemSelected none→ASC, asc→DESC, desc→ASC write (3) | `SortOrderTest` cycle from NONE stores ASC / cycle from ASC stores DESC / cycle from DESC stores ASC |
| …SortMenuItemSelected_post_GroupNamesSortedEvent | `SortOrderTest` cycle returns the new order that sorts the names |
| **SelectionGridUseCaseTest** register_on_bus | drop: bus |
| …GridColumnSelected one → (1,2), two → (2,1) (2) | `GridColumnsTest` one column toggles to two / two toggles to one |
| …GridColumnSelected One/Two write (2) | `GridLayoutTest` selecting 1 stores 1 / selecting 2 stores 2 |
| …LoadSelectionGridPreference | `GridLayoutTest` observes the stored count with its toggle target |
| **SelectionAdapterDataWrapperTest** registers_on_create / unregister_on_pause / register_on_resume (3) | drop: bus |
| …store_data, …post_SelectionDataUpdatedEvent (on load) (2) | drop: list holding (the names `Flow` from `NameRepository` replaces the wrapper) |
| …NameSelectionCheckChanged_toggle_selection | `NameEditorTest` setSelected turns one name on and off |
| …NameSelectionCheckChanged_post_SelectionDataUpdated | drop: list holding |
| …NameSelectionCheckChanged_post_NameStateChanged | `NameEditorTest` setSelected persists only that name |
| …SelectAll_select_all, …post_SelectionDataUpdated (2) | `NameEditorTest` selectAll selects every name in the group |
| …SelectAll_post_SelectAllEvent | drop: analytics |
| …SelectAll_post_MassNameStateChanged | `NameEditorTest` selectAll writes in bulk for the group only |
| …ClearAll_clear_all, …post_SelectionDataUpdated (2) | `NameEditorTest` clearAll deselects every name in the group |
| …ClearAll_post_ClearAllEvent | drop: analytics |
| …ClearAll_post_MassNameStateChanged | `NameEditorTest` clearAll writes in bulk for the group only |
| …EnableSelectionEmptyState / DisableSelectionEmptyState (2) | `DrawStateTest` hasNames false when empty / hasNames true with names |
| **ActiveGroupUseCaseTest** register_to_bus | drop: bus |
| …SetActiveGroup / ClearActiveGroup / NotActive / Active (4) | `ActiveGroupTest` open stores the id / close clears it / observe is null when none / observe returns the id |
| **StorageUseCaseTest** registers_on_bus | drop: bus |
| …CreateGroupDetails add + post success (2) | `GroupEditorTest` create stores the trimmed label and returns its id |
| …AddNameToGroup save | `NameEditorTest` add stores the trimmed text as selected |
| …AddNameToGroup post GroupNamesRetrieved | drop: read pass-through (the names `Flow` re-emits) |
| …AddNameToGroup post NameAddedSuccessfully | `NameEditorTest` add returns Saved with the new id |
| …OverviewBecameVisible post OverviewRetrieved | drop: read pass-through (`GroupSummary.nameCount` is supplied by NAM-5's query and tested there) |
| …RetrieveGroupNames post GroupNamesRetrieved | drop: read pass-through |
| …DeleteName remove + post label | `NameEditorTest` delete returns the deleted name |
| …DeleteGroup remove + post label | `GroupEditorTest` delete returns the deleted group and removes its sort order |
| …EditGroupDetails edit + post | `GroupEditorTest` rename stores the trimmed label |
| …RetrieveGroupDetails post details | drop: read pass-through (`observeGroup`) |
| …NameStateChanged update | covered by `NameEditorTest` setSelected persists only that name |
| …MassNameStateChanged toggle | covered by `NameEditorTest` selectAll writes in bulk for the group only |
| **AdapterDataWrapperTest** (5) | drop: AdapterDataWrapper not ported (ticket) |
| **OverviewHintUseCaseTest** (2) | drop: overview hint has no domain code (ticket) |

## Out of scope
- Room and DataStore implementations (NAM-5, NAM-6), ViewModels and UI state (screen tickets), the remaining-pool rule (NAM-12).
- Removing `AppInfo`, and any `:app` change.

## Verification
- Machine: `./gradlew :app:assembleDebug :shared:allTests` (repo verify; on macOS this includes `iosSimulatorArm64Test`), `./gradlew :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64`, and the AC1 grep. On the PR, CI must show `unit-tests` and `ios-compile` green.
- Outcome: there is no UI in this ticket. The outcome is the test reports themselves: open `shared/build/reports/tests/jvmTest/index.html` (and the iosSimulatorArm64 report) and confirm the test classes and counts match the mapping table. Also check that `:app` still builds against `:shared`.
