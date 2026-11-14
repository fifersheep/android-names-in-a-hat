# Plan — Build the groups screen: list, create, rename, delete (NAM-8)

## Intent
Replace NAM-7's Lists and Edit list placeholders with the real 3.0 screens from the design spec.
- **Lists:** cards with name and drawn counts, a delayed delete with Undo (including large-screen close and reopen), the empty state, the FAB, and the About sheet on the overflow.
- **Edit list:** the whole screen (by user decision): top bar, Done, the list-name field with create and rename commit rules and inline validation, and an **empty names slot** that NAM-9 fills.

NAM-8 also runs the on-device install-over checks deferred from NAM-5 (AC7) and NAM-6 (AC6).

## Acceptance criteria
<!-- Ticket AC1–9, the spec items the ticket defers to, and the inherited checks. Phone = niah_debug_api36, tablet = niah_tablet_api36, 2.7.0 data = api36_niah (release build only, never pm clear). -->
- [ ] **AC1. Migrated data shows correctly** (also NAM-5 AC7).
  - Steps on api36_niah (2.7.0 data, `-no-snapshot-save`; a `google_apis` image, so `adb root` works):
    1. Check the installed version is 2.7.0.
    2. In 2.7.0, add a group, then delete **the newest** group. That's the only case where a non-AUTOINCREMENT table would reuse the id.
    3. Note the titles and counts, and record the max id with `adb root` + `sqlite3` (read-only).
    4. Install the local **release-signed** 3.0 build over 2.7.0 (`adb install -r`).
  - Expected:
    - Lists shows every group with the same title and count, newest first.
    - A list created in 3.0 gets an id above the deleted group's id. Read with `adb root` + `sqlite3 … 'SELECT id,label FROM name_groups'`, read-only.
  - Afterwards: `adb emu kill` discards the changes. Never `pm clear`.
- [ ] **AC1b. Settings carry over** (NAM-6 AC6).
  - Steps: in 2.7.0 on api36_niah, open a group and sort it Z–A, then install over it as in AC1.
  - Expected:
    - The app launches straight into that list (`[Lists, Draw(id)]`; Draw is still the placeholder showing the title).
    - With `adb root`, the DataStore file `files/datastore/settings.preferences_pb` holds `group_sort_type_for_group_<id>` with the Z–A value, and `active_group_id` = that id. Read-only, through `strings`/`xxd`.
- [ ] **AC2. Live updates.** Create, rename and delete each update Lists without leaving it:
  - phone: create → Done → back
  - tablet: Lists stays visible beside the detail

  On the tablet the change shows with no navigation.
- [ ] **AC3. Validation.**
  - In the ViewModel tests:
    - committing `""`, `"   "` or `"\t\n"` sets the "Enter a list name" error and writes nothing
    - committing `"  Team  "` stores `"Team"`
    - typing clears the error
  - Device: blank + Next shows the inline error, and focus stays.
- [ ] **AC4. Delayed delete (D12).**
  - In `ListsViewModelTest`:
    - delete hides the card at once and leaves the group in the repository
    - `undo(id)` restores it
    - `commit(id)` deletes: the group is gone, `removeSortType` and `clearActiveGroupIf` are called
    - `commit(id)`/`undo(id)` for an id that isn't the pending one do nothing
    - a second delete commits the first
    - `onCleared` commits (through `ViewModelStore` + `ViewModelProvider` + `store.clear()`)
    - a pending delete survives ViewModel re-creation from `SavedStateHandle`
  - Device, phone:
    - Delete → the card collapses, "Year 5 Maple deleted · Undo" → Undo brings it back with its names.
    - Delete again → wait about 10 s (`SnackbarDuration.Long`) → the snackbar goes and the delete commits: `run-as sqlite3` on the debug DB shows no `name_groups` or `names` rows for that id.
    - Delete → Home → the delete commits on stop: check with `sqlite3` while backgrounded.
    - Delete → rotate → the card stays hidden, and the snackbar is shown again with Undo.
  - Tablet:
    - Deleting the open list closes the detail to the "Pick a list to draw from" placeholder; Undo reopens it in the detail.
    - Deleting a list whose Edit list is open in the detail with unsaved text, then pressing Undo, keeps the **old** name: no rename on leave.
