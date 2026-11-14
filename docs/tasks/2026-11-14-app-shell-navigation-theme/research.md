# NAM-7 research — app shell

Sources: `main` @ 271fd7b, Linear NAM-7 (description + spec comment 2026-11-12), `docs/design/3.0-spec.md`, mock-up sources, web (see "Libraries").

## Findings — codebase

### `:app` today
- One `MainActivity` (`app/src/main/kotlin/uk/lobsterdoodle/namepicker/MainActivity.kt:22-34`): `enableEdgeToEdge()`, bare `MaterialTheme`, a placeholder that uses `safeDrawingPadding()`. `@AndroidEntryPoint` is already there.
- `NamesApplication` is `@HiltAndroidApp` (`NamesApplication.kt:6-7`). Hilt 2.60.1 plugin and KSP are applied (`app/build.gradle.kts:3-6`). There are no Hilt modules yet.
- Manifest: one activity, no service, no deep links (`app/src/main/AndroidManifest.xml:16-26`). The legacy `http(s)` deep links were already dropped by NAM-1, so AC1 and the deep-link note hold today. `tools:targetApi="31"`, no `enableOnBackInvokedCallback`.
- Window theme: `Theme.NamesInAHat` → `android:Theme.Material.Light.NoActionBar` (`res/values/themes.xml:4`). It is light only.
- **The ticket's "Material 2 theme in `ui/theme/`" no longer exists.** NAM-1 already replaced it with the bare placeholder, so nothing needs removing.
- `strings.xml:3-38` holds 2.7.0 copy using the old "Draw" = group wording. The spec's copy deck replaces it (D1). The shell only needs the destination titles and placeholders. Screen tickets own the rest.
- Launcher icon: the old adaptive icon (`mipmap-anydpi-v26/ic_launcher.xml`), PNG mipmaps for all densities, and `drawable-v24/ic_launcher_foreground.xml`. It has no monochrome layer. Because minSdk is 26, the anydpi-v26 XML always applies, so the PNG mipmaps are dead weight.
- Dependencies: Compose BOM 2026.09.00 (material3 1.4.0), activity-compose 1.13.0, core-ktx 1.19.1, Hilt 2.60.1. There is no navigation3, adaptive, lifecycle-viewmodel-compose, splashscreen or serialization yet (`gradle/libs.versions.toml`). compileSdk is 37 (`app/build.gradle.kts:20`).
- AGP 9 built-in Kotlin: `:app` must not apply `org.jetbrains.kotlin.android` (`app/build.gradle.kts:1`). The serialization plugin has to work with that (see Libraries).
- `:app` has no tests. CI runs `:app:lintDebug`, `:shared:jvmTest :app:testDebugUnitTest` and `:app:assembleDebug` (`.github/workflows/android.yml:36,48,60`), so JVM unit tests in `:app` run in CI. Instrumented tests do not.

### `:shared` surface the shell uses
- `PreferencesRepository` (`shared/.../repository/PreferencesRepository.kt:10-40`):
  - `activeGroupId: Flow<Long?>`, `setActiveGroup`, `clearActiveGroup`, `clearActiveGroupIf`
  - `gridColumns` (retired by the spec; NAM-6 hand-off) and the sort-type members
  - **No implementation exists in main code.** NAM-6 (DataStore) is in Backlog. The only implementation is `FakePreferencesRepository` in `commonTest`, which `:app` can't see.
- `ActiveGroup` (`shared/.../usecase/ActiveGroup.kt:7-14`): `open(id)`, `close()`, `observe()`. Thin wrappers over prefs.
- `GroupEditor(groups, prefs)` (`usecase/GroupEditor.kt:8-35`): `delete` already does `clearActiveGroupIf` (spec Launch, "Deleted").
- `GroupRepository.observeGroup(id): Flow<Group?>` (`repository/GroupRepository.kt:16`). It is null once the group no longer exists, which gives the stale-id check.
- `NamesStorage(context)` (`androidMain/.../NamesStorage.android.kt:11-14`) is "one per process, a singleton in the DI graph". It exposes `groups` and `names` (`commonMain/.../data/NamesStorage.kt:10-16`). The Room build is lazy until the first query.
- `:shared` classes have no DI annotations, so the Hilt modules in `:app` provide them (ticket note, agreed).

### Spec decisions the shell implements
- Destinations: `Lists`, `Draw(listId: Long)`, `EditList(listId: Long?)` (spec `3.0-spec.md:67-77`). Reveal and About are in-screen layers, not destinations (`:79`).
- Back-stack rules 1–6 (`3.0-spec.md:81-87`):
  - `EditList(null)` is **replaced** by `EditList(id)` on create.
  - Done on a new list replaces it with `Draw(id)`.
  - Launch seeds `[Lists, Draw(id)]`.
  - Predictive back on every pop.
  - In this ticket the shell owns the back-stack operations. The placeholders trigger them through debug buttons.
