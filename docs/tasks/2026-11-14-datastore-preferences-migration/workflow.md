---
task: datastore-preferences-migration
title: Replace SharedPreferences with DataStore and migrate existing settings
ticket: NAM-6 — Replace SharedPreferences with DataStore and migrate existing settings (https://linear.app/northtrack/issue/NAM-6/replace-sharedpreferences-with-datastore-and-migrate-existing-settings)
ticket-id: NAM-6
tracker-synced: In Engineering
mode: full
phase: implementation
branch: flow/datastore-preferences-migration
worktree: ../android-names-in-a-hat-nam-6
created: 2026-11-14
updated: 2026-11-14
---

## Next action
Gate 1 passed 2026-11-14. Baseline at setup: verify PASS, 234 tests / 0 failures @ 271fd7b. Next: implement plan.md Task 1 (dependencies, Robolectric host-test setup and smoke test).

## Decision log
- 2026-11-14 mode: full (user confirmed). Reason: new multiplatform library (DataStore in :shared), Android-only SharedPreferencesMigration wiring, open questions on per-platform file location and how a group delete reaches the sort key.
- 2026-11-14 isolation: git worktree at ../android-names-in-a-hat-nam-6 (user chose), so NAM-7 (research, uncommitted task folder) stays untouched in the main checkout.
- 2026-11-14 scope (ticket comment 2026-11-12, from the NAM-2 spec): the 1/2 column toggle is retired. Do not migrate `current_grid_column_count` and drop it from the DataStore implementation; NAM-10 removes it from `PreferencesRepository`. Per-group sort order is unchanged. This overrides ACs 1, 2 and 6 where they mention columns.
- 2026-11-14 Q: AC6 (device check: 2.7.0 then this build over it) needs :app wiring, which is NAM-7's. A: defer it to NAM-8's install-over check, as with NAM-5's AC7. No :app changes in NAM-6; prove the migration with a Robolectric test on a 2.7.0-format name_list.xml. Add the settings check to NAM-8 at reporting.
- 2026-11-14 Q: AC1 test route? A: Robolectric (4.17, not 4.18, which is one day old) host tests in :shared androidHostTest, and add the Android host test task to CI's unit-tests job.
- 2026-11-14 gate 1: plan approved by user

## Progress
- [ ] 1. Dependencies and test setup (DataStore 1.3.0, Robolectric 4.17 host tests, smoke test)
- [ ] 2. Repository and builder, test-first (AC2, AC3, AC4, persistence, atomicity, corruption)
- [ ] 3. Threading proof (AC5)
- [ ] 4. Android factory and legacy migration (AC1, plus AC2 and AC3 on Android)
- [ ] 5. CI and full verify (AC7)

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
