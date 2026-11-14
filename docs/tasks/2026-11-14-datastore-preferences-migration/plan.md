# Plan — Replace SharedPreferences with DataStore and migrate existing settings

## Intent
3.0 updates the 2.7.0 install in place, so the settings 2.7.0 kept in the `name_list` SharedPreferences file must carry over:
- the active group
- each group's sort order

This change implements `PreferencesRepository` (NAM-4) on a Preferences DataStore in `:shared`. An Android-only, one-time migration copies the 2.x settings keys (`active_group_id` and every `group_sort_type_for_group_*` key; the retired column count is not copied), then deletes the old file.

## Acceptance criteria
<!-- Ticket ACs, adjusted by the 2026-11-12 ticket comment (the column toggle is retired) and the 2026-11-14 decisions. -->
- [ ] **AC1 (adjusted).** A Robolectric test seeds `name_list` with `active_group_id`=7L, `current_grid_column_count`=1L, and sort orders `ASC` for group 3 and `DESC` for group 4.
  - After the first read of a `SettingsStorage(context)`, the active group is 7, group 3 is `ASC` and group 4 is `DESC`.
  - The DataStore holds no `current_grid_column_count` key.
  - `shared_prefs/name_list.xml` no longer exists.
  - A second test does the same from a hand-written 2.7.0-format XML file (`<long …/>`, `<string …>`) placed on disk before first use.
- [ ] **AC2.** With no old file, `activeGroupId` emits null, `sortType(n)` emits `NONE`, and `gridColumns` emits 2. This is tested on both the JVM and Robolectric.
- [ ] **AC3.** An unrecognised stored sort value reads as `NONE` without throwing.
  - Tested twice: once written straight into the DataStore (JVM) and once migrated from `name_list` (Robolectric).
- [ ] **AC4.** With the real Room storage and the real DataStore, `GroupEditor.delete(id)` removes that group's sort key (no raw key left in the store) and clears the active group when it matched. Other groups' keys stay.
- [ ] **AC5.** No preferences read or write does file input or output on the caller's thread.
  - The store is built with a named single-thread dispatcher and a recording okio `FileSystem`.
  - Reads (`activeGroupId.first()`, `sortType(g).first()`) each run against a newly opened store on an existing file. A cold read must hit the file, and a warm read is served from memory.
  - Every write runs on a warm store: `setActiveGroup`, `clearActiveGroup`, `clearActiveGroupIf`, `updateSortType` and `removeSortType`. Each `edit` reads the file, per `DataStoreImpl.kt:422-426`.
  - For each of those calls, at least one file-system call is recorded, none on the caller's thread, and all on the store's dispatcher.
  - `gridColumns` and `setGridColumns` do no input or output by design, so the test asserts only that they record nothing on the caller's thread.
- [ ] **AC6 (deferred).** The device check (2.7.0, then this build installed over it) is moved to NAM-8 by user decision 2026-11-14. It is not checked here.
- [ ] **AC7.**
  - `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests` passes, including `iosSimulatorArm64Test` and `testAndroidHostTest`. The test-result XML counts rise above the 234 baseline with 0 failures.
  - The iOS main targets compile.
  - CI's `unit-tests` job runs `:shared:testAndroidHostTest`.
- [ ] **Extra (interface contract).** Atomicity, the interface's documented contract (`PreferencesRepository.kt:19-22,32-36`):
  - 50 concurrent `updateSortType(g) { it.next() }` calls start from NONE on `Dispatchers.Default`.
  - The returned values must be exactly 25 `ASC` and 25 `DESC`, and the final stored value `DESC`. A lost update would repeat a value.
  - `clearActiveGroupIf(other)` keeps the current id.
- [ ] **Extra (corruption).** A garbage `settings.preferences_pb` reads as the defaults (null active group, `NONE`) instead of throwing on every access.
- [ ] **Extra (persistence).** Values survive closing and reopening the store on the same file.

