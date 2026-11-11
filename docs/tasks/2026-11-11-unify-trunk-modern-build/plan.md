# Plan: Unify the trunk and stand up the modern build (NAM-1)

## Intent
Make `main` the single trunk. Merge the shipped 2.7.0 history from `origin/master` into it, then replace the 2022 skeleton with an empty, modern two-module project: `:app` (Compose + Hilt) and `:shared` (KMP). Every later 3.0 ticket builds on it. The output is a placeholder screen; there are no features.

## Acceptance criteria
- [ ] **AC1** `./gradlew :app:assembleDebug` succeeds, and the installed APK launches to a placeholder Compose screen on an API 26 emulator and an API 36 emulator. The screen shows text sourced from `:shared`. Check: screenshot, plus `adb logcat` showing no FATAL.
- [ ] **AC2** `./gradlew :shared:allTests` passes, and `shared/build/test-results/jvmTest/TEST-*.xml` reports `tests` ≥ 1 with 0 failures. On this Mac, `:shared:compileKotlinIosArm64` and `:shared:compileKotlinIosSimulatorArm64` succeed.
- [ ] **AC3** `aapt2 dump badging app/build/outputs/apk/debug/app-debug.apk` shows `package: name='uk.lobsterdoodle.namepicker' versionCode='2000000002' versionName='3.0.0'`, `minSdkVersion:'26'` and `targetSdkVersion:'36'`.
- [ ] **AC4** With the four `niah*` properties supplied, `./gradlew :app:assembleRelease` produces an APK. `apksigner verify --print-certs` on it shows the same certificate SHA-256 as `../release-2.7.0/names-in-a-hat-2.7.0-vc2000000001.apk`. Without those properties, `assembleDebug` still works. Evidence: `GRADLE_USER_HOME=$(mktemp -d) ./gradlew :app:assembleDebug -PniahKeystoreFile=` style run, or CI's assemble job (AC7), which has no secrets.
- [ ] **AC5** Before the PR: `git merge-base --is-ancestor f9773d5 HEAD` exits 0. **After the PR merges:** `git fetch && git merge-base --is-ancestor f9773d5 origin/main` exits 0. This depends on the merge-strategy decision; see Assumptions.
- [ ] **AC6** `git grep -nIE 'kapt|enableJetifier|fabric|google-services|JAVA7_HOME|mavenLocal|fileTree' -- '*.gradle*' '*.properties' '*.toml'` returns nothing, and no `google-services.json` exists in the tree.
- [ ] **AC7** (from the CI decision) The PR's GitHub Actions run (lint, assemble, test) is green.

## Approach
- **History:** `git merge -s ours origin/master` on the task branch. This records 2.7.0 as a parent while keeping main's tree. Later commits then replace the tree. Shipped files (icons, strings) are pulled with `git checkout origin/master -- <path>`. Alternative: a real merge resolving every conflict by hand. Rejected, because every conflict is resolved toward the new tree anyway and `-s ours` says that explicitly.
- **Build:** Gradle Kotlin DSL with `gradle/libs.versions.toml`. Versions are from research.md: Kotlin 2.4.21, AGP 9.5.1, Gradle 9.7.1, KSP 2.3.12, Hilt 2.60.1, Compose BOM 2026.09.00, activity-compose 1.13.0, core-ktx 1.19.1, lifecycle 2.11.0.
  - compileSdk is 37 because the libraries' metadata requires it. targetSdk is 36 and minSdk 26.
  - JDK 17 toolchain.
  - `:app` uses AGP 9 built-in Kotlin, so no `kotlin-android` plugin.
  - `:shared` uses `kotlin-multiplatform` + `com.android.kotlin.multiplatform.library`.
  - The root declares the KMP plugin `apply false` to put KGP 2.4.21 on the classpath.
- **Signing:** `signingConfigs.release` is created only when all four `niah*` properties are present. The keystore path is resolved with `rootProject.file(...)`.
- **De-risk first:** stand up the build and run `:app:kspDebugKotlin` and the iOS compile before writing anything else. If Hilt fails on Kotlin 2.4 metadata or Kotlin/Native refuses Xcode 16.4, apply the fallbacks in Assumptions and log a deviation.

