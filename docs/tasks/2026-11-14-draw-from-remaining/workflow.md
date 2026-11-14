---
task: draw-from-remaining
title: Draw additional names from those remaining (drawn state persisted per group)
ticket: NAM-12 — Draw additional names from those remaining (https://linear.app/northtrack/issue/NAM-12/draw-additional-names-from-those-remaining)
ticket-id: NAM-12
tracker-synced: In Engineering
mode: full
phase: planning
branch: flow/draw-from-remaining
worktree: ../android-names-in-a-hat-nam-12
created: 2026-11-14
updated: 2026-11-14
---

## Next action
Planning: write plan.md from research.md. Scope: a :shared draw+mark use case, a per-name clearDrawn(ids), Reset with an undo snackbar (top bar and AllDrawn bar), Put back on drawn chips (menu plus a11y action), and flipping `a draw never marks names drawn`. Then run the plan critic → gate 1. Baseline verify at 42bc63a PASSES: app 146/0, shared 291/0, lint clean.

## Decision log
- 2026-11-14 mode: full, confirmed by the user. It spans :shared draw logic, a new per-name un-draw in NameRepository/Room, and draw-screen UI, with 11 ACs.
- 2026-11-14 isolation: a worktree at ../android-names-in-a-hat-nam-12, branched from origin/main @ 42bc63a (NAM-10 merged). The main checkout is shared with other sessions and is behind origin, so it was left alone.
- 2026-11-14 design inputs (NAM-12 comment from the owner, 2026-11-12): always on. Reset goes in the top bar with an undo snackbar and no confirmation. Drawn names get strikethrough, a ticket icon and the TalkBack state "drawn". "Put back in the hat" clears drawn and sets selected, and needs a new per-name clearDrawn(ids) in NameRepository/RoomNameRepository. Counter, stepper and draw use the "in the hat" pool (selected and not drawn), minimum 1. "Everyone's been drawn" + Reset shows only when the hat is empty and at least one selected name is drawn. Spec sections: docs/design/3.0-spec.md#draw-from-remaining and #chip-states.

- 2026-11-14 research: no shape-changing questions, since spec D2/D7/D8/D9 and the owner's NAM-12 comment settle behaviour. Defaults (use-case placement, put-back atomicity, undo semantics and lifetime) are recorded in research.md and carry into plan.md Assumptions. AC11 (close GitHub #16) is deferred to wrap, with the user's OK.

## Progress
<!-- replace this comment with one checkbox per plan task; keep this heading (never add a second ## Progress) -->

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
