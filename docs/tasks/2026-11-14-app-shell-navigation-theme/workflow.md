---
task: app-shell-navigation-theme
title: Build the app shell: single activity, navigation, theme and insets
ticket: NAM-7 — Build the app shell: single activity, navigation, theme and insets (https://linear.app/northtrack/issue/NAM-7/build-the-app-shell-single-activity-navigation-theme-and-insets)
ticket-id: NAM-7
tracker-synced: In Engineering
mode: full
phase: research
branch: flow/app-shell-navigation-theme
worktree:
created: 2026-11-14
updated: 2026-11-14
---

## Next action
HOLD: research done (research.md). Waiting for NAM-6 (DataStore PreferencesRepository, being built by another agent) to merge into main. When it has merged: rebase flow/app-shell-navigation-theme onto main, add the NAM-6 implementation's Hilt binding to research.md findings, then set phase: planning and run flow-plan. Baseline 2026-11-14 @ 271fd7b: `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests` PASS, 234 tests / 0 failures. NAM-7 → In Engineering.

## Decision log
- 2026-11-14 Q: what to bind for PreferencesRepository before NAM-6? A: another agent is building NAM-6 now. Wait for it to merge and use the real implementation; no fake.
- 2026-11-14 Q: stale active-list id vs not gating the splash on the DB? A: route optimistically. The splash waits for the prefs read only; Draw pops to Lists and clears the id if the list is gone.
- 2026-11-14 mode: full (user confirmed). Reason: new libraries (Hilt, Navigation 3, material3 1.5 RC, adaptive), many files, and open design questions (list-detail scenes, process-death restore, launch routing).

## Progress
<!-- replace this comment with one checkbox per plan task; keep this heading (never add a second ## Progress) -->

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
