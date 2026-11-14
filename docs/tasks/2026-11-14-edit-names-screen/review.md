# NAM-9 code review

One `flow-reviewer` pass over `0201792...HEAD` (at ce7af1f) against plan.md. The reviewer didn't rerun verify; it relied on the green run (shared 277/0, app 110/0). I re-verified after the fixes (below).

## Blocking
None.

## Advisory
| # | Finding | Action |
|---|---|---|
| A1 | `EditNamesSection.kt:219`: `RenameRow` requests focus every time it enters composition. A rename row that scrolls out and back (or is recomposed after Send) takes focus back from the add field mid-typing, and the IME action flips from Send to Done. | **Fixed:** the section remembers (saveable) which rename it already focused, so the request fires once per opened rename. |
| A2 | `EditListScreen.kt:76`, `EditNamesSection.kt:169,281`: while an existing list's status is `Loading`, `namesListId` is null, so the add bar flashes disabled with "Name the list first". | **Fixed:** the slot also gets `listCreated` (the ViewModel's id is non-null). "Name the list first" shows only before the list exists. While loading, the bar is disabled with no supporting text. |
| A3 | `EditNamesSection.kt:143-164`: the undo snackbar overlays the bottom of the list with no content padding, so a name added during an undo window is scrolled under the snackbar (breaks AC7's "entirely visible" in that state). | **Fixed:** the list gets 72 dp of bottom content padding while a snackbar is showing. |
| A4 | `EditNamesSection.kt:121-126`: a failed add leaves `scrollToNew` true, so a later count change (for example an Undo) jumps to the bottom. | **Fixed:** the scroll fires only when the count grew, and any change in the count clears the flag. |

## Acceptance criteria (reviewer, plus notes)
| AC | Result | Notes |
|---|---|---|
| [1] Order | MET | DAO `ORDER BY id`, ViewModel test, device (Aaron last), install-over (Dana, Eli). |
| [2] Add | MET | Snackbar overlap case fixed (A3). |
| [3] Validation | PARTIAL → accepted | Line-break paste not driven on device (adb can't set the clipboard). Covered by `LabelsTest`/`NameEditorTest` on 3 targets and the ViewModel tests. Recorded in Deviations. |
| [4] Delete | MET | Tests, plus phone and tablet scripts. |
| [5] Selected | MET | Test and database. |
| [7] Long lists | MET (bounds) · smoothness UNVERIFIED → owner | Bounds were already shown, and the snackbar case is fixed (A3). Smoothness on the headless emulator: names list 140 frames, 76% janky, p50 61 ms; **system Settings on the same emulator** 213 frames, 82% janky, p50 89 ms. The emulator's software rendering is the bottleneck, so this can't show real-device smoothness. The list is lazy and keyed and is no worse than a system app here. Real-device scroll check → owner follow-up. |
| [8] Saved state | MET | Tests, rotation, and `am kill`. |
| Rename (D13) | MET | |
| Name the list first | MET | Loading flash fixed (A2). |
| [9] Spec match | PARTIAL → accepted | The 840 dp cap never applies on `niah_tablet_api36`: its detail pane is under 840 dp. The cap is a plain `widthIn` that also wraps the list-name field (NAM-8, checked). There is no dark-landscape shot; light landscape and dark portrait are covered. The error and supporting text were compared with `EditScreen.dc.html`: same strings, error colour, and placement below the field. |
| [10] Tests | MET | Re-verified below. |

## Checked and clean (reviewer)
- `listWritable()` and the tablet list-pending path: `PendingDeletes` is set synchronously before the entry pops.
- The dispose commit and `onCleared` are idempotent.
- The `focusAdd` effect keys.
- No double counting of insets.
- Two ViewModels in one entry each get their own `SavedStateHandle`.
- Strings match the copy deck, and the five superseded keys have no references left.
- Accessibility labels.
- No scope creep, no silent deviations, and no weakened verification.

## Re-verify after fixes (2d6d040)
- **Machine:** `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug` exit 0. shared 277/0, app 110/0 (XML counts).
- **Outcome** (`niah_debug_api36`, dark):
  - **A2:** four back-to-back screencaps while opening Year 5 Maple. The loading frame shows the add bar disabled with **no** "Name the list first"; the next frame shows the loaded list.
  - **A1:**
    1. Open a rename on Aaron (top row).
    2. Tap the add field and add "Xavi" (the list scrolls to the bottom).
    3. Scroll back to the top: Aaron's rename row is visible again and **focus stays on the add field** (`foc=true` at `[42,1317]`). `review-a1-rename-scrolled-back.png`.
  - **A3:** delete Pupil 01, then add "Yara" within the undo window. Yara at `[53,957][922,1083]` is fully above the snackbar at `[74,1163]…`. `review-a3-add-during-undo.png`.

