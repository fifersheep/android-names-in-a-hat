---
task: edit-names-screen
title: Build the edit names screen
ticket: NAM-9 — Build the edit names screen (https://linear.app/northtrack/issue/NAM-9/build-the-edit-names-screen)
ticket-id: NAM-9
tracker-synced: In Engineering
mode: full
phase: planning
branch: flow/edit-names-screen
worktree: ../android-names-in-a-hat-nam-9
created: 2026-11-14
updated: 2026-11-14
---

## Next action
BLOCKED on NAM-8 (user decision 2026-11-14). Research is done (research.md). When NAM-8 (flow/groups-screen) has merged to main: rebase flow/edit-names-screen onto main, re-run the baseline verify, re-read NAM-8's Edit list screen and ViewModel (the placeholder code cited in research.md will have changed), then write plan.md for the names section only (spec split; AC6 → NAM-10). The earlier baseline was GREEN at ff34628 (300 tests, 0 failures).

## Decision log
<!-- gate entries look like: "- 2026-07-05 gate 1: plan approved by user"
     answered unknowns look like: "- 2026-07-05 Q: per-user or per-team limits? A: per-user"
     log an answer the moment it is given — conversation context does not
     survive a compaction; this file does -->
- 2026-11-14 mode: full (user confirmed). Reason: new Compose screen + ViewModel, delayed-delete undo, inline rename, scope overlap with NAM-8 (screen merged with list creation) and NAM-10 (draw empty state link, AC6).
- 2026-11-14 Q: who builds the shared Edit list screen while NAM-8 runs in parallel? A: wait for NAM-8 to merge first, then build the names section (spec split) on its real screen.
- 2026-11-14 Q: AC6 (draw empty state → Edit list)? A: hand AC6 to NAM-10; NAM-9 does not touch Draw.
- 2026-11-14 isolation: git worktree at ../android-names-in-a-hat-nam-9. A parallel session is running NAM-8 (flow/groups-screen) in the main checkout.

## Progress
<!-- replace this comment with one checkbox per plan task; keep this heading (never add a second ## Progress) -->

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
- 2026-11-14 setup briefly checked out flow/edit-names-screen in the main checkout while the NAM-8 session had flow/groups-screen there (no commits, no file changes; restored within a minute). NAM-9 moved to a worktree.