- Launch (`3.0-spec.md:92-100`):
  - The splash uses background `#35319A` and the Top Hat icon, and stays up until the route is known, not until the DB is ready.
  - Route: `[Lists, Draw(id)]` if the id is stored and the list still exists; otherwise `[Lists]`, and a stale id is cleared.
  - Opening Draw sets the active list.
  - The active list is cleared by an explicit phone back-out, or on large screens by a delete. Picking another list on a large screen replaces it.
- Large screens (`3.0-spec.md:270-291`):
  - List-detail only when width ≥ 840 dp **and** height ≥ 480 dp.
  - Lists pane 360 dp, or 400 dp at ≥ 1600 dp width.
  - Detail placeholder: the hat illustration and "Pick a list to draw from".
  - `EditList` replaces `Draw` in the detail pane, and back returns to Draw.
  - Implemented with `ListDetailSceneStrategy`.
- Theme (`3.0-spec.md:333-367`):
  - Dynamic colour on 12+, otherwise the full fallback light and dark schemes (every role is listed).
  - Seed `#4D4BB3`.
  - Type: DM Serif Display for display and headline styles; the shell measures the APK cost (`:408-417`).
  - Shapes (`:419-425`).
  - `MotionScheme.expressive()` (`:436-438`).
  - material3 1.5 pinned outside the BOM; adaptive 1.3.0; no navigation suite (`:470-492`).
- Icon (`3.0-spec.md:427-434`):
  - The Top Hat SVG source is at `docs/design/3.0-mockups/project/Main.dc.html:66-74` (108×108 viewBox, so it maps 1:1 to an adaptive-icon canvas).
  - The monochrome concept is at `:87-91`. It still draws direction A's **ticket** rectangle (`:88`), a leftover from the ticket concept, so it doesn't match Top Hat.

## Findings — libraries (checked 2026-11-14 against Google Maven metadata, AAR metadata, sources jars, release notes and nav3-recipes HEAD 5ecbad6)

| Artifact | Version | Notes |
|---|---|---|
| `androidx.compose.material3:material3` | **1.5.0-rc01** (2026-11-04) | **1.5.0 final has not shipped.** Pin it outside the BOM. It pulls compose ui/foundation **1.12.0**, so with the BOM's 1.12.1 nothing is forced up. It adds a strict `material3-ripple:1.5.0-rc01`. AAR: minCompileSdk 37, min AGP 9.1.0 |
| Compose BOM | 2026.09.00 (newest stable) | Maps material3 to 1.4.0. `compose-bom-alpha:2026.11.00` would move the whole stack to 1.13 RCs, so don't use it |
| `androidx.compose.material3.adaptive:adaptive-navigation3` (+ adaptive, adaptive-layout) | 1.3.0 | **Already managed by BOM 2026.09.00**, so no version is needed. minCompileSdk 37 |
| `androidx.navigation3:navigation3-runtime` / `-ui` | 1.2.1 | compileSdk 36. Its POM only has kotlinx-serialization-core at runtime scope, so add it explicitly |
| `androidx.lifecycle:lifecycle-viewmodel-navigation3` | 2.11.0 | minCompileSdk 37 |
| `androidx.hilt:hilt-lifecycle-viewmodel-compose` | 1.4.0 | `androidx.hilt.navigation.compose.hiltViewModel` is deprecated in 1.4. minCompileSdk 37, min AGP 9.1/9.2 |
| `org.jetbrains.kotlinx:kotlinx-serialization-core` | 1.13.0 | Built on Kotlin 2.4.10. Skip 1.12.0 (a behaviour change, since reverted) |
| plugin `org.jetbrains.kotlin.plugin.serialization` | 2.4.21 (= Kotlin) | Root `apply false` next to compose-compiler, applied in `:app` (same pattern as nav3-recipes on AGP 9) |
| `androidx.core:core-splashscreen` | 1.2.0 | Pulls appcompat-resources only, not AppCompat themes |
| DM Serif Display | `google/fonts` `ofl/dmserifdisplay/DMSerifDisplay-Regular.ttf`, 76,580 B, OFL 1.1 | Skip the Italic. Ship `OFL.txt` |

All of these fit compileSdk 37 and AGP 9.5.1. NAM-2's API-stability table is still accurate.

