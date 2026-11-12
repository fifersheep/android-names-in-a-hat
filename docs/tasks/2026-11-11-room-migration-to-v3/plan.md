# Plan: open the existing database with Room and migrate it to v3 (NAM-5)

## Intent
3.0 must open the `Classrooms.db` that 2.7.0 (schema v2), or a pre-2017 install (v1), leaves on the device and keep every group and name. The database moves to Room 3 in `:shared`, at schema v3 with a `drawn` flag. `GroupRepository` and `NameRepository` (NAM-4) are implemented on top of it, with reads as `Flow` and writes as `suspend`, all off the caller's thread.

## Acceptance criteria
- [ ] **AC1** A v2 fixture built with the legacy DDL verbatim migrates to v3. It holds 3+ groups and names with mixed 0/1 selected values. After migration every group and name keeps its id and text, every name keeps the **same group id**, `selected` is unchanged, and `drawn` = 0 everywhere. This is asserted as an exact set of `(id, group_id, text, selected, drawn)` tuples (`MigrationTest`).
- [ ] **AC2** The same holds for a v1 fixture built with the 2014 DDL (`git show 200e6ef:…/ClassroomDbHelper.java:55-63`) and migrated 1→2→3. The exact tuple set is asserted, and every v1 pupil comes out `selected = 0`, as 2.7.0 showed them. The fixture also holds the orphans that the v1 code really produced: a pupil with classroom id `-1` (from `getClassroomId` on a miss, `200e6ef:…:171,178,277-278`) and one with a NULL classroom id. Both are absent afterwards, and nothing throws (`MigrationTest`).
- [ ] **AC3** A v2 fixture with these rows migrates without throwing (`MigrationTest`):
  - `column_name_toggled` NULL and `'false'` (text) both become `selected = 0`.
  - A name whose group id doesn't exist is absent after migration.
  - A name with a NULL group id is absent after migration.
- [ ] **AC4** Schema validation passes in two places:
  - `MigrationTestHelper.runMigrationsAndValidate(3, …)` on each fixture.
  - An end-to-end open with the production builder (`buildNamesDatabase`, `DATABASE_NAME`) reads the migrated data through the repositories, for **each** of the v2, v1 and dirty fixtures.
  - `git ls-files shared/schemas` lists `uk.lobsterdoodle.namepicker.shared.data.NamesDatabase/3.json`.
  - `DATABASE_NAME` (commonMain, used by `NamesStorage(context)` and by the end-to-end test) is asserted to equal `"Classrooms.db"`.
- [ ] **AC5** `GroupRepository.delete(id)` removes the group's names and returns the group; another group's names are untouched (`RoomRepositoryTest`).
- [ ] **AC6** With the production builder given a named query dispatcher and a recording driver, every repository method records **at least one** `prepare`, and every recorded `prepare` runs on that dispatcher's thread, never on the calling test thread (`DatabaseThreadingTest`).
- [ ] **AC-ids** Ids are never reused, including ids of groups and names deleted before the upgrade (per-group 2.7.0 prefs keyed by id outlive their group; see `master:core/.../StorageUseCase.kt:56-59`).
  - Fixture setup: the v2 (and v1) fixtures delete the highest-id group and name, so `sqlite_sequence.seq` is greater than the largest surviving id.
  - Assertion: after migration, the next `create`/`add` returns an id greater than the **legacy `seq`** (`MigrationTest`).
- [ ] **AC-repo** Repository behaviour matches the interface docs (`RoomRepositoryTest`):
  - Groups come newest first with correct name counts.
  - Names come in insertion order.
  - `add` stores the given `selected` and `drawn = false`.
  - `setSelected` and `setAllSelected` never change `drawn`; `markDrawn` and `clearDrawn` never change `selected`.
  - `delete(name)` returns the name; deleting a missing id returns null.
  - The Flows re-emit after writes.
- [ ] **AC-nodestroy** `git grep -n -i "fallbackToDestructive" shared/` returns nothing.
- [ ] **AC8** `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :shared:compileKotlinIosArm64 :app:lintDebug` exits 0. The `shared/build/test-results` XML shows 0 failures/errors and includes results for `MigrationTest`, `RoomRepositoryTest` and `DatabaseThreadingTest`, each with a non-zero test count.
- AC7 (on-device check: install 2.7.0, then install this build over it) is **deferred to NAM-8** by user decision. It is not checked here.

## Approach
- **Wiring:** Room 3.0.3 (`androidx.room3`) with `sqlite-bundled` 2.7.1 and KSP 2.3.12 on all four targets, applied in `:shared` only. The schema is exported to `shared/schemas`.
- **Code layout:** commonMain holds the entities, DAOs, `NamesDatabase` (+ `expect object` constructor), the migrations, the two repository implementations, and a small `NamesStorage` facade. The facade exposes only the repository interfaces, so Room stays an `implementation` dependency and never leaks into `:app`. androidMain adds `NamesStorage(context)`, which opens `Classrooms.db` in the standard database directory.
- **Shared builder function:** one extension, `RoomDatabase.Builder<NamesDatabase>.buildNamesDatabase(driver = BundledSQLiteDriver(), queryContext = Dispatchers.IO)`, sets the driver, adds both migrations and sets the query context. Production and tests both call it. The platform builder that comes before it differs: Android uses `Room.databaseBuilder(context, DATABASE_NAME)`, and jvmTest uses `Room.databaseBuilder(name = <temp dir>/DATABASE_NAME)`.
- **v3 schema:** both primary keys are `@PrimaryKey(autoGenerate = true)`, which gives `AUTOINCREMENT`, as the legacy tables had.
  - `name_groups(id, label)`
  - `names(id, group_id → name_groups.id ON DELETE CASCADE, indexed; text; selected DEFAULT 0; drawn DEFAULT 0)`
