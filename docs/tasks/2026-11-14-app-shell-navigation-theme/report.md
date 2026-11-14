# Report: build the app shell — single activity, navigation, theme and insets (NAM-7)

## Changed vs plan
All 8 plan tasks are done ([plan.md](plan.md); Progress in [workflow.md](workflow.md)). Every acceptance criterion is met except one partial: the themed (monochrome) icon couldn't be switched on from adb, so it's left for the owner to check on a device. See [review.md](review.md) for the review.

| Task | Commit | What |
|---|---|---|
| 1 Build dependencies | 14fa478 | material3 is pinned to **1.5.0-rc01** outside the BOM (compose-ui stays 1.12.1). Adaptive (1.3.0) comes from BOM 2026.09.00. Also added: navigation3 1.2.1, lifecycle-viewmodel-navigation3 and lifecycle-runtime-compose 2.11.0, hilt-lifecycle-viewmodel-compose 1.4.0, kotlinx-serialization 1.13.0 (plugin at the Kotlin version, root `apply false`), and core-splashscreen 1.2.0. :app gets JVM test deps and `isReturnDefaultValues`. |
| 2 Theme and font | a58720e | `NamesTheme` = `MaterialExpressiveTheme(colorScheme, MotionScheme.expressive(), NamesTypography)`. Dynamic colour is used on API 31+; below that, the Top Hat light/dark fallback uses every spec Palette role. DM Serif Display is bundled (OFL licence in `assets/licenses`). |
| 3 Icon, splash, manifest | f88898a | Adaptive icon from the Top Hat art (scaled 0.8 into the safe zone), plus a monochrome layer. PNG mipmaps, `roundIcon` and `drawable-v24` are deleted. The `Theme.SplashScreen` starting theme is `#35319A` with the hat. `enableOnBackInvokedCallback`. Room's unused `MultiInstanceInvalidationService` is removed from the merged manifest (approved at gate 1). |
| 4 DI | d059d23 | `StorageModule` provides `NamesStorage` and `SettingsStorage` as `@Singleton`, plus the repositories, `ActiveGroup` and `GroupEditor`. |
| 5 Navigation model | 5f5b82b | Typed `@Serializable` keys: `Lists`, `Draw(listId)` and `EditList(listId, isNew, session)`. Pure back-stack operations cover spec rules 1–6. `isListDetail` requires width ≥ 840 dp **and** height ≥ 480 dp. The spec's EditList row was updated. |
| 6 Launch | 60c2cfd | `ShellViewModel` routes from the settings only (an IOException goes to Lists). `MainActivity` runs `installSplashScreen` → `enableEdgeToEdge`, and keeps the splash up only until the route is known. |
| 7 Host and placeholders | 993700a | `NavDisplay` with saveable-state and ViewModel-store decorators, `ListDetailSceneStrategy` (custom directive, `PopLatest`, 360/400 dp list pane), `SharedTransitionLayout` with a `LocalSharedTransitionScope` hook for the screen tickets, and placeholder destinations backed by Hilt ViewModels. |
| 8 Device verification | 9bd0dd4, 8caafd4 | AC2–AC7, splash, icon and font checked on API 26, 30 and 36 phones and an API 36 tablet. Found and fixed: dark status-bar icons on the splash. |
| Review fixes | 3ce8801, ea41560, 7d0a49e | The active list is set only while the Draw entry is on the back stack. `listCreated` matches the visit's session. DM Serif goes only on the styles the spec lists. |

