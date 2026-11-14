# NAM-9 research: Build the edit names screen

Worktree `../android-names-in-a-hat-nam-9`, branch `flow/edit-names-screen` from `main@ff34628`.

## Findings

### What the spec settles (supersedes the ticket where they differ)
- **The screen is shared.** One `EditList(listId?)` screen creates a list, renames it and edits its names (D3). Ownership is split: "Edit list = NAM-8 (the list-name header) **and** NAM-9 (the names section)" (`docs/design/3.0-spec.md:87`). The NAM-9 ticket comment says the same: "Merged with list creation (see NAM-8)".
- **Names section layout** (`3.0-spec.md:131-143`):
  - Header "N names" (plurals `edit_names_count`, labelSmall).
  - Lazy list in insertion order, keyed by name id. Each row has the name (bodyLarge) and a trailing delete icon button ("Delete Ada").
  - Tapping a name renames it inline. The row turns into a field with ✓ (`edit_rename_save`) and ✕ (`edit_rename_cancel`). IME Done saves. Blank shows "Enter a name" and keeps the old name.
  - The add field is docked at the bottom above the keyboard: label "Add a name", placeholder "e.g. Ada", a trailing Add icon button, IME action Send with label "Add". Adding clears the field and keeps focus and the keyboard. The list scrolls to show the new name. Until the list exists the field is **disabled** with the supporting text "Name the list first".
  - Mock-up: `docs/design/3.0-mockups/project/EditScreen.dc.html`. The add bar is on `surfaceContainer` with a 48 dp filled primary circular Add button. Rows are at least 56 dp with an `outlineVariant` divider. The snackbar sits above the add bar.
- **Delete is a delayed delete** (D12, `3.0-spec.md:309-318`). The row is hidden immediately and a snackbar "Ada deleted" offers **Undo**. The real delete commits when the snackbar times out or is dismissed, or when the screen leaves composition. Re-inserting is ruled out because it would lose the id, position and drawn state. "The NAM-8 and NAM-9 ViewModels own the pending-delete state."
- **Validation wording** is "Enter a name" (D15, `3.0-spec.md:303`), not the ticket's "Names cannot be blank". The ticket comment confirms the change. Nothing is saved, focus and the keyboard stay, and typing clears the error.
- **Hand-off NAM-9.H1** (`3.0-spec.md:143,518`): replace inner line breaks and tabs with single spaces **before** `validateLabel`, which only trims the ends (`shared/.../usecase/Labels.kt:9`).
- **Duplicates** are allowed with no warning (D14). Bulk paste is out of scope.
- **Landscape** (`3.0-spec.md:145-148`): small top app bar, the names list fills the space, and the add field stays docked.
- **Large screens:** Edit list sits in the detail pane, and back returns to Draw (`3.0-spec.md:279`). The shell already handles this.
- **AC6 target:** Draw's empty state shows "This hat is empty" and an **Add names** button that goes to `EditList(id)` (`3.0-spec.md:298`). That screen belongs to NAM-10, so today only `DrawPlaceholder` exists.
- **Copy:** `edit_names_count`, `edit_add_name`, `edit_add_name_hint`, `edit_add_name_action`, `edit_name_blank`, `edit_name_first`, `edit_delete_name`, `edit_rename_name`, `edit_rename_save`, `edit_rename_cancel`, `name_deleted`, `action_undo`, `draw_empty_*` (`3.0-spec.md:589-604`). Only the `edit_title*`, `edit_done` and `edit_list_name` strings exist so far (`app/src/main/res/values/strings.xml:16-19`).

### The shell (NAM-7) that this builds on
- `EditList(listId: Long?, isNew, session)` (`app/.../navigation/AppKey.kt:30-31`). The content key is per `session` (`AppNavigation.kt:143`), so when `EditList(null)` becomes `EditList(id)` the entry, its ViewModel and its saveable state are kept.
- Back-stack ops already exist: `newList`, `editList(id, fromDraw)`, `listCreated`, `doneEditing`, `listGone` (`navigation/BackStack.kt:30-90`). Draw → Edit list already pushes through `DrawPlaceholder.onEdit` (`AppNavigation.kt:136`).
- `EditListPlaceholder` (`ui/placeholder/Placeholders.kt:110-155`) has a Scaffold, a top bar with Up and Done, the list-name field and a Create button. `EditListViewModel` (`ui/placeholder/PlaceholderViewModels.kt:85-125`) is assisted-injected with `listId`, owns `id` as a `MutableStateFlow` and `status`, and has `create()`. The NAM-7 comment says "The screen tickets (NAM-8/9/10) grow these into the real screens' ViewModels".
- The per-entry `rememberSaveableStateHolderNavEntryDecorator` keeps `rememberSaveable` text across process death (`AppNavigation.kt:114-117`). The placeholder relies on it for the typed list name (`Placeholders.kt:122-123`). This is the mechanism for AC8.
- DI: `StorageModule` provides `GroupRepository`, `NameRepository` and `GroupEditor`, but **not `NameEditor`** (`di/StorageModule.kt:44-58`).

