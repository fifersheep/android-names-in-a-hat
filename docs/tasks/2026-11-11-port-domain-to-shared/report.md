# Report — Port the domain logic and its tests into `:shared` (NAM-4)

## Changed vs plan
All 7 plan tasks are done, plus one review fix ([plan.md](plan.md), Progress in [workflow.md](workflow.md)). The diff to `shared/` and `gradle/` is 33 files, +1147 lines, and `:app` is unchanged.

| Plan task | Commit | Result |
|---|---|---|
| 1. Coroutines in `:shared` | 037c60a | `kotlinx-coroutines-core` 1.11.0 as `api` in commonMain; `-test` in commonTest |
| 2. Models, sort type, validation | 956b51d | `Group`, `GroupSummary`, `Name` (with `drawn`), `NameSortType` (`next`, lenient `fromStored`), `validateLabel` + `WriteResult` |
| 3. Draw rules | cc31af1 | `NumberGenerator`/`RandomNumberGenerator`, `NameDraw.draw` (clamped) and `drawFrom` (selected only), `DrawState` + `MIN_NAMES_TO_DRAW = 2` |
| 4. Sorting and grid | f51af5d | `sortNames` (stable, ignores case), `GridColumns.of` (1↔2; out-of-range reads as 2) |
| 5. Repository interfaces + fakes | 2691235 | `GroupRepository`, `NameRepository`, `PreferencesRepository` with KDoc contracts for NAM-5/NAM-6; recording fakes in commonTest |
| 6. Use cases | ddb76fb | `GroupEditor`, `NameEditor`, `SortOrder`, `GridLayout`, `ActiveGroup` |
| 7. Final checks | 33beeba | AC1 grep empty, full build, every mapped test name resolves |
| Review fix | c7bb28d | Atomic `clearActiveGroupIf`; lone `\r` test; plan wording |

## Deviations
From workflow.md, each with its reason:
- **Gradle file watching gave stale results (tooling).** Right after files were created, `jvmTest` twice reported results that didn't match the sources.
  - Cause: Gradle file-system watching misses newly created files in this environment.
  - Fix: every verify run uses `--no-watch-fs`, and green is confirmed from the per-class XML counts.
  - Why it matters: a plain `./gradlew` here can report BUILD SUCCESSFUL on unchanged results.
- **No red run for task 3.** NameDrawTest and DrawStateTest were written in the same batch as the code, so the TDD red step wasn't observed. Tasks 4 and 6 did see red before green.
- **Two test names renamed for Kotlin/Native.** Native rejects `,` in backtick names (`Name contains illegal characters`). The renames are `add stores the trimmed text as selected` and `cycle returns the new order that sorts the names`. The plan's mapping table was updated to match.
- **Three more mapping cells expanded to the real test names.** The table had shortened forms ("from ASC stores DESC", "true with names", "selectAll writes in bulk"); they now give the full names. No meaning changed.
- **File layout differs from the plan.** `GroupSummary` is in `model/Group.kt` and `RandomNumberGenerator` is in `draw/NumberGenerator.kt`. Each pair is small and tightly coupled, and there's no API effect.
- **`PreferencesRepository.clearActiveGroupIf(id)` added (review fix).** It isn't in plan.md's interface list.
  - Why: the planned read-then-clear in `GroupEditor.delete` could wipe a group opened between the read and the clear.
  - It is atomic and documented, mirroring `updateSortType`. NAM-6 implements it.

Behaviour changes from 2.7.0 that were planned and disclosed in plan.md's Assumptions:
- Zero selected now gives empty draw options (was `["0"]`).
- An empty group is no longer "all selected".
- The domain returns numbers and flags instead of English strings.
- The draw returns a `List` instead of a `"\n"`-joined string plus a `multiple` flag.
- Over-draw clamps instead of crashing.
- Unknown stored sort values read as `NONE` instead of crashing.
- Out-of-range column counts read as 2.
- Deleting the active group clears it.
- Labels are trimmed and blank labels rejected (GitHub #5/#6).

## Evidence
Fresh run on d057efb with `--rerun-tasks`:

```
$ ./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64 --rerun-tasks
BUILD SUCCESSFUL in 20s

# totals from shared/build/test-results/<target>/*.xml
jvmTest: 12 classes, 70 tests, 0 failures/errors, 0 skipped
testAndroidHostTest: 12 classes, 70 tests, 0 failures/errors, 0 skipped
iosSimulatorArm64Test: 12 classes, 70 tests, 0 failures/errors, 0 skipped
```

Per-class counts, from `shared/build/reports/tests/jvmTest/index.html`, which I opened and read:
- AppInfoTest 1, DrawStateTest 7, NameDrawTest 9, NameSortTypeTest 4
- GridColumnsTest 4, SortNamesTest 4, ActiveGroupTest 4, GridLayoutTest 3
- GroupEditorTest 9, LabelsTest 6, NameEditorTest 13, SortOrderTest 6

Other checks:
- **AC1:** `grep -rnE 'import (android|org\.greenrobot|dagger|javax|java\.)' shared/src/commonMain` returns no output (exit 1).
- **AC2:** all 38 distinct test names in plan.md's Test mapping resolve to exactly one `fun` in commonTest (Task 7 grep, independently repeated by the reviewer). The 67 legacy cases are each ported or dropped for an allowed reason.
- **Acceptance criteria:** AC1–AC9 are MET, with evidence in [review.md](review.md). AC7 is MET locally; CI `ios-compile` on the PR is the remaining confirmation.

## Follow-ups
- **NAM-5:**
  - Add DAO tests proving `setAllSelected` keeps `drawn` and touches only the given group. The `NameEditorTest` state checks rely on the fake.
  - Honour the KDoc contracts: groups newest first, names in insertion order, cascade delete, and that the selected and drawn flags never change each other.
- **NAM-6:**
  - Implement `clearActiveGroupIf` and `updateSortType` atomically with DataStore `edit {}`.
  - Decide whether `GroupEditor.delete`'s prefs cleanup is best-effort. Today a DataStore IOException after a successful Room delete would make `delete()` throw.
- **CI:** commonTest runs on Native only locally (`:shared:allTests` on macOS). CI runs `:shared:jvmTest` and only compiles iOS main. Adding `:shared:iosSimulatorArm64Test` to the `ios-compile` job would have caught the comma names in CI. It's a small change, suitable for a future NAM-3-style ticket.
- **NAM-2/NAM-12:** `MIN_NAMES_TO_DRAW` is 2, and they own lowering it. `drawFrom` is where NAM-12 adds "not yet drawn" to the pool.
- **Flow tooling:** worth a learning. In this repo, verify with `--no-watch-fs` and confirm green from the XML counts.
