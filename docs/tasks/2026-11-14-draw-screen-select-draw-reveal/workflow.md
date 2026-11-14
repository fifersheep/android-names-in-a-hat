---
task: draw-screen-select-draw-reveal
title: Build the draw screen: select names, draw, reveal
ticket: NAM-10 — Build the draw screen: select names, draw, reveal (https://linear.app/northtrack/issue/NAM-10/build-the-draw-screen-select-names-draw-reveal)
ticket-id: NAM-10
tracker-synced: In Engineering
mode: full
phase: implementation
branch: flow/draw-screen-select-draw-reveal
worktree: /Users/scoot/dev/names-in-a-hat/android-names-in-a-hat-nam10
created: 2026-11-14
updated: 2026-11-14
---

## Next action
Implementation: BLOCKED until NAM-8 (flow/groups-screen) merges into main. Then `git fetch && git rebase origin/main` in this worktree, check that NAM-8 landed what plan.md assumes (ui/ListStatus.kt, ui/components/{UpButton,HatIllustration}.kt, ic_add/ic_more_vert, action_more, the Draw entry's singlePane and title key), and start plan task 1. Baseline at ff34628: verify PASSED.

## Decision log
<!-- gate entries look like: "- 2026-07-05 gate 1: plan approved by user"
     answered unknowns look like: "- 2026-07-05 Q: per-user or per-team limits? A: per-user"
     log an answer the moment it is given — conversation context does not
     survive a compaction; this file does -->
- 2026-11-14 gate 1: plan approved by user
- 2026-11-14 mode: full (user confirmed). Reason: the app's main screen, with many files; a full-window reveal with motion and a reduced-motion version; a draw result that must survive process death; :shared changes (sort cycle, grid columns retired).
- 2026-11-14 scope: the ticket's 2026-11-12 spec comment overrides the description. Names are wrapping chips (delete GridLayout/GridColumns and PreferencesRepository.gridColumns; AC1's column clause drops). The count control is a stepper [− n +] plus "Draw n". The counter reads "8 of 30 in the hat · 4 drawn" and labels a tri-state select-all checkbox computed over not-drawn names. Sort cycles NONE → ASC → DESC → NONE (update NameSortTypeTest, SortOrderTest, the SortOrder KDoc). MIN_NAMES_TO_DRAW = 1. The reveal is a full-window takeover with a reduced-motion crossfade.
- 2026-11-14 scope: NAM-10 owns NAM-9's AC6 (hand-off comment 2026-11-14, flagged by user). The Draw empty state shows the hat illustration, "This hat is empty", "Add some names to start drawing." and a filled "Add names" button (draw_empty_title / draw_empty_body / draw_empty_action). The button pushes EditList(id) via `backStack.editList(id, fromDraw = true)` in navigation/BackStack.kt. NAM-9 doesn't touch Draw.
- 2026-11-14 Q: where does the NAM-10/NAM-12 line fall? A: drawn-aware, no marking. NAM-10 builds all drawn-aware state and UI: the in-the-hat pool, the "· n drawn" suffix, the tri-state over not-drawn names, the Drawn chip visual, the bar states and the reveal endings. Draw does NOT call markDrawn, so nothing becomes drawn yet. NAM-12 adds markDrawn in the draw step, Reset with undo, and Put back.
- 2026-11-14 Q: how to sequence against the parallel NAM-8 and NAM-9? A: wait for NAM-8. Plan now; start implementation only after NAM-8 (flow/groups-screen) merges, since it owns ic_hat, the ListStatus move and the app-scope DI. Then rebase onto main.

## Progress
<!-- replace this comment with one checkbox per plan task; keep this heading (never add a second ## Progress) -->

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
- 2026-11-14 setup: created the branch in the shared main checkout by mistake. Another session's flow/groups-screen was checked out there, with an untracked task folder. Switched it back within seconds (both branches at ff34628, so no files changed) and moved this task to its own worktree, android-names-in-a-hat-nam10. Other sessions share this repo: work only in this worktree.