### Theme
- **1.5 is required, not merely preferred.** In 1.4.0 `MaterialExpressiveTheme`, `MotionScheme.expressive()` and the `motionScheme` overload of `MaterialTheme` are `internal`.
- In 1.5 `MaterialExpressiveTheme(colorScheme?, motionScheme?, shapes?, typography?, content)` is public with no opt-in.
- `MaterialExpressiveTheme`'s default colour scheme is light-only, so always pass our scheme.
- `MaterialTheme(colorScheme, motionScheme = MotionScheme.expressive(), shapes, typography)` is equivalent.
- `dynamicLight/DarkColorScheme(context)` needs `@RequiresApi(31)`. Gate on `SDK_INT >= S`.
- The `ColorScheme` constructor is unchanged in 1.5 (48 roles, including fixed and surfaceContainer). Build the fallback with `lightColorScheme(...)` / `darkColorScheme(...)` and override the spec's roles. The roles the spec doesn't list (surfaceBright/Dim, surfaceVariant, `*Fixed*`) keep token defaults.
- `Typography(fontFamily)` is new in 1.5. For display and headline only, `copy()` those styles with the DM Serif family.

### Splash
- Call `installSplashScreen()` before `super.onCreate()`, then `enableEdgeToEdge()`, then `setContent`. Use `setKeepOnScreenCondition { route == null }`, which is evaluated every frame, so keep it cheap.
- The theme parent is `Theme.SplashScreen` (or `.IconBackground`). Attributes: `windowSplashScreenBackground` `#35319A`, `windowSplashScreenAnimatedIcon`, and `postSplashScreenTheme`. The post theme can be the platform `android:Theme.Material…NoActionBar`, because the library only calls `setTheme` (read from the code, not the docs).
- Icon: a dedicated vector on a 288 dp canvas with the art inside a 192 dp circle. Don't point it at the adaptive mipmap, which gets cropped below API 31.
- **Gotcha:** on API 31–32 a custom `setOnExitAnimationListener` re-applies system-bar flags from the post theme over `enableEdgeToEdge`. Don't add an exit animation.

### Edge-to-edge
- `enableEdgeToEdge()` (activity 1.13.0) handles API 26–28: a transparent status bar, a scrim on the nav bar, and light or dark icons following the **system** night mode.
- API 29 uses enforced nav contrast.
- The cutout mode is set to SHORT_EDGES or ALWAYS automatically, so no manifest attribute is needed.
- Content has to pad itself with `safeDrawing` or `displayCutout`; landscape side cutouts are the risk.
- The M3 1.5 `Scaffold` default `contentWindowInsets` = systemBars ∪ displayCutout (not the IME).

### Navigation 3
- Typed keys: `@Serializable sealed interface AppKey : NavKey` with `Lists` / `Draw(listId)` / `EditList(listId: Long?)`.
- `rememberNavBackStack(vararg)` (Android reflection overload) survives process death. On restore the initial elements are ignored.
- `NavBackStack` is a `MutableList` (a SnapshotStateList), so replace and seed with plain list operations.
- Decorators:
  - Passing a list replaces the default.
  - Use `listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator())`.
  - The saveable decorator is what keeps in-progress text (`rememberSaveable`) per entry across process death. That covers the "in-progress text input" requirement in the nav set-up.
- Entry ViewModels: `hiltViewModel<VM, VM.Factory>(creationCallback = { it.create(key.listId) })` with `@HiltViewModel(assistedFactory=…)` + `@AssistedInject`. The scope comes from the ViewModelStore decorator (contentKey). Assisted arguments come back from the restored key.
- **contentKey on replace:** the default contentKey includes the key's `toString()`. So `EditList(null)` → `EditList(id)` is a pop plus a push: the ViewModel is cleared and saveable state dropped.
  - `entry<EditList>(clazzContentKey = { … })` can keep it stable, but the assisted `listId` would then be stale. The plan must pick one approach. The safe option is to let the replace remount and carry state through the ViewModel or the key. This is NAM-8's concern: the shell exposes the operation.
- `NavDisplay`'s default `onBack` pops. The predictive-pop spec is `predictivePopTransitionSpec`. Add `android:enableOnBackInvokedCallback="true"` so Android 13–15 get predictive back (it's the default on 16 at targetSdk 36).
- Shared elements: `SharedTransitionLayout { NavDisplay(sharedTransitionScope = this) }` plus `LocalNavAnimatedContentScope`. The shell only provides the hook; screen tickets do the morphs.

