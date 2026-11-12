# Review: NAM-5 (`main...HEAD`, one flow-reviewer pass at 73ccef0)

## Blocking
None.

## Advisory
- **Untested `carrySequence` INSERT branch** (`shared/src/commonMain/.../data/Migrations.kt`). No fixture reached the "no `sqlite_sequence` row yet" path. A 2.7.0 user who deleted every group would be the case.
  - **Done:** added the `EMPTIED_V2` fixture: every group and name deleted, legacy seq 2/3. Two tests cover it: `runMigrationsAndValidate` and end to end (the next ids must exceed the legacy seq).
  - **Finding while fixing it:** a mutation test (removing the INSERT) still passed. sqlite3 3.35 shows that an `INSERT … SELECT` copying zero rows still creates the target's `sqlite_sequence` row (seq 0), so the INSERT branch can't be reached from either migration. The `UPDATE` is what lifts the sequence, and the new tests do cover that path (an empty table is the case where only the `UPDATE` protects the ids). The INSERT is kept as a safety net, and its comment now says why. Commit 5ea9261.

## Acceptance criteria
| AC | Verdict | Evidence |
|---|---|---|
| AC1 clean v2 | MET | `MigrationTest` exact `(id, group_id, text, selected, drawn)` set for `CLEAN_V2` |
| AC2 v1 | MET | `V1` fixture: verbatim 200e6ef DDL, `-1`/NULL orphans absent, all `selected = false`, path 1→2→3 |
| AC3 dirty | MET | `DIRTY_V2`: NULL, `'false'`, `'true'` and defaulted values all become 0; orphan and NULL group dropped; no throw |
| AC4 validation + schema | MET | `runMigrationsAndValidate(3)` and end-to-end open for every fixture; `git ls-files` lists 3.json; `DATABASE_NAME == "Classrooms.db"` asserted; migration DDL is byte-identical to 3.json |
| AC5 cascade | MET | `RoomRepositoryTest`; generated `NamesDatabase_Impl.onOpen` runs `PRAGMA foreign_keys = ON` |
| AC6 threads | MET | `DatabaseThreadingTest`: 13/13 methods, each records ≥1 `prepare`, all on the query thread (identity compare) |
| AC-ids | MET | `CLEAN_V2`, `V1`, `EMPTIED_V2` end to end: new ids > legacy seq |
| AC-repo | MET | `RoomRepositoryTest` (13) |
| AC-nodestroy | MET | `git grep -i fallbackToDestructive shared/` is empty |
| AC8 build | MET | Verify command exits 0; 234 tests, 0 failures/errors after the fix |
| AC7 on-device | DEFERRED | To NAM-8 by user decision |

## Checked (reviewer)
- Both migrations diffed against the legacy helpers. The CAST/CASE mapping matches 2.7.0's `getInt > 0`. The `NOT NULL` legacy columns rule out NULL text.
- FK timing: `foreign_keys = ON` only in `onOpen`, after migration, so v1 `-1` pupils copy without an FK error.
- `NamesStorage(context)` resolves via `Context.getDatabasePath(name)`, confirmed by disassembling room3-runtime-android 3.0.3.
- The repository implementations honour the interface contracts. No existing test, CI or config was weakened. Every change traces to a plan task or a recorded deviation.
- One 2.7.0 divergence, not raised as a finding: a group id stored as **text** `'1'` now joins its group, which 2.7.0's filter would have missed. No legacy code path ever stored text group ids. Nothing is lost.

## Outcome verification (main session)
- With no UI until NAM-7/8, the outcome check drove the real migration on a file written by **real Android SQLite**:
  - The `niah_api26` emulator (SQLite 3.18.2) built a v1 database with the 2014 DDL, then ran 2.7.0's exact `onUpgrade` SQL, then used 2.7.0-style writes (create group, add names, deselect, delete group).
  - The file was pulled and opened through `openStorage` → `buildNamesDatabase` (the production path).
- Observed on device: API 26 SQLite stores `DEFAULT false` as the **text** `'false'` (4 rows, `typeof = text`). This confirms the dirty-value case is real, not hypothetical.
- Result:
  - 3 groups newest first: Class 3C (2), Room 2 (1), Room 1 (2).
  - Dev selected; Eve, Bob, Ann and Cara not selected (`'false'` becomes 0); the `-1` orphan dropped.
  - New group id 5 and new name id 9, above legacy seq 4/8.
  - `user_version` 3, with tables `android_metadata`, `name_groups`, `names` and `room_master_table`.
- The throwaway probe test was deleted; nothing from it is committed.