## Approach
- **Dependencies.**
  - `androidx.datastore:datastore-preferences-core:1.3.0` in commonMain. It brings `datastore-core-okio` and okio 3.17 as API dependencies.
  - `androidx.datastore:datastore-preferences:1.3.0` in androidMain, for `SharedPreferencesMigration` and `preferencesDataStoreFile`.
- **Store construction, in commonMain** (`internal fun buildSettingsStore(path, migrations, scope, fileSystem = FileSystem.SYSTEM)`):
  - Builds `PreferenceDataStoreFactory.create(storage = OkioStorage(fileSystem, PreferencesSerializer) { path }, corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }, migrations, scope)`.
  - A corrupt settings file costs only the active group and sort orders, rather than making every read throw.
  - Using `OkioStorage` on every platform gives the threading test the same kind of seam as NAM-5's `RecordingDriver` (`DatabaseThreadingTest.kt:80-95`).
  - The factory wrapper still freezes the returned `Preferences` (research §DataStore).
- **Public type.** `SettingsStorage` mirrors `NamesStorage` (`NamesStorage.kt:11-16`):
  - It has an internal constructor and exposes only `preferences: PreferencesRepository`.
  - It owns its app-lifetime scope, `CoroutineScope(Dispatchers.IO + SupervisorJob())`.
  - It is `AutoCloseable`. `close()` cancels the scope, and an internal `suspend fun closeAndJoin()` lets tests reopen the same file.
- **Android factory.** `SettingsStorage(context)` in androidMain:
  - The path is `context.preferencesDataStoreFile("settings")`, which resolves to `files/datastore/settings.preferences_pb`.
  - It passes one migration, `legacyNameListMigration(context)`. This is the generic `SharedPreferencesMigration<Preferences>(context, "name_list", migrate = …)` with MIGRATE_ALL.
  - The migration copies only the 2.x settings: `active_group_id` when it is a Long, and every `group_sort_type_for_group_*` entry that is a String. Matching by prefix covers the dynamic keys.
  - It skips `current_grid_column_count` and any 1.x leftovers, and doesn't overwrite keys the store already has.
  - It ends with `.toPreferences()`. Migrations bypass the factory's freezing wrapper (`DataStoreImpl.kt:596-611`), so this mirrors the stock migration.
  - With the `(context, name)` constructor and MIGRATE_ALL, cleanup clears and deletes the file however many keys were copied, and the migration is idempotent if it is retried.
- **Repository.** `DataStorePreferencesRepository` (internal, commonMain) keeps the legacy key names and types:
  - `longPreferencesKey("active_group_id")`, where absence means none
  - `stringPreferencesKey("group_sort_type_for_group_$id")`, read with `NameSortType.fromStored`
  - Reads are `data.map { … }.distinctUntilChanged()`.
  - Each write is a single `edit {}`, which makes `clearActiveGroupIf` and `updateSortType` atomic. `updateSortType` returns the value it wrote, captured inside the `edit`.
  - `gridColumns` is `flowOf(DEFAULT_GRID_COLUMNS)` and `setGridColumns` does nothing (retired; NAM-10 removes them).
- **Alternatives considered:**
  - `createWithPath`: there is no file-system seam on the JVM or Android (it uses `FileStorage`), so AC5 would have to be inferred.
  - The stock `SharedPreferencesMigration(context, name)` followed by a key-removal migration: it works, but the column value passes through, and there are two migrations to reason about.
  - A `keysToMigrate` subset: it can't name the dynamic sort keys, and a leftover key blocks deletion of the file.
  - Copying every key except the column count, as the ticket words it: this would carry 1.x leftovers (`ClassroomCoord.java:35,66` and `DrawActivity.java:114-120` on `8748033^`) into the store for good. The allow-list keeps the ticket's intent of copying all the settings, including the dynamic sort keys.

