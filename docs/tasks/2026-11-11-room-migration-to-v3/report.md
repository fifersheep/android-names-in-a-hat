# Report: open the existing database with Room and migrate it to v3 (NAM-5)

## Changed vs plan
All 7 plan tasks are done ([plan.md](plan.md), Progress in [workflow.md](workflow.md)). Every acceptance criterion is met except AC7, which the user deferred to NAM-8.

| Task | Commit | What |
|---|---|---|
| 1 Build wiring | f117ba6 | Room 3.0.3 (`androidx.room3`), `sqlite-bundled` 2.7.1, KSP 2.3.12 on android/jvm/iosArm64/iosSimulatorArm64, `room3 { schemaDirectory }`, `room3-testing` in jvmTest |
| 2 Schema | 513a6ca | `GroupEntity` (`name_groups`), `NameEntity` (`names`, FK CASCADE, index, `selected`/`drawn` DEFAULT 0), `GroupDao`, `NameDao`, `NamesDatabase` v3; `shared/schemas/…/3.json` checked in |
| 3 Migrations + builder | 715e585 | `MIGRATION_1_2` (legacy `onUpgrade` replay), `MIGRATION_2_3` (rebuild, `CAST > 0` selection, inner join drops orphans, AUTOINCREMENT sequence carry, child-first drops), `DATABASE_NAME`, `buildNamesDatabase` |
| 5 Repositories | 44ad143 | `RoomGroupRepository`, `RoomNameRepository`, `NamesStorage` facade (Room stays `implementation`), `NamesStorage(context)` on Android; 13 tests |
| 4 Migration tests | 6aee98f | v1, clean v2 and dirty v2 fixtures from verbatim legacy DDL; `MigrationTestHelper` plus end to end through the production builder |
| 6 Threading | b5c9128 | Recording `SQLiteDriver`: every repository method's SQL runs on the query dispatcher |
| 7 Verification | (none) | Full verify green |
| Review fix | 5ea9261 | `EMPTIED_V2` fixture (every group deleted); comment explaining `carrySequence`'s safety-net INSERT |

## Deviations
- **`-Xexpect-actual-classes` added to `:shared`.** Room's `@ConstructedBy` needs an `expect object` with KSP-generated actuals, and Kotlin warns on expect/actual classes without the flag. This is Room's documented KMP setup. Entities and DAOs stayed `internal`, which KSP accepted, so no fallback to public was needed.
- **Task 5 ran before task 4, and task 3's file is `DatabaseBuilder.kt` instead of `NamesStorage.kt`.** The plan's end-to-end migration tests read through the repositories, so those had to exist first. The `NamesStorage` facade moved to task 5 along with them. Same files, different order.
- **`TestDatabases.kt` (jvmTest helper) was added.** `openStorage(dir, driver, queryContext)` opens a file through the production `buildNamesDatabase`, the same way `NamesStorage(context)` does. Tasks 4–6 share it, so every database test uses the production configuration.
- **Review fix (not in the plan): the `EMPTIED_V2` fixture.** A review advisory. While mutation-testing it, I found that `carrySequence`'s INSERT branch can't be reached, because SQLite creates the `sqlite_sequence` row on any INSERT into an AUTOINCREMENT table, even a zero-row `INSERT … SELECT` (checked in sqlite3 3.35). The `UPDATE` is what protects the ids, and `EMPTIED_V2` tests it in the case where it alone matters. The INSERT stays as a documented safety net.

## Evidence
**Verify** at 5ea9261 (re-run after the review fix):
```
$ ./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :shared:compileKotlinIosArm64 :app:lintDebug
> Task :shared:compileKotlinJvm
BUILD SUCCESSFUL in 23s
$ (sum of shared/build/test-results/**/*.xml)
tests 234 skipped 0 failures 0 errors 0          # baseline on main: 210
jvmTest: MigrationTest 10, RoomRepositoryTest 13, DatabaseThreadingTest 1 (all 0 failures)
$ git grep -n -i fallbackToDestructive shared/   → (no output)
$ git ls-files shared/schemas → shared/schemas/uk.lobsterdoodle.namepicker.shared.data.NamesDatabase/3.json
```

