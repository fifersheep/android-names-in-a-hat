# Research — Port the domain logic and its tests into `:shared` (NAM-4)

Legacy source: `origin/master` (f9773d5). Paths below are relative to `core/src/main/kotlin/uk/lobsterdoodle/namepicker/` (main) and `core/src/test/java/uk/lobsterdoodle/namepicker/` (test). **Several of the ticket's line citations are off by a few lines;** the corrected ones are below.

## Findings

### Current `:shared` state
- `commonMain` holds only `shared/AppInfo.kt`, a placeholder whose comment says "until the domain port (NAM-4)". `:app` uses it at `app/src/main/kotlin/uk/lobsterdoodle/namepicker/MainActivity.kt:20,45`. `commonTest` holds only `AppInfoTest.kt`.
- Targets are `android` (KMP library, `withHostTest`), `jvm`, `iosArm64` and `iosSimulatorArm64` (`shared/build.gradle.kts:9-17`). `commonTest` depends on `kotlin.test` only (`:21-23`).
- There is no coroutines dependency. Latest on Maven Central: `kotlinx-coroutines-core`/`-test` 1.11.0. Turbine 1.2.1 is available but optional.
- The package convention is `uk.lobsterdoodle.namepicker.shared`.
- CI (NAM-3) runs `:shared:jvmTest` on Linux and compiles iOS on macOS. Locally on macOS, `:shared:allTests` also runs the `iosSimulatorArm64` tests.

### Rules to port

