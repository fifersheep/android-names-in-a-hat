# Plan — Build the edit names screen (NAM-9)

## Intent
Fill NAM-8's empty `EditListNamesSlot` with the names section of Edit list (`docs/design/3.0-spec.md#screen-edit-list`), so names can be added, renamed inline and deleted with a delayed-delete undo:
- the names header and count
- the lazy names list
- the add field, docked above the keyboard

The spec overrides the ticket: the message is "Enter a name", rename is inline, and delete is delayed with Undo. AC6 moved to NAM-10 (decision log).

## Acceptance criteria
Ticket AC numbers are in brackets; AC6 is NAM-10's.
- [ ] **[1] Order.** Names show in the order they were added, not A–Z.
  - On device, "Aaron" added after "Ben" appears **last**.
  - Release-signed install-over on `api36_niah`: "Team Red" shows Dana, then Eli.
  - Existing `RoomRepositoryTest` and `MigrationTest` cover `ORDER BY id`, and a ViewModel test asserts a non-alphabetical order.
- [ ] **[2] Add.**
  - Add (icon or IME Send) clears the field **at once**, before the write.
  - The name appears at the end of the list and is scrolled into view.
  - Focus and the keyboard stay.
  - Two quick Sends never insert the name twice.
  - Covered by ViewModel tests (text cleared synchronously, one insert, name stored) and on device.
- [ ] **[3] Validation.** Blank or whitespace-only input on add or rename shows the inline "Enter a name". Nothing is saved, the typed text stays, and typing clears the error.
  - `"  Ada  "` is stored as `Ada`.
  - `"Ada\nBen"` and `"Ada\tBen"` are stored as `Ada Ben` (hand-off H1).
  - Covered by ViewModel tests, a `:shared` `NameEditorTest`, and a device sqlite read.
- [ ] **[4] Delete.**
  - Delete hides the row at once and shows "Ada deleted · Undo", with no database write.
  - Undo brings the row back with the same id and position.
  - The delete is committed by any of: the snackbar timing out, a second delete, leaving the app (not rotating), or leaving the screen.
  - It is **not** committed when the screen closes because its list is pending a delete or gone.
  - On device: a pending delete survives **rotation**, and the database is checked with sqlite before and after.
  - The restore from saved state after process death is a ViewModel test only. On device, leaving the app commits the delete before `am kill` can happen.
- [ ] **[5] Selected.** A name added here has `selected = 1` in the database. `NameEditor.add` already does this; a ViewModel test asserts it.
- [ ] **[7] Long lists.** With 50 names and the keyboard up, adding one leaves the add bar's bounds entirely above the IME, and the new row's bounds entirely visible between the header and the add bar. Checked from the `uiautomator` bounds and a screenshot. The list is lazy and keyed by id, and scrolling 50 rows shows no visible jank on the emulator.
- [ ] **[8] Saved state.** Typed add text and an open inline rename (which row and its draft) survive rotation, and survive `am kill` with `pidof` empty. Checked on device and by ViewModel tests that restore from `SavedStateHandle`. A restored pending delete is covered by a ViewModel test only (see [4]).
- [ ] **Rename (D13).**
  - Tapping a name turns the row into a field with ✓ and ✕. IME Done or ✓ saves the trimmed, normalised text.
  - Blank shows "Enter a name" and keeps the old name. ✕ cancels.
  - Covered by ViewModel tests and on device.
- [ ] **Name the list first.** On a new list (`listId == null`), the add field is disabled with "Name the list first" and there is no names header until the list exists. After that the header shows, even during the same create visit. Pressing Next on a valid list name moves focus to the add field. Opening an existing list doesn't raise the keyboard. Checked on device.
- [ ] **[9] Spec match.** Checked on `niah_debug_api36` (portrait and landscape) and `niah_tablet_api36` (list-detail), in light and dark. Screenshots go in `evidence/`. Checklist, using `uiautomator` bounds where they apply:
  - name rows are at least 56 dp, with `outlineVariant` dividers
  - delete, ✓, ✕ and Add are 48 dp touch targets
  - the Add button is a filled primary circle
  - the add bar spans the full width on `surfaceContainer`, down to the screen's bottom edge when the keyboard is closed
  - the header reads "N names" in labelSmall
  - content is capped at 840 dp and centred on the tablet
  - the error and the "Name the list first" supporting text match `EditScreen.dc.html`
