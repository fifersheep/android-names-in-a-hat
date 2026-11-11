---
task: modernise-ci
title: Bring CI up to date for the new build
ticket: NAM-3: Bring CI up to date for the new build (https://linear.app/northtrack/issue/NAM-3/bring-ci-up-to-date-for-the-new-build)
ticket-id: NAM-3
tracker-synced: In Engineering
mode: lite
phase: implementation
branch: flow/modernise-ci
worktree:
created: 2026-11-11
updated: 2026-11-11
---

## Next action
Implement plan.md task 1 (rewrite `.github/workflows/android.yml`). Baseline 2026-11-11: `./gradlew :app:assembleDebug :shared:allTests` PASS on flow/modernise-ci @ 82285a1.

## Decision log
<!-- gate entries look like: "- 2026-07-05 gate 1: plan approved by user"
     answered unknowns look like: "- 2026-07-05 Q: per-user or per-team limits? A: per-user"
     log an answer the moment it is given — conversation context does not
     survive a compaction; this file does -->
- 2026-11-11 mode: lite (user confirmed). Ticket spec already settles the design; change is mostly `.github/workflows/android.yml`.
- 2026-11-11 Q: how to prove AC2/AC3 (red on failing test / compile error)? A: temp commits on the real PR, record red run links, then revert.
- 2026-11-11 Q: make the new jobs required status checks on `main`? A: yes — after they've run once, with user's OK at that moment.
- 2026-11-11 iOS frequency: every PR + push to main — repo is PUBLIC so standard macOS minutes are free.
- 2026-11-11 plan-critic: no Blocking; advisories folded in (citations, squash-merge required, wait for red runs, no job `name:`).
- 2026-11-11 gate 1: plan approved by user

## Progress
- [ ] 1. Rewrite `.github/workflows/android.yml` (four jobs, frequency comment)
- [ ] 2. Push branch, open draft PR, all four jobs green (AC1)
- [ ] 3. Red proofs: failing commonTest (AC2), iosMain compile error (AC3), reverted
- [ ] 4. Required status checks on `main` (AC6) — needs user OK

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
