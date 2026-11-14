---
task: edit-names-screen
title: Build the edit names screen
ticket: NAM-9 — Build the edit names screen (https://linear.app/northtrack/issue/NAM-9/build-the-edit-names-screen)
ticket-id: NAM-9
tracker-synced: In Engineering
mode: full
phase: reporting
branch: flow/edit-names-screen
worktree: ../android-names-in-a-hat-nam-9
created: 2026-11-14
updated: 2026-11-14
---

## Next action
Code review done (review.md): 0 blocking, 4 advisory, all fixed (e97a81b, 5714c96, 8eea812, 2d6d040) and re-checked on niah_debug_api36. Verify GREEN at 2d6d040: shared 277/0, app 110/0, lint clean. Owner follow-ups: TalkBack walk; real-device scroll smoothness (emulator-bound, AC7). Next: write report.md, open the PR, NAM-9 → In Review, hold at gate 2.

## Decision log
<!-- gate entries look like: "- 2026-07-05 gate 1: plan approved by user"
     answered unknowns look like: "- 2026-07-05 Q: per-user or per-team limits? A: per-user"
     log an answer the moment it is given — conversation context does not
     survive a compaction; this file does -->
- 2026-11-14 gate 1: plan approved by user (including the tablet Lists-card count lag during a name's undo window, and 'Unnamed' for blank 2.7.0 names)
- 2026-11-14 mode: full (user confirmed). Reason: new Compose screen + ViewModel, delayed-delete undo, inline rename, scope overlap with NAM-8 (screen merged with list creation) and NAM-10 (draw empty state link, AC6).
- 2026-11-14 Q: who builds the shared Edit list screen while NAM-8 runs in parallel? A: wait for NAM-8 to merge first, then build the names section (spec split) on its real screen.
- 2026-11-14 Q: AC6 (draw empty state → Edit list)? A: hand AC6 to NAM-10; NAM-9 does not touch Draw.
- 2026-11-14 isolation: git worktree at ../android-names-in-a-hat-nam-9. A parallel session is running NAM-8 (flow/groups-screen) in the main checkout.

## Progress
- [x] 1. :shared normalisation (H1) — fa0320a
- [x] 2. DI, manifest and EditListViewModel hooks — a252b4e
- [x] 3. EditNamesViewModel and tests — 55f6c8c
- [x] 4. Copy and icons — ed6191b
- [x] 5. Names section UI and wiring — 7278cc1
- [x] 6. Device verification and evidence — 412152b

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
- 2026-11-14 task 1: added `validateName(raw)` (normalise + validateLabel) in Labels.kt as the single entry point, so NameEditor and the EditNamesViewModel's local add check share one function.
- 2026-11-14 setup briefly checked out flow/edit-names-screen in the main checkout while the NAM-8 session had flow/groups-screen there (no commits, no file changes; restored within a minute). NAM-9 moved to a worktree.
- 2026-11-14 task 2: the `name_untitled` string was added here, not in task 4, because `nameLabel()` needs it to compile.
- 2026-11-14 task 6: inner line-break paste not checked on device (adb can't set the clipboard on this image); covered by LabelsTest/NameEditorTest on all targets and EditNamesViewModelTest.
- 2026-11-14 task 6: a first tablet run let the 10 s snackbars time out between tool calls (correct commits, test data lost); the list-pending check was rerun as one script. See evidence/README.md.