- [ ] **[10] Tests.** ViewModel unit tests cover add, rename, delete and undo, and validation. The verify command is green.

## Approach
**A separate `EditNamesViewModel` per list, created inside the slot.** `EditListViewModel` keeps the list name; its 166 lines already cover NAM-8's commit rules.
- The ViewModel is assisted-injected with `listId: Long`. It is created with `hiltViewModel(key = "names:$listId", …)` only once the list exists, in the same entry's `ViewModelStore`, so it gets its own `SavedStateHandle`.
- Before the list exists, the slot shows only the disabled add bar, with no ViewModel.
- `listId` comes from `EditListViewModel` (a new `listId: StateFlow<Long?>`), not from the key. This is NAM-8 review A3: the key can lag behind a create that's in flight.

**State.** It follows NAM-8's `EditListViewModel` pattern: snapshot state backed by `SavedStateHandle`.
- `addText` and `addError`.
- `renamingId`, `renameText` and `renameError`.
- The pending delete: id and text.
- A `uiState: StateFlow` holds the visible names, which is `observeNames(listId)` minus the pending and committed ids, plus a `loaded` flag so "0 names" doesn't flash before the first read (as `ListsViewModel.kt:29-30` does).

**Writes.**
- Add and rename go through `NameEditor`, and so through `normaliseName` and `validateLabel`.
- **Add validates locally first.** It calls the `:shared` `normaliseName` and `validateLabel` synchronously. A blank result shows the error. A valid one clears the field at once and runs `NameEditor.add` on `@AppScope`. This avoids two quick Sends inserting twice, and Done straight after Add cancelling the write (the same failure class as NAM-8 A2, `EditListViewModel.kt:140-142`). Rename also runs on `@AppScope`.
- Writes on `@AppScope` are wrapped so a foreign-key failure (the list deleted underneath, NAM-8 A3 window) is logged, not thrown. The slot also only gets a `listId` while the list's status is `Present` (`EditListScreen.kt:109`).
- **Leaving the screen** (`onCleared`):
  - It commits the pending name delete and a non-blank changed rename draft, **unless the list is in `PendingDeletes` or gone**. Then it writes nothing, keeping NAM-8's contract (`PendingDeletes.kt:10-13`, `EditListViewModel.kt:130`), so Undo of the list brings back its names unchanged.
  - Typed add text is discarded, never added. That matches the list rule "back on a new list discards", because add text is a new item, while a rename draft is an edit of an existing one.
- Delete follows NAM-8's `ListsViewModel` delayed-delete shape: `delete`, `undo(id)`, `commit(id)`, `commitPending()` and `onCleared`. Commits run on `@AppScope`. A local `committed` set keeps a committed row hidden until the Flow drops it.

**Normalisation (H1)** lives in `:shared`. A new `normaliseName(raw)` replaces each run of tab, CR, LF, U+2028 or U+2029 with one space. `NameEditor.add` and `NameEditor.rename` call it before `validateLabel`. List labels aren't changed, because the spec assigns H1 to names only.

**UI** goes in a new `ui/editlist/EditNamesSection.kt`.
- A names header ("N names", labelSmall).
- A `LazyColumn` keyed by name id, with `animateItem()`. Rows are at least 56 dp, with an `outlineVariant` divider, a 48 dp delete icon button, and an inline rename row with ✓ and ✕.
- A `SnackbarHost` above the add bar.
- `AddNameBar`: an outlined field, IME Send, and a 48 dp filled circular Add button.
- **Insets:**
  - The manifest activity gets `android:windowSoftInputMode="adjustResize"`, so the window doesn't pan.
  - `EditListScreen`'s content Column takes only the **top** Scaffold padding.
  - The list's content gets horizontal `safeDrawing` insets.
  - `AddNameBar` draws `surfaceContainer` edge to edge, then applies `windowInsetsPadding(WindowInsets.safeDrawing.only(Horizontal + Bottom))` inside its background. `safeDrawing` includes the IME, so the bar sits on the keyboard when it's up and on the nav bar when it's down, with no double counting.