### List-detail (`ListDetailSceneStrategy`, `@ExperimentalMaterial3AdaptiveApi`)
- The metadata helpers are `ListDetailSceneStrategy.listPane(detailPlaceholder = {…})`, `detailPane()`, `extraPane()` and `preferredPaneSize(width)`.
- If the stack's top entries carry no pane metadata, or only one pane fits, the strategy returns null. NavDisplay then falls back to single pane.
- **The default directive ignores height.** An expanded-width phone in landscape (e.g. 915×412 dp) gets 2 panes, so a custom directive is required:
  - `twoPane = isWidthAtLeastBreakpoint(840) && isHeightAtLeastBreakpoint(480)`
  - `calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()).copy(maxHorizontalPartitions = if (twoPane) 2 else 1, …)`
- List pane width: `preferredPaneSize(width = 360.dp | 400.dp at ≥1600)` on the Lists entry.
- `EditList` in the detail pane needs `detailPane()` metadata too. Otherwise it renders full window and hides the Lists pane, but spec `:279` wants it in the detail pane.
- `LocalListDetailSceneScope` is null outside list-detail. Use it to hide Up in two-pane.
- The default `backNavigationBehavior = PopUntilScaffoldValueChange`. Picking another list in two-pane should **replace** the top `Draw` rather than push, so back doesn't walk through every list opened (spec rule 5: back from Draw lands on Lists).

## Constraints & prior art
- NAM-2 research already checked API stability on 2026-11-11 (`docs/tasks/2026-11-11-design-spec-3-0/research.md:33-57`). Re-confirmed above.
- The root `build.gradle.kts` declares the KMP plugin `apply false` to lift built-in Kotlin's KGP to 2.4.21. Register the serialization plugin the same way.
- KSP stays at 2.3.12 (`libs.versions.toml:10`, google/ksp#3286).
- Memory, build gotchas: use `--no-watch-fs`, and no commas in KMP test names. CI runs JVM tests only.
- Memory, device verification: headless AVDs exist, including api26 (SQLite 3.18). AC4 needs API 26, 30 and 36 images and a tablet-sized one.
- Release R8 and backup rules are out of scope (NAM-13).

## Open questions

### Recorded as defaults (not asked)
- **D-a, material3:** pin `1.5.0-rc01`. Bump to 1.5.0 when it ships (spec API stability). Stay on BOM 2026.09.00.
- **D-b, two-pane list pick:** replaces the top `Draw` (or `EditList`) entry and doesn't push. Back from a detail returns to Lists.
- **D-c, active list on large screens:**
  - Back from the detail pane does **not** clear the active list (spec: the Lists pane is always visible, so it never counts as "left").
  - Only a delete clears it.
  - "Phone" means single-pane layout, not a device class. A rotate that changes the layout doesn't clear it either.
- **D-d, icon:**
  - Adaptive foreground and background vectors come from `Main.dc.html:66-74`, plus a monochrome layer.
  - The monochrome layer is the Top Hat silhouette with sparkles. **Drop the ticket rectangle** in `:88`: it's a leftover from direction A.
  - Delete the PNG mipmaps and `roundIcon`, which are dead at minSdk 26.
  - Owner reviews the result on device.
- **D-e, placeholders:**
  - Each destination shows its title, its key, and buttons that exercise every back-stack rule: open list, new list → "create" (fake id), done, edit, back.
  - The Lists placeholder lists the groups from Room, so routing to real lists works.
  - Screen tickets replace them.
- **D-f, verification:**
  - JVM unit tests in `:app` for the pure navigation logic (back-stack operations, launch route, directive choice).
  - On-device checks via AVDs for AC3, AC4, AC5 and AC7, with screenshots in report.md.
  - No instrumented tests: CI doesn't run them.
- **D-g, legacy 2.7.0 strings** stay until the screen tickets replace their copy. The shell adds only what it uses.
- **D-h, EditList contentKey on replace:** decided in the plan, with the trade-off above. The shell guarantees the replace is a single back-stack write.

### Shape-changing (asked)
- **Q1, preferences until NAM-6.** No `PreferencesRepository` exists in main code. Binding an in-memory fake (as the ticket says) means the active list never survives a relaunch, so AC6 can be shown only in unit tests until NAM-6 lands. **A (owner, 2026-11-14): another agent is building NAM-6 now. Wait for it and bind the real DataStore implementation; no fake.**
- **Q2, splash vs the "still exists" check.** The ticket says not to gate the splash on the DB. The spec routes to `Draw` only if the list "still exists", which needs a Room query (and on first 3.0 launch, the v3 migration). **A (owner, 2026-11-14): route optimistically. The splash waits only for the prefs read and seeds `[Lists, Draw(id)]`. If Draw finds the list gone, it pops to Lists and clears the id.**
