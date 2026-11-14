---
task: draw-from-remaining
title: Draw additional names from those remaining (drawn state persisted per group)
ticket: NAM-12 — Draw additional names from those remaining (https://linear.app/northtrack/issue/NAM-12/draw-additional-names-from-those-remaining)
ticket-id: NAM-12
tracker-synced: In Engineering
mode: full
phase: plan-review
branch: flow/draw-from-remaining
worktree: ../android-names-in-a-hat-nam-12
created: 2026-11-14
updated: 2026-11-14
---

## Next action
Gate 1: waiting for the user to approve plan.md. The critic has run once and plan.md is revised (1 Blocking issue on the AC10 evidence was fixed with a Robolectric semantics test; Advisory findings were folded in).

## Decision log
- 2026-11-14 mode: full, confirmed by the user. It spans :shared draw logic, a new per-name un-draw in NameRepository/Room, and draw-screen UI, with 11 ACs.
- 2026-11-14 isolation: a worktree at ../android-names-in-a-hat-nam-12, branched from origin/main @ 42bc63a (NAM-10 merged). The main checkout is shared with other sessions and is behind origin, so it was left alone.
- 2026-11-14 design inputs (NAM-12 comment from the owner, 2026-11-12): always on. Reset goes in the top bar with an undo snackbar and no confirmation. Drawn names get strikethrough, a ticket icon and the TalkBack state "drawn". "Put back in the hat" clears drawn and sets selected, and needs a new per-name clearDrawn(ids) in NameRepository/RoomNameRepository. Counter, stepper and draw use the "in the hat" pool (selected and not drawn), minimum 1. "Everyone's been drawn" + Reset shows only when the hat is empty and at least one selected name is drawn. Spec sections: docs/design/3.0-spec.md#draw-from-remaining and #chip-states.

- 2026-11-14 research: no shape-changing questions, since spec D2/D7/D8/D9 and the owner's NAM-12 comment settle behaviour. Defaults (use-case placement, put-back atomicity, undo semantics and lifetime) are recorded in research.md and carry into plan.md Assumptions. AC11 (close GitHub #16) is deferred to wrap, with the user's OK.
- 2026-11-14 plan critic: 1 Blocking finding: AC10 can't be evidenced by a uiautomator dump, which has no Compose stateDescription or custom actions. Fixed by adding one Robolectric Compose semantics test to :app, with the owner's TalkBack check as the fallback. 8 Advisory findings were folded in: the 4 more VM tests that marking breaks, fake failure/gate hooks, VM catches a failed mark, DatabaseThreadingTest, no snackbar for an empty reset, a one-script Reset→Undo device step, the landscape snackbar check, and the assumption-2 evidence corrected.

## Progress
<!-- replace this comment with one checkbox per plan task; keep this heading (never add a second ## Progress) -->

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