- Content is capped at 840 dp wide and centred, like the list-name field.
- Adding scrolls to the new id once it appears in the list.
- The snackbar pattern is NAM-8's `DelayedDeleteSnackbar`, with an explicit `SnackbarDuration.Long`.
- A `LifecycleStartEffect` commits the pending delete on stop when the activity isn't changing configuration.

**Focus.**
- The add field or a rename field gaining focus calls a new `EditListViewModel.commitNameOnFocusLeave()`.
  - It renames an existing list when the text is non-blank and changed, and does nothing otherwise, with no error shown (spec: "focus moving to another field with non-blank text"; NAM-8 follow-up).
  - It has no create branch, because the add field is disabled until the list exists.
- **Next moves focus to the add field:**
  - `commitName` returns `Boolean`: `false` for blank, `true` when it renames or starts the create.
  - On `true`, the screen sets a saveable `focusAddOnEnable` flag.
  - A `LaunchedEffect(addEnabled, focusAddOnEnable)` in the slot requests focus once the field is enabled, then clears the flag.
  - The flag is only ever set by Next, so opening an existing list never raises the keyboard.

`EditListScreen` changes only its layout and the slot wiring:
- the top-only padding
- `listId` taken from `vm.listId` while the status is `Present`
- the focus flag

`onGone`, `openId` and `doneEditing` still use the key. That's the remaining half of NAM-8 A3; it's out of scope and guarded by the `Present` gate and the wrapped writes above.

Alternatives considered:
- Grow `EditListViewModel` to own the names. Rejected: one ViewModel mixing two lifecycles (null id, then the id).
- `rememberSaveable` for the text. Rejected: validation would sit outside the ViewModel and AC10's tests.
- Reuse `PendingDeletes` for names. Rejected (see Assumptions).

## Assumptions & open questions
- [answered 2026-11-14] Wait for NAM-8, then build only the names section on NAM-8's real screen. Decision log.
- [answered 2026-11-14] AC6 (the Draw empty state opening Edit list) goes to NAM-10; Draw isn't touched. Decision log, and the comment on NAM-10.
- [answered 2026-11-12, spec D15 and the ticket comment] The message is "Enter a name", not the ticket's "Names cannot be blank". `3.0-spec.md:303`.
- [assumed] H1 normalisation goes in `:shared` `NameEditor` (add and rename), not in the UI. The ticket says to call the `:shared` rules from the ViewModel, and `NameEditor.kt:10-19` is the single write path for names.
- [assumed] Don't add name ids to `PendingDeletes` (the NAM-8 report suggested it). It holds list ids, and name ids are a separate id space. `EditNamesViewModel` **does** read `PendingDeletes` for its list id, to skip writes on leave (see Approach).
- [assumed, **flag at gate 1**] On list-detail, the Lists pane stays beside Edit list. Its card reads `nameCount` from `observeGroups()` (`ListsViewModel.kt:63-67`), so during a name's undo window the card still counts the hidden name (for example "3 names" while the header says "2 names"). It corrects itself when the delete commits (about 10 s, or on leaving Edit list). This is accepted rather than adding a cross-screen pending-names registry.
- [assumed] One pending delete at a time: a second delete commits the first, as in `ListsViewModel.kt:74-78`.
- [assumed] Process death with a pending delete:
  - The pending delete is restored from saved state and the snackbar shows again, as Lists does (`ListsViewModel.kt:116-119`).
  - If the entry isn't restored, nothing was deleted and the name is still there.
- [assumed] Typed add text is discarded on leaving, while a valid rename draft is committed. The reasoning is in Approach.
- [assumed] One inline rename at a time.
  - Tapping another name commits the open rename if its text is non-blank and changed, and cancels it otherwise.
  - Leaving the screen does the same. This mirrors the list-name rule "back commits a non-blank change" (`3.0-spec.md:127`).
  - The delete icon is hidden while a row is renaming (the mock-up shows ✓ ✕ in its place).