## Assumptions & open questions
- [answered 2026-11-12] The column toggle is retired, so `current_grid_column_count` is neither migrated nor stored, and the implementation drops `gridColumns`. Source: ticket comment, spec `docs/design/3.0-spec.md:171,530`.
- [answered 2026-11-14] AC6 (device install-over) is deferred to NAM-8. There are no `:app` changes and no Hilt wiring in NAM-6; providing `SettingsStorage(context)` as a singleton is NAM-7's job. Mirrored in the workflow.md decision log.
- [answered 2026-11-14] The AC1 test route is Robolectric 4.17 host tests in `:shared` (`androidHostTest`), and `:shared:testAndroidHostTest` is added to CI `unit-tests`. Mirrored in the decision log.
- [assumed] `gridColumns` stays on the interface until NAM-10, so the DataStore implementation satisfies it with a constant 2 and a no-op setter rather than deleting it now. Evidence: the ticket comment says "NAM-10 removes it from `PreferencesRepository`", and `GridLayout.kt:11,13` still uses it.
- [assumed] Robolectric 4.17 rather than 4.18: 4.18 was released 2026-11-13, one day ago, while 4.17 supports SDK 37 and is what androidx pins. Evidence: research §DataStore testing.
- [assumed] Robolectric runs at `sdk=34` via `src/androidHostTest/resources/robolectric.properties`. The sandboxes for SDK 35 and later need JDK 21, but CI and the toolchain are JDK 17 (`.github/workflows/android.yml:45`, `shared/build.gradle.kts:9`). SharedPreferences and `deleteSharedPreferences` behave the same from API 24 on. If the runtime rejects 34, fall back to the highest SDK the JDK 17 runtime accepts and record a deviation.
- [assumed] `OkioStorage` on Android, not `FileStorage`. Both are single-process and enforce the one-instance-per-file check, and the okio type is the common one. Evidence: research §DataStore (`PreferenceDataStoreFactory.kt:48`, the common `create(storage…)`).
- [assumed] There is no public iOS or JVM factory, matching `NamesStorage` (only `NamesStorage.android.kt`). Being multiplatform-ready means the commonMain builder and repository compile for iOS.
  - The new tests run on the JVM and on Robolectric only. `iosSimulatorArm64Test` runs commonTest, which has no temp-file support, so the DataStore code is compiled for iOS but not run there. The path checks for an iOS factory are left to whoever adds that factory.
- [assumed] The migration copies an allow-list rather than every key: `active_group_id` and the `group_sort_type_for_group_` prefix. This drops 1.x leftovers in the same file and still deletes the file. It meets the ticket's "copy all keys, not a fixed list" because the sort keys are matched by prefix.
- [assumed] A corrupt `settings.preferences_pb` is replaced with empty preferences. Read I/O errors (rare, with no recovery) still surface through the flows, and NAM-7 decides how launch handles a failing read.
- [assumed] Adding `:shared:testAndroidHostTest` to CI makes Robolectric download its `android-all-instrumented` SDK 34 jar (about 100 MB) on each cold runner, outside the Gradle cache. This is accepted. If it becomes slow, cache `~/.m2/repository/org/robolectric`.
- [assumed] The file is `files/datastore/settings.preferences_pb`, from `preferencesDataStoreFile("settings")`, which matches the location the DataStore backup docs assume.
- [assumed] Orphaned sort keys from groups deleted in 2.7.0 are migrated as they are. The ticket says they are harmless and need no clean-up.
- [open — NAM-7] Hilt provides `SettingsStorage(context)` as an app-lifetime singleton (one DataStore per file per process). NAM-7 also handles a dangling active id at launch.
- [open — NAM-13] Backup and data-extraction rules for `files/datastore/`, which Auto Backup includes by default.