## Assumptions & open questions
- [answered 2026-11-11] Full mode. `.flow.md` saved with Linear sync. The dirty `.idea` edits were discarded.
- [answered 2026-11-11] CI gets a minimal change in this PR: JDK 11 → 17, plus whatever task-name fix keeps it green. The full rework stays in NAM-3. Logged as a scope deviation from the ticket.
- [answered 2026-11-11] versionCode is `2000000002`, then +1 per release. Play's ceiling is 2100000000.
- [assumed] compileSdk is 37, not 36. Compose BOM 2026.09.00, core-ktx 1.19.x and androidx.hilt declare `minCompileSdk 37` (research.md, Toolchain versions). This satisfies the ticket's "36 or newer".
- [assumed] Merge `origin/master` (f9773d5), not local `master`. Local `master` is stale at 77d6a8e / 2.6.1 (research.md, History). The local `master` ref is left untouched, as the ticket requires.
- [assumed] Gradle 9.7.1 and KSP 2.3.12, one step behind the newest. Gradle 9.7.1 is inside KGP 2.4.21's tested range. KSP 2.3.13 has a flagged concurrency regression (research.md).
- [assumed] Fallback if Hilt 2.60.1 can't read Kotlin 2.4 metadata: drop Kotlin to the latest 2.3.x and log a deviation. If Kotlin/Native hard-fails on Xcode 16.4, set `kotlin.apple.xcodeCompatibility.nowarn=true` if it's only a warning; otherwise stop and ask (installing Xcode is the user's call).
- [assumed] Tracked `.idea/*` files are removed from the index with `git rm --cached`. `.gitignore:5` already ignores `/.idea`, and the tracked `gradle.xml` would point at the deleted Groovy build. Local IDE files are kept.
- [assumed] main's manifest backup attributes and `res/xml/backup_rules.xml` / `data_extraction_rules.xml` are kept unchanged. Backup rules belong to NAM-13. Only the `package=` attribute is removed, since AGP 9 rejects it in favour of `namespace`. `tools:targetApi` is dropped.
- [assumed] Launcher icons: all mipmaps are byte-identical between main and origin/master. Take origin/master's `drawable-v24/ic_launcher_foreground.xml` and `drawable/ic_launcher_background.xml` (the shipped ones) and its `strings.xml`. The only differences are two hint strings (research.md).
- [assumed] The placeholder theme is a minimal `Theme.Material3`-free XML theme (`android:Theme.Material.Light.NoActionBar`, as on main) plus Compose `MaterialTheme` with M3 defaults. The real theme belongs to NAM-7. main's `ui/theme/*`, `GroupsPage`, `CallToAction` and `App.kt` are deleted, as are `res/values/colors.xml` unless the theme needs it.
- [assumed] `:shared` namespace is `uk.lobsterdoodle.namepicker.shared`; the Kotlin package is the same. It is not an applicationId and has no Play impact.
- [assumed] Hilt's minSdk is 23 (Dagger 2.59 or newer), which is below our 26. No multidex needed.
- [answered 2026-11-11] Merge path: at gate 2, switch off linear history for this PR, merge it with a **merge commit** (not squash), then switch the rule back on. Each `gh` protection change still needs the user's OK at that moment. Background: **Branch protection blocks the history merge.** `main` has `required_linear_history: true` with `enforce_admins: true`, verified via `gh api .../branches/main/protection`. The repo squash-merges (`daf7a6c` and `3deec93` each have one parent). Squash or rebase drops the `-s ours` parent, so the ticket's AC5 fails on `main`. A merge commit can land only if linear history is relaxed for this PR.
- [assumed] SDK IDs: AGP's `compileSdk = 37` maps to `platforms;android-37.0`; the critic found no `platforms;android-37` in sdkmanager. Build-tools ≥ 36.0.0 are needed (local max is 35.0.0). Both are installed in Task 3, before any AGP 9 build, rather than relying on auto-download.
- [assumed] `org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8`. main's 2 GB heap is tight for Kotlin/Native compile and link in the same daemon as KSP, and this machine has never run K/N (`~/.konan` is absent).
- [assumed] Add `*.apk`, `*.aab` and `release/` to `.gitignore`. master ignored them, and the `-s ours` merge doesn't carry master's `.gitignore`.
- [open — implementation] Whether `allTests` includes the `androidHostTest`. It's not needed for AC2, so host tests stay off (`withHostTest` not enabled) and the JVM covers common code.

## Tasks
1. **Merge history.** `git merge -s ours --no-ff origin/master -m "Merge 2.7.0 (origin/master) into main trunk"`. Files: none. Verify: AC5 command.
2. **Clear the old skeleton.**
   - `git rm` the Groovy build files (`build.gradle`, `settings.gradle`, `app/build.gradle`).
   - `git rm` `app/src/main/java/**`, `app/src/test/**` and `app/src/androidTest/**`.
   - Remove `.idea/` from the index with `git rm --cached -r`.
   - Delete `app/src/main/res/values/colors.xml` if it's unused.
   Verify: `git status` shows only the intended deletions.
3. **Gradle skeleton.**
   - **Order matters:** run the wrapper bump first, while the root has no build scripts left after Task 2. Use `./gradlew wrapper --gradle-version 9.7.1` with the old 7.3.3 wrapper, then `./gradlew wrapper` again on 9.7.1 to refresh the jar. There is no system `gradle`. Only then write the Kotlin DSL files.
   - Install SDK packages up front: `sdkmanager "platforms;android-37.0" "build-tools;36.0.0"` (the newest 36.x if 36.0.0 isn't listed).
   - Files: `gradle/wrapper/*` + `gradlew*`, `gradle/libs.versions.toml`, `settings.gradle.kts` (`:app`, `:shared`, repos google + mavenCentral, FAIL_ON_PROJECT_REPOS), `build.gradle.kts` (plugins `apply false`), `gradle.properties` and `.gitignore`.
     - `gradle.properties`: keep main's settings, change jvmargs to `-Xmx4g`, add `niahKeystoreFile=../android-keystore`. No Jetifier, no JAVA7_HOME.
     - `.gitignore`: add `*.apk`, `*.aab`, `release/`.
   - Verify: `./gradlew help` and `./gradlew buildEnvironment | grep kotlin-gradle-plugin` shows 2.4.21.
4. **`:shared` module.**
   - Files: `shared/build.gradle.kts` (jvm, iosArm64, iosSimulatorArm64, and `android { namespace; compileSdk 37; minSdk 26 }`, JVM 17), `shared/src/commonMain/kotlin/uk/lobsterdoodle/namepicker/shared/AppInfo.kt` (e.g. `object AppInfo { const val NAME = "Names in a Hat" }`), and `shared/src/commonTest/.../AppInfoTest.kt` (kotlin-test).
   - Verify: `./gradlew :shared:allTests :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64`.
5. **`:app` module.**
   - `app/build.gradle.kts`:
     - Plugins: android-application, compose compiler, ksp, hilt.
     - `namespace` and `applicationId` `uk.lobsterdoodle.namepicker`; compileSdk 37, minSdk 26, targetSdk 36; versionCode 2000000002, versionName "3.0.0".
     - Conditional release signing; `buildFeatures.compose`; release minify off with main's `proguard-rules.pro`.
     - Dependencies: `project(":shared")`, the Compose BOM + material3 + ui-tooling(-preview), activity-compose, core-ktx, `hilt-android` + `ksp(hilt-compiler)`, and JUnit for a smoke unit test so CI's test job has something to run.
   - Sources: `app/src/main/kotlin/uk/lobsterdoodle/namepicker/NamesApplication.kt` (`@HiltAndroidApp`) and `MainActivity.kt` (`@AndroidEntryPoint`, `enableEdgeToEdge`, M3 `Surface` showing `AppInfo.NAME` + "3.0 coming soon").
   - Test: `app/src/test/kotlin/.../SmokeTest.kt`.
   - Manifest: `app/src/main/AndroidManifest.xml` drops `package=`, adds `android:name=".NamesApplication"`, keeps backup attributes.
   - Resources:
     - `res/values/themes.xml`: as on main, minus the colour reference if `colors.xml` is gone.
     - `git checkout origin/master -- app/src/main/res/values/strings.xml app/src/main/res/drawable-v24/ic_launcher_foreground.xml app/src/main/res/drawable/ic_launcher_background.xml`.
   - Verify: `./gradlew :app:kspDebugKotlin` (run first; this is the Hilt/Kotlin 2.4 risk), then `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`.
6. **CI minimal bump.**
   - File: `.github/workflows/android.yml`. Change `java-version: '11'` → `'17'` in all 3 jobs and rename the step labels to match. Change `:app:testDebug` → `:app:testDebugUnitTest` if the abbreviation is ambiguous under AGP 9.
   - Leave everything else for NAM-3.
   - Verify: run the three CI commands locally on JDK 17. AC7 is checked on the PR.
7. **Device and signing checks.**
   - No repo files change.
   - Install `system-images;android-26;google_apis;arm64-v8a` and create an AVD `niah_api26`.
   - Run the APK on `niah_api26` and `api36_niah`.
   - Run AC3 (aapt2), AC4 (assembleRelease + apksigner vs the 2.7.0 APK) and AC6 (grep).
   - Verify: each AC's command or screenshot is recorded for report.md.

## Out of scope
- The full CI rework (actions versions, `:shared` tests, the iOS job): NAM-3.
- Any domain, data, DI graph or UI beyond the placeholder: NAM-4 to NAM-10.
- R8/minify, backup-rule changes, manifest clean-up, the deep-link intent filter, and deleting `master` or the stale branches: NAM-13.
- Updating the local `master` ref.
- `docs/play-store-compliance.md:20-21` still calls `master` the published app and `main` an abandoned skeleton. That goes stale with this PR, but the ticket says to keep `docs/` as is. Fix it in NAM-13 when `master` is retired.

## Verification
- Machine: `./gradlew :app:assembleDebug :shared:allTests` (the `.flow.md` verify), plus `:app:lintDebug :app:testDebugUnitTest :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64`, and the AC3–AC6 commands.
- Outcome:
  - Install `app-debug.apk` on API 26 and API 36 emulators, launch from the launcher icon (it's the shipped hat icon), and see the placeholder screen with the app name from `:shared`. Rotate once; there should be no crash.
  - Optional and cheap: on the API 36 emulator, install the release-signed 3.0 APK over 2.7.0 with `adb install -r`. That's an early signal that the update path is accepted. It isn't an AC, and data migration is NAM-5/6.