- [ ] **AC5. Empty state.** With no lists: the hat illustration, "No lists yet", the body line, and the FAB. On the tablet, the detail shows "Pick a list to draw from".
- [ ] **AC6. Plurals.**
  - "1 name", "2 names", "24 names · 4 drawn", "1 name · 1 drawn".
  - The drawn part is hidden when 0.
  - Checked with a Room `jvmTest` for `drawnCount` and on device.
- [ ] **AC7. Rotation and process death mid-edit.**
  - **Rotation:** type into Edit list (new and existing) and rotate; the text and any error are kept.
  - **Process death:** type into Edit list, press Home, `am kill`, confirm with `pidof` that it's empty, relaunch; the same entry and text are shown.
  - Also in the ViewModel tests, through a re-created `SavedStateHandle`.
- [ ] **AC8. Matches the spec, light and dark.** Screenshots compared against the canvas artboards `PhoneLists`, `LandPhone`, `LargeDraw`, `LargeEmpty`, `PhoneEdit` and `LargeEdit`:
  - phone portrait and landscape
  - tablet list-detail with the selected card in `secondaryContainer`
  - light and dark
  - the About sheet

  Pass means every element on this checklist is present, in the spec's order and style role:
  - **Lists:** large "Your lists" (DM Serif), overflow → About, cards with title + "N names[ · M drawn]" + 48 dp overflow, extended FAB "New list" bottom-end.
  - **Empty:** hat, "No lists yet", body, FAB.
  - **Landscape:** small bar, cards ≤ 640 dp centred.
  - **Tablet:** 360 dp Lists pane, selected fill, hat + "Pick a list to draw from" placeholder.
  - **Edit:** Up, title, Done, outlined "List name" field with placeholder.
  - **About:** icon, name, version, "Made by Northtrack Studio", "Previously published by Lobster Doodle", privacy link.

  Pixel-exact match isn't required.
- [ ] **AC9. ViewModel unit tests** for create, rename, delete and validation pass: `ListsViewModelTest`, `EditListViewModelTest`.
- [ ] **AC10. Edit list rules** (spec Navigation 1–3 and Edit list):
  - Next on a new list creates it once, and the entry becomes `EditList(id, isNew)` with the same session.
  - Done then goes to Draw.
  - Back or Up on a new, uncommitted list creates nothing.
  - Back or Up on an existing list commits a non-blank change and discards a blank one, keeping the old name.
  - Done on an existing list with a blank name shows the error and stays.
  - Checked in the ViewModel tests and walked on the phone.
- [ ] **AC11. Accessibility.**
  - Every icon button has its copy-deck description: "Options for %s", "More options", "Back".
  - Touch targets are ≥ 48 dp.
  - TalkBack reaches open, the card overflow, Edit, Delete, Undo, the FAB and About (walked once on the phone with TalkBack on).
- [ ] **AC12. Copy.**
  - No hard-coded user-facing strings in the new code: `grep -nE '"[A-Za-z][^"]*"' ` over `ui/lists`, `ui/editlist` and `ui/components` turns up only keys, routes and log tags. This covers `Text(`, `contentDescription`, labels and placeholders.
  - The 2.7.0 strings this ticket supersedes are removed from `strings.xml`.
  - `:app:lintDebug` passes, as CI runs it (`.github/workflows/android.yml:36`).

## Approach
**Data (`:shared`).**
- Add `drawnCount: Int = 0` to `GroupSummary`. `GroupDao.observeSummaries` adds `COALESCE(SUM(n.drawn), 0) AS drawnCount`.
- The default keeps the existing call sites compiling.