### `:shared` APIs (NAM-4/5)
- `NameEditor(names)` (`shared/.../usecase/NameEditor.kt`):
  - `add(groupId, raw)` validates, then inserts with `selected = true` (AC5 is already met here; don't set it in the UI).
  - `rename(id, raw)` validates.
  - `delete(id)` returns the `Name`.
  - Results are `WriteResult.Saved` or `WriteResult.Blank`.
- `NameRepository.observeNames(groupId): Flow<List<Name>>` returns names in insertion order (`NameDao.kt:12` `ORDER BY id ASC`), which covers AC1.
- `Name(id, groupId, text, selected, drawn)` (`model/Name.kt`).
- Test fakes: `shared/src/commonTest/.../fakes/FakeNameRepository.kt` is commonTest-only, so `:app` can't see it. `:app` has its own `FakeGroups.kt` and `FakePreferences.kt` (`app/src/test/kotlin/uk/lobsterdoodle/namepicker/`), so a `FakeNames` would go there.

### Tests and conventions in `:app`
- JVM unit tests use `kotlin.test`, `UnconfinedTestDispatcher` with `Dispatchers.setMain`, and calls the ViewModel constructor directly with fakes (`app/src/test/.../ui/placeholder/EditListViewModelTest.kt`). Test names are backticked sentences; KMP test names can't contain commas (memory: build gotchas).
- Test deps: `kotlin-test-junit`, `junit`, `kotlinx-coroutines-test` (`app/build.gradle.kts:101-104`). There's no Turbine and no Compose UI test dependency in `:app`. Robolectric is in the catalog (`libs.versions.toml:16,58`) but only `:shared` uses it.
- `unitTests.isReturnDefaultValues = true` because ViewModels log through `android.util.Log` (`app/build.gradle.kts:65-67`).

## Constraints & prior art
- **A parallel session is building NAM-8** on `flow/groups-screen` in the main checkout. It is in research and has no research.md yet. Both tickets edit the Edit list screen (`Placeholders.kt` and `PlaceholderViewModels.kt`), so a merge conflict is likely whichever lands second.
- **Delayed delete outliving the ViewModel.** "Commit when the screen leaves composition" means the commit must survive `viewModelScope` being cancelled in `onCleared`. It needs an app-lifetime scope or `NonCancellable`. The NAM-8 Lists delete has the same need, so a small generic pending-delete helper in `:app` could serve both.
- **Process death with a pending delete:** the name was never deleted, so it reappears. This is acceptable because nothing is lost.
- **Classrooms.db must be opened only through the `NamesStorage` singleton** (memory: names-3-0-rebuild).
- **Device checks:** never debug-install on `api36_niah`. Use `niah_debug_api36` and a tablet AVD for AC9 (memory: names-device-verification).
- Legacy behaviour (ticket context): blank names were saved, deletes had no undo, and there was no rename. All three are replaced by the spec.

## Open questions → triage
| # | Question | Class | Resolution |
|---|---|---|---|
| 1 | Who builds the shared Edit list screen (scaffold, top bar, list-name header, create/rename commit, back-discard rule) while NAM-8 runs in parallel? | **shape-changing** | **Answered 2026-11-14: wait for NAM-8 to merge**, then build the names section on the real Edit list screen (spec split: NAM-9 = names section + add field) |
| 2 | AC6: add the empty-state **Add names** button to `DrawPlaceholder` now, or leave AC6 to NAM-10? | **shape-changing** (scope across tickets) | **Answered 2026-11-14: hand AC6 to NAM-10.** Draw is not touched; the `EditList(id)` route already exists (`BackStack.kt` `editList`) |
| 3 | Validation copy: "Enter a name" or the ticket's "Names cannot be blank"? | resolved | The spec (D15) and the ticket comment win: "Enter a name" |
| 4 | Where does newline/tab normalisation live? | conventional default | In `:shared`, as a pure function next to `validateLabel` (e.g. `NameEditor` normalises before validating, used by add and rename) with commonTest coverage. Not in the UI |
| 5 | A second delete while one is pending | conventional default | Commit the earlier one immediately; the snackbar shows the newest (Material: one snackbar at a time) |
| 6 | Pending delete across process death | conventional default | Not persisted; the name reappears (nothing lost) |
| 7 | Rename and add text across rotation and process death | code-resolved | `rememberSaveable` under the shell's saveable decorator, as the placeholder does |
| 8 | `NameEditor` binding | code-resolved | Add `@Provides NameEditor` to `StorageModule` |
| 9 | Compose UI tests for the screen | deferred | AC10 asks only for ViewModel tests. UI and AC7/AC9 are checked by device verification at review |
