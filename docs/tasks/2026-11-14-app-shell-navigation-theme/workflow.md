---
task: app-shell-navigation-theme
title: Build the app shell: single activity, navigation, theme and insets
ticket: NAM-7 — Build the app shell: single activity, navigation, theme and insets (https://linear.app/northtrack/issue/NAM-7/build-the-app-shell-single-activity-navigation-theme-and-insets)
ticket-id: NAM-7
tracker-synced: In Engineering
mode: full
phase: planning
branch: flow/app-shell-navigation-theme
worktree:
created: 2026-11-14
updated: 2026-11-14
---

## Next action
Research done (research.md, including the NAM-6 section). NAM-6 merged as PR #27, and the branch was rebased onto main @ c25a883 on 2026-11-14. Next: write plan.md, run the plan-critic, then hold at gate 1. Baseline 2026-11-14 @ 271fd7b: `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests` PASS, 234 tests / 0 failures (re-run on the rebased branch during planning).

## Decision log
- 2026-11-14 Q: what to bind for PreferencesRepository before NAM-6? A: another agent is building NAM-6 now. Wait for it to merge and use the real implementation; no fake.
- 2026-11-14 Q: stale active-list id vs not gating the splash on the DB? A: route optimistically. The splash waits for the prefs read only; Draw pops to Lists and clears the id if the list is gone.
- 2026-11-14 mode: full (user confirmed). Reason: new libraries (Hilt, Navigation 3, material3 1.5 RC, adaptive), many files, and open design questions (list-detail scenes, process-death restore, launch routing).

## Progress
<!-- replace this comment with one checkbox per plan task; keep this heading (never add a second ## Progress) -->

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
