---
task: port-domain-to-shared
title: Port the domain logic and its tests into `:shared`
ticket: NAM-4: Port the domain logic and its tests into `:shared` (https://linear.app/northtrack/issue/NAM-4/port-the-domain-logic-and-its-tests-into-shared)
ticket-id: NAM-4
tracker-synced: In Review
mode: full
phase: complete
branch: flow/port-domain-to-shared
worktree:
created: 2026-11-11
updated: 2026-11-11
---

## Next action
Complete. PR #24 squash-merged into `main` after gate 2. Learnings: `.flow.md` verify now uses `--no-watch-fs` (this branch); memory `names-build-gotchas` saved. Ticket moves to Done after the merge. Baseline 2026-11-11: `./gradlew :app:assembleDebug :shared:allTests` PASS on flow/port-domain-to-shared @ 9eedd6d.

## Decision log
<!-- gate entries look like: "- 2026-07-05 gate 1: plan approved by user"
     answered unknowns look like: "- 2026-07-05 Q: per-user or per-team limits? A: per-user"
     log an answer the moment it is given — conversation context does not
     survive a compaction; this file does -->
- 2026-11-11 mode: full (user confirmed). Many files, legacy research on `origin/master`, design calls (repository interfaces, oversized draw, coroutines dependency).
- 2026-11-11 Q: where do group/name write rules (validation, new names selected, delete returns label) live? A: thin use cases (e.g. GroupEditor/NameEditor) over the repository interfaces; port StorageUseCase tests against fake repos.
- 2026-11-11 Q: draw more names than available? A: clamp to min(count, available); never throws.
- 2026-11-11 plan-critic: no Blocking; mapping covers all 67 non-analytics legacy cases. Folded in: literal test names, whitespace kinds (#5/#6), negative count, selected-only `drawFrom`, grid out-of-range → 2, delete clears active group + only-if-deleted, repo KDoc contracts (ordering, cascade, setAllSelected keeps drawn), atomic `updateSortType`, `NameEditor.rename` returns WriteResult, coroutines as `api`; research.md counts fixed (18/14).
- 2026-11-11 gate 1: plan approved by user
- 2026-11-11 gate 2: merge approved by user (squash-merge PR #24)

## Progress
- [x] 1. Add coroutines to `:shared` (core as `api`, test in commonTest) — 037c60a
- [x] 2. Models, sort type and validation (test-first) — 956b51d (NameSortTypeTest 4, LabelsTest 6)
- [x] 3. Draw rules: NameDraw (clamp, drawFrom), DrawState, generators (test-first) — cc31af1 (NameDrawTest 9, DrawStateTest 7)
- [x] 4. Sorting and grid pure rules (test-first) — red seen, then green; f51af5d (SortNamesTest 4, GridColumnsTest 4)
- [x] 5. Repository interfaces (contract KDoc) + test fakes — compiles (`:shared:compileTestKotlinJvm`); 2691235
- [x] 6. Use cases: GroupEditor, NameEditor, SortOrder, GridLayout, ActiveGroup (test-first) — red seen; `:shared:allTests` green: 12 classes / 69 tests on each of jvmTest, testAndroidHostTest and iosSimulatorArm64Test; ddb76fb
- [x] 7. Final checks: AC1 grep, full build + iOS compile, mapping-name grep — AC1 grep empty; `--no-watch-fs :app:assembleDebug :shared:allTests :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64` BUILD SUCCESSFUL; all mapped test names resolve (one shortened "covered by" cell in plan.md fixed to the full name)

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
- 2026-11-11 task 2 (tooling, not scope): right after new files were written, `:shared:jvmTest` twice reported a stale result: first BUILD SUCCESSFUL with the new tests not compiled, then "Unresolved reference" with the new main files present. A standalone `:shared:compileKotlinJvm` picked them up. Green is now confirmed from the per-class counts in `shared/build/test-results/jvmTest/*.xml`, not the BUILD line.
- 2026-11-11 task 3: the cause of the staleness is Gradle file-system watching, which misses newly created files in this daemon. `--no-watch-fs` fixes it; every verify run from now on uses `./gradlew --no-watch-fs …`.
- 2026-11-11 task 3: the tests and the implementation were written in the same batch, so no red run was observed for NameDrawTest/DrawStateTest (TDD red step skipped). Tasks 4 and 6 run red first.
- 2026-11-11 task 6: Kotlin/Native rejects `,` in backtick test names (`compileTestKotlinIosSimulatorArm64`: "Name contains illegal characters"). Two mapped tests were renamed: `add stores the trimmed text as selected` and `cycle returns the new order that sorts the names`. plan.md's mapping table was updated to match.
- 2026-11-11 tasks 6–7: three more mapping-table cells were expanded from shortened forms to the real test names: "/ from ASC stores DESC" → "/ cycle from ASC stores DESC"; "/ true with names" → "/ hasNames true with names"; "covered by … selectAll writes in bulk" → "… for the group only". No meaning changed (flagged in review).
- 2026-11-11 file layout differs from Tasks 2 and 3: `GroupSummary` is in `model/Group.kt`, and `RandomNumberGenerator` is in `draw/NumberGenerator.kt`, rather than separate files. No API effect.
- 2026-11-11 review fix (c7bb28d): added `PreferencesRepository.clearActiveGroupIf(id)`, which is not in plan.md's interface list. It replaces the read-then-clear in `GroupEditor.delete`, which had a race; NAM-6 implements it.