| Rule | Legacy (corrected lines) | Behaviour |
|---|---|---|
| Draw N without replacement | `selection/SelectionUseCase.kt:19-28` | For `drawCount` (a String, `parseInt`) iterations: `generator.randomInteger(available.size)`, then `removeAt`. The result is joined with `"\n"`, plus a `multiple = size > 1` flag. Asking for more than are available crashes (`removeAt` on an empty list, or `nextInt(0)`). |
| Random source | `selection/NumberGenerator.kt:3-5`, `RandomNumberGenerator.kt:5-8` | `randomInteger(from): Int` in `[0, from)`, backed by `java.util.Random().nextInt`. |
| Draw options, counter, toggle | `SelectionUseCase.kt:30-39` | `checked = count(toggledOn)`. Options are `min(1, checked)..checked`, so **0 selected gives `["0"]`**. Counter is `"$checked out of $total names selected"`. Label is `"Clear All"` iff `checked == total` (so **an empty list reads "Clear All"**), else `"Select All"`. Draw is enabled iff `checked >= 2`. |
| Select all / clear all | `selection/SelectionAdapterDataWrapper.kt:45-60` | Sets every name's `toggledOn` to true or false and persists in bulk (`MassNameStateChangedEvent` → `db.toggleAllNamesInGroup`). Also posts analytics `SelectAllEvent(previouslyChecked, total)` and `ClearAllEvent(total)`; analytics is not ported. |
| Single toggle | `SelectionAdapterDataWrapper.kt:37-43` | `copyWith(isChecked)`, then persist one name (`db.updateName`). |
| Empty state | `SelectionAdapterDataWrapper.kt:27-35` | Enabled iff the names list is empty. This is UI, but it's a legacy tested case. |
| Sort cycle | `selection/SelectionNamesUseCase.kt:15-23,27-46` | NONE→ASC→DESC→ASC. ASC/DESC compare with `compareTo(other, ignoreCase = true)`. NONE keeps input order (`Collections.sort` with a comparator that always returns 0, so it's stable). Stored as `nextSortType.toString()` under `group_sort_type_for_group_<id>` (`:29,36,39`). Read via `valueOf`, which **throws on an unknown value** (`:45`). |
| Grid columns | `selection/SelectionGridUseCase.kt:17-32` | `next = option + 1`, or 1 if `next ∉ {1, 2}`, so 1→2 and 2→1. Stored as a **Long** under `current_grid_column_count`, default 2 (`:20,25,35`). |
| Active group | `storage/ActiveGroupUseCase.kt:16-36` | Set puts a Long under `active_group_id`, clear removes the key, check uses `contains()` (absence means none). |
| Storage pass-through | `storage/StorageUseCase.kt:21-83` over `storage/DbHelper.kt:7-18` | List groups → overview cells `(id, name, names.size)`; create group (returns id); add name, then re-read the group's names; delete a name or group (returns what was deleted, for a "deleted X" message); rename group; get group details; update one name; toggle all in a group. **No validation anywhere.** |
| Overview hint | `overview/OverviewHintUseCase.kt:12-15` | Shows a hint iff there are no groups. The ticket says this needs no domain code. |

Models: `Name(id: Long, name: String, toggledOn: Boolean)` (`model/Name.kt:3`), `GroupDetails(id: Long, name: String)`, `Group(details, names)`. `NameSortType { NONE, ASC, DESC }` (`selection/NameSortType.kt:3-5`).

### Legacy tests (non-analytics) → port disposition

| Test file | Cases | Disposition |
|---|---|---|
| `selection/SelectionUseCaseTest.kt` | 9: 1 bus-register, 3 draw, 3 draw-actions, 2 enable/disable | 8 port to return values; drop bus-register |
| `selection/SelectionNamesUseCaseTest.kt` | 8: 1 bus, 3 sort-by-stored-type, 3 cycle+persist, 1 cycle+sort | 7 port (persist via fake prefs repo); drop bus |
| `selection/SelectionGridUseCaseTest.kt` | 6: 1 bus, 2 next-option, 2 persist, 1 load-default | 5 port; drop bus |
| `selection/SelectionAdapterDataWrapperTest.kt` | 18: 3 register/unregister, 15 data/select/clear/empty-state | Select-all/clear-all/toggle/bulk-persist/empty-state cases port. Register cases dropped. The `SelectAllEvent`/`ClearAllEvent` cases are analytics, so dropped |
| `storage/ActiveGroupUseCaseTest.kt` | 5: 1 bus, 4 set/clear/check | 4 port via fake prefs repo; drop bus |
| `storage/StorageUseCaseTest.kt` | 14: 1 bus, 13 pass-through | Depends on the design question below |
| `adapter/AdapterDataWrapperTest.kt` | 5 | Dropped: `AdapterDataWrapper` is "not ported" per the ticket spec |
| `overview/OverviewHintUseCaseTest.kt` | 2 | Dropped: "no domain code is needed" per the ticket; becomes a groups-screen empty state |
| `Testing.kt` | Mockito helpers | Dropped |

So AC2's "every non-analytics legacy test case has an equivalent" can't be literal. The ticket itself removes the bus, `AdapterDataWrapper` and the overview hint. plan.md needs a case-by-case mapping table so AC2 can be checked.

### What the downstream tickets need from NAM-4's interfaces
- **NAM-5 (Room):** the repository must cover list groups *with name counts*; create, rename and delete a group; list, add, rename and delete names; set one name's selected flag; set all names' selected flag in a group; mark names drawn; clear drawn for a group. Groups come back newest first and names in insertion order. **New names are created selected** (legacy `ClassroomDbHelper.kt:105`). IDs are Long.
- **NAM-6 (DataStore):** active group as a nullable Long, with set and clear. Columns default to 2. Sort per group: get, set, and remove on group delete. **Unknown stored sort reads as `NONE`**. The enum names are a stored format.
- **NAM-12 (draw from remaining):** the remaining-pool rule goes "next to the draw function" later. The minimum-to-draw constant must be a single named value (NAM-12 wants 1; legacy is 2). Selection and drawn are independent flags, so select all / clear all must not touch `drawn`.
- **NAM-10 (draw screen):** the counter text and labels need plurals/resources. The domain should return numbers and state, not English strings.

## Constraints & prior art
- `commonMain` has no `java.*`. `kotlin.random.Random(seed)` is multiplatform and deterministic per seed. `String.compareTo(other, ignoreCase = true)` and `String.CASE_INSENSITIVE_ORDER` are in common stdlib. `sortedWith` is documented as stable, which preserves the NONE semantics. Verify with the iOS compile and the `iosSimulatorArm64Test` run.
- Mockito is JVM-only, so write hand-written fakes. `runTest` from kotlinx-coroutines-test works in `commonTest`.
- No DI framework in `:shared` (memory, names-3-0-rebuild): use plain constructors, which Hilt wires in `:app` later.
- NAM-1's task folder (`docs/tasks/2026-11-11-unify-trunk-modern-build/`) is the prior art for flow artifacts on this repo.

## Open questions (triaged)
- **Shape-changing → asked 2026-11-11:**
  1. Use-case layer or bare interfaces? **Answer: thin use cases** over the repository interfaces, enforcing validation. The `StorageUseCaseTest` cases are ported against fake repos.
  2. Draw more names than are available? **Answer: clamp** to `min(count, available)`; never throws.
- **Conventional default → record as assumption:**
  - The domain returns numbers and state (counts, an `allSelected` flag, `Int` options), not the legacy English strings.
  - The random source stays an injectable `NumberGenerator`-style interface, with a default backed by `kotlin.random.Random` (seedable for AC3).
  - 0 selected gives empty draw options (legacy gave `["0"]`).
  - Validation lives in one place and returns a trimmed value or a rejection.
  - `NameSortType` gets a lenient parse (`fromStored`) that maps unknown values to `NONE`, ready for NAM-6.
  - Coroutines 1.11.0 is added to `commonMain`/`commonTest`, with no Turbine.
  - `AppInfo` stays (`:app` uses it) or is removed only if `:app` stops using it.
- **Code-resolvable:** none outstanding.
- **Deferred:** the minimum-to-draw value (NAM-2/NAM-12 own it; it's a constant here at 2); the remaining-pool rule (NAM-12).