**Delayed delete in `ListsViewModel`.**
- **Pending-delete state** is three primitive `SavedStateHandle` keys: `pendingId: Long`, `pendingLabel: String`, `pendingReopen: Boolean`. They are Bundle-safe with no parcelize, and survive rotation and process death.
- **Hidden ids.** `uiState` combines `observeGroups()` with them: the pending id, plus ids already committed in this ViewModel. Ids are never reused (AUTOINCREMENT), so the committed set needs no cleanup and the card can't flash back while Room's Flow catches up.
- **Shared registry.** A `@Singleton PendingDeletes` (`StateFlow<Set<Long>>`) mirrors the pending id. `EditListViewModel` reads it, and NAM-9 can reuse it for names.
- **Snackbar.** The UI shows it from `LaunchedEffect(pending.id)` with **`SnackbarDuration.Long`**, which is explicit: with an action label, material3 defaults to `Indefinite`, which never times out.
  - `ActionPerformed` → `undo(id)`
  - `Dismissed` (timeout) → `commit(id)`
  - Both are **no-ops unless `id` is still the pending id**, so a late continuation can't commit or undo a newer delete.
  - After rotation the effect re-shows the snackbar.
- **Other commit triggers:**
  - **Navigating away in single pane:** open, new and edit call `commit(pending)` first. In list-detail the Lists pane and its snackbar stay on screen, so Undo stays available.
  - **App backgrounded:** a `LifecycleStopEffect` on the Lists screen commits unless the activity `isChangingConfigurations`. This covers API 31+, where back from the root only moves the task to the back and never clears the ViewModel.
  - **`onCleared`** (the activity really finishing) commits on an injected app-lifetime `CoroutineScope`, because `viewModelScope` is already cancelled.
- `commit` calls `GroupEditor.delete`. That already drops the sort order and clears the active list if it matches.
- Rejected:
  - re-insert on Undo (spec forbids it: loses names, ids and prefs)
  - a `DisposableEffect` commit (it fires on rotation)

**Navigation stays in the UI layer.**
- `AppNavigation` passes to the Lists entry:
  - `openId`: the list shown in the detail, list-detail only, for the `secondaryContainer` highlight
  - `onHideOpen(id)`: `backStack.listGone(id)` + `shell.onListGone(id)`, which clears the active list at once
  - `onReopen(id)`: `backStack.openList(id)`
- `delete(id, wasOpen)` records `reopen`; Undo returns it, so the screen calls `onReopen`.

**Edit list.**
- The field text and the error live in `EditListViewModel` via `SavedStateHandle`, so they survive process death; the shell's saveable decorator is not needed for them.
- Commit points:
  - IME **Next** → `commitName()`: create, then `onCreated(id)` → `listCreated(id, session)`; or rename. Blank → error, focus stays.
  - **Done**:
    - new and created → `doneEditing()` (to Draw)
    - existing → rename if non-blank and changed, then pop; blank → error
  - **Leaving** (Up, system back or predictive back, `listGone`): `onCleared` renames a non-blank change of an existing list on the app scope. It skips the rename when the list's id is in `PendingDeletes`, so deleting a list whose edit is open, then Undo, keeps the old name. A new, uncommitted list writes nothing.
  - The rename lands once the popped entry is disposed, after the exit transition. Lists shows the old name for that moment, which is accepted.
- Because leaving is handled in `onCleared`, there's no `BackHandler`, and predictive back keeps the shell's default animation.
- `commitName()` is public, so NAM-9's add field can call it when focus moves to it (spec "focus moving to another field").
- The names area is an empty `EditListNamesSlot(listId)` composable for NAM-9 to fill.

**Motion.**
- `sharedBounds` from the FAB to the Edit list container (key `"new-list"`).
- `sharedBounds` from the card title to the Draw placeholder title (key `"list-title-$id"`), single pane only: in list-detail both are on screen together.
- `animateItem()` on the cards.

