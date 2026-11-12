---
task: room-migration-to-v3
title: Open the existing database with Room and migrate it to v3
ticket: NAM-5 — Open the existing database with Room and migrate it to v3 (https://linear.app/northtrack/issue/NAM-5)
ticket-id: NAM-5
tracker-synced: In Review
mode: full
phase: reporting
branch: flow/room-migration-to-v3
worktree:
created: 2026-11-11
updated: 2026-11-12
---

## Next action
Baseline verify passed (`./gradlew --no-watch-fs :app:assembleDebug :shared:allTests`, exit 0, 210 tests / 0 failures). Review done (review.md: 0 blocking, 1 advisory fixed in 5ea9261; outcome verified on an API 26 SQLite-written file). Verify green at 5ea9261: 234 tests / 0 failures. report.md written; PR #25 open (https://github.com/fifersheep/android-names-in-a-hat/pull/25); NAM-5 → In Review; AC7 carried to NAM-8 as a comment. Awaiting gate 2 (merge approval).

## Decision log
- 2026-11-11 mode: full (user confirmed). Reason: High data-loss risk, adds KMP Room/KSP, two migrations plus fixtures, open design questions.
- 2026-11-11 Q: Room 3 (androidx.room3 3.0.3) or Room 2.8.5? A: Room 3.0.3.
- 2026-11-11 Q: how to support AC7 (2.7.0 → this build on device) with no screens yet? A: defer AC7 to NAM-8 (groups screen). NAM-5 proves migrations with JVM tests only; no `:app` changes. Carry AC7 onto NAM-8 at reporting.
- 2026-11-12 gate 1: plan approved by user

## Progress
- [x] 1. Build wiring — f117ba6
- [x] 2. Schema (entities, DAOs, NamesDatabase, 3.json) — 513a6ca
- [x] 3. Migrations and builder — 715e585
- [x] 4. Migration tests — 6aee98f (8 tests green; mutation check: dropping the seq carry, NULL→1, and wrong group id each fail 4–6 tests)
- [x] 5. Repository implementations — 44ad143 (done before task 4; 13 tests green; FK cascade confirmed enforced by Room 3, open question closed, no fallback needed)
- [x] 6. Threading test — b5c9128 (mutation check: dropping setQueryCoroutineContext fails it)
- [x] 7. Full verification — no code change; `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :shared:compileKotlinIosArm64 :app:lintDebug` BUILD SUCCESSFUL, 232 tests / 0 failures / 0 errors (baseline 210), MigrationTest 8 + RoomRepositoryTest 13 + DatabaseThreadingTest 1 XML present; no fallbackToDestructive; 3.json tracked

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
- 2026-11-12 task 2: added `-Xexpect-actual-classes` to `:shared` compilerOptions (Room's `expect object` constructor warns without it). Entities/DAOs stayed `internal`; KSP accepted it.
- 2026-11-12 order: task 5 (repositories + `NamesStorage` facade + `NamesStorage(context)`) runs before task 4, because task 4's end-to-end tests read through the repositories. Task 3 holds only migrations, `DATABASE_NAME` and `buildNamesDatabase` (file `DatabaseBuilder.kt` instead of `NamesStorage.kt`).
- 2026-11-12 task 5: added jvmTest helper `TestDatabases.kt` (`openStorage(dir, driver, queryContext)`), shared by tasks 4–6; not in the plan's file list.
