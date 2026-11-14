# Research: NAM-6 DataStore preferences and the `name_list` migration

## Findings

### What already exists in `:shared`
- **The interface is already fixed (NAM-4).** It is `PreferencesRepository` (`shared/src/commonMain/kotlin/uk/lobsterdoodle/namepicker/shared/repository/PreferencesRepository.kt:10-40`).
  - `activeGroupId: Flow<Long?>`, `setActiveGroup`, `clearActiveGroup`, and `clearActiveGroupIf(id)`, which must be atomic (`:19-22`).
  - `gridColumns: Flow<Int>` and `setGridColumns` (`:25-27`). These are retired; see Constraints.
  - `sortType(groupId): Flow<NameSortType>`, which reads NONE when the value is missing or unrecognised (`:29-30`).
  - `updateSortType(groupId, transform)`, an atomic read-modify-write that returns the new value (`:32-36`).
  - `removeSortType(groupId)` (`:38-39`).
- **AC4's delete hook is already wired.** `GroupEditor.delete` calls `prefs.removeSortType(id)` and then `prefs.clearActiveGroupIf(id)` once a delete succeeds (`shared/.../usecase/GroupEditor.kt:29-34`). NAM-6 only needs the real store to implement `removeSortType`, plus a test against that real store.
- **Lenient parsing exists.** `NameSortType.fromStored(String?)` returns NONE for null or unknown values (`shared/.../model/NameSortType.kt:20`). The constant names are a stored format (`:5-7`).
- **The fake shows the expected semantics.** `FakePreferencesRepository` (`shared/src/commonTest/.../fakes/FakePreferencesRepository.kt`) defaults to 2 columns and NONE sort.
- **There is no real implementation yet,** and no preferences code anywhere in `:app`. `NamesApplication` is an empty `@HiltAndroidApp` and `MainActivity` is a placeholder.

### Prior art: NAM-5's `NamesStorage`
- **Factory pattern.**
  - `NamesStorage` has an internal constructor and exposes only the repository interfaces (`shared/.../data/NamesStorage.kt:11-16`).
  - The only public factory is Android's `NamesStorage(context)` (`shared/src/androidMain/.../data/NamesStorage.android.kt:11-14`). There is no iOS or JVM factory.
  - A shared internal builder, `buildNamesDatabase(driver, queryContext)`, takes its seams as parameters for tests (`shared/.../data/DatabaseBuilder.kt:21-27`).
- **Test pattern.** jvmTest opens storage through the production builder (`shared/src/jvmTest/.../data/TestDatabases.kt:14-21`).
  - `DatabaseThreadingTest` proves that no work runs on the caller's thread. It records threads at a seam (the SQLite driver) and asserts they all belong to a named dispatcher (`DatabaseThreadingTest.kt:25-77`).
- **Deferral precedent.** NAM-5 made no `:app` changes. Hilt wiring went to NAM-7, and the on-device install-over check (its AC7) went to NAM-8 by user decision (`docs/tasks/2026-11-11-room-migration-to-v3/plan.md:32,57,98-99`, `report.md:71-72`).
- **Build.** `shared/build.gradle.kts` targets android (KMP library plugin, `withHostTest {}`), jvm, iosArm64 and iosSimulatorArm64.
  - commonMain keeps Android and DI libraries out (`:27`). jvmTest holds the database tests (`:41-43`).
  - There is no `androidHostTest` source set or Robolectric yet.

### Legacy 2.7.0 storage (`master`)
- **File.** It is named `name_list` (`app/.../Util.kt:7`) and written with synchronous `commit()` (`app/.../storage/SharedPrefsKeyValueStore.kt:51-53`).
- **`active_group_id` (Long).**
  - `putLong` stores it and `remove` clears it. Presence means "active"; when present the value is read as `getLong(KEY, 0L)`.
  - Source: `core/.../storage/ActiveGroupUseCase.kt:18,23,28-31,35`.
