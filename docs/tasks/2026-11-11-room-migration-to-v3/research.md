# Research: NAM-5, open the existing database with Room and migrate it to v3

## Findings

### What exists in `:shared` (NAM-4 output)
- The contracts to implement are `shared/src/commonMain/kotlin/uk/lobsterdoodle/namepicker/shared/repository/GroupRepository.kt` and `NameRepository.kt`.
  - `GroupRepository`: `observeGroups(): Flow<List<GroupSummary>>` (newest first, with name counts), `observeGroup(id): Flow<Group?>`, `create(label): Long`, `rename`, `delete(id): Group?` (also deletes the group's names).
  - `NameRepository`: `observeNames(groupId)` (insertion order), `add(groupId, text, selected): Long` (adds the name not drawn), `rename`, `delete(id): Name?`, `setSelected`, `setAllSelected`, `markDrawn(ids)`, `clearDrawn(groupId)`. `selected` and `drawn` are independent: a write to one must never change the other.
- Models: `model/Group.kt` has `Group(id: Long, label)` and `GroupSummary(group, nameCount: Int)`. `model/Name.kt` has `Name(id, groupId, text, selected, drawn = false)`.
- The use cases already depend on these interfaces: `usecase/GroupEditor.kt` (delete, then prefs cleanup) and `usecase/NameEditor.kt` (adds names selected). The fakes in `commonTest/.../fakes/` describe the expected behaviour and serve as a contract reference.
- `shared/build.gradle.kts:1-31` has the KMP android-library plugin, `android { withHostTest {} }`, `jvm()`, `iosArm64()` and `iosSimulatorArm64()`. commonMain is DI-free and exposes Flow with `api(coroutines)`. KSP is already in the catalog (`ksp = 2.3.12`) and declared `apply false` at the root (`build.gradle.kts:1-11`). Only `:app` applies it today, for Hilt.
- `:app` is still a placeholder: `MainActivity.kt` shows `PlaceholderScreen` until NAM-7, `NamesApplication.kt` is a bare `@HiltAndroidApp`, and there are no Hilt modules yet.
- CI (`.github/workflows/ci.yml`): `unit-tests` runs `:shared:jvmTest :app:testDebugUnitTest`, and `ios-compile` compiles iOS main only.

### The legacy database (`master`)
- v2 is `master:app/src/main/kotlin/uk/lobsterdoodle/namepicker/api/ClassroomDbHelper.kt`.
  - DDL is at `:200-210`; the file name `Classrooms.db` and version 2 are at `:213-217`.
  - `onUpgrade` (`:21-34`) runs `onCreate`, then copies `column_classroom_ID, column_classroom_name` into `table_groups`, then copies `column_pupil_ID, column_pupil_name, column_classroom_ID` into `table_names`. It does **not** set `column_name_toggled`, so the column default applies. It then drops both v1 tables.
  - `onDowngrade` re-runs the upgrade (`:36-38`).
- **The real v1 DDL is in git history.** `git show 200e6ef:app/src/main/java/uk/lobsterdoodle/namepicker/core/ClassroomDbHelper.java` (2014-06-21) has `DATABASE_VERSION = 1` and `Classrooms.db` (`:20-21`), and its DDL is at `:55-63`:
  - `table_classroom_names(column_classroom_ID integer primary key autoincrement, column_classroom_name text not null)`
  - `table_pupil_names(column_pupil_ID integer primary key autoincrement, column_pupil_name text not null, column_classroom_ID references table_classroom_names(column_classroom_ID))`
  - v2 (the toggled state) arrived in `f38d66d` on 2017-03-13. The v1 fixture can therefore use the exact historical DDL; nothing has to be reconstructed.
- Toggled reads use `cursor.getInt(col) > 0` (`:123,181`). `getInt` returns 0 for NULL and for non-numeric text such as `'false'`. **What 2.7.0 showed is therefore `CAST(col AS INTEGER) > 0`**, and NULL or `'false'` appeared as not selected.
- `DEFAULT false` stores 0 on SQLite 3.23 and later. On older framework SQLite a bare identifier in DEFAULT is a string literal, so the column could hold `'false'` (NUMERIC affinity cannot convert it). Names copied from v1 get that default; names added in v2 get an explicit 1 (`:105`); `updateName` and `toggleAll` write 0 or 1.
- `column_group_id` has no declared type, so its affinity is none/BLOB. Values arrived through `ContentValues.put(Long)`, so they are stored as INTEGER. The legacy FK was never enforced (`PRAGMA foreign_keys` is off by default). When joined against the INTEGER PK, SQLite applies numeric affinity to the untyped side.
- The legacy `DbHelper` surface is in `master:core/.../storage/DbHelper.kt:7-18`; the NAM-4 interfaces already cover it.
- Ordering: groups `ORDER BY column_group_id DESC` (`:151`). Names use `ORDER BY column_group_id DESC` within one group (`:172`), which is effectively rowid/insertion order.
- `allowBackup="true"` and `fullBackupContent="true"` are in `master:app/src/main/AndroidManifest.xml:7,12`.
- The real 2.7.0 APK for AC7 is at `../release-2.7.0/names-in-a-hat-2.7.0-vc2000000001.apk`.
- Prior art: branch `sqlite-room-migration` (2022, WIP; `af55d75`) is an Android-only Room sketch with `GroupsDatabase`, entities and `app/schemas/.../3.json`. It has no migrations and is not worth reusing beyond a glance.

### Room on KMP (flow-researcher, sources checked against published artifacts on 2026-11-11)
- **There are two Room lines.**
  - `androidx.room3` 3.0.3 is stable: 3.0.0 shipped 2026-07-01 and 3.0.3 on 2026-09-09. It is KSP2-only, generates Kotlin only, and is coroutines-only (DAOs must be `suspend` or return `Flow`). It requires a `SQLiteDriver`.
  - `androidx.room` 2.8.5 is stable but in maintenance mode (patch releases only), per https://developer.android.com/blog/posts/modernizing-the-room.
  - Both support the `com.android.kotlin.multiplatform.library` plugin. Room 3 needs AGP 8.10 or newer; this repo is on AGP 9.5.1.
  - Kotlin 2.4.21 reads the room3 klibs, which were built with 2.3.20.
- **KSP**: stay on 2.3.12. 2.3.13 has a concurrent-run regression (google/ksp#3286). 2.3.12 fixes the Android-KMP Lint task dependency (#3128) and KMP klib declarations (#2396).
- **Wiring (Room 3)**
  - Plugin id `androidx.room3`, with a `room3 { schemaDirectory("$projectDir/schemas") }` extension. The KMP doc page wrongly shows `room {}`.
  - KSP compiler goes on `kspAndroid`, `kspJvm`, `kspIosArm64` and `kspIosSimulatorArm64`.
  - commonMain takes `room3-runtime` and `androidx.sqlite:sqlite-bundled:2.7.1`; jvmTest takes `room3-testing`, which brings JUnit4.
  - Schemas are written to `schemas/<db FQN>/<version>.json`. The build fails if targets export different JSON for the same path, which can't happen when the entities are common.
- **Constructor**: `@ConstructedBy(XConstructor::class)` plus `expect object XConstructor : RoomDatabaseConstructor<X>`; KSP generates the actuals.
- **Driver**: on Android, `build()` silently falls back to `AndroidSQLiteDriver` unless `setDriver(BundledSQLiteDriver())` is called. On JVM and iOS it throws without a driver.
- **Android path**: `Room.databaseBuilder<X>(context, name)` resolves a relative name with `context.getDatabasePath(name)`.
- **Opening the legacy file**: the file format is the same.
  - Room ignores `android_metadata` and does not reject unknown tables in production validation.
  - On first open Room sets `journal_mode=WAL` (TRUNCATE on low-RAM Android), which leaves `-wal` and `-shm` sidecar files. Harmless, but backups must carry them.
  - **Never open `Classrooms.db` with both framework SQLite and bundled SQLite in one process.** Separate lock bookkeeping can corrupt the file (sqlite.org/howtocorrupt.html §2.3). This is moot here because the legacy helper is not ported.
- **Migration mechanics**
  - Room reads `user_version`. Inside one `BEGIN EXCLUSIVE` transaction it runs the migrations, then `onValidateSchema`, then writes `room_master_table`, then sets `user_version`.
  - A file at v2 with no master table therefore goes through 2→3, then validation, then gets its identity written.
  - Do not `BEGIN`/`COMMIT` inside `migrate()`. `PRAGMA foreign_keys=OFF` has no effect inside a transaction; use `PRAGMA defer_foreign_keys` if needed.
  - Room 3's `Migration.migrate(connection: SQLiteConnection)` is `suspend`.
- **What validation compares**
  - Per entity table: column name, `notNull`, `isPrimaryKey` and **affinity**; foreign keys (table, onDelete, onUpdate, columns); and indices.
  - The affinity of `boolean` or an empty type is UNDEFINED, while a Kotlin `Boolean` maps to INTEGER. That forces the `table_names` rebuild the ticket predicts.
  - Defaults are compared only when the entity declares `@ColumnInfo(defaultValue = …)`, and then as an exact string. With `defaultValue = "0"` the migration DDL must say `DEFAULT 0`.
- **Testing on JVM**
  - room3-testing's JVM `MigrationTestHelper(schemaDirectoryPath, databasePath, driver, databaseClass)` is a JUnit4 rule, so it lives in `jvmTest`, not `commonTest`.
  - `runMigrationsAndValidate(3, migrations)` loads **only the target version's JSON** and opens whatever file sits at `databasePath`. A hand-made v1 or v2 fixture therefore works without a 1.json or 2.json.
  - Build fixtures with `BundledSQLiteDriver().open(path)`, the legacy DDL verbatim, inserts, then `PRAGMA user_version = N`.
  - Add an end-to-end `Room.databaseBuilder(name = path).setDriver(...).addMigrations(...)` test that reads through the DAOs.
- **Android host tests can't load the bundled native library**: the Android variant ships device `.so` files only. Database tests therefore go in `jvmTest`. sqlite-bundled-jvm 2.7.1 has natives for osx_arm64, linux_x64, linux_arm64 and windows_x64. This host is arm64 and CI is ubuntu x64, so both are fine.
- **Threads**: the default query context is `Dispatchers.IO` on Android and JVM. Every suspend DAO call and every Flow emission runs through `withContext(queryContext)`.
  - To prove it: in tests, inject `setQueryCoroutineContext(<named single-thread dispatcher>)` and wrap `BundledSQLiteDriver` in a recording driver that logs `Thread.currentThread()` on `open`/`prepare`. Assert that no statement ran on the calling thread or the main thread.

## Constraints & prior art
- Never `fallbackToDestructiveMigration`. Never downgrade, which is Room's default refusal. Migrations must stay forever because backup restore can bring a 2.7.0 database to a 3.0 install.
- commonMain stays DI-free; Hilt is in `:app` only (decided 2026-11-11, `shared/build.gradle.kts:20`).
- `.flow.md` verify is `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests`. Read pass counts from `shared/build/test-results/*/*.xml`.
- Don't put commas in commonTest backtick test names (Kotlin/Native). jvmTest names are JVM-only, but stay consistent.
- The bundled SQLite driver adds a native `.so` per ABI to the APK (roughly 1 MB each). The ticket's spec accepts this.

## Open questions (triaged)
| # | Question | Class | Resolution |
|---|---|---|---|
| 1 | Room 3 (`androidx.room3` 3.0.3) or Room 2.8.5 (maintenance)? | shape-changing | **User: Room 3.0.3.** |
| 2 | AC7 manual check: with no screens until NAM-7/8, how does the app open the DB on device? Does this ticket wire `:app` (Hilt)? | shape-changing | **User: defer AC7 to NAM-8.** NAM-5 makes no `:app` changes and proves the migrations with JVM tests. Add AC7 to NAM-8 at reporting. |
| 3 | Coalesce NULL or `'false'` toggled to 0 or 1? | code-resolvable | `CASE WHEN CAST(col AS INTEGER) > 0 THEN 1 ELSE 0 END`, matching what 2.7.0 displayed (`getInt > 0`). NULL and `'false'` become 0, and the test says so. |
| 4 | Orphaned or NULL group ids | code-resolvable | Copy names with an inner join to `table_groups` and take the group id from the groups side (an INTEGER), which drops orphans and NULLs. |
| 5 | Path from v1 | conventional | `MIGRATION_1_2` reproduces the legacy `onUpgrade` (create the v2 tables, copy, drop), then `MIGRATION_2_3`, as the ticket's spec says. The fixture uses the 2014 DDL verbatim. |
| 6 | v3 table and column names | conventional (plan) | Use tidy names that are not SQL keywords. `groups` is a keyword (window frames), so plan.md picks the exact names. |
| 7 | Does Room 3 turn on `PRAGMA foreign_keys` when entities declare FKs, so cascade works? | code-resolvable during implementation | The AC5 test proves it. If FKs are not enforced, `delete` does an explicit two-statement `@Transaction`. Plan both. |
| 8 | Where the builder lives | conventional | `androidMain` exposes `fun databaseBuilder(context)` and `iosMain`/`jvmMain` expose a path-taking builder. commonMain has the entities, DAOs, `@Database` and the repository implementations. `room3-runtime` is `implementation` scope unless `:app` needs Room types. |
| 9 | Do real devices still have v1 files? | deferred / unknowable | It doesn't change the work: the migration is written regardless. |