- **`MIGRATION_1_2`** replays the legacy `onUpgrade` (`master:…/ClassroomDbHelper.kt:21-34`). It adds a `sqlite_sequence` carry-over, described below, and drops the child table before the parent.
- **`MIGRATION_2_3`:**
  1. Create both v3 tables and the index. The DDL is pasted from the exported `3.json` `createSql`, so it matches validation by construction.
  2. `INSERT … SELECT` the groups.
  3. `INSERT … SELECT` the names with an inner join to `table_groups`, which drops orphans and NULL group ids, with `CASE WHEN CAST(column_name_toggled AS INTEGER) > 0 THEN 1 ELSE 0 END`. The group id is taken from the `table_groups` side.
  4. **Carry `sqlite_sequence` over:** set the new table's `seq` to the larger of its own value and the legacy table's `seq`, inserting the row if it doesn't exist yet (an empty table has none). Without this, dropping the legacy table lowers the sequence to the highest *surviving* id and a deleted group's id gets reused. The critic reproduced this in sqlite3.
  5. Drop the legacy tables, **child first** (`table_names`, then `table_groups`), so the drops are safe even if foreign keys are enforced during the migration. No rename is needed because the new names differ.
- **Tests** live in `jvmTest`, using JUnit4 `MigrationTestHelper` and the bundled driver. Fixtures are written with raw SQL and `PRAGMA user_version`.
- Alternatives considered:
  - Room 2.8.5: maintenance mode; the user chose Room 3.
  - Entities that mirror the legacy tables: impossible, because Room can't describe an untyped column or a `boolean`.
  - A direct 1→3 migration: the ticket's spec asks for 1→2→3, which keeps each step reviewable against the legacy code.
  - Tests in `commonTest`: `MigrationTestHelper` is JVM-only JUnit4, and Android host tests can't load the bundled native library.

## Assumptions & open questions
- [answered 2026-11-11] Room 3.0.3 (`androidx.room3`), not Room 2.8.5. See the workflow.md decision log.
- [answered 2026-11-11] AC7 (on-device 2.7.0 → 3.0 check) is deferred to NAM-8, and NAM-5 makes no `:app` changes. AC7 is added to NAM-8 at reporting.
- [assumed] NULL or `'false'` toggled values become `selected = 0`. 2.7.0 displayed `cursor.getInt(col) > 0` (`master:…/ClassroomDbHelper.kt:123,181`), which is 0 for both, so users see what they saw before.
- [assumed] Orphaned names and names with a NULL group id are dropped by the inner join. The ticket says to drop orphans, and a NULL group id is the same case.
- [assumed] The v1 fixture uses the real 2014 DDL from `200e6ef` (research.md) instead of a reconstruction. The critic confirmed it is unchanged through `0b5f486`, the last commit before v2.
- [assumed] Both migrations preserve `sqlite_sequence` and drop child tables first. These are deliberate departures from the "verbatim" legacy upgrade. They change no row data, they only prevent id reuse and FK-ordered drop failures. That id reuse matters because NAM-6 migrates 2.7.0 prefs keyed by group id, which 2.7.0 never cleaned up on delete (`master:core/.../StorageUseCase.kt:56-59`).
- [assumed] The v3 tables are `name_groups` and `names`, and the columns mirror the model fields (`label`, `text`, `group_id`). This avoids `groups`, which is an SQLite keyword (window `GROUPS`). The ticket allows tidying.
- [assumed] Default order is groups by `id DESC` and names by `id ASC`, the insertion order (`GroupRepository.kt:12`, `NameRepository.kt:14`).
- [assumed] Room stays internal to `:shared`. `NamesStorage` exposes only the repository interfaces, and `room3-runtime`/`sqlite-bundled` are `implementation`. If KSP rejects `internal` entities or DAOs, they become public within the `data` package and nothing else changes.
- [assumed] There is no production database builder for iOS or JVM: there is no iOS app (decision 2026-11-11), and iOS only has to compile. The JVM builder lives in `jvmTest`.
- [assumed] KSP stays at 2.3.12, because 2.3.13 has regression google/ksp#3286 (research.md).
- [assumed] Room's WAL journal mode is acceptable, and the legacy helper is never opened alongside Room (it isn't ported).
- [open — implementation, task 5] Does Room 3 turn on `PRAGMA foreign_keys` when entities declare FKs, so `ON DELETE CASCADE` fires? The AC5 test decides. If it doesn't, `GroupDao.delete` explicitly deletes the names in the same `@Transaction`, and that gets logged as a deviation.