- **`group_sort_type_for_group_<id>` (String).**
  - The value is `NameSortType.toString()`, so it is `NONE`/`ASC`/`DESC`.
  - It is read with `valueOf`, which crashes on an unknown value.
  - Source: `core/.../selection/SelectionNamesUseCase.kt:29,36,39,45`.
- **`current_grid_column_count` (Long).** It is retired and must not be carried over (see Constraints).
- **Backup.** The 2.7.0 manifest had `allowBackup="true"`, so `shared_prefs/name_list.xml` could come back from a backup.

### DataStore 1.3.0 (researcher; sources are the 1.3.0 `-sources.jar` from maven.google.com, plus developer.android.com)
- **Latest stable is 1.3.0 (2026-10-21).**
  - commonMain uses `androidx.datastore:datastore-preferences-core:1.3.0`. It publishes android, jvm, iosArm64 and iosSimulatorArm64 variants, and brings okio 3.17.0 as an API dependency.
  - `SharedPreferencesMigration` and `preferencesDataStoreFile` exist only in the android variant of `androidx.datastore:datastore-preferences:1.3.0`, so that artifact goes in androidMain.
  - The minimums (Kotlin 2.2.20, coroutines 1.9.0, AGP 8.1.1) are all exceeded here. There is no okio conflict with Room 3.0.3, which has no okio dependency.
- **Factory.** `PreferenceDataStoreFactory.createWithPath(corruptionHandler, migrations, scope, produceFile: () -> okio.Path)` is a common `expect object`.
  - Prefer it to `DataStoreFactory`, because it wraps the store and freezes the returned `MutablePreferences`.
  - The file name must end in `.preferences_pb` (enforced with `check`). Native also needs an absolute path.
  - On Android, `context.preferencesDataStoreFile(name)` resolves to `files/datastore/<name>.preferences_pb`.
- **`SharedPreferencesMigration` semantics.**
  - Types map one-to-one by runtime type, so **a Long stays a Long**. Reading a Long key through `intPreferencesKey` throws ClassCastException at the use site.
  - Keys already in the DataStore are not overwritten.
  - `cleanUp` deletes the XML file only when the prefs end up empty **and** the `(context, name)` constructor was used. A `keysToMigrate` subset leaves the file behind if any other key remains, and it can't express the dynamic sort keys anyway.
  - **So "every key except the column count, with the file deleted" is best done with one custom migration.** Use the generic `androidx.datastore.migrations.SharedPreferencesMigration<Preferences>(context, "name_list", migrate = …)` with MIGRATE_ALL. It copies every entry by type except `current_grid_column_count`; cleanUp then clears everything and deletes the file. A file holding only the column count is still deleted.
- **When migrations run.**
  - They run lazily on the first read or write, in list order, inside one locked `updateData`, with a single disk write.
  - `cleanUp` runs after that write.
  - If anything throws, the whole initialisation is retried on the next access, so migrations must be idempotent.
- **Threading.** The default scope is `CoroutineScope(Dispatchers.IO + SupervisorJob())`. All file input and output, migrations and the edit actor run in that scope, so `data` and `edit` are main-safe.
  - The scope must live as long as the app: cancelling it permanently disables the store.
  - commonMain needs `import kotlinx.coroutines.IO`.
- **Atomicity.** A read-modify-write inside one `edit {}` is atomic. Updates go one at a time through an actor under a lock, and each transform sees the latest data.
  - This satisfies `clearActiveGroupIf` and `updateSortType`.
  - Nesting `edit` calls on the same store throws.
