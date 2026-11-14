# Report: build the groups screen — list, create, rename, delete (NAM-8)

## Changed vs plan
All 9 plan tasks are done, plus one gate-2 change ([plan.md](plan.md); Progress is in [workflow.md](workflow.md)).

- **AC status:** every acceptance criterion is met except **AC11**, which is partial. The TalkBack walk couldn't be driven on the emulator, so it's left for the owner.
- **Review:** 0 Blocking and 6 Advisory. Two advisories were fixed and three are follow-ups ([review.md](review.md)).
- **Inherited checks:** NAM-5 AC7 and NAM-6 AC6 (install over 2.7.0) are done here.

| Task | Commit | What |
|---|---|---|
| 1 Drawn count + verify | af892c3 | `GroupSummary.drawnCount`. The groups query sums `names.drawn` with `COALESCE`. `.flow.md` verify now runs `:app:testDebugUnitTest :app:lintDebug` too (flagged at gate 1). |
| 2 Shared pieces | f0db60f | `@AppScope` CoroutineScope, `PendingDeletes`, `ListStatus` and `UpButton` moved out of the placeholders, and `HatIllustration` in Compose (two scheme colours). The "Pick a list" pane shows the hat. |
| 3 ListsViewModel | c71df26 | Lists newest first with live counts. Delayed delete: hide at once, `undo(id)`/`commit(id)` guarded by id, pending kept in `SavedStateHandle`, commits on the app scope. 10 tests. |
| 4 EditListViewModel | 10bfa13 | The list-name field as snapshot state backed by `SavedStateHandle`. `commitName` creates once, then renames. Blank input shows "Enter a list name". `done`, and rename-on-leave skipped while a delete is pending. 19 tests (they replace the 6 placeholder tests). |
| 5 Copy + icons | 914d9a9 | Copy-deck keys for Lists, About and the Edit header. The 2.7.0 keys they supersede are deleted. Material Symbols add, more_vert and open_in_new, plus an About badge. |
| 6 Lists + About | f40122a | Large flexible top bar (small in compact height, cards ≤ 640 dp), segmented cards with an overflow, selected fill in list-detail, extended FAB, empty state, Undo snackbar (`Long`), commit on app stop and on leaving in single pane, About sheet. |
| 7 Edit list | 5bb483c | Top bar (Up hidden in list-detail, New list/Edit list, Done once the list exists), outlined List name field (IME Next, inline error, focus kept), an empty `EditListNamesSlot` for NAM-9, and the FAB → Edit list shared bounds. |
| 8 Shell wiring | c3f22f2 | Real screens in `AppNavigation`. The Lists pane gets `openId`, close-on-delete and reopen-on-undo. The card title shares a key with the Draw placeholder title. Dead placeholder code is removed. |
| 9 Device checks | 07b7f16, 5624bb2 | Phone, tablet, and install-over on api36_niah. Two bugs found and fixed (07b7f16). |
| Review fixes | aebc5be, f0079ac | Done/Next renames run on the app scope. Undo reopens only into an empty detail pane. |
| Gate 2 change | 5964626 | Blank list names from 2.7.0 display as **"Untitled list"** on the card, the overflow description, the delete snackbar and the Draw title. New key `list_untitled`, also added to the spec's Copy deck. 3 tests. |

## Deviations
All of these are logged in workflow.md as they happened.
- **Shared `FakeGroupRepository` unchanged.** The `drawnCount = 0` default already passes through, so the planned edit wasn't needed.
- **The app scope lives in a new `di/AppModule.kt`.** That was the plan's own alternative to `StorageModule`.
- **The Edit list text and error are Compose snapshot state in the ViewModel, backed by `SavedStateHandle`, not a `StateFlow`.** That's Google's pattern for `TextField` state: a collected flow lags a frame and can jump the cursor. Persistence is the same as planned.
- **`placeholder_create` stayed until task 8.** The placeholder still used it until the wiring task. Task 5 also added `ic_open_in_new` for the privacy link.
- **No `LifecycleStopEffect` exists**, so the app-stop commit uses `LifecycleStartEffect { onStopOrDispose }`, which is the same trigger. Rotation is excluded through `isChangingConfigurations`.
- **New `ui/components/SharedBounds.kt`.** It reads `LocalNavAnimatedContentScope` only in single pane, where the scene provides it.
- **Phone landscape puts the name field in the top bar's title row** at titleMedium size, not headlineSmall, so it fits the small bar. The spec says "on the same row as the title".
- **`isCompactHeight()` added to `navigation/Layout.kt`,** with 2 `LayoutTest` cases.
- **Device checks found two bugs** (07b7f16):
  - Cards had no visible fill, because M3's segmented default is close to the surface. They now use `surfaceContainer`, as the canvas does.
  - The New list FAB had **no accessible label**. material3 clears the extended FAB's text semantics and labels the FAB from its icon, which had `contentDescription = null`.
