# Review: NAM-1 unify trunk + modern build

One flow-reviewer pass over `main...HEAD` (the content is in `d407579..1df37f5`; the merge commit's tree is identical to main's).

## Blocking
None.

## Advisory
- **CI runs zero tests.** `.github/workflows/android.yml:69` runs `:app:testDebug`, which is now NO-SOURCE, and no job runs `:shared` tests. A break in `:shared` would still show a green PR.
  - **Action:** follow-up for NAM-3 ("Bring CI up to date"): add `:shared:allTests` (or `jvmTest` + `testAndroidHostTest` on Linux, iOS on a macOS runner).
  - Not fixed here: the user scoped this PR's CI change to the minimal JDK bump, and the test-coverage gap was a recorded Task 5 deviation.
- **`android.nonTransitiveRClass=true` dropped from `gradle.properties` without a logged deviation.** No behaviour change, since AGP ≥ 8 defaults to true. **Action:** added to workflow.md Deviations.

## Acceptance criteria
| AC | Status | Evidence |
|---|---|---|
| AC1 assembleDebug + launch on API 26/36 | MET | assembleDebug green (reviewer re-ran it). Placeholder screenshots on API 26 (debug) and API 36 (release, upgraded over 2.7.0), 0 FATAL, survives rotation. Screenshots inspected |
| AC2 `:shared:allTests` + iOS compile | MET | jvmTest, testAndroidHostTest and iosSimulatorArm64Test each `tests="1" failures="0"`; both `compileKotlinIos*` green |
| AC3 manifest values | MET | aapt2: `uk.lobsterdoodle.namepicker`, vc `2000000002`, `3.0.0`, min 26, target 36 |
| AC4 release signing | MET | Cert SHA-256 `4fa5cd07…ae951` matches the 2.7.0 APK. With a blank property: unsigned release, and debug still builds |
| AC5 2.7.0 reachable from main | MET on branch / **pending merge** | `merge-base --is-ancestor f9773d5 HEAD` = 0. On `main` it holds only if the PR lands as a merge commit (gate 2 plan: relax linear history for this PR) |
| AC6 no cruft | MET | grep is empty; no `google-services.json` |
| AC7 PR CI green | **pending PR** | The reviewer judged it likely to pass: JDK 17, no iOS tasks in the CI task graph, and AGP auto-downloads `android-37.0` (seen locally with build-tools 36.0.0). Risk: an unwritable or unlicensed SDK dir on the runner |

## Checked (by the reviewer)
- Merge commit parents and tree.
- File-by-file conformance against plan Tasks 1–7, the Assumptions, research.md and the Deviations.
- The CI diff: JDK and labels only.
- Carried resources are byte-identical to origin/master (plus the one placeholder string).
- The wrapper jar matches the 9.7.1 distribution.
- All build scripts, the manifest, the theme and the Kotlin sources were read.
- Re-ran: verify, iOS compiles, aapt2, the AC5/AC6 checks, and CI dry-runs.