- **One instance per file.** A static `activeFiles` set throws `IllegalStateException` on the second instance's first access to the same path in the process. The path is released only when the first store's scope Job completes.
- **Testing.**
  - On the JVM: use a temp `.preferences_pb` file, with the scope set to `backgroundScope` or `TestScope(UnconfinedTestDispatcher())`, never the `runTest` scope itself. To simulate a restart, use `cancelAndJoin` before reopening.
  - For `SharedPreferencesMigration` without a device: Robolectric in the KMP plugin's `withHostTest {}` is viable. androidx's own build configures it this way (`RobolectricHelper.kt:55-78`: `isIncludeAndroidResources`, `isReturnDefaultValues`, `androidHostTestImplementation`). Robolectric's real `ContextImpl` writes and deletes `shared_prefs/name_list.xml`.
  - Robolectric 4.17 (2026-09-10) adds SDK 37. 4.18 came out 2026-11-13, which is too new to adopt; androidx itself pins 4.17. Pin the SDK with `robolectric.properties`. The SDK 36 sandbox needs JDK 21.
- **Pitfalls.**
  - Auto Backup includes `files/datastore/` by default.
  - The R8 consumer rules ship in the AARs.

## Constraints & prior art
- **The grid preference is retired** (NAM-2 spec D5, `docs/design/3.0-spec.md:36,171,530`; ticket comment 2026-11-12). Don't migrate `current_grid_column_count`, and drop it from the DataStore implementation. NAM-10 deletes `PreferencesRepository.gridColumns`, `GridLayout` and `GridColumns`.
  - This overrides the column parts of AC1 ("all four values" becomes active group plus two sort orders, and the column key is gone), AC2 (the "2 columns" default) and AC6 ("in one column").
- **Process and threading rules.** Create exactly one DataStore per file per process (ticket notes). Reads are `Flow`, writes are `suspend`, and nothing runs on the main thread (AC5).
- **Keep the legacy key names and value types** (the ticket calls this the simplest correct migration). Absence of `active_group_id` means no active group; there is no sentinel value.
- **The Android-only migration lives in androidMain;** commonMain holds the store-backed repository (ticket implementation notes).
- **Out of scope:**
  - database contents (NAM-5)
  - launch decisions and the dangling active id (NAM-7)
  - DataStore backup rules (NAM-13)
  - Hilt wiring (NAM-7, following NAM-5's precedent)
- **Test conventions** (memory `names-build-gotchas`):
  - Verify with `--no-watch-fs` and check the counts in the XML results.
  - Keep commas out of backtick test names in commonTest.
  - CI runs only `:shared:jvmTest` (plus iOS compile), so it does not run Android host tests.

## Open questions
1. **Shape-changing.** AC6 (the device check with 2.7.0 installed and this build over it) needs a running build that creates the store, which means `:app` wiring (NAM-7's job). The 2.7.0 APK isn't debuggable, so `run-as` can't read its prefs. Defer it, as NAM-5 did, or add wiring now?
2. **Shape-changing.** How should AC1 seed and check a real `name_list` SharedPreferences file? Options: Robolectric host tests in `:shared` (new test dependencies; CI doesn't run them unless the unit-tests job is extended), or a device test (`withDeviceTest`, emulator only, never in CI).
3. **Conventional default, recorded.** `gridColumns` stays in the interface until NAM-10. The DataStore implementation returns a constant `flowOf(DEFAULT_GRID_COLUMNS)` and `setGridColumns` does nothing. Nothing is persisted, and the KDoc points at NAM-10.
4. **Conventional default, recorded.** The public API mirrors `NamesStorage`.
   - androidMain exposes a factory taking a `Context`, for NAM-7 to provide as a singleton. It builds the file path, the migration and an app-lifetime IO scope.
   - commonMain has an internal builder taking `migrations`, `scope` and a path, so the JVM tests can use the production builder.
   - There is no iOS or JVM public factory, as with `NamesStorage`. Being multiplatform-ready means commonMain compiles for iOS.
5. **Conventional default, recorded.** The file is `files/datastore/settings.preferences_pb`, via `preferencesDataStoreFile("settings")`.
6. **Deferred to NAM-13.** Backup rules: `files/datastore/` is backed up by default, and a restored 2.x backup re-migrates harmlessly.
7. **Code-resolvable.** The AC5 proof follows `DatabaseThreadingTest`: inject a named single-thread dispatcher as the store's scope and record the threads at the storage seam. The planner picks the exact seam.
