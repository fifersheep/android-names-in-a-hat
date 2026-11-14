---
task: app-shell-navigation-theme
title: Build the app shell: single activity, navigation, theme and insets
ticket: NAM-7 — Build the app shell: single activity, navigation, theme and insets (https://linear.app/northtrack/issue/NAM-7/build-the-app-shell-single-activity-navigation-theme-and-insets)
ticket-id: NAM-7
tracker-synced: In Review
mode: full
phase: reporting
branch: flow/app-shell-navigation-theme
worktree:
created: 2026-11-14
updated: 2026-11-14
---

## Next action
report.md written and PR #28 opened (https://github.com/fifersheep/android-names-in-a-hat/pull/28); NAM-7 → In Review with the PR linked. Verify PASS at 4ca74b2 (app 45/0, shared 255/0, lint clean, iOS compile). CI was pending when the PR opened. Holding at gate 2 for merge approval.

## Decision log
- 2026-11-14 gate 1: plan approved by user (including removing Room's unused MultiInstanceInvalidationService from the merged manifest for AC1)
- 2026-11-14 Q: what to bind for PreferencesRepository before NAM-6? A: another agent is building NAM-6 now. Wait for it to merge and use the real implementation; no fake.
- 2026-11-14 Q: stale active-list id vs not gating the splash on the DB? A: route optimistically. The splash waits for the prefs read only; Draw pops to Lists and clears the id if the list is gone.
- 2026-11-14 mode: full (user confirmed). Reason: new libraries (Hilt, Navigation 3, material3 1.5 RC, adaptive), many files, and open design questions (list-detail scenes, process-death restore, launch routing).

## Progress
- [x] 1. Build dependencies (14fa478): material3 resolves to 1.5.0-rc01, compose ui stays 1.12.1, adaptive-navigation3 1.3.0 from the BOM
- [x] 2. Theme and font (a58720e): font is 41,259 B compressed in the APK (76,580 B installed), plus 2,140 B for OFL.txt
- [x] 3. Icon, splash and manifest (f88898a): release merged manifest has 1 activity and 0 services; lintDebug passes
- [x] 4. DI (d059d23): StorageModule compiles; the Hilt graph is checked by task 6
- [x] 5. Navigation model (5f5b82b): BackStackTest 18/0 and LayoutTest 7/0; spec EditList row updated
- [x] 6. Launch (60c2cfd): ShellViewModelTest 6/0; Hilt graph compiles with the StorageModule bindings
- [x] 7. NavDisplay and placeholders (993700a): 41 :app tests / 0 failures; lint clean. Smoke-tested on niah_debug_api36: rules 1, 2, 3 and 5; relaunch into Draw; stale id goes to Lists twice; process death restores EditList and its unsaved text
- [x] 8. Device verification (8caafd4). Evidence is in screens/.
  - AC1: release merged manifest has 1 activity and 0 services.
  - AC2: back-stack rules 1, 2, 3 and 5 walked on niah_debug_api36.
  - AC3: predictive back held mid-gesture shows Lists behind Draw (API 36, gesture nav). On the tablet (1280x900 dp): list-detail with a 360 dp Lists pane; picking a list replaces the detail; back from EditList goes to Draw, then the placeholder; editing another list gets fresh state; deleting the open list closes the detail. A phone in landscape stays single pane.
  - AC4: API 26, 30 and 36, portrait and landscape, gesture and 3-button nav, with the tall cutout overlay on 30 and 36: nothing hidden.
  - AC5: API 36 is dynamic (primaryContainer #D9E2FF); API 26 is the fallback, exact (surface #FCF8FF and primaryContainer #E2DFFF light; #131318 and #35319A dark); API 30 is the fallback; night mode switches live.
  - AC6: relaunch opens Draw; after a back-out it opens Lists; a stale id (row deleted via sqlite3) opens Lists, twice.
  - AC7: after process death, EditList and its unsaved text are restored on the phone and in two-pane on the tablet.
  - AC8: grep finds nothing.
  - Splash: #35319A with the hat on API 26, 30 and 36. Icon: correct on API 26 and 36.
  - Final verify: shared 255/0, app 41/0, lint clean, debug APK 19,683,589 B.

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
- 2026-11-14 Rebased baseline (c25a883 + docs): `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests` PASS, 255 tests / 0 failures (NAM-6 added 21). Debug APK 17,589,808 B before task 1.
- 2026-11-14 Task 2: the fallback schemes also set background/onBackground to surface/onSurface (the plan said unlisted roles keep token defaults). The token background differs from the spec's surface, and the two shouldn't disagree.
- 2026-11-14 Task 2: MainActivity's placeholder switched to NamesTheme now, so the font ships in the size measurement. Task 6 rewrites MainActivity anyway.
- 2026-11-14 Task 3: the Room service removal needs `tools:ignore="MissingClass"`. Room is an implementation dependency of :shared, so lint can't see the class. Also, the first assemble after deleting drawable-v24 failed on a stale resource merge; `:app:clean` fixed it.
- 2026-11-14 Task 5: added `kotlin-test-junit` to the :app test dependencies. AGP 9 built-in Kotlin doesn't infer the JUnit adapter, so `kotlin.test.Test` didn't resolve. The plan listed only kotlin-test and junit.
- 2026-11-14 Task 6: the in-test fake is `FakePreferences` in :app's test sources; the plan said it would sit inside ShellViewModelTest. DrawViewModelTest and EditListViewModelTest reuse it.
- 2026-11-14 Task 7: `LocalListDetailSceneScope` is also `@ExperimentalMaterial3AdaptiveApi`; opted in on the placeholder Up button (the same accepted opt-in). Confirmed in navigation3-ui 1.2.1 source that NavDisplay calls onBack once per entry to pop (NavigationEventHandler.kt:53), so `back()` popping one per call is correct.
- 2026-11-14 Task 7: DrawViewModel calls `ActiveGroup.open` only once the list is known to exist (first Present), not in `init` as the plan said. Opening in init would race the Gone path's `clearActiveGroupIf` and could re-store a stale id.
- 2026-11-14 Task 7: added an Up icon vector (`res/drawable/ic_arrow_back.xml`) because material3 1.5 doesn't bring material-icons. Also `ListStatus.Unsaved` for Edit list before creation, and a placeholder-only string `placeholder_create`.
- 2026-11-14 INCIDENT, task 7 device check: I ran the smoke test on `api36_niah`, which the memory says holds the release-signed 2.7.0 app and its data and must never be cleared. The debug install failed on the signature, and my script then ran `pm clear uk.lobsterdoodle.namepicker` on the 2.7.0 app. Recovery: the emulator had booted from snapshot `default_boot` with `-no-snapshot-save`, so I killed it without saving. A `-read-only` reboot confirmed the 2.7.0 data is intact (it opens into the active list "Team Red" with names Dana and Eli). From now on, 3.0 debug checks use the new AVD `niah_debug_api36` (Pixel 6, android-36 arm64). The plan's AC mentions of api36_niah are read as niah_debug_api36.
- 2026-11-14 The android-30 system image installed here is x86, which can't run on this arm64 Mac. Task 8 needs `system-images;android-30;google_apis;arm64-v8a` downloaded to create niah_api30.
- 2026-11-14 Task 8: predictive back was checked on niah_debug_api36 (API 36, gesture nav), not Px7_API_34 as the plan said. It's the same in-app predictive behaviour (API 34+), and I didn't inspect Px7_API_34's contents.
- 2026-11-14 Task 8: the themed (monochrome) icon couldn't be switched on from adb (the launcher provider needs BIND_WALLPAPER). The monochrome layer builds and ships, but it hasn't been checked on a device: carried to the report for the owner.
- 2026-11-14 Task 8: AC5 sampled primaryContainer (the FAB) rather than primary, because the placeholders show no primary fill. The fallback match is exact.
- 2026-11-14 Task 8: the first tablet process-death run was invalid (`am kill` left the process alive). The rerun confirmed the process was gone, and restored EditList with its text. The UI dump after pressing back on the restored tablet came back empty (probably mid-animation), so back-after-restore in two-pane wasn't captured. On the phone it was.
- 2026-11-14 Task 8: a splash fix was found and made during verification (9bd0dd4): the status-bar icons were dark on the indigo splash.
- 2026-11-14 Task 8: created AVDs niah_debug_api36 (Pixel 6), niah_tablet_api36 (pixel_c) and niah_api30 (Pixel 6), and downloaded system image android-30 google_apis arm64-v8a.
- 2026-11-14 Review fixes changed the plan's approach in two places: the active list is now set from the Draw entry's `LifecycleStartEffect` (via `onShown`), not inside the ViewModel; and `listCreated` takes the visit's session.
