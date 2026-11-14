---
task: app-shell-navigation-theme
title: Build the app shell: single activity, navigation, theme and insets
ticket: NAM-7 — Build the app shell: single activity, navigation, theme and insets (https://linear.app/northtrack/issue/NAM-7/build-the-app-shell-single-activity-navigation-theme-and-insets)
ticket-id: NAM-7
tracker-synced: In Engineering
mode: full
phase: implementation
branch: flow/app-shell-navigation-theme
worktree:
created: 2026-11-14
updated: 2026-11-14
---

## Next action
Gate 1 passed. Next: re-run the baseline on the rebased branch, then task 1 (build dependencies). Baseline 2026-11-14 @ 271fd7b: `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests` PASS, 234 tests / 0 failures.

## Decision log
- 2026-11-14 gate 1: plan approved by user (including removing Room's unused MultiInstanceInvalidationService from the merged manifest for AC1)
- 2026-11-14 Q: what to bind for PreferencesRepository before NAM-6? A: another agent is building NAM-6 now. Wait for it to merge and use the real implementation; no fake.
- 2026-11-14 Q: stale active-list id vs not gating the splash on the DB? A: route optimistically. The splash waits for the prefs read only; Draw pops to Lists and clears the id if the list is gone.
- 2026-11-14 mode: full (user confirmed). Reason: new libraries (Hilt, Navigation 3, material3 1.5 RC, adaptive), many files, and open design questions (list-detail scenes, process-death restore, launch routing).

## Progress
- [ ] 1. Build dependencies
- [ ] 2. Theme and font
- [ ] 3. Icon, splash and manifest
- [ ] 4. DI
- [ ] 5. Navigation model
- [ ] 6. Launch
- [ ] 7. NavDisplay and placeholders
- [ ] 8. Device verification

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