- **The TalkBack walk (AC11) goes to the owner.** On the emulator TalkBack ignores injected swipes and Alt+arrow shortcuts; it was tried 3 ways. Labels were checked in the accessibility tree instead.
- **AC6 names were seeded with sqlite.** Names can't be added in the UI until NAM-9, so they went into the debug DB with the app stopped.
- **The install-over found an unplanned case: a blank group title from 2.7.0.** 2.7.0 had no validation (GitHub #6), and the title migrates as-is. At gate 2 the user chose to fix it now: it displays as "Untitled list". That string wasn't in the approved copy deck, so it was added to the spec's Copy deck table.

## Evidence
**Machine** at e3da5ee: `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug`

```
BUILD SUCCESSFUL in 4s
117 actionable tasks: 3 executed, 114 up-to-date
EXIT=0
```

Counts from the test-results XML: `app:testDebugUnitTest 71` (74 after the gate-2 change at 5964626, also exit 0), `shared:jvmTest 109`, `shared:iosSimulatorArm64Test 70`, `shared:testAndroidHostTest 77`, with 0 failures and 0 errors. The baseline was shared 255; the new test is the Room drawn count.

**Device** (full table and screenshots in [evidence/README.md](evidence/README.md)):
- **Install-over (AC1, AC1b, NAM-5 AC7, NAM-6 AC6).** api36_niah, release-signed, same certificate as 2.7.0.
  - 2.7.0 data: Team Red (Dana, Eli; active; Z–A) and a blank-titled group 2. Group 3 "Newest" was created and then deleted.
  - After installing over it: launch opened `Draw(1)`. Lists showed both groups with correct counts, newest first. DataStore holds `group_sort_type_for_group_1 = DESC`, and `name_list.xml` is gone.
  - A new list "Fresh" got **id 4**, not the deleted 3.
  - Then `emu kill` (`-no-snapshot-save`).
- **Phone (niah_debug_api36):**
  - Blank + Next shows the error with focus kept. `"  Team  "` is stored as `'Team'`.
  - Rotation and `am kill` keep the typed text.
  - Done → Draw → back → Lists.
  - Rename on leave works, and a blank name keeps the old one.
  - Delete hides the list. Undo restores it with its names. Timeout (~12 s) and Home both commit (checked in the DB). Rotate keeps it pending and re-shows the snackbar.
  - Plurals: "1 name", "3 names · 1 drawn".
  - Empty state and About, in light and dark. Landscape: small bar, cards ≤ 640 dp.
- **Tablet (niah_tablet_api36):**
  - List-detail with a 360 dp pane and the selected fill.
  - Deleting the open list shows "Pick a list" and clears `active_group_id`. Undo reopens it, and the id is set again.
  - Delete during an unsaved edit, then Undo, keeps the old name.
  - After the review fix: delete the open list, open another, Undo restores the deleted list and leaves the detail alone.
- **Gate 2 change (phone):** a blank-named list shows "Untitled list". The overflow reads "Options for Untitled list", the Draw title is "Untitled list", and delete shows "Untitled list deleted · Undo".

## Follow-ups
- **Owner: TalkBack walk on a real device** (AC11): open, the card overflow, Edit, Delete, Undo, the FAB and About.
- **A pending delete stranded by a resize** (review A1). On list-detail, delete a list and then resize to single pane with another list open. The delete stays pending (hidden, not deleted) until the user goes back to Lists. It needs a commit trigger outside the Lists composition (process lifecycle or the shell).
- **NAM-9:**
  - Fill `EditListNamesSlot`, and take its `listId` from the ViewModel, not the key (review A3).
  - Call `EditListViewModel.commitName` when the add field takes focus (spec: "focus moving to another field").
  - Reuse `PendingDeletes` for name deletes.
  - The 2.7.0 names-section strings are still in `strings.xml`.
- **NAM-10:** keep the `ListsSharedKeys.title(id)` shared key on the real Draw title. Accepted risk from plan review: a very fast Undo on the tablet can reuse a `DrawViewModel` with `opened = true`, so the active list stays cleared until the list is next opened.