**Files.**
- `ui/lists/` holds `ListsScreen`, `ListsViewModel` and `AboutSheet`.
- `ui/editlist/` holds `EditListScreen` and `EditListViewModel`.
- `ListStatus` and the `status()` helper move to `ui/ListStatus.kt`, so `DrawPlaceholder` (NAM-10) and Edit list share them.
- `ui/components/` gets:
  - `UpButton`, moved from `Placeholders.kt:173` and made internal, keeping its list-detail hiding rule
  - `HatIllustration`, a Compose `ImageVector`/`Canvas` drawn in `colorScheme.primary` and `tertiary`. A resource vector can't take two Compose-scheme colours: `painterResource` tints with one filter, and `?attr` resolves against the platform theme, not the dynamic Compose scheme.
- `DrawPlaceholder` and `DrawViewModel` stay in `ui/placeholder/`. `PickAListPlaceholder` gains the hat (spec `:287`).

**Alternatives.**
- Commit the rename on every keystroke: rejected, the spec commits on Next or on leaving.
- A `BackHandler` to commit on back: rejected, it disables predictive back for the screen.

## Assumptions & open questions
- [answered 2026-11-14] NAM-8 builds the whole Edit list screen first, with a stub names area; NAM-9 waits for NAM-8 to merge — decision log.
- [answered 2026-11-14] NAM-8 builds the About sheet (D19) — decision log.
- [answered 2026-11-11] Install-over checks NAM-5 AC7 and NAM-6 AC6 run here — `room-migration-to-v3/report.md:71`, `datastore-preferences-migration/report.md:67`.
- [assumed] The install-over uses a local release-signed build: debug can't install over release-signed 2.7.0. Signing is configured (`app/build.gradle.kts:11-41`, `gradle.properties:12`, secrets in `~/.gradle/gradle.properties`), and `versionCode 2000000002` > 2.7.0's. The APK stays local and is not published.
- [assumed] One pending delete at a time; a second delete commits the first. This matches the M3 single-snackbar pattern.
- [assumed] The spec's "default snackbar duration" (`3.0-spec.md:311`) is read as **`SnackbarDuration.Long`**. material3 1.5's actual default for a snackbar with an action is `Indefinite`, which would never commit (critic, Blocking). `Short` (4 s) is too quick for an undo. Long is about 10 s, and the system stretches it further under accessibility timeouts.
- [assumed] A pending delete interrupted by process death is restored as **not deleted**, with the snackbar re-shown. This is the safe direction, and rare now that backgrounding commits.
- [assumed] "Leaves composition" (`3.0-spec.md:311`) is implemented as three triggers instead of composition disposal, which also fires on rotation:
  - navigate-away in single pane
  - app stop (not a config change)
  - `onCleared`
- [assumed] In list-detail, opening or editing another list **does not** commit a pending delete: the Lists pane and its snackbar stay on screen, so Undo stays valid.
- [assumed] Accepted risk: if Undo on the tablet comes before the old `Draw(X)` entry has finished leaving, the reused `DrawViewModel` has `opened = true`, so the active list stays cleared until X is next opened. It needs sub-transition timing (critic). Only launch-reopen is affected, and NAM-10 replaces `DrawViewModel`.
- [assumed] Focus moving to another field commits the name, but the field doesn't exist until NAM-9. NAM-8 exposes `commitName()`, and NAM-9 calls it from its add field's focus. Recorded for NAM-9 in the report.
- [assumed] On an existing list, Done with a blank name shows the error and stays. The spec defines blank-on-commit (`:137`) but not Done-while-blank.
- [assumed] Lists → Draw title morph: NAM-8 tags both sides, including the Draw placeholder title, single pane only. NAM-10 keeps the key on its real title.
- [assumed] The plus, more-vert and About app-badge icons are new vector drawables. The two-colour hat is a Compose `HatIllustration`, ported from the mock-up SVG (`ListsScreen.dc.html:43-50`). No material-icons dependency is added.
- [assumed] The version on the About sheet is read from `PackageManager` (`versionName`), with no `buildConfig` switch-on.
- [assumed] The 2.7.0 strings that the Lists and Edit-header copy supersede are deleted:
  - `action_settings` (`strings.xml:26`)
  - `title_activity_overview` and `title_activity_create_group` (`:28-29`)
  - `overview_*` (`:33-35`, `:46-49`)
  - `add_group_input_label_name`, `add_group_button_done` and `add_group_group_name_input_hint` (`:37-38`, `:41`)
  - `group_saved_successfully` (`:44`)
  - `placeholder_create`

  The names-section strings (`add_group_button_add_name`, `add_group_label_names`, `add_group_name_input_hint`, `delete_name_button_description`, `title_activity_add_names`) and the draw strings stay for NAM-9 and NAM-10.
