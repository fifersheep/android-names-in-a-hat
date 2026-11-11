# Review — Bring CI up to date for the new build (NAM-3)

Reviewer: flow-reviewer, one pass over `origin/main...HEAD` (82285a1..30afbb7) against plan.md.

## Blocking
None.

## Advisory
- **The PR merge state was stale in workflow.md.** "CLEAN" was recorded for c5dbc64; the head had since moved to 30afbb7 and its run was still in progress.
  - Done: the head moved again to 5ff21e3 (the fix below). Its run, https://github.com/fifersheep/android-names-in-a-hat/actions/runs/43103836715, passed all 4 jobs, and the PR shows `mergeStateStatus: CLEAN`. The wrap must re-check the head before merging.
- **Merge method.** The repo allows merge commits and rebase-merge, so anything but squash would put the red-proof commits fb601c6 and c342bee onto `main`.
  - Done: already required by the plan and stated in the PR body. The wrap merges with `gh pr merge --squash`.
- **Kotlin/Native cache key.** The key hashed all of `libs.versions.toml` and had a `restore-keys` fallback. Every Kotlin bump would stack a new toolchain onto the old `~/.konan`, and any unrelated dependency bump would create a new cache entry.
  - Done: **fixed in 5ff21e3.** The key is now `konan-<os>-<arch>-kotlin-<version>`, with the version read from `libs.versions.toml` and no fallback. Run 43103836715 resolved `konan-macOS-ARM64-kotlin-2.4.21`, missed (new key) and saved.

## Acceptance criteria
| AC | Status | Evidence |
|----|--------|----------|
| AC1: four jobs run on a PR to `main` and go green | MET | Runs [43085406431](https://github.com/fifersheep/android-names-in-a-hat/actions/runs/43085406431) (851b5bd), [43087070902](https://github.com/fifersheep/android-names-in-a-hat/actions/runs/43087070902) (c5dbc64) and [43103836715](https://github.com/fifersheep/android-names-in-a-hat/actions/runs/43103836715) (5ff21e3): all 4 success |
| AC2: failing `:shared` test turns `unit-tests` red | MET | [43085988075](https://github.com/fifersheep/android-names-in-a-hat/actions/runs/43085988075) on fb601c6: `AppInfoTest[jvm] > appNameMatchesThePlayListing[jvm] FAILED`; other 3 green |
| AC3: `iosMain` compile error turns `ios-compile` red | MET | [43086529337](https://github.com/fifersheep/android-names-in-a-hat/actions/runs/43086529337) on c342bee: `Broken.kt:4:17 Initializer type mismatch`, `:shared:compileKotlinIosArm64 FAILED`; other 3 green |
| AC4: no signing secret | MET | `grep -nE 'secrets\.\|niah' .github/workflows/android.yml` → no match (rechecked after 5ff21e3) |
| AC5: frequency comment at the top | MET | `android.yml:3-6` |
| AC6: four jobs are required checks on `main` | MET | `required_status_checks.contexts` = lint, unit-tests, assemble, ios-compile; `strict: false` |

## Checked (by reviewer)
- The temp commits net to zero: `git diff origin/main HEAD -- shared/ app/` is empty, and no `iosMain` file is tracked at HEAD.
- The diff touches only `android.yml`, plan.md and workflow.md. There is no scope creep and no weakened verification.
- Action versions, Gradle tasks and the dropped `-PbuildDir`/`chmod` all match the plan.
- Triggers and the concurrency block are unchanged, and runs on `main` are never cancelled (each gets a unique `run_id` group).
- No job has a `name:`, so the check contexts equal the job IDs and match the protection contexts.
- setup-gradle writes its cache from `main` only, which is expected.
- Nothing else references the old `test` job ID.

## Re-verify
- `./gradlew :app:assembleDebug :shared:allTests`: BUILD SUCCESSFUL (2026-11-11, after 5ff21e3; the net diff has no Gradle or source changes).
- Outcome: CI itself is the feature. The latest PR run is green, both red runs were seen and their logs read (above), and the PR merge state against the new required checks is CLEAN.
