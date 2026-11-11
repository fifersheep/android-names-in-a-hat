---
task: unify-trunk-modern-build
title: Unify the trunk and stand up the modern build
ticket: NAM-1 — Unify the trunk and stand up the modern build (https://linear.app/northtrack/issue/NAM-1)
ticket-id: NAM-1
tracker-synced: In Engineering
mode: full
phase: implementation
branch: flow/unify-trunk-modern-build
worktree:
created: 2026-11-11
updated: 2026-11-11
---

## Next action
Implement plan.md Task 1 (the `-s ours` merge of origin/master), then Tasks 2–7 in order, committing per task. Baseline verify (`./gradlew :app:assembleDebug :shared:allTests`) is RED: `Project 'shared' not found` (expected, because `:shared` doesn't exist yet). `:app:assembleDebug` alone is GREEN on main with JDK 17.0.21. This contradicts the ticket's "nothing can be built" for main.

## Decision log
- 2026-11-11 mode: full (confirmed by user): history merge, new KMP module, build-stack decisions
- 2026-11-11 Q: .flow.md? A: save with verify `./gradlew :app:assembleDebug :shared:allTests`, tracker linear, start→In Engineering / pr-open→In Review / merged→Done
- 2026-11-11 Q: uncommitted .idea changes on main? A: discard (restored gradle.xml, misc.xml, vcs.xml)
- 2026-11-11 Q: CI is JDK 11 and the PR will go red; what to do? A: minimal JDK bump to 17 (+ fix the test task name) in NAM-1 so the PR is green; full CI rework stays in NAM-3; log as a deviation from the ticket's scope
- 2026-11-11 Q: versionCode for 3.0.0? A: 2000000002, +1 per release (Play ceiling 2100000000)
- 2026-11-11 Q: main requires linear history (enforced for admins), so squash/rebase would drop the 2.7.0 parent. How to land it? A: at gate 2, switch the linear-history rule off for this PR, merge with a MERGE COMMIT, then switch it back on. Each gh protection change needs the user's OK at that moment
- 2026-11-11 gate 1: plan approved by user

## Progress
- [ ] 1. Merge history (-s ours origin/master)
- [ ] 2. Clear the old skeleton
- [ ] 3. Gradle skeleton (wrapper 9.7.1, catalog, settings, SDK packages)
- [ ] 4. `:shared` module
- [ ] 5. `:app` module
- [ ] 6. CI minimal bump
- [ ] 7. Device + signing checks

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
