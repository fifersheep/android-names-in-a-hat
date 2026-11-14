---
task: datastore-preferences-migration
title: Replace SharedPreferences with DataStore and migrate existing settings
ticket: NAM-6 — Replace SharedPreferences with DataStore and migrate existing settings (https://linear.app/northtrack/issue/NAM-6/replace-sharedpreferences-with-datastore-and-migrate-existing-settings)
ticket-id: NAM-6
tracker-synced: In Engineering
mode: full
phase: reporting
branch: flow/datastore-preferences-migration
worktree: ../android-names-in-a-hat-nam-6
created: 2026-11-14
updated: 2026-11-14
---

## Next action
Code review done 2026-11-14 (review.md): 0 blocking, 2 advisory, both addressed. The lazy settings path is fixed in b1cbfee, and the decoded .preferences_pb is captured. Verify PASS: 255 tests / 0 failures. Outcome driven under Robolectric with the production factory: a 2.7.0 XML file becomes settings.preferences_pb with the 4 expected keys and the old file deleted. Next: write report.md, open the PR, move NAM-6 to In Review, then gate 2.

## Decision log
- 2026-11-14 mode: full (user confirmed). Reason: new multiplatform library (DataStore in :shared), Android-only SharedPreferencesMigration wiring, open questions on per-platform file location and how a group delete reaches the sort key.
- 2026-11-14 isolation: git worktree at ../android-names-in-a-hat-nam-6 (user chose), so NAM-7 (research, uncommitted task folder) stays untouched in the main checkout.
- 2026-11-14 scope (ticket comment 2026-11-12, from the NAM-2 spec): the 1/2 column toggle is retired. Do not migrate `current_grid_column_count` and drop it from the DataStore implementation; NAM-10 removes it from `PreferencesRepository`. Per-group sort order is unchanged. This overrides ACs 1, 2 and 6 where they mention columns.
- 2026-11-14 Q: AC6 (device check: 2.7.0 then this build over it) needs :app wiring, which is NAM-7's. A: defer it to NAM-8's install-over check, as with NAM-5's AC7. No :app changes in NAM-6; prove the migration with a Robolectric test on a 2.7.0-format name_list.xml. Add the settings check to NAM-8 at reporting.
- 2026-11-14 Q: AC1 test route? A: Robolectric (4.17, not 4.18, which is one day old) host tests in :shared androidHostTest, and add the Android host test task to CI's unit-tests job.
- 2026-11-14 gate 1: plan approved by user

## Progress
- [x] 1. Dependencies and test setup (DataStore 1.3.0, Robolectric 4.17 host tests, smoke test) — 6818fd5 (235 tests, 0 failures)
- [x] 2. Repository and builder, test-first (AC2, AC3, AC4, persistence, atomicity, corruption) — 4cb0af1 (12 tests; mutation check: a non-atomic updateSortType fails the atomicity test)
- [x] 3. Threading proof (AC5) — e8eb880 (1 test, jvmTest 107)
- [x] 4. Android factory and legacy migration (AC1, plus AC2 and AC3 on Android) — 2162b90 (6 Robolectric tests; mutation check: copying every key or overwriting existing keys fails 4 of them)
- [x] 5. CI and full verify (AC7) — 37290e9 (verify + iOS compile PASS: 254 tests / 0 failures = jvmTest 107, testAndroidHostTest 77, iosSimulatorArm64Test 70; CI unit-tests command PASS locally)

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
- 2026-11-14 Task 4: the path comes from `context.dataStoreFile(SETTINGS_FILE_NAME)`, not `preferencesDataStoreFile("settings")`. It is the same file (`files/datastore/settings.preferences_pb`), with one name constant shared with the commonMain builder.
- 2026-11-14 Task 2: `openSettingsStorage` takes `fileSystem` with no default, so commonMain doesn't depend on `FileSystem.SYSTEM` being in okio's common API. The Android factory and the JVM tests pass `FileSystem.SYSTEM`.