**Mutation checks** (each mutant temporarily applied, then restored; `git diff` empty afterwards):
| Mutant | Result |
|---|---|
| Skip the group `sqlite_sequence` carry in 2→3 | MigrationTest: 4 failures |
| NULL/`'false'` selected becomes 1 (`ELSE 1`) | MigrationTest: 6 failures |
| Copy every name into group 1 | MigrationTest: 4 failures |
| Drop `setQueryCoroutineContext` from the builder | DatabaseThreadingTest: 1 failure |
| Remove `carrySequence`'s INSERT branch | 0 failures: the branch can't be reached (see Deviations) |

**Outcome: a database written by real Android SQLite.** There is no UI until NAM-7/8, so I drove the production migration on a file built by the `niah_api26` emulator's SQLite 3.18.2, the oldest SQLite this app's minSdk can meet. The steps were: the 2014 v1 DDL, then 2.7.0's exact `onUpgrade` SQL, then 2.7.0-style writes (create group, add names, deselect, delete group).
- Before, on the device:
  ```
  user_version 2
  1|Ann|'false'|text|1      2|Bob|'false'|text|2      3|Cara|'false'|text|1
  5|Minus one|'false'|text|-1   6|Dev|1|integer|3      7|Eve|0|integer|3
  sqlite_sequence: table_groups|4  table_names|8
  ```
- After, opened through `openStorage` → `buildNamesDatabase` → the repositories:
  ```
  GroupSummary(group=Group(id=3, label=Class 3C), nameCount=2)
      Name(id=6, groupId=3, text=Dev, selected=true, drawn=false)
      Name(id=7, groupId=3, text=Eve, selected=false, drawn=false)
  GroupSummary(group=Group(id=2, label=Room 2), nameCount=1)
      Name(id=2, groupId=2, text=Bob, selected=false, drawn=false)
  GroupSummary(group=Group(id=1, label=Room 1), nameCount=2)
      Name(id=1, groupId=1, text=Ann, selected=false, drawn=false)
      Name(id=3, groupId=1, text=Cara, selected=false, drawn=false)
  new group id=5 new name id=9
  user_version 3; tables: android_metadata, sqlite_sequence, name_groups, names, room_master_table
  ```
- This confirms on a real device SQLite that `DEFAULT false` is stored as the **text** `'false'`, so the dirty-value handling is needed, not hypothetical. Every name with a group survived with the same id and group. The `-1` orphan was dropped. New ids continue above the legacy sequences.

**Review:** one flow-reviewer pass found 0 blocking and 1 advisory (fixed). All ACs were MET apart from AC7, which is deferred. See [review.md](review.md).

## Follow-ups
- **NAM-8: AC7 on-device check** (user decision). Install the 2.7.0 APK (`../release-2.7.0/names-in-a-hat-2.7.0-vc2000000001.apk`), create groups and names, delete one group, then install the NAM-8 debug build over it with the same signature. All data should be present, and a new group should not reuse the deleted group's id.
- **NAM-7 (app shell):** provide `NamesStorage(context)` as an app-lifetime singleton in Hilt. Never open `Classrooms.db` through any other SQLite in the same process, because framework and bundled SQLite in one process can corrupt the file.
- **NAM-6 (DataStore):** 2.7.0 kept per-group preferences (`group_sort_type_for_group_<id>`, active group) after a group was deleted, and NAM-5 now guarantees ids aren't reused. NAM-6 may also want to drop prefs for group ids that no longer exist.
- **NAM-13 (backup rules):** Room uses WAL, so any backup or restore rule must include `Classrooms.db-wal`/`-shm`, or exclude them consistently.
- **CI:** jvmTest needs `sqlite-bundled` natives, which ship for linux_x64, linux_arm64, osx_arm64 and windows_x64. There is no osx_x64, so Intel Macs can't run the database tests locally.
- **Optional:** commit the API 26-written `Classrooms.db` as a binary regression fixture. It's currently only in this report's evidence.