## Deviations
Every one is logged in workflow.md as it happened. Here they are, with the reason for each.
- **Fallback scheme sets `background`/`onBackground` to `surface`/`onSurface`.** The plan said unlisted roles keep their token defaults. The token background (`#FEF7FF`) differs from the spec's surface, and the two shouldn't disagree.
- **`MainActivity` switched to `NamesTheme` in task 2** so the font shipped for the size measurement. Task 6 rewrote it anyway.
- **The Room service removal needs `tools:ignore="MissingClass"`.** Room is an `implementation` dependency of `:shared`, so lint can't see the class. The element is only a removal marker.
- **`kotlin-test-junit` was added to the :app test deps.** AGP 9's built-in Kotlin doesn't infer kotlin-test's JUnit adapter, so `kotlin.test.Test` didn't resolve.
- **The test fakes (`FakePreferences`, `FakeGroups`) live in :app test sources**, not inside each test, so all three ViewModel tests can share them.
- **Opening Draw sets the active list only once the list exists, and (after review) only while the entry is started.** The plan said "in `init`". That would race the stale-id path and re-store a deleted list. A popped entry's late `Present` would also re-store a list the user had just left, because Navigation 3 caps popped entries at CREATED and they never start again.
- **`listCreated(id, session)`** (review). Without the session, a late create from an earlier visit could rewrite a newer `EditList(null)`.
- **Extra small pieces:** an `ic_arrow_back` vector (material3 1.5 doesn't bring material-icons), `ListStatus.Unsaved`, and a placeholder-only `placeholder_create` string. `LocalListDetailSceneScope` needs the same accepted `@ExperimentalMaterial3AdaptiveApi` opt-in.
- **`back()` returns `Long?`** (the id of the list left) instead of the plan's `BackResult`. It behaves the same and is simpler.
- **Device checks used new AVDs.** Rule: the 2.7.0 data device `api36_niah` gets no debug builds. AVDs used:
  - `niah_debug_api36`, a Pixel 6 phone, for API 36. Predictive back was also checked here, not on Px7_API_34.
  - `niah_tablet_api36`, a pixel_c (1280×900 dp).
  - `niah_api30`. This needed the arm64 android-30 image to be downloaded, because the installed one was x86.
- **⚠ Incident.** My first smoke-test run went to `api36_niah`. The debug install failed on the signature, and the script then ran `pm clear` on the release 2.7.0 app.
  - Recovery: that AVD boots from snapshot `default_boot` and was run with `-no-snapshot-save`, so I killed it without saving.
  - Check: a `-read-only` reboot showed the 2.7.0 data intact (it opens into the active list "Team Red" with Dana and Eli).
  - Prevention: memory now records "never debug-install or `pm clear` on api36_niah", and that its changes never persist. That also means the 2026-11-11 "upgraded in place to 3.0.0" note was wrong, so I corrected it.
- **Splash bar icons fixed during verification (9bd0dd4).** The starting window inherited light-theme (dark) bar icons on the indigo splash.

## Evidence
**Verify** at 4ca74b2 (re-run for this report, with the iOS compile added):
```
$ ./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug :app:processReleaseMainManifest :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64
(exit 0; lint: 0 errors)
$ (sums of */build/test-results/<target>/*.xml)
app:testDebugUnitTest          45 0   # new: BackStack 20, Layout 7, ShellViewModel 6, DrawViewModel 6, EditListViewModel 6
shared:jvmTest                108 0
shared:testAndroidHostTest     77 0
shared:iosSimulatorArm64Test   70 0   # :shared total 255, same as the rebased baseline
$ grep -c '<activity' / '<service' in the release merged manifest
activities=1 services=0                                  # AC1
$ grep -rnE 'onBackPressed|TaskStackBuilder|startActivity' app/src/main
(no output, exit 1)                                       # AC8
```

**Font cost.** `res/font/dm_serif_display.ttf` is 41,259 B compressed in the APK (76,580 B installed), and `OFL-DMSerifDisplay.txt` is 2,140 B. That's measured from the APK's zip entries, not as an APK before/after delta, because task 1's libraries landed first. Overall the debug APK went from 17,589,808 B to 19,683,589 B, mostly from the navigation and adaptive libraries.

**Outcome verification on devices.** Screenshots are in [screens/](screens/).

| AC | Device | Observed |
|---|---|---|
| AC2 rules 1–5 | niah_debug_api36 | New list → create: the entry becomes `EditList(1, isNew=true)` with the **same session** and the typed text kept. Done → `Draw(1)`. Draw → Edit → Done → Draw. Lists → Edit → back → Lists (walked after review). Back from Draw → Lists. |
| AC3 phone | niah_debug_api36, gesture nav | A back swipe held mid-gesture shows Lists behind the shrinking Draw (`predictive-back-api36-mid.png`). Releasing lands on Lists. |
| AC3 tablet | niah_tablet_api36, 1280×900 dp | List-detail with a 360 dp Lists pane, and no Up in two-pane (`tablet-list-detail.png`). Checked: picking another list replaces the detail; Edit → back → Draw → back → "Pick a list to draw from"; Edit on Alpha while Beta's edit had "Typed for Beta" gives Alpha a **fresh, empty** field (`tablet-edit-beta.png`, `tablet-edit-alpha.png`); deleting Alpha from the Lists pane with its Edit open closes the detail. |
| AC3 compact height | niah_debug_api36, landscape | Single pane (`inset-api36-*-r1-*`). |
| AC4 | API 26 / 30 / 36 | Portrait, landscape and reverse landscape (API 36), gesture and 3-button nav, with tall, corner and punch-hole cutouts: nothing under the bars or cutout (`inset-*`, `cutout-api36-*`). `screencap` doesn't paint the cutout shape, so the cutout evidence is the content's inset padding. |
| AC5 | API 36 / 30 / 26 | API 36 is dynamic: FAB container `#D9E2FF`, not Top Hat's `#E2DFFF`. API 26 is the fallback, exact: light surface `#FCF8FF` and FAB `#E2DFFF`, dark `#131318` and `#35319A`. API 30 surface is `#FCF8FF`. `cmd uimode night yes/no` switches live. |
| AC6 | niah_debug_api36 | Open a list → force-stop → relaunch → Draw. Back out → relaunch → Lists. Open → force-stop → `DELETE FROM name_groups` via `run-as sqlite3` → relaunch → Lists, and again → Lists. All re-checked after the review fix. |
| AC7 | phone + tablet | Unsaved text in Edit → Home → `am kill` (process confirmed gone) → relaunch restores the same `EditList` session with its text. On the phone, back then went Draw → Lists. On the tablet the restore was confirmed in two-pane; the back step after it wasn't captured. |
| Splash | API 26 / 30 / 36 | `#35319A` with the Top Hat (`splash-api26/30/36.png`). It doesn't wait for Room: the keep condition reads only the route. Light bar icons. |
| Icon | API 26 / 36 | Top Hat adaptive icon in a circle mask (`icon-api26.png`, `icon-api36.png`). |

## Follow-ups
- **Owner: check the themed icon on a device.** Turn on "Themed icons" on Android 13+ and look at the monochrome layer (hat and sparkles, with the band as a gap). adb couldn't enable it on the emulator (the launcher provider needs `BIND_WALLPAPER`).
- **Bump material3 to 1.5.0** when it ships (`gradle/libs.versions.toml`, `material3 = "1.5.0-rc01"`).
- **For NAM-8/9/10:**
  - Placeholders are in `ui/placeholder/`. The ViewModels keep the `ListStatus`/Gone and `onShown` rules.
  - Create via `listCreated(id, session)`.
  - Shared-element morphs via `LocalSharedTransitionScope` + `LocalNavAnimatedContentScope`.
  - The ListsViewModel's immediate delete is a placeholder; D12 delayed delete replaces it.
  - The 2.7.0 strings are still in `strings.xml`.
- **Whichever ticket uses a material3 1.5 `*Emphasized` display or headline style** (e.g. the reveal) sets DM Serif on it in `Type.kt`. Those styles currently keep the default font.
- **Spec predictive-back motion** (the shared-bounds title morph) is the screen tickets' job. The shell uses Navigation 3's default predictive pop.
- **Two-pane insets:** on the tablet, each pane's Scaffold applies window insets, and nothing looked wrong. Revisit if a real device with a side cutout shows a doubled inset in the detail pane.