## Tasks
1. **Build wiring.** Add catalog entries (`room3` 3.0.3, `sqlite` 2.7.1, runtime/compiler/testing/bundled, plugin `androidx.room3`) and `apply false` at the root. In `:shared` apply `ksp` and `androidx.room3`, add `room3 { schemaDirectory("$projectDir/schemas") }`, the compiler on `kspAndroid`/`kspJvm`/`kspIosArm64`/`kspIosSimulatorArm64`, runtime and bundled SQLite in commonMain, and `room3-testing` in jvmTest.
   - Files: `gradle/libs.versions.toml`, `build.gradle.kts`, `shared/build.gradle.kts`.
   - Verify: `./gradlew --no-watch-fs :shared:compileKotlinJvm` passes.
2. **Schema.** Add `GroupEntity`, `NameEntity` (FK CASCADE, index on `group_id`, `@ColumnInfo(defaultValue = "0")` on `selected`/`drawn`), `GroupDao`, `NameDao` (queries per the repository interfaces, plus a summary row with `COUNT`), and `NamesDatabase` (version 3, `exportSchema`, `@ConstructedBy`).
   - Files: `shared/src/commonMain/kotlin/uk/lobsterdoodle/namepicker/shared/data/{GroupEntity,NameEntity,GroupDao,NameDao,NamesDatabase}.kt`, `shared/schemas/…/3.json` (generated).
   - Verify: `:shared:compileKotlinJvm :shared:compileKotlinIosSimulatorArm64` passes, and `3.json` exists with the expected tables.
3. **Migrations and builder.** Add `MIGRATION_1_2`, `MIGRATION_2_3` (DDL from `3.json`, the sequence carry-over, child-first drops) and `ALL_MIGRATIONS`. Add `DATABASE_NAME = "Classrooms.db"`, the `Builder.buildNamesDatabase(driver, queryContext)` extension and the `NamesStorage` facade, plus `NamesStorage(context)` on Android.
   - Files: `shared/src/commonMain/.../data/{Migrations,NamesStorage}.kt`, `shared/src/androidMain/kotlin/uk/lobsterdoodle/namepicker/shared/data/NamesStorage.android.kt`.
   - Verify: all targets compile.
4. **Migration tests (AC1–4, AC-ids).**
   - Add the v1/v2 DDL verbatim, and fixture builders on the bundled driver. The fixtures include deleted highest ids, so `seq` is greater than the max id, and v1 orphans (`-1` and NULL).
   - Add `runMigrationsAndValidate` tests for v2, v1 and dirty v2, asserting exact tuple sets.
   - Open each fixture end to end through `buildNamesDatabase` on `<temp>/DATABASE_NAME` and read it back via the repositories.
   - Assert `DATABASE_NAME == "Classrooms.db"`.
   - Files: `shared/src/jvmTest/kotlin/uk/lobsterdoodle/namepicker/shared/data/{LegacySchema,MigrationTest}.kt`.
   - Verify: `:shared:jvmTest`, with the counts read from the XML.
5. **Repository implementations (AC5, AC-repo).** `RoomGroupRepository` and `RoomNameRepository` map entities to models. `markDrawn` with no ids does nothing.
   - Files: `shared/src/commonMain/.../data/{RoomGroupRepository,RoomNameRepository}.kt`, `shared/src/jvmTest/.../data/RoomRepositoryTest.kt`.
   - Verify: `:shared:jvmTest`. Resolve the FK open question here.
6. **Threading (AC6).** A recording `SQLiteDriver` wrapper logs the thread for each `prepare`. The test drives every repository method from `runBlocking` on the test thread, with a named single-thread query dispatcher.
   - Files: `shared/src/jvmTest/.../data/DatabaseThreadingTest.kt`.
   - Verify: `:shared:jvmTest`.
7. **Full verification.** Run the AC8 command and the AC-nodestroy grep; tick the ACs.
   - Files: none.
   - Verify: as in AC8.

## Out of scope
- `:app` and Hilt wiring of `NamesStorage` (NAM-7).
- The on-device AC7 check (moved to NAM-8).
- DataStore and preferences (NAM-6).
- When names become drawn, and the "remaining" rule (NAM-12).
- Backup rules (NAM-13).
- An iOS database builder.
- CI workflow changes.

## Verification
- Machine:
  - `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :shared:compileKotlinIosArm64 :app:lintDebug`, then sum `tests`/`failures`/`errors` across `shared/build/test-results/**/*.xml` and confirm that the `MigrationTest`, `RoomRepositoryTest` and `DatabaseThreadingTest` XML files exist.
  - `git grep -n -i fallbackToDestructive shared/` returns nothing.
  - `git ls-files shared/schemas` lists `3.json`.
- Outcome: there is no UI yet. The end-to-end proof is the `MigrationTest` that builds a real v2 `Classrooms.db` with 2.7.0's exact DDL, opens it through `NamesStorage` and the production migrations, and reads every group and name back through `GroupRepository` and `NameRepository`. The on-device install-over check runs in NAM-8.
