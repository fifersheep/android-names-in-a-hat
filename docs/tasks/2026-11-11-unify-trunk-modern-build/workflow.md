---
task: unify-trunk-modern-build
title: Unify the trunk and stand up the modern build
ticket: NAM-1 — Unify the trunk and stand up the modern build (https://linear.app/northtrack/issue/NAM-1)
ticket-id: NAM-1
tracker-synced: In Review
mode: full
phase: reporting
branch: flow/unify-trunk-modern-build
worktree:
created: 2026-11-11
updated: 2026-11-11
---

## Next action
Review done (review.md): 0 blocking, 2 advisory (1 fixed, 1 carried to NAM-3); verify GREEN. PR #22 is open (https://github.com/fifersheep/android-names-in-a-hat/pull/22), CI green (AC7), Linear set to In Review. Holding at gate 2. The merge must be a MERGE COMMIT, after the user OKs relaxing linear history on main (decision log). After merge, check AC5 on origin/main. Baseline verify (`./gradlew :app:assembleDebug :shared:allTests`) is RED: `Project 'shared' not found` (expected, because `:shared` doesn't exist yet). `:app:assembleDebug` alone is GREEN on main with JDK 17.0.21. This contradicts the ticket's "nothing can be built" for main.

## Decision log
- 2026-11-11 mode: full (confirmed by user): history merge, new KMP module, build-stack decisions
- 2026-11-11 Q: .flow.md? A: save with verify `./gradlew :app:assembleDebug :shared:allTests`, tracker linear, start→In Engineering / pr-open→In Review / merged→Done
- 2026-11-11 Q: uncommitted .idea changes on main? A: discard (restored gradle.xml, misc.xml, vcs.xml)
- 2026-11-11 Q: CI is JDK 11 and the PR will go red; what to do? A: minimal JDK bump to 17 (+ fix the test task name) in NAM-1 so the PR is green; full CI rework stays in NAM-3; log as a deviation from the ticket's scope
- 2026-11-11 Q: versionCode for 3.0.0? A: 2000000002, +1 per release (Play ceiling 2100000000)
- 2026-11-11 Q: main requires linear history (enforced for admins), so squash/rebase would drop the 2.7.0 parent. How to land it? A: at gate 2, switch the linear-history rule off for this PR, merge with a MERGE COMMIT, then switch it back on. Each gh protection change needs the user's OK at that moment
- 2026-11-11 gate 1: plan approved by user

## Progress
- [x] 1. Merge history (-s ours origin/master): d407579 (AC5 on branch OK)
- [x] 2. Clear the old skeleton: e0aeae1 (flow artifacts committed separately in the commit before it)
- [x] 3. Gradle skeleton (wrapper 9.7.1, catalog, settings, SDK packages): 00895d4. KGP resolves 2.2.10 → 2.4.21
- [x] 4. `:shared` module: cfa5244. allTests is green (jvm, iosSimulatorArm64, androidHostTest: 1 test each); iOS compiles on Xcode 16.4 with no warning
- [x] 5. `:app` module: 7b420d1. kspDebugKotlin generated Hilt injectors on Kotlin 2.4.21 (no fallback needed); assembleDebug, lintDebug (0 errors, 34 warnings) and testDebug are green
- [x] 6. CI minimal bump: daffd49. All three CI commands green locally on JDK 17; `:app:testDebug` still resolves
- [x] 7. Device + signing checks (no commit). AC1: API 26 debug (launch 1042 ms) and API 36 release (709 ms) show the placeholder, 0 FATAL, survive rotation; screenshots in the session scratchpad. AC3: aapt2 shows uk.lobsterdoodle.namepicker / 2000000002 / 3.0.0 / min 26 / target 36. AC4: release cert SHA-256 4fa5cd07…ae951 matches the 2.7.0 APK; with `-PniahKeyAlias=` the build outputs app-release-unsigned.apk and debug still builds. AC6: grep is empty, no google-services.json. Update path: `adb install -r` over 2.7.0 (vc 2000000000) on API 36 succeeds

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
- 2026-11-11 Task 3: settings includes only `:app` until Task 4. Gradle 9 rejects an `include` for a project directory that doesn't exist yet. Also installed `build-tools;36.1.0` (the newest 36.x) rather than 36.0.0
- 2026-11-11 Task 3: dropped `android.nonTransitiveRClass=true` from gradle.properties (the plan said keep main's settings). It's the AGP ≥ 8 default, so nothing changes. Logged at code review
- 2026-11-11 Task 3: `.gitignore` also gets `build/` and `.kotlin/`, so module build dirs and the KGP 2.x project cache stay untracked (the old ignore only covered `/build`)
- 2026-11-11 Task 4: enabled `withHostTest {}` on `:shared`'s android target (the plan said off). AGP warns without it, and `allTests` then includes `testAndroidHostTest`, which resolves the open question
- 2026-11-11 Task 5: dropped the planned JUnit SmokeTest and the junit catalog entry. `:app:testDebug` resolves to testDebugUnitTest and passes as NO-SOURCE; the real tests are in `:shared`
- 2026-11-11 Task 5: added `lint { disable += "HighAppVersionCode" }`. Lint errored on versionCode 2000000002, which is forced by Play history. The other 34 warnings are mostly UnusedResources (strings carried from 2.7.0) plus items for NAM-13
- 2026-11-11 Task 5: kept `tools:targetApi="31"` in the manifest (the plan said drop it). Removing it would add an UnusedAttribute lint warning for `dataExtractionRules`; only `package=` was removed. Added the `placeholder_message` string
- 2026-11-11 Task 7: AC1 on API 36 used the release-signed APK, not the debug one. AVD `api36_niah` already had a release-signed 2.7.0 installed, and the debug key was rejected (INSTALL_FAILED_UPDATE_INCOMPATIBLE). I upgraded in place rather than uninstall, which would have wiped the emulator's data; that also exercised the update path
- 2026-11-11 Task 7: the AC4 no-properties check used `-PniahKeyAlias=` (blank overrides the property) instead of an empty GRADLE_USER_HOME, which would re-download the whole toolchain. CI's assemble job is the secrets-free proof
- 2026-11-11 Task 7: CI's `lintDebug -PbuildDir=lint` writes `app/lint/` and `shared/lint/` dirs locally; deleted after the run. That's a CI quirk for NAM-3