## Tasks
1. **Dependencies and test setup.**
   - Files:
     - `gradle/libs.versions.toml`: datastore 1.3.0 with both artifacts; robolectric 4.17, androidx-test-core 1.7.0 and junit 4.13.2.
     - `shared/build.gradle.kts`: commonMain gets `datastore-preferences-core`; androidMain gets `datastore-preferences`; `androidHostTest` gets robolectric, androidx-test-core and junit. kotlin-test and coroutines-test already come from commonTest.
     - `withHostTest { isIncludeAndroidResources = true }`, following androidx's own setup.
     - `shared/src/androidHostTest/resources/robolectric.properties` (`sdk=34`)
     - `shared/src/androidHostTest/.../shared/RobolectricSetupTest.kt`: one test asserting `Build.VERSION.SDK_INT == 34`, so a properties file that isn't picked up fails here, not in Task 4.
   - Verify: `./gradlew --no-watch-fs :shared:allTests` is green with 235 tests, and the new test appears in the `testAndroidHostTest` XML.
2. **The repository and its builder, test-first (AC2, AC3, AC4, persistence, atomicity).**
   - Files:
     - `shared/src/commonMain/.../shared/data/DataStorePreferencesRepository.kt` (new)
     - `shared/src/commonMain/.../shared/data/SettingsStorage.kt` (new: the class and `buildSettingsStore`)
     - `shared/src/jvmTest/.../shared/data/TestSettings.kt` (new: opens on a temp folder)
     - `shared/src/jvmTest/.../shared/data/PreferencesRepositoryTest.kt` (new; also covers the corruption case)
     - KDoc touch on `shared/src/commonMain/.../repository/PreferencesRepository.kt`, saying `gridColumns` is retired and not persisted
   - Verify: `./gradlew --no-watch-fs :shared:jvmTest`, then read the XML for the new test class.
3. **Threading proof (AC5).**
   - Files: `shared/src/jvmTest/.../shared/data/PreferencesThreadingTest.kt` (new). It holds a recording `ForwardingFileSystem` and a named single-thread dispatcher, and makes the calls listed in AC5 (cold reads on a newly opened store, writes on a warm store).
   - Verify: `:shared:jvmTest` and the XML counts.
4. **The Android factory and the legacy migration (AC1, AC2 and AC3 on Android).**
   - Files:
     - `shared/src/androidMain/.../shared/data/SettingsStorage.android.kt` (new)
     - `shared/src/androidMain/.../shared/data/LegacyNameListMigration.kt` (new)
     - `shared/src/androidHostTest/.../shared/data/LegacyNameListMigrationTest.kt` (new, Robolectric)
   - The test cases:
     - seeded through the API
     - a raw 2.7.0 XML file
     - a file holding only the column count, which gets deleted
     - no file, which gives the defaults
     - an unknown sort value, which reads as NONE
   - Verify: `./gradlew --no-watch-fs :shared:testAndroidHostTest`, plus the XML in `shared/build/test-results/testAndroidHostTest`.
5. **CI and the full check (AC7).**
   - Files: `.github/workflows/android.yml` (`unit-tests` gets `:shared:testAndroidHostTest`).
   - Verify: `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests` passes with the XML totals, and `:shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64` passes.

## Out of scope
- `:app` and Hilt wiring, and launch behaviour for a dangling active id (NAM-7).
- The on-device install-over check from 2.7.0 (AC6, moved to NAM-8).
- Removing `gridColumns`, `GridLayout` and `GridColumns` from `:shared`, and the D17 change to `NameSortType.next()` (NAM-10).
- Backup rules for the DataStore file (NAM-13).
- iOS and JVM public factories or file locations.
- Cleaning up orphaned legacy sort keys.

## Verification
- Machine: `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests`, with totals from `shared/build/test-results/{jvmTest,testAndroidHostTest,iosSimulatorArm64Test}/*.xml` (more than 234 tests, 0 failures).
- Outcome: there is no UI or wiring yet, so the end-to-end proof is the Robolectric run. The real Android framework `ContextImpl` reads a hand-written 2.7.0-format `shared_prefs/name_list.xml`, `SettingsStorage(context)` (the production factory and migration) is opened, the values are read back through `PreferencesRepository`, and the test asserts the XML file is gone and `files/datastore/settings.preferences_pb` exists.
  - Report the Robolectric XML results, and the decoded key set from the `.preferences_pb` written by the test.
  - The install-over check on a real device runs in NAM-8.