- [assumed] `.flow.md` `verify` gains `:app:testDebugUnitTest :app:lintDebug`. Today it runs no `:app` tests and no lint, while CI does run lint (critic). This is a one-line repo config change, flagged at gate 1.

## Tasks
<!-- `V` = `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug`, then XML counts. -->
1. **Drawn count in `:shared` + verify config.**
   - What: `GroupSummary.drawnCount`; the `GroupSummaryRow` field; the `GroupDao.observeSummaries` SQL; the `RoomGroupRepository` mapping; the shared `FakeGroupRepository` passes it through. `.flow.md` verify gains `:app:testDebugUnitTest :app:lintDebug`.
   - Files: `shared/…/model/Group.kt`, `data/GroupEntity.kt`, `data/GroupDao.kt`, `data/RoomGroupRepository.kt`, `jvmTest/…/RoomRepositoryTest.kt` (new test: drawn count per group, 0 for an empty group), `.flow.md`.
   - Verify: V.
2. **App scope, `PendingDeletes`, shared UI pieces.**
   - What:
     - `@AppScope CoroutineScope` (`SupervisorJob` + `Dispatchers.Default`) and `@Singleton PendingDeletes`.
     - `ListStatus` and `status()` move to `ui/ListStatus.kt`.
     - `UpButton` moves to `ui/components/UpButton.kt`.
     - `HatIllustration` in `ui/components/`.
     - `PickAListPlaceholder` gains the hat.
   - Files: `app/…/di/AppModule.kt` (new), `ui/PendingDeletes.kt`, `ui/ListStatus.kt`, `ui/components/{UpButton,HatIllustration}.kt`, `ui/placeholder/{Placeholders,PlaceholderViewModels}.kt`, tests' imports.
   - Verify: V.
3. **`ListsViewModel` + tests.**
   - What: `uiState` (cards with title and counts, `pending`); `delete(id, wasOpen)`, `undo(id)`, `commit(id)` (id-guarded), `commitPending()`, `onCleared` on the app scope; `SavedStateHandle` primitives; the `PendingDeletes` mirror. `FakeGroups` gains per-group counts. `FakePreferences` records `removeSortType`.
   - Files: `ui/lists/ListsViewModel.kt`, `test/…/ui/lists/ListsViewModelTest.kt`, `test/…/FakeGroups.kt`, `test/…/FakePreferences.kt`.
   - Verify: V; the AC4 and AC9 cases, with `onCleared` driven through `ViewModelStore.clear()`.
4. **`EditListViewModel` + tests.**
   - What: grows the placeholder VM: `text`/`error` in `SavedStateHandle`; `onTextChange` clears the error; `commitName()` creates or renames; `done()` returns an outcome; `onCleared` rename-on-leave, skipped when the id is pending a delete. The old placeholder tests are migrated.
   - Files: `ui/editlist/EditListViewModel.kt`, `test/…/ui/editlist/EditListViewModelTest.kt` (replaces `ui/placeholder/EditListViewModelTest.kt`).
   - Verify: V; the AC3, AC7 (SavedStateHandle) and AC10 cases, plus "pending delete → no rename on clear".
