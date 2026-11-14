---
task: draw-from-remaining
title: Draw additional names from those remaining (drawn state persisted per group)
ticket: NAM-12 — Draw additional names from those remaining (https://linear.app/northtrack/issue/NAM-12/draw-additional-names-from-those-remaining)
ticket-id: NAM-12
tracker-synced: In Review
mode: full
phase: reporting
branch: flow/draw-from-remaining
worktree: ../android-names-in-a-hat-nam-12
created: 2026-11-14
updated: 2026-11-14
---

## Next action
Gate 2: waiting for the user to approve the merge of PR #32 (https://github.com/fifersheep/android-names-in-a-hat/pull/32); report.md is written. NAM-12 → In Review. Verify at 4017cf3 is green: app 165/0, shared jvm 133 / iOS 94 / androidHost 101, lint 0 errors.

## Decision log
- 2026-11-14 mode: full, confirmed by the user. It spans :shared draw logic, a new per-name un-draw in NameRepository/Room, and draw-screen UI, with 11 ACs.
- 2026-11-14 isolation: a worktree at ../android-names-in-a-hat-nam-12, branched from origin/main @ 42bc63a (NAM-10 merged). The main checkout is shared with other sessions and is behind origin, so it was left alone.
- 2026-11-14 design inputs (NAM-12 comment from the owner, 2026-11-12): always on. Reset goes in the top bar with an undo snackbar and no confirmation. Drawn names get strikethrough, a ticket icon and the TalkBack state "drawn". "Put back in the hat" clears drawn and sets selected, and needs a new per-name clearDrawn(ids) in NameRepository/RoomNameRepository. Counter, stepper and draw use the "in the hat" pool (selected and not drawn), minimum 1. "Everyone's been drawn" + Reset shows only when the hat is empty and at least one selected name is drawn. Spec sections: docs/design/3.0-spec.md#draw-from-remaining and #chip-states.

- 2026-11-14 research: no shape-changing questions, since spec D2/D7/D8/D9 and the owner's NAM-12 comment settle behaviour. Defaults (use-case placement, put-back atomicity, undo semantics and lifetime) are recorded in research.md and carry into plan.md Assumptions. AC11 (close GitHub #16) is deferred to wrap, with the user's OK.
- 2026-11-14 plan critic: 1 Blocking finding: AC10 can't be evidenced by a uiautomator dump, which has no Compose stateDescription or custom actions. Fixed by adding one Robolectric Compose semantics test to :app, with the owner's TalkBack check as the fallback. 8 Advisory findings were folded in: the 4 more VM tests that marking breaks, fake failure/gate hooks, VM catches a failed mark, DatabaseThreadingTest, no snackbar for an empty reset, a one-script Reset→Undo device step, the landscape snackbar check, and the assumption-2 evidence corrected.
- 2026-11-14 gate 1: plan approved by user (including the Robolectric semantics test for AC10)

- 2026-11-14 Q: which emulator for task 5, given the disk-full failure? A: the user freed disk space; retry niah_api30.

## Progress
- [x] 1. Per-name un-draw in the repo + fake hooks (3b0c1ef)
- [x] 2. HatDraw use case + tests (28dd911)
- [x] 3. DrawViewModel on HatDraw: mark on draw, reset/undo, put back (8e9ca3c)
- [x] 4. UI: Reset, undo snackbar, Put back menu + a11y action, semantics test (9a4bc2f)
- [x] 5. Device check (evidence/README.md)

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
- 2026-11-14 task 4: the compact-height SnackbarHost was put in the names area up front, instead of "check on device first, then move". The trailing panel is inside the content, so a Scaffold snackbar would sit over its button. Device step 6 still checks placement.
- 2026-11-14 task 4: :app lint now reports NewerVersionAvailable for robolectric 4.17 → 4.18, because :app uses the catalog entry. Kept at 4.17 to match :shared (warning only; 0 errors).
- 2026-11-14 task 4: unitTests.isIncludeAndroidResources = true was added to :app (needed for the Robolectric semantics test to resolve strings). The plan didn't name it.
- 2026-11-14 task 5: niah_debug_api36 was already running with an app updated at 20:17, and the names-in-a-hat-bd session was busy, so niah_api30 was used instead to avoid interfering. niah_api30's adb hung (uiautomator, screencap and shell all stalled; ColorBuffer errors). A reboot with -gpu swiftshader_indirect was refused: "not enough disk space" (9.5 GiB free). The debug build was installed on niah_api30 and a "Year 6 Oak" list seeded there before it hung. Now waiting on the user.
- 2026-11-14 task 5: run on niah_api30 (API 30), not niah_debug_api36 (which another session may be using). After the user freed disk space, niah_api30 booted fine. The check covered portrait and landscape on a phone; no tablet run (not in the plan).
- 2026-11-14 review A1 fix: added `DrawViewModel.dropPendingReset()` and a stop-time LifecycleStartEffect in DrawScreen, neither in plan.md. Backgrounding the app now also ends Undo, as with Lists and Edit names.
