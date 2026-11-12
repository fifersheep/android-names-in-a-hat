---
task: room-migration-to-v3
title: Open the existing database with Room and migrate it to v3
ticket: NAM-5 — Open the existing database with Room and migrate it to v3 (https://linear.app/northtrack/issue/NAM-5)
ticket-id: NAM-5
tracker-synced: In Engineering
mode: full
phase: implementation
branch: flow/room-migration-to-v3
worktree:
created: 2026-11-11
updated: 2026-11-12
---

## Next action
Baseline verify passed (`./gradlew --no-watch-fs :app:assembleDebug :shared:allTests`, exit 0, 210 tests / 0 failures). Gate 1 approved. Implementation: start plan task 1 (build wiring).

## Decision log
- 2026-11-11 mode: full (user confirmed). Reason: High data-loss risk, adds KMP Room/KSP, two migrations plus fixtures, open design questions.
- 2026-11-11 Q: Room 3 (androidx.room3 3.0.3) or Room 2.8.5? A: Room 3.0.3.
- 2026-11-11 Q: how to support AC7 (2.7.0 → this build on device) with no screens yet? A: defer AC7 to NAM-8 (groups screen). NAM-5 proves migrations with JVM tests only; no `:app` changes. Carry AC7 onto NAM-8 at reporting.
- 2026-11-12 gate 1: plan approved by user

## Progress
- [ ] 1. Build wiring
- [ ] 2. Schema (entities, DAOs, NamesDatabase, 3.json)
- [ ] 3. Migrations and builder
- [ ] 4. Migration tests
- [ ] 5. Repository implementations
- [ ] 6. Threading test
- [ ] 7. Full verification

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