5. **Strings + drawables.**
   - What:
     - Copy-deck keys for Lists, About and the Edit header: `lists_empty_*`, `lists_item_drawn` plurals, `list_options`, `list_deleted`, `action_undo`, `action_more`, `about*`, `edit_list_name_hint`, `edit_list_name_blank`.
     - Delete the superseded 2.7.0 keys (the Assumptions list) and `placeholder_create`.
     - Vectors `ic_add`, `ic_more_vert`, `ic_app_badge`.
   - Files: `res/values/strings.xml`, `res/drawable/*.xml`.
   - Verify: V (lint catches unused or missing resources).
6. **Lists screen + About sheet.**
   - What:
     - Large top bar ("Your lists", display face) with the overflow → About.
     - Cards with the overflow menu (Edit list, Delete list), the selected fill and `animateItem`.
     - The FAB with `sharedBounds`.
     - The empty state.
     - Snackbar host + `LaunchedEffect(pending.id)` (Long) + `LifecycleStopEffect` commit.
     - Compact height: small bar and 640 dp max width.
     - The About `ModalBottomSheet` with `LocalUriHandler`.
   - Files: `ui/lists/ListsScreen.kt`, `ui/lists/AboutSheet.kt`.
   - Verify: V; device in task 9.
7. **Edit list screen.**
   - What: top bar (`UpButton`, "New list"/"Edit list", Done enabled once the list exists); the outlined name field (label, placeholder, headlineSmall, IME Next, inline error, focus kept); the empty `EditListNamesSlot`; the `sharedBounds` target.
   - Files: `ui/editlist/EditListScreen.kt`.
   - Verify: V; device in task 9.
8. **Wire into the shell.**
   - What: replace the `ListsPlaceholder`/`EditListPlaceholder` entries; pass `openId`, `onHideOpen`, `onReopen` and `singlePane`; the title shared key on `DrawPlaceholder`; delete the dead placeholder code.
   - Files: `navigation/AppNavigation.kt`, `ui/placeholder/Placeholders.kt`, `PlaceholderViewModels.kt`.
   - Verify: V; `BackStackTest` unchanged and green.
9. **Device verification + evidence.**
   - What: AC2–AC8, AC10 and AC11 on phone and tablet, light and dark, with screenshots in `evidence/`. Then AC1 and AC1b on api36_niah with `assembleRelease`: check `versionName` before installing, read-only `adb root` sqlite and DataStore reads, `emu kill` afterwards.
   - Files: evidence only.
   - Verify: the AC checklist in the report.

## Out of scope
- The names section of Edit list (add, rename, delete names, the "Name the list first" add field, the names-count header): NAM-9.
- The Draw screen itself, and the Draw-side half of the title morph beyond tagging the placeholder: NAM-10.
- Reordering lists, list colours or icons.
- Compose UI tests (no androidTest source set exists; the ticket asks for ViewModel tests).
- Retiring the grid preference (NAM-10).

## Verification
- **Machine:**
  - `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug` (the new `.flow.md` verify).
  - Confirm the counts from `shared/build/test-results/*/*.xml` and `app/build/test-results/testDebugUnitTest/*.xml`.
  - `:app:assembleRelease` for the install-over.
- **Outcome:**
  - **Phone (niah_debug_api36):** debug build.
    - New list → blank Next (error) → "  Team  " Next (created, title "Team").
    - Rotate mid-typing; `am kill` mid-typing.
    - Done → Draw → back → Lists shows "Team · 0 names".
    - Edit → rename → back → Lists shows the new name.
    - Delete → Undo → delete → wait → `run-as sqlite3` shows no names rows.
    - Empty state; About sheet; dark mode; TalkBack walk.
  - **Tablet (niah_tablet_api36):** list-detail, selection fill, delete the open list → placeholder → Undo reopens; delete during an unsaved edit → Undo → old name kept.
  - **2.7.0 device (api36_niah):** release build installed over 2.7.0; data, counts, order, the active list and the Z–A sort are as noted (read-only `adb root` reads); then `emu kill`.
