# Research: NAM-1 unify trunk + modern build

## Findings

### History / branches
- **Local `master` is stale.** It points at `77d6a8e` (2.6.1, versionCode 1910152108, compileSdk 34). The shipped 2.7.0 is on **`origin/master` = `f9773d5`** "Bump versionCode to 2000000001", which is 3 commits ahead: `79a4a01` API 36 → `0096449` insets fix → `f9773d5`. The merge must use `origin/master`. Merging local `master` would fail AC5. There is no `v2.7.0` tag; the latest tag is `v2.6`.
- Merge base of `main` and `origin/master` is `6488be0` (matches the ticket). `main` has 4 commits not on master; master has 4 not on main.
- `main` == `origin/main` == `daf7a6c`. Task branch `flow/unify-trunk-modern-build` is cut from it.
- Stale branches to ignore: `fresh-compose`, `overview`, `sqlite-room-migration`, `api36`, `versioncode-bump`, `upgrade-kotlin-etc`. The last three are pre-merge copies of master commits.

### `main` today (what gets replaced)
- Groovy build. AGP 7.2.2, Kotlin 1.5.31, Compose 1.1.0-beta01, Gradle 7.3.3 (`gradle/wrapper/gradle-wrapper.properties`), compileSdk/targetSdk 32, minSdk 21, versionCode 1 (`app/build.gradle:6-13`). JVM 11 target.
- **`:app:assembleDebug` builds green locally on JDK 17.0.21.** The ticket says nothing builds; that's true only for the CI JDK and for the stack's modernity.
- Sources live in `app/src/main/java/...`: `MainActivity`, `ui/App.kt`, `ui/groups/GroupsPage.kt`, `ui/theme/*`, `ui/common/CallToAction.kt`. There are example tests in `src/test` and `src/androidTest`.
- `res/xml/backup_rules.xml` and `data_extraction_rules.xml` exist on main. Backup rules belong to NAM-13 (out of scope).
- `.idea/*` is tracked even though `.gitignore` lists `/.idea`. `.gitignore` already ignores `android-keystore` and `local.properties`.
- `docs/` (index, privacy policy, play-store-compliance.md) and `.github/workflows/android.yml` stay as is.

### `origin/master` (2.7.0): what to carry over
- Signing: `app/build.gradle` `signingConfigs.release` reads `niahKeyAlias`, `niahKeyPassword`, `niahKeystoreFile`, `niahKeystorePassword`. `gradle.properties` sets `niahKeystoreFile=../android-keystore`. The other three are in `~/.gradle/gradle.properties` (present locally).
  - **Path gotcha:** `file(niahKeystoreFile)` in the app module resolves `../android-keystore` relative to `app/`, i.e. `<repo>/android-keystore`, which doesn't exist. The keystore is at `<repo>/../android-keystore` (`/Users/scoot/dev/names-in-a-hat/android-keystore`). With `rootProject.file(...)` the existing property value resolves correctly.
- Launcher icons: all `mipmap-*` PNGs and `mipmap-anydpi-v26/*.xml` are **byte-identical** on main and master. Only `drawable-v24/ic_launcher_foreground.xml` and `drawable/ic_launcher_background.xml` differ, and master's are the shipped ones.
- `strings.xml`: identical apart from two hint strings. Master has "e.g. Team Rocket" / "e.g. Jesse"; main has "e.g. Team Lobster Doodle" / "e.g. Sebastian". Take master's (shipped). Most strings belong to old screens and will be reworked by the screen tickets.
- Manifest (master): `.application.App`, `SplashActivity` launcher, a `lobsterdoodle.co.uk` VIEW deep-link intent filter, `allowBackup=true`, `fullBackupContent=true`. None of this belongs in the placeholder. Deep link and backup go to NAM-7 and NAM-13.
- Cruft (per the ticket) confirmed on origin/master: `kotlin-kapt` + `kapt { generateStubs }`, `lintOptions { abortOnError false }`, `fileTree` jars, `mavenLocal()`, `JAVA7_HOME`, `android.enableJetifier=true`. `io.fabric` and `google-services` (and `google-services.json`) are already gone from origin/master but still exist at local `master`/`77d6a8e`.

### Local toolchain
- JDK 17.0.21 (arm64) is the active Gradle JDK. Also installed: 18, 11, 8. Android SDK has platform `android-36` and build-tools up to 35.0.0. Xcode 16.4.
- AVDs: `Px7_API_34` and `api36_niah`. **There is no API 26 system image** (images: 24, 28, 30, 34, 36). AC1 needs `system-images;android-26;google_apis;arm64-v8a` plus a new AVD.

### CI (`.github/workflows/android.yml`)
- Runs on push/PR to `main`: `lintDebug`, `assembleDebug` and `:app:testDebug`, all on **JDK 11**. A modern AGP needs JDK 17+, so **this PR's CI will go red** until NAM-3. The ticket puts CI changes out of scope.

