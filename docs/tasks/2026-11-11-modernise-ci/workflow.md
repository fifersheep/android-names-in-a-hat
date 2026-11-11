---
task: modernise-ci
title: Bring CI up to date for the new build
ticket: NAM-3: Bring CI up to date for the new build (https://linear.app/northtrack/issue/NAM-3/bring-ci-up-to-date-for-the-new-build)
ticket-id: NAM-3
tracker-synced: In Engineering
mode: lite
phase: code-review
branch: flow/modernise-ci
worktree:
created: 2026-11-11
updated: 2026-11-11
---

## Next action
Review done (review.md: no Blocking, all 6 AC MET; konan cache key fixed in 5ff21e3, run 43103836715 green, PR #23 CLEAN). Next: flow-wrap (lite) — learnings, mark complete, gate 2, then `gh pr ready 23` + `gh pr merge 23 --squash` only with user approval; re-check head is green first. Baseline 2026-11-11: `./gradlew :app:assembleDebug :shared:allTests` PASS on flow/modernise-ci @ 82285a1.

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
- 2026-11-11 Q: apply required status checks on `main` now? A: yes — applied (lint, unit-tests, assemble, ios-compile; strict false)

## Progress
- [x] 1. Rewrite `.github/workflows/android.yml` (four jobs, frequency comment) — 851b5bd
- [x] 2. Push branch, open draft PR, all four jobs green (AC1) — PR #23, run https://github.com/fifersheep/android-names-in-a-hat/actions/runs/43085406431 (all 4 pass)
- [x] 3. Red proofs: failing commonTest (AC2), iosMain compile error (AC3), reverted
  - AC2: fb601c6 → https://github.com/fifersheep/android-names-in-a-hat/actions/runs/43085988075 — `unit-tests` failure (`AppInfoTest[jvm] > appNameMatchesThePlayListing[jvm] FAILED`), other 3 green
  - AC3: c342bee → https://github.com/fifersheep/android-names-in-a-hat/actions/runs/43086529337 — `ios-compile` failure (`Broken.kt:4:17 Initializer type mismatch`), other 3 green
  - cleanup: Broken.kt removed in c5dbc64; final head green → https://github.com/fifersheep/android-names-in-a-hat/actions/runs/43087070902 (all 4 pass)
- [x] 4. Required status checks on `main` (AC6) — user OK 2026-11-11; contexts = lint, unit-tests, assemble, ios-compile (strict: false); PR #23 mergeStateStatus CLEAN

## Quick report
- Changed: `.github/workflows/android.yml` rewritten as `lint`, `unit-tests`, `assemble` (ubuntu) and `ios-compile` (macOS, ~/.konan cached on the Kotlin version), JDK 17, current action majors, no secrets; `main` now requires those four checks. PR #23.
- Verify: `./gradlew :app:assembleDebug :shared:allTests` → `BUILD SUCCESSFUL in 3s` (after 9872c5d). CI on 5ff21e3: run 43103836715, lint/unit-tests/assemble/ios-compile all success.
- Red proofs: AC2 run 43085988075 (`AppInfoTest[jvm] > appNameMatchesThePlayListing[jvm] FAILED`), AC3 run 43086529337 (`Broken.kt:4:17 Initializer type mismatch`). Both temp changes reverted; `git diff origin/main HEAD -- shared/ app/` empty.
- Review: no Blocking; all 6 AC MET (review.md). Deviations below: comment rewording, temp-commit cleanup by restore not revert. Merge must be **squash**.

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
- 2026-11-11 task 1: the workflow comment originally said "niah* properties", which tripped the AC4 grep; reworded to "keystore properties" before the commit.
- 2026-11-11 task 3: the AC2 temp commit (fb601c6) also swept in the workflow.md progress tick (`commit -a`), so it was undone by restoring the test file in the AC3 commit, not by `git revert`. The AC3 commit both restored the test and added Broken.kt, so its run isolates the iOS failure (unit-tests green). Squash-merge still drops all of it from `main`.