- [assumed] An existing list with no names shows the header "0 names" and the add bar. Before the list exists (`listId == null`, not `key.isNew`, which stays true for the whole create visit, `BackStack.kt:42`) there is no header, as in the mock-up's pre-create state (`EditScreen.dc.html:110`).
- [assumed] IME Next on a successfully committed list name moves focus to the add field. NAM-8's comment at `EditListScreen.kt:148-149` anticipates it, and Next means "next field".
- [assumed] IME Send can't carry a custom "Add" label from Compose (`KeyboardOptions` has no action label). The Send action is used, and the visible Add button carries `edit_add_name_action`.
- [assumed, **flag at gate 1**] A blank name migrated from 2.7.0 (GitHub #5: 2.7.0 saved blank names) shows as "Unnamed".
  - This is a new copy key, `name_untitled`, mirroring NAM-8's gate-2 `list_untitled`. The spec copy deck gets one row.
  - It uses a `nameLabel()` helper beside `listLabel` (`ui/ListLabel.kt:11-15`). The row text (in onSurfaceVariant), the snackbar "Unnamed deleted", and "Delete Unnamed" / "Rename Unnamed" all use it.
  - Opening a rename on it starts with an empty field.
- [assumed] The superseded 2.7.0 names strings are deleted once nothing references them: `title_activity_add_names`, `add_group_button_add_name`, `add_group_label_names`, `add_group_name_input_hint` and `delete_name_button_description`. NAM-8 left them for NAM-9 (NAM-8 `report.md` follow-ups).
- [open — owner, at review] A TalkBack walk of the names section on a real device. TalkBack focus can't be driven on the emulator (memory); labels are checked in the `uiautomator` tree.

## Tasks
1. **`:shared` normalisation (H1).** Add `normaliseName` to `usecase/Labels.kt`. It replaces each run of `\t \n \u000B \f \r \u0085 \u2028 \u2029` with one space. `NameEditor.add` and `NameEditor.rename` normalise, then validate. Add cases to `NameEditorTest` and `LabelsTest`: `\n`, `\t`, `\r\n`, U+2028, runs collapse to one space, and whitespace-only stays blank. Test names have no commas.
   - Files: `shared/src/commonMain/.../usecase/Labels.kt`, `NameEditor.kt`; `shared/src/commonTest/.../usecase/LabelsTest.kt`, `NameEditorTest.kt`.
   - Verify: `./gradlew --no-watch-fs :shared:allTests`, with the XML counts.
2. **DI, manifest and the `EditListViewModel` hooks.**
   - Add `@Provides NameEditor` to `StorageModule`.
   - Set `adjustResize` on `MainActivity` in the manifest.
   - In `EditListViewModel`: expose `listId: StateFlow<Long?>`, make `commitName` return `Boolean`, and add `commitNameOnFocusLeave()`.
   - Add cases to `EditListViewModelTest`:
     - on focus leave: blank is a no-op with no error, a change renames, and with no list it does nothing
     - `commitName` returns false for blank and true for valid
   - Add `nameLabel()` and `ListLabelTest` cases.
   - Files: `app/.../di/StorageModule.kt`, `app/src/main/AndroidManifest.xml`, `ui/editlist/EditListViewModel.kt`, `ui/ListLabel.kt`, `app/src/test/.../ui/editlist/EditListViewModelTest.kt`, `app/src/test/.../ui/ListLabelTest.kt`.
   - Verify: `:app:testDebugUnitTest`.
3. **`EditNamesViewModel` and its tests.** State, add, rename, delete, undo, commit, `onCleared` and saved-state restore, as in Approach. Add `FakeNames` (a `NameRepository`) to `app/src/test`. `EditNamesViewModelTest` covers:
   - order and live updates
   - add: trims, stores selected, clears the text synchronously, and two quick adds insert once each
   - a non-alphabetical insertion order is kept
   - blank add: error, nothing saved, text kept
   - typing clears the error
   - rename: valid, blank, cancel, switching rows commits or cancels
   - delete hides the row, and Undo restores the same id
   - commit on timeout, on a second delete and on clear
   - restoring the text, the rename and the pending delete from `SavedStateHandle`
   - leaving commits a valid rename draft and drops the add text
   - leaving while the list is in `PendingDeletes` (or gone) writes nothing
   - "0 names" isn't emitted before the first read (`loaded`)
   - Files: `app/.../ui/editlist/EditNamesViewModel.kt`, `app/src/test/.../FakeNames.kt`, `app/src/test/.../ui/editlist/EditNamesViewModelTest.kt`.
   - Verify: `:app:testDebugUnitTest`.
4. **Copy and icons.** Add the copy-deck keys:
   - `edit_names_count` (plurals), `edit_add_name`, `edit_add_name_hint`, `edit_add_name_action`
   - `edit_name_blank`, `edit_name_first`
   - `edit_delete_name`, `edit_rename_name`, `edit_rename_save`, `edit_rename_cancel`
   - `name_deleted` and `name_untitled`

   Delete the five superseded strings. Add Material Symbols `ic_delete`, `ic_check` and `ic_close`. Add a `name_untitled` row to the spec copy deck.
   - Files: `app/src/main/res/values/strings.xml`, `app/src/main/res/drawable/ic_{delete,check,close}.xml`, `docs/design/3.0-spec.md`.
   - Verify: `:app:lintDebug`, plus `grep -rn` for each deleted key under `app/src` with zero hits. Lint doesn't fail on unused resources.
5. **The names section UI and wiring.** Build `EditNamesSection` (header, list, rows, rename row, snackbar, `AddNameBar`, focus and scroll) and replace the `EditListNamesSlot` stub. Adjust `EditListScreen`: padding, `listId` taken from the ViewModel, Next moves focus to the add field, and the add field's focus commits the list name.
   - Files: `app/.../ui/editlist/EditNamesSection.kt` (new), `ui/editlist/EditListScreen.kt`.
   - Verify: `:app:assembleDebug`, then a smoke test on `niah_debug_api36`.
6. **Device verification and evidence.** Run the Outcome steps below and write `evidence/README.md` and the screenshots.
   - Files: `docs/tasks/2026-11-14-edit-names-screen/evidence/`.
   - Verify: every AC row has a result.

## Out of scope
- AC6 and anything on Draw (NAM-10).
- The list-name field and its commit and back rules, beyond the two hooks in task 2 (NAM-8, merged).
- Bulk paste or import, and duplicate warnings (D14).
- Reordering names, and selecting names for a draw (NAM-10).
- NAM-8's review A1 (a stranded list delete after a resize).
- A Compose UI test harness. AC10 asks for ViewModel tests; the UI is checked on device.

## Verification
- **Machine:** `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug` (`.flow.md`). Confirm the counts from the `build/test-results` XML files, not the BUILD line.
- **Outcome** (on `niah_debug_api36`; never a debug build on `api36_niah`):
  1. New list: the add field is disabled with "Name the list first". Type a list name and press Next: the list is created and focus moves to the add field.
  2. Add "  Ada  ", then "Ben", then "Aaron" with IME Send. They appear in that order, with Aaron last. The field clears and the keyboard stays up. `run-as … sqlite3` shows `Ada`, `Ben` and `Aaron` with `selected = 1`.
  3. Blank add shows "Enter a name" and the database is unchanged. Paste "Cy\nDee" and it is stored as `Cy Dee`.
  4. Tap Ben, rename to "Benji", press Done: the database is updated. Rename to blank: the error shows and the old name stays. ✕ cancels.
  5. Delete Ada: the row is hidden and the database is unchanged. Undo brings back the same id. Delete again and wait: the row is gone from the database. Delete, then Home: committed. Delete, then rotate: still hidden, the snackbar shows again, and Undo works. On the tablet, rename a name (draft open) and delete another, then delete the list from the Lists pane and Undo: the names are unchanged in the database.
  6. Seed 50 names (sqlite while the app is stopped) and add one: the field stays above the keyboard and the list scrolls to the new row.
  7. Type add text and open a rename draft, then rotate, then Home, `am kill` with `pidof` empty, and relaunch: both are restored. A pending delete is not part of this step (see AC4).
  8. Screenshots in portrait, landscape and on the tablet (list-detail: Edit list in the detail pane, back returns to Draw), in light and dark, compared with `EditScreen.dc.html` and `LargeEdit.dc.html`.
  9. Release-signed install-over on `api36_niah` with `-no-snapshot-save` (memory procedure): Team Red shows Dana, then Eli.