## Constraints & prior art
- applicationId/namespace `uk.lobsterdoodle.namepicker` must not change. versionCode must be > 2000000001 (2000000000 is burned).
- `:shared` commonMain: no Hilt and no Android-only libraries.
- `docs/play-store-compliance.md` §5: no Fabric or google-services in the build.
- The old domain (`core/`) is ported later by NAM-4 using `git show origin/master:...`. The merge just needs to make that history reachable.

## Toolchain versions (web research, 2026-11-11, primary sources: maven-metadata, dev.android.com, kotlinlang.org)

| Item | Pick | Why |
|---|---|---|
| Kotlin / compose-compiler plugin | 2.4.21 | Current stable (2.5.0 is at RC) |
| AGP + `com.android.kotlin.multiplatform.library` | 9.5.1 | Current stable. Needs Gradle ≥ 9.6.0, JDK 17, build-tools ≥ 36.0.0 |
| Gradle wrapper | **9.7.1** (not 9.9.1) | The newest version inside both AGP 9.5's minimum (9.6.0) and KGP 2.4.21's tested maximum (9.7.0). 9.9.1 is one day old |
| KSP | **2.3.12** (not 2.3.13) | 2.3.13's release notes flag a concurrency regression. KSP is no longer tied to Kotlin versions since 2.3.0 |
| Dagger/Hilt | 2.60.1 | Supported with KSP. Hilt's minSdk is now 23 and AGP 9 is the minimum |
| androidx.hilt | 1.4.0 | Needs compileSdk 37. Only add it if needed; the placeholder doesn't need `hiltViewModel()` |
| Compose BOM | 2026.09.00 | ui 1.12.1 / material3 1.4.0. **Needs compileSdk 37** |
| activity-compose / core-ktx / lifecycle | 1.13.0 / 1.19.1 / 2.11.0 | core-ktx 1.19.x **needs compileSdk 37** |

**compileSdk = 37, targetSdk = 36.** Current Compose, core-ktx and androidx.hilt all declare `minCompileSdk 37` in their AAR metadata. The ticket says "36 or newer", so this fits the spec. **The local SDK has no `android-37` platform**; install it with sdkmanager. The Play targetSdk floor is 36 (since 2026-08-31).

AGP 9 gotchas that shape the build:
- **Built-in Kotlin:** do NOT apply `org.jetbrains.kotlin.android` in `:app` (that errors). Use top-level `kotlin { compilerOptions {} }`; `android.kotlinOptions` is gone. `kotlin-kapt` is incompatible, so use KSP.
- AGP 9.5.1 pins KGP 2.2.10 by default. Declaring `org.jetbrains.kotlin.multiplatform` 2.4.21 `apply false` at the root puts 2.4.21 on the classpath for all modules. Verify with `buildEnvironment`.
- `:shared` must use `org.jetbrains.kotlin.multiplatform` + `com.android.kotlin.multiplatform.library`, configured as `kotlin { android { namespace; compileSdk; minSdk } }`. Mixing `com.android.library` with KMP isn't allowed under AGP 9. The new plugin has a single variant, no resources by default, and host tests off unless `withHostTest {}`.
- `:shared:allTests` covers JVM + `iosSimulatorArm64Test` (needs macOS + Xcode). Whether it includes `androidHostTest` is unverified; check with `--dry-run`. The AC only requires JVM.

Unverified risks to check early in implementation:
- Hilt 2.60.1 (it bundles kotlin-metadata-jvm 2.3.21) reading Kotlin 2.4 metadata. Run `:app:kspDebugKotlin` first. Fallback: Kotlin 2.3.x.
- KGP 2.4.21 vs Xcode 16.4 for the iOS targets. Kotlin/Native may want a newer Xcode (a warning or a hard error). If it's an error: upgrade Xcode, or set `kotlin.apple.xcodeCompatibility.nowarn`.
- KGP 2.4.21 + AGP 9.5.1 is outside Kotlin's published tested matrix (AGP ≤ 9.3.1).

## Open questions / assumptions
- Merge mechanics: `git merge -s ours origin/master` records the history (AC5) while keeping main's tree, and the new tree is built in later commits. This is conventional, so it's recorded rather than asked. Carried files are pulled with `git checkout origin/master -- <path>`.
- Keystore path: use `rootProject.file(niahKeystoreFile)`. Make release signing conditional on the four properties being present so `assembleDebug` and CI don't need secrets. Conventional; recorded.
- versionCode: Play's ceiling is **2100000000**, so only about 100M codes remain above 2000000001. Pick something small and monotonic, e.g. `2000000002` (+1 per release) or a scheme like `2003000000` (2 + major/minor/patch digits). Tunable; see the questions.
- API 26 emulator image must be downloaded during implementation (owner: implementation).
- CI red on this PR: **answered.** Do a minimal JDK 17 bump in `android.yml` (plus whatever task-name fix keeps it green) in NAM-1. The full rework stays in NAM-3.
- versionCode: **answered.** 2000000002, +1 per release.
- compileSdk 37 (not 36): forced by library metadata and within the ticket's "36 or newer". Recorded, not asked.
