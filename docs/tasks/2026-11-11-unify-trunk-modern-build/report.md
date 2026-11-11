# Report: Unify the trunk and stand up the modern build (NAM-1)

## Changed vs plan
All 7 tasks in [plan.md](plan.md) are done. Commits are listed in [workflow.md](workflow.md) under Progress.

1. **History:** `d407579` is a `-s ours` merge of `origin/master` (f9773d5, the shipped 2.7.0). Its tree is identical to main's.
2. **Old skeleton:** the 2022 Compose skeleton (Groovy build, sources, example tests, `colors.xml`) is gone, and `.idea/` is untracked.
3. **Build:** Gradle 9.7.1 wrapper, Kotlin DSL settings and root script, and a version catalog. The root declares every plugin `apply false`, which lifts KGP from AGP's bundled 2.2.10 to 2.4.21. `gradle.properties` gets 4 GB of heap and `niahKeystoreFile`, and `.gitignore` covers `*.apk`, `*.aab` and `release/`.
4. **`:shared`:** a KMP module (`kotlin-multiplatform` + `com.android.kotlin.multiplatform.library`) with targets android, jvm, iosArm64 and iosSimulatorArm64. It holds the placeholder `AppInfo` and one common test.
5. **`:app`:** built-in Kotlin, Compose with Material 3 (BOM 2026.09.00), and Hilt 2.60.1 via KSP 2.3.12.
   - Version and SDK levels: versionCode 2000000002 / 3.0.0, minSdk 26, targetSdk 36, compileSdk 37.
   - Release signing is configured only when all four `niah*` properties are present; the path is resolved with `rootProject.file`.
   - Launcher drawables and `strings.xml` come from 2.7.0.
   - The placeholder screen shows `AppInfo.NAME` from `:shared`.
6. **CI:** JDK 11 → 17 in all three jobs. Nothing else changed.
7. **Checks:** device, signing and manifest checks (evidence below).

## Deviations
- **Task 3:** settings included only `:app` until Task 4, because Gradle 9 rejects an `include` for a directory that doesn't exist yet. Installed `build-tools;36.1.0` (newest 36.x) rather than 36.0.0.
- **Task 3:** dropped `android.nonTransitiveRClass=true`. It's the AGP ≥ 8 default, so nothing changes. Found at code review.
- **Task 3:** `.gitignore` also gets `build/` (module build dirs) and `.kotlin/` (the KGP 2.x project cache).
- **Task 4:** enabled `withHostTest {}` on `:shared`'s android target, though the plan said off. Without it AGP warns on every build, and with it `allTests` runs the common tests on the Android host too. That resolved the plan's open question.
- **Task 5:** dropped the planned JUnit SmokeTest in `:app`. With no test sources, `:app:testDebug` is NO-SOURCE and passes, and the real tests live in `:shared`. Consequence: CI runs no tests until NAM-3 (see Follow-ups).
- **Task 5:** added `lint { disable += "HighAppVersionCode" }`. Lint errors on any versionCode this high, but 2000000002 is forced by Play history.
- **Task 5:** kept `tools:targetApi="31"` in the manifest. Removing it would add an UnusedAttribute warning for `dataExtractionRules`. Only `package=` was removed (AGP 9 uses `namespace`).
- **Task 7:** AC1 on API 36 used the release-signed APK. The `api36_niah` AVD had a release-signed 2.7.0 installed, and the debug key was rejected. Upgrading in place avoided wiping the emulator's data and also showed the Play update path works.
- **Task 7:** the AC4 no-properties check used `-PniahKeyAlias=` instead of an empty `GRADLE_USER_HOME`, which would have re-downloaded the whole toolchain. CI's assemble job, which has no secrets, is the second proof.
- **Task 7:** CI's `lintDebug -PbuildDir=lint` leaves `app/lint/` and `shared/lint/` behind locally. I deleted them; NAM-3 can drop the flag.
- **Scope (user decision):** the CI JDK bump is outside the ticket's stated scope (CI belongs to NAM-3). It was agreed at research so this PR can go green.

