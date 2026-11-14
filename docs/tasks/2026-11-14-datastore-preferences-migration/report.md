# Report: replace SharedPreferences with DataStore and migrate existing settings (NAM-6)

## Changed vs plan
All 5 plan tasks are done ([plan.md](plan.md), Progress in [workflow.md](workflow.md)). Every acceptance criterion is met except AC6, which the user deferred to NAM-8. The column parts of AC1, AC2 and AC6 were dropped by the 2026-11-12 ticket comment, because the column toggle is retired.

| Task | Commit | What |
|---|---|---|
| 1 Dependencies and test setup | 6818fd5 | DataStore 1.3.0: `datastore-preferences-core` in commonMain, `datastore-preferences` in androidMain. Robolectric 4.17, androidx.test core 1.7.0 and JUnit 4.13.2 in `androidHostTest`. `robolectric.properties` sets `sdk=34`, guarded by `RobolectricSetupTest`. |
| 2 Repository and builder | 4cb0af1 | `DataStorePreferencesRepository` uses the legacy key names and types, and each write is a single `edit`. `SettingsStorage` mirrors `NamesStorage`. `openSettingsStorage` builds `OkioStorage` with an injectable `FileSystem`, `ReplaceFileCorruptionHandler` and an app-lifetime IO scope. `gridColumns` is retired (constant 2, no-op setter). 12 JVM tests. |
| 3 Threading proof | e8eb880 | `PreferencesThreadingTest` uses a recording okio `ForwardingFileSystem` and a named dispatcher. Cold reads and warm writes all do their file work off the caller's thread. |
| 4 Android factory and migration | 2162b90 | `SettingsStorage(context)` writes `files/datastore/settings.preferences_pb`. `legacyNameListMigration` is the generic `SharedPreferencesMigration`: it copies `active_group_id` and every `group_sort_type_for_group_*`, never overwrites existing keys, and deletes `name_list.xml`. 6 Robolectric tests. |
| 5 CI and verify | 37290e9 | CI's `unit-tests` job runs `:shared:testAndroidHostTest`. |
| Review fix | b1cbfee | The settings path is produced lazily on the store's scope, so no `getFilesDir()` disk access happens when the store is created. Adds `SettingsPathThreadingTest`. |

## Deviations
- **The path comes from `context.dataStoreFile(SETTINGS_FILE_NAME)`, not `preferencesDataStoreFile("settings")`.** It is the same file, `files/datastore/settings.preferences_pb`. One name constant is now shared by the commonMain builder, the JVM tests and the Android factory, so the name can't drift between them.
- **`openSettingsStorage` takes a `FileSystem` with no default.** commonMain then doesn't depend on `FileSystem.SYSTEM` being available in okio's common API. The Android factory and the JVM tests pass `FileSystem.SYSTEM`.
- **The plan's `buildSettingsStore(...)` became `openSettingsStorage(...)`, and it returns the `SettingsStorage`.** This is a cosmetic name change that matches `openStorage` in the NAM-5 tests (the reviewer noted it, unlogged).
- **Review fix (not in the plan): the path is produced lazily.** The plan passed an eager `path`. On Android that runs `getFilesDir()`, which stats and may create a directory, on whichever thread builds the store, and NAM-7 will build it on the main thread at launch. The plan's AC5 covers reads and writes only, so the reviewer flagged this as advisory. Fixing it was cheap: `producePath: () -> Path` goes straight into `OkioStorage`, which evaluates it lazily.

## Evidence
**Verify** at 68dc540 (re-run for this report, with the iOS compile and lint added):
```
$ ./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64 :app:lintDebug
BUILD SUCCESSFUL in 1m 8s
$ (sums of shared/build/test-results/<target>/*.xml)
jvmTest 108 0
testAndroidHostTest 77 0
iosSimulatorArm64Test 70 0
total 255 fail 0                  # baseline on main @ 271fd7b: 234
```
- Lint passes with 37 warnings. All but one existed before NAM-6 (`UnusedResources` 28, `ObsoleteSdkInt` 2, and so on).
- The new one is `NewerVersionAvailable` for Robolectric 4.18. That is deliberate: 4.18 was released 2026-11-13, one day before this work.
- The CI `unit-tests` command run locally, `./gradlew --no-watch-fs :shared:jvmTest :shared:testAndroidHostTest :app:testDebugUnitTest`, gives BUILD SUCCESSFUL.

**Mutation checks** (temporary code changes, reverted afterwards):
- With a non-atomic `updateSortType` (read the flow, then `edit`), `concurrent sort updates are atomic` fails.
- With a migration that copies every Long/String key and overwrites existing keys, 4 of the 6 `LegacyNameListMigrationTest` tests fail.

**Outcome: the production factory on a 2.7.0 file.** There is no UI or DI wiring yet (NAM-7/8). A throwaway Robolectric test, since deleted, drove `SettingsStorage(context)` against the real framework `ContextImpl`. It wrote a 2.7.0-format `shared_prefs/name_list.xml`:
```xml
<long name="active_group_id" value="12" />
<long name="current_grid_column_count" value="1" />
<string name="group_sort_type_for_group_12">DESC</string>
<string name="group_sort_type_for_group_5">ASC</string>
<string name="group_sort_type_for_group_31">NONE</string>
```
It then opened the store, read the values back through `PreferencesRepository` and closed it:
```
files before: [shared_prefs/name_list.xml]
read back: activeGroupId=12 sort12=DESC sort5=ASC sort31=NONE sort99=NONE gridColumns=2
files after: [files/datastore/settings.preferences_pb]
```
The written `settings.preferences_pb` was decoded with an independent protobuf reader, not through DataStore:
```
141 bytes
group_sort_type_for_group_31 = string 'NONE'
group_sort_type_for_group_12 = string 'DESC'
group_sort_type_for_group_5 = string 'ASC'
active_group_id = long 12
```
The active group stays a Long, every sort key carries over unchanged, there is no column count, and the legacy file is gone.

**Review:** one flow-reviewer pass found 0 blocking and 2 advisory issues, both addressed. Every AC is MET apart from AC6, which is DEFERRED. See [review.md](review.md).

## Follow-ups
- **NAM-8: the settings part of the install-over check** (AC6, user decision).
  - On 2.7.0, open a group and sort it Z–A, then install the NAM-8 debug build over it.
  - The app should reopen that group sorted Z–A.
  - The column count is intentionally not carried over.
- **NAM-7: wiring.**
  - Provide `SettingsStorage(context)` as an app-lifetime Hilt singleton. Never create two in one process: the second throws on first use.
  - Creating it is cheap and main-safe.
  - Handle a dangling `activeGroupId` at launch.
  - Decide what launch does if a settings read throws an `IOException`. Corruption is already handled by replacing the file with empty settings.
- **NAM-13: backup rules.**
  - Auto Backup includes `files/datastore/settings.preferences_pb` by default.
  - A restored 2.x `name_list.xml` re-migrates harmlessly: newer keys are never overwritten, and the file is deleted again.
- **NAM-10:** delete `PreferencesRepository.gridColumns`/`setGridColumns`, `GridLayout` and `GridColumns`, as the spec hands off. The DataStore implementation already ignores them.
- **Later:**
  - Bump Robolectric to 4.18 once it has been out for a while (lint flags it).
  - If CI's `unit-tests` slows down from Robolectric downloading its SDK 34 jar on cold runners, cache `~/.m2/repository/org/robolectric`.
  - An iOS `SettingsStorage` factory when there is an iOS app. Use Application Support with `create = true`, and an absolute path.
