# Review: NAM-6 DataStore preferences migration

One flow-reviewer pass over `main...HEAD` (351581d..c0d0e5c, 17 files), checked against plan.md.

## Blocking
None.

## Advisory
1. **The settings path was produced eagerly, on the caller's thread** (`shared/src/androidMain/.../data/SettingsStorage.android.kt:16`).
   - `dataStoreFile()` calls `getFilesDir()`, which stats the directory and creates it the first time.
   - NAM-7 will build the Hilt singleton on the main thread at launch, so StrictMode would report a disk read there.
   - **Fixed in b1cbfee.** `openSettingsStorage` now takes `producePath: () -> Path` and passes it straight to `OkioStorage`, which evaluates it lazily on the store's scope.
   - The new `SettingsPathThreadingTest` checks two things: creating the store produces no path, and the first read produces it off the caller's thread.
2. **The plan's Verification asked for "the decoded key set from the `.preferences_pb` written by the test", and it hadn't been delivered.**
   - **Done during outcome verification (below).** The file was decoded with an independent protobuf reader, not through DataStore.

## Acceptance criteria
| AC | Verdict | Evidence |
|---|---|---|
| AC1 (adjusted) | MET | `LegacyNameListMigrationTest.kt`, two tests. One seeds through the API, the other writes a raw 2.7.0 XML file. Both check the exact stored key set (no column count), that `name_list.xml` is gone, and that `settings.preferences_pb` exists. Confirmed by the outcome decode below. |
| AC2 | MET | JVM: `PreferencesRepositoryTest` "a new file…" and "the retired grid columns…". Robolectric: "without an old file the defaults apply". |
| AC3 | MET | JVM: `SHUFFLE` written straight into the DataStore. Robolectric: `SHUFFLE` migrated from `name_list`. |
| AC4 | MET | `PreferencesRepositoryTest`, two tests through `GroupEditor` with the real Room storage and the real DataStore. |
| AC5 | MET | `PreferencesThreadingTest`: a recording okio `ForwardingFileSystem` and a named dispatcher; cold reads and warm writes; a non-empty guard. Also `SettingsPathThreadingTest` (from advisory 1). |
| AC6 | DEFERRED | Moved to NAM-8 by user decision 2026-11-14. |
| AC7 | MET | Verify passes with 255 tests and 0 failures (jvmTest 108, testAndroidHostTest 77, iosSimulatorArm64Test 70). The iOS main targets compile. `android.yml` `unit-tests` runs `:shared:testAndroidHostTest`. |
| Extra: atomicity | MET | 50 concurrent calls give 25 ASC and 25 DESC, and the final value is DESC. A mutation check (a non-atomic `updateSortType`) fails this test. |
| Extra: corruption | MET | A garbage file reads as the defaults and can be written again. |
| Extra: persistence | MET | Values survive `closeAndJoin` and reopening. |

## Checked (reviewer)
- **Weakened verification:** none. The CI and Gradle changes are additive only, and `robolectric.properties` is guarded by `RobolectricSetupTest`.
- **Scope creep:** none. Every file traces to Tasks 1–5.
- **Deviations:** both are logged. One cosmetic rename is unlogged: the plan's `buildSettingsStore` is implemented as `openSettingsStorage`.
- **Library behaviour, against the DataStore 1.3.0 sources:**
  - `getSharedPreferences` is lazy and runs on the store's scope.
  - With no legacy file, no file is created.
  - `cleanUp` deletes the file once it is empty, even though only allow-listed keys were copied.
  - A retry is idempotent.
- **Legacy formats** were compared with `master`. The key names and value types match.

## Outcome verification
- **Machine.** After the fix, `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests` passes with 255 tests and 0 failures, counted from the test-result XML. Locally, the CI `unit-tests` command `:shared:jvmTest :shared:testAndroidHostTest :app:testDebugUnitTest` passes.
- **Driven end to end.** There is no UI or wiring yet (NAM-7/8), so the production `SettingsStorage(context)` was driven under Robolectric's real framework `ContextImpl` by a throwaway capture test, which was deleted afterwards.
  - It wrote a hand-made 2.7.0 `shared_prefs/name_list.xml` (`active_group_id`=12, column count 1, sorts 12=DESC, 5=ASC, 31=NONE), then opened the store, read the values back and closed it.
  - Files before: `[shared_prefs/name_list.xml]`. Files after: `[files/datastore/settings.preferences_pb]`.
  - Read back: `activeGroupId=12 sort12=DESC sort5=ASC sort31=NONE sort99=NONE gridColumns=2`.
  - The written `settings.preferences_pb` (141 bytes) was decoded with an independent protobuf reader:
    - `group_sort_type_for_group_31 = string 'NONE'`
    - `group_sort_type_for_group_12 = string 'DESC'`
    - `group_sort_type_for_group_5 = string 'ASC'`
    - `active_group_id = long 12`
  - That is no `current_grid_column_count`, a Long kept as a Long, and the legacy file deleted.
- **Not done here:** the device install-over check from 2.7.0 (AC6, deferred to NAM-8).
