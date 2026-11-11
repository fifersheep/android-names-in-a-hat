# Plan — Bring CI up to date for the new build

## Intent
Replace the 2022 Compose-skeleton CI with four jobs that match the NAM-1 build: lint, unit tests, assemble (Linux) and an iOS compile of `:shared` (macOS). After that, make them required checks on `main`. CI must never need a signing secret.

## Acceptance criteria
- [ ] AC1: a PR to `main` runs `lint`, `unit-tests`, `assemble` and `ios-compile`, and all four go green. Check: the PR's checks tab.
- [ ] AC2: a temporary commit with a failing `:shared` commonTest turns `unit-tests` red. Check: the link to the run, recorded in report.md.
- [ ] AC3: a temporary commit with a compile error in a new `shared/src/iosMain` file turns `ios-compile` red. Check: the link to the run, recorded in report.md.
- [ ] AC4: no job references `secrets.*` or a `niah*` property. Check: `grep -nE 'secrets\.|niah' .github/workflows/android.yml` returns nothing.
- [ ] AC5: a comment at the top of the workflow gives the iOS job frequency (every PR plus pushes to `main`) and the reason (public repo, so standard macOS runners are free). Check: read the file.
- [ ] AC6: `main` branch protection lists the four job names as required status checks. Check: `gh api repos/fifersheep/android-names-in-a-hat/branches/main/protection --jq .required_status_checks.contexts`.

## Approach
Rewrite `.github/workflows/android.yml` as four jobs. They share one pattern: `actions/checkout@v7`, `actions/setup-java@v6` (Temurin 17), and `gradle/actions/setup-gradle@v6` for the Gradle cache, which by default writes the cache only from `main`. The jobs:
- `lint`: `./gradlew :app:lintDebug`. The unused `-PbuildDir=lint` flag is dropped.
- `unit-tests`: `./gradlew :shared:jvmTest :app:testDebugUnitTest`.
- `assemble`: `./gradlew :app:assembleDebug`.
- `ios-compile` on `macos-latest`: `./gradlew :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64`, plus `actions/cache@v6` on `~/.konan`, keyed on `hashFiles('gradle/libs.versions.toml')`.

The triggers and the `concurrency` block stay as they are. `chmod +x gradlew` is dropped because the file is already executable in git (checked in task 1).

Alternatives considered:
- `setup-java`'s `cache: gradle` instead of setup-gradle: it caches less well and isn't the action Gradle recommends.
- A reusable composite action for the shared steps: too much for three steps × four jobs.
- `:shared:allTests` in `unit-tests`: on Linux it would also pull in the Android host tests, and the spec names the JVM target only.

## Assumptions & open questions
- [answered 2026-11-11] iOS job frequency: every PR plus pushes to `main`. The repo is PUBLIC (`gh repo view` → `visibility: PUBLIC`), so standard macOS runner minutes are free. Recorded as a comment at the top of the workflow file (AC5).
- [answered 2026-11-11] AC2/AC3 proof: temporary commits on the real PR, each red run's link recorded, then reverted. The repo allows rebase-merge as well as squash, and #22 used a merge commit, so the wrap MUST merge this PR with `gh pr merge --squash` to keep the temp commits off `main`.
- [answered 2026-11-11] Make the four jobs required status checks on `main`, after they've run once and with the user's OK at that moment.
- [assumed] No signing change is needed. NAM-1 already made release signing optional (`app/build.gradle.kts:12-15`, `:32-42`, `:48-50`), and CI builds debug only.
- [assumed] The jobs have no `name:` key, so their check contexts equal the job IDs that task 4 marks as required. A `name:` that differs would leave required checks that never report.
- [assumed] `:app` has no test sources yet (`app/src` has only `main`), so `:app:testDebugUnitTest` is NO-SOURCE. Only `:shared:jvmTest` gets a red proof (AC2). This is accepted; screen tickets will add `:app` tests.
- [assumed] Job renames are safe: `main` has no required checks today (protection API: `contexts: []`).
- [assumed] JDK 17 is what the build needs: `shared/build.gradle.kts:7` `jvmToolchain(17)`, `app/build.gradle.kts:55-56`, and NAM-1 commit daffd49 "ci: run on JDK 17 for the AGP 9 build".
- [assumed] Current action majors are checkout v7, setup-java v6, gradle/actions v6 and cache v6, from the GitHub releases API on 2026-11-11.
- [assumed] `macos-latest` ships an Xcode that Kotlin 2.4.21's Kotlin/Native accepts for `compileKotlinIos*`. A klib compile doesn't link, so Xcode version pressure is low. If the job fails on this, pin `macos-15` or add `maxim-lobanov/setup-xcode`, and log it as a deviation.
- [assumed] The branch is pushed and the PR opened during implementation (task 2), because the GitHub-side criteria can't be checked any other way. The PR stays a draft until the wrap's gate.

## Tasks
1. Rewrite the workflow as described in Approach, with the frequency comment at the top. Files: `.github/workflows/android.yml`. Verify: `git ls-files -s gradlew` shows mode 100755; YAML parses (`ruby -ryaml -e 'YAML.load_file(".github/workflows/android.yml")'`; this checks parsing only, and the PR run in task 2 is the real test of the workflow); no job has a `name:` key; AC4 grep is empty; the same Gradle tasks pass locally: `./gradlew :app:lintDebug :shared:jvmTest :app:testDebugUnitTest :app:assembleDebug :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64`.
2. Push `flow/modernise-ci` and open a draft PR to `main`. Files: none. Verify: AC1, all four jobs green (`gh pr checks`).
3. Red proofs. (a) Commit a failing assertion in `shared/src/commonTest/.../AppInfoTest.kt`, push, record the red `unit-tests` run, then revert. (b) Commit `shared/src/iosMain/kotlin/.../Broken.kt` with a type error, push, record the red `ios-compile` run, then revert. Before each next push, wait for the red run to finish (`gh run watch`). `cancel-in-progress` would otherwise turn it from failed into cancelled. Files: those two, temporary only. Verify: the AC2/AC3 run links are noted in workflow.md Progress straight away (copied into report.md at wrap), and the final head is green again.
4. With the user's OK, set the required status checks on `main` to `lint`, `unit-tests`, `assemble`, `ios-compile` (`gh api -X PATCH .../protection/required_status_checks`, `strict: false`). Files: none (GitHub setting). Verify: AC6.

## Out of scope
- Signed release builds and Play publishing (NAM-11).
- Instrumented or emulator tests.
- iOS simulator tests or linking iOS frameworks.
- Lint on `:shared`.

## Verification
- Machine: `./gradlew :app:assembleDebug :shared:allTests` (repo verify), plus the full task list from task 1, run locally.
- Outcome: the PR's checks tab shows four green jobs. The two recorded red runs show each failure mode is caught. After task 4, branch protection lists the four checks.