## Evidence
Verify (`./gradlew :app:assembleDebug :shared:allTests`, JDK 17.0.21) is green: `BUILD SUCCESSFUL`. Fresh test runs (test-result XML):
```
iosSimulatorArm64Test: tests="1" skipped="0" failures="0" errors="0" timestamp="2026-11-11T16:35:31.530Z"
jvmTest:               tests="1" skipped="0" failures="0" errors="0" timestamp="2026-11-11T16:35:37.073Z"
testAndroidHostTest:   tests="1" skipped="0" failures="0" errors="0" timestamp="2026-11-11T16:35:28.981Z"
```
`:shared:compileKotlinIosArm64` and `:shared:compileKotlinIosSimulatorArm64` are green on Xcode 16.4 with no compatibility warning. `:app:kspDebugKotlin` generated `Hilt_MainActivity`, `MainActivity_GeneratedInjector` and `NamesApplication_GeneratedInjector` on Kotlin 2.4.21. `:app:lintDebug` has 0 errors.

**AC3** (`aapt2 dump badging app-debug.apk`):
```
package: name='uk.lobsterdoodle.namepicker' versionCode='2000000002' versionName='3.0.0' ... compileSdkVersion='37'
minSdkVersion:'26'
targetSdkVersion:'36'
```
**AC4** (`apksigner verify --print-certs`):
```
app-release.apk                         SHA-256 4fa5cd07e6639b26219ff3df99f7f2f459fc31a4f71ca1b6ee64324ceecae951
names-in-a-hat-2.7.0-vc2000000001.apk   SHA-256 4fa5cd07e6639b26219ff3df99f7f2f459fc31a4f71ca1b6ee64324ceecae951
```
With `-PniahKeyAlias=` the build outputs `app-release-unsigned.apk` and `assembleDebug` succeeds.

**AC1**, on emulators:
- **API 26** (`niah_api26`, debug APK): `am start -W` gives Status ok, TotalTime 1042 ms, 0 `FATAL EXCEPTION`.
- **API 36** (`api36_niah`): `adb install -r` of the release APK over the installed 2.7.0 succeeded. `dumpsys` went from `versionCode=2000000000 versionName=2.7.0` to `versionCode=2000000002 versionName=3.0.0`. TotalTime 709 ms, 0 FATAL.
- On both, the screenshot (inspected) shows "Names in a Hat" over "Version 3.0 is on its way", centred. The landscape screenshot after rotation is the same, with no crash.
- Screenshots (local, not committed): `/private/tmp/claude-501/-Users-scoot-dev-names-in-a-hat/961050aa-7e93-4675-a4b5-4f3345e3932d/scratchpad/placeholder_api26.png`, `placeholder_api26_land.png`, `placeholder_api36.png` and `placeholder_api36_land.png` in the same folder.

**AC5:** `git merge-base --is-ancestor f9773d5 HEAD` returns 0. On `main` it holds only if the PR is merged with a **merge commit** (see the gate 2 decision).

**AC6:** the cruft grep over `*.gradle*`, `*.properties` and `*.toml` returns nothing (exit 1), and no `google-services.json` is tracked.

**AC7:** PR #22 CI run 43059997906: lint pass (2m15s), test pass (1m49s), assemble pass (3m1s), on ubuntu-latest with JDK 17.

Review: [review.md](review.md). 0 blocking, 2 advisory (1 fixed, 1 → NAM-3).

## Follow-ups
- **NAM-3 (CI):**
  - Run `:shared:allTests` (Linux: `jvmTest` + `testAndroidHostTest`; iOS on a macOS runner), since CI currently runs no tests.
  - Drop `-PbuildDir=lint`.
  - Update `actions/checkout` and `setup-java` from v3.
- **NAM-13 (cutover):**
  - Update `docs/play-store-compliance.md:20-21`, which still calls `master` the published app and `main` an abandoned skeleton.
  - Review the 34 lint warnings: mostly UnusedResources from the 2.7.0 strings, plus MonochromeLauncherIcon, ObsoleteSdkInt, OldTargetApi and RedundantLabel.
- **Repo housekeeping (user):**
  - Local `master` is stale at 77d6a8e. `origin/master` is the real 2.7.0, and there is no `v2.7.0` tag; consider tagging f9773d5.
  - The emulator's installed 2.7.0 was versionCode 2000000000 (the burned internal build), not the Play build.
