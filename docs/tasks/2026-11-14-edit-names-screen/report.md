# Report — Build the edit names screen (NAM-9)

## Changed vs plan
All six tasks in [plan.md](plan.md) are done, plus four review fixes ([review.md](review.md)). The diff is `0201792..HEAD`: 19 code and resource files, +1398 / −39.

| Task | Commit | What landed |
|---|---|---|
| 1 `:shared` normalisation (H1) | fa0320a | `normaliseName` turns each run of tab/CR/LF/VT/FF/NEL/LS/PS into one space. `validateName` = normalise, then `validateLabel`. `NameEditor.add` and `rename` use it. 7 new tests on all 3 targets. |
| 2 DI, manifest and Edit list hooks | a252b4e | `@Provides NameEditor`. `adjustResize` on `MainActivity`. `EditListViewModel`: `listId` StateFlow (NAM-8 review A3), `commitName` returns Boolean, `commitNameOnFocusLeave()`. `nameLabel()` shows "Unnamed" for blank names. 6 new tests. |
| 3 `EditNamesViewModel` | 55f6c8c | Add validates locally, clears at once and writes on `@AppScope`. Inline rename, one at a time. Delayed delete with Undo, committed on timeout, a second delete, leaving the app, or `onCleared`. Nothing is written while the list itself is pending a delete or gone. State is in `SavedStateHandle`. `FakeNames` and 30 tests. |
| 4 Copy and icons | ed6191b | 12 copy-deck keys. The 5 superseded 2.7.0 strings are deleted (grep: 0 references). `name_untitled` added to the spec copy deck. `ic_delete`, `ic_check`, `ic_close`. |
| 5 Names section UI | 7278cc1 | `EditNamesSection.kt`: header, lazy keyed list with `animateItem`, inline rename row, Undo snackbar, docked add bar (edge-to-edge `surfaceContainer`, `safeDrawing` insets including the IME, IME Send). `EditListScreen` takes only the top padding, follows the ViewModel's id, and moves focus to the add field after Next. |
| 6 Device verification | 412152b | [evidence/README.md](evidence/README.md): phone, tablet, and the `api36_niah` install-over. |
| Review A1–A4 | e97a81b, 5714c96, 8eea812, 2d6d040 | No "Name the list first" flash while loading. A rename row takes focus once. The list keeps clear of the snackbar. Scroll-to-new only when the list grew. |

## Deviations
- **Setup: checkout collision.** Setup briefly checked out `flow/edit-names-screen` in the main checkout while the NAM-8 session had `flow/groups-screen` there. I switched it back within a minute; there were no commits or file changes. NAM-9 then moved to its own worktree (`../android-names-in-a-hat-nam-9`), so two sessions never share a working tree.
- **Task 1: `validateName` helper.** I added `validateName(raw)` beside `normaliseName`. `NameEditor` and the ViewModel's synchronous add check needed the same normalise-then-trim rule, and one function keeps them from drifting.
- **Task 2: `name_untitled` added early.** The string was added in task 2, not task 4, because `nameLabel()` (task 2) references it and wouldn't compile without it.
- **Task 6: line-break paste not driven on device.** `adb` on this emulator image can't set the clipboard ("No shell command implementation"), and `input text` can't type a line break into a field (Enter is the IME action). Normalisation is covered by `LabelsTest` and `NameEditorTest` on JVM, iOS simulator and Android host, and by `EditNamesViewModelTest` (`"Cy\nDee\tE"` → `add(7, Cy Dee E, true)`).
- **Task 6: tablet run redone.** The first tablet run of the "list deleted while editing" check was slower than the 10 s snackbars, so a name delete and then a list Undo timed out and committed. That was correct behaviour, but it wiped emulator test data. I reran the check as one script inside the undo window; the result is in the evidence.

## Evidence
**Verify at a5f39c2** (`./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug`):
```
BUILD SUCCESSFUL in 5s
117 actionable tasks: 3 executed, 114 up-to-date
shared: 277 tests, 0 failures   (jvmTest 116, iosSimulatorArm64Test 77, testAndroidHostTest 84)
app: 110 tests, 0 failures
```
Counts are from the `build/test-results` XML. Baseline at 0201792 was shared 256/0 and app 74/0.

Lint has 0 errors and 13 warnings, none in files this change adds:
- 9 `UnusedResources`: the 2.7.0 draw strings that NAM-10 replaces.
- `OldTargetApi`, `RedundantLabel` (the existing activity label), `AndroidGradlePluginVersion` and `NewerVersionAvailable`.

**Outcome on devices** ([evidence/README.md](evidence/README.md) has bounds, database rows and screenshots):
- **Install-over:** release-signed 3.0 (certificate `4fa5cd07…`, the same as 2.7.0) installed with `install -r` over 2.7.0 on `api36_niah`. Team Red opens with **Dana, Eli**, and "Aaron" is added **last**: `3|1|Aaron|1|0`. The emulator ran with `-no-snapshot-save`, so the 2.7.0 data is unchanged.
- **Phone (`niah_debug_api36`):**
  - "  Ada  ", "Ben", "Aaron" are stored as `Ada`, `Ben`, `Aaron`, each with `selected=1`, and shown in that order.
  - A blank add shows "Enter a name" with the database unchanged.
  - Rename: save (`Benji`), blank shows the error, ✕ cancels.
  - Delete hides the row and the database is unchanged until commit. Undo restores id 6. Rotation keeps the pending delete. Home commits it.
  - 52 names with the keyboard up: the new row is fully visible above the add bar.
  - Add text "Half" and rename draft "Zedek" survive `am kill` with `pidof` empty.
  - A new list's add field is disabled with "Name the list first". Next creates the list and moves focus to the add field.
  - Light and dark, portrait and landscape.
- **Tablet (`niah_tablet_api36`, list-detail):**
  1. With a pending name delete and an open rename draft, delete the list from the Lists pane: **no name writes**.
  2. Undo the list delete: Ada, Ben, Cy are unchanged and Alpha reopens.
  3. Back from Edit list returns to Draw.
  4. Light and dark.
- **After review:**
  - A2: the loading frame shows no "Name the list first".
  - A1: focus stays on the add field after a rename row scrolls back into view.
  - A3: "Yara" added during an undo window sits above the snackbar.

## Follow-ups
- **Owner: TalkBack walk** of the names section on a real device. Focus can't be driven on the emulator. The labels are in the accessibility tree: "Delete Ada", "Rename Ada" (click label), "Save name", "Cancel rename", "Add", the error semantics on the add field, and "Ada deleted · Undo".
- **Owner: scroll smoothness on a real device (AC7).** On the headless emulator the names list (76% janky, p50 61 ms) and system Settings (82%, p50 89 ms) are both limited by the emulator's software rendering, so this can't be judged here.
- **Accepted at gate 1:** on list-detail, the Lists card's name count still includes a name that is hidden pending delete, until the delete commits (about 10 s or on leaving Edit list).
- **NAM-10:** owns NAM-9's AC6 (the Draw empty state's "Add names" → `EditList(id)`). The hand-off comment is on NAM-10.
- **NAM-8 review A3 (rest):** `onGone`, `openId` and `doneEditing` still read the key, not the ViewModel's id. NAM-9 guards its side: the slot is gated on `Present`, and writes are wrapped.
- **Not covered:** a dark-landscape screenshot. The 840 dp cap is unexercised because the tablet's detail pane is narrower than that.
