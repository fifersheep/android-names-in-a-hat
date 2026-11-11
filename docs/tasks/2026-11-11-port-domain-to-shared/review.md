# Review — Port the domain logic and its tests into `:shared` (NAM-4)

Reviewer: flow-reviewer, one pass over `origin/main...HEAD` (9eedd6d..33beeba) against plan.md. Fixes below landed in c7bb28d.

## Blocking
None.

## Advisory
- **The active-group clear on delete was a read followed by a separate clear** (`GroupEditor.kt:32-33`). If the user deleted the active group and then quickly opened another, the clear could wipe the newly opened id.
  - **Fixed in c7bb28d:** new `PreferencesRepository.clearActiveGroupIf(id)`, which is atomic and documented, mirroring `updateSortType`. `GroupEditor.delete` uses it; new test `deleting a group clears the active group with one conditional write`.
  - **Follow-up (NAM-6):** the prefs cleanup runs after the Room delete and isn't transactional with it. A DataStore IOException would make `delete()` throw after a successful delete. NAM-6 should decide whether the cleanup is best-effort (catch and log) or retried.
- **Some `NameEditorTest` state assertions check the fake, not a rule** (lines 60-65, 74-77, 87-91, 107). The use case only delegates. The real checks are the call-recording assertions (lines 70, 82, 97, 106), which are sound, and AC8 is met as written (recorded calls plus KDoc).
  - **Follow-up (NAM-5):** add DAO tests for "`setAllSelected` keeps `drawn`" and for its group-only scope against Room.
- **Plan edits during implementation were under-disclosed.** Five mapping-table cells changed: the two comma renames (disclosed), plus three expansions of shortened names to the real test names. None changes meaning.
  - **Done:** all five are now listed in workflow.md Deviations.
- **File layout differs from Tasks 2 and 3.** `GroupSummary` lives in `model/Group.kt` and `RandomNumberGenerator` in `draw/NumberGenerator.kt`. No behaviour or API effect.
  - **Done:** recorded in Deviations.
- **No lone `"\r"` case in `LabelsTest`.**
  - **Fixed in c7bb28d.**
- **"67 non-analytics cases" was mislabelled:** the 67 include the two analytics-event cases.
  - **Fixed:** plan.md wording, in c7bb28d.

## Acceptance criteria
| AC | Status | Evidence |
|----|--------|----------|
| AC1: commonMain has no Android, EventBus, Dagger or java imports | MET | grep over `shared/src/commonMain` is empty; the only external imports are `kotlin.random.Random` and `kotlinx.coroutines.flow.*` |
| AC2: mapping ported or dropped, and allTests green | MET | All 38 mapped names resolve to exactly one `fun`; drop reasons are all from the allowed list; legacy inputs carried over. After c7bb28d: 70 tests, 0 failures, on each of jvmTest, testAndroidHostTest and iosSimulatorArm64Test |
| AC3: seeded draw repeatable | MET | `seeded draws are repeatable` (three runs with `Random(42)`) |
| AC4: clamp, zero, negative | MET | `NameDraw.kt:16` uses `coerceIn(0, pool.size)` and never calls `nextInt(0)`; tests for clamp, zero (no generator calls), negative and empty pool |
| AC5: blank rejected, trimmed, all four writes | MET | `LabelsTest` (spaces, tabs, `\n`, `\r`, `\r\n`, trim); reject and trim tests for GroupEditor create/rename and NameEditor add/rename |
| AC6: NameSortType format, `fromStored` | MET | `sort type names are a stored format`; null, "", "asc" and "SIDEWAYS" read as NONE |
| AC7: iOS compiles | MET locally | `compileKotlinIosArm64`/`IosSimulatorArm64` built, and iosSimulatorArm64Test ran. CI `ios-compile` is checked at PR time |
| AC8: selectAll/clearAll use only setAllSelected, plus KDoc | MET | `NameEditor.kt:27,30`; recorded calls in `select all and clear all leave drawn untouched`; `NameRepository.kt:28` |
| AC9: pool is selected names only | MET | `NameDraw.kt:22`; `drawFrom draws only selected names` (4 requested from 2 selected) |

## Checked (by reviewer)
- No test or CI config edits and nothing skipped. Coroutines are `api` (Flow is in public signatures), and `:app` resolves core and android to 1.11.0, so they stay aligned. No `:app` changes.
- Behaviour matches legacy for:
  - sort comparators and the NONE→ASC→DESC→ASC cycle
  - the grid toggle
  - new names being created selected
  - draw options and `canDraw` for one or more selected
- Every behaviour drift is disclosed in plan.md's Assumptions. GroupEditor delete ordering is: repo delete, then cleanup only if something was deleted.

## Re-verify (after fixes)
- `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :shared:compileKotlinIosArm64` passed: BUILD SUCCESSFUL, 70 tests and 0 failures/errors per target (from the XML).
- Outcome: there is no UI in this ticket, so the test reports are the artifact. `shared/build/reports/tests/jvmTest/index.html` (generated 23:05:49) lists 12 classes and 70 tests, all passing:
  - AppInfo 1, DrawState 7, NameDraw 9, NameSortType 4
  - GridColumns 4, SortNames 4, ActiveGroup 4, GridLayout 3
  - GroupEditor 9, Labels 6, NameEditor 13, SortOrder 6

  The testAndroidHostTest (23:05:47) and iosSimulatorArm64Test (23:05:40) reports were regenerated in the same run, and their XML totals are also 70 and 0.
