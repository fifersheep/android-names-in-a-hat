# NAM-2 research — 3.0 design spec

Sources: `origin/master` (2.7.0, f9773d5), `main` @ e73220a, Linear NAM-2/7/8/9/10/12, web (Compose API stability).

## Findings

### Shipped app (2.7.0, `origin/master`) — what the redesign replaces
- **User-facing word for a group is "Draw"**: "Draws", "Add Draw", "Draw Title", "Delete Draw", "Create Draw" (`app/src/main/res/values/strings.xml:4-23`). Code and the new `:shared` say "group". The draw *action* is also "Draw", so "Draw" means both the list and the action.
- **Overview** (`overview/OverviewActivity.kt`): RecyclerView of cards (title + "N names", `ui/OverviewCard.kt`), overflow per card: Edit Names / Edit Draw Details / Delete Draw (`res/menu/menu_overview_card.xml`). Delete is immediate, with a snackbar "<name> deleted" and no undo (`OverviewActivity.kt:96-100`). Empty state is an arrow + hint text "Begin by adding a draw…" (`:102-106`, `strings.xml:10`). A toolbar "Settings" item exists with no handler (`res/menu/menu_overview.xml`), so it's a dead item. `onResume` clears the active group (`:82`).
- **Edit group** (`edit/EditGroupDetailsActivity.kt`): one field ("Draw Title", hint "e.g. Team Rocket"). Create → back stack [Overview, EditNames] (`:78-85`); rename → Overview (`:87-93`). Title "Edit: <name>".
- **Edit names** (`edit/EditNamesActivity.kt`): field (hint "e.g. Jesse") + "Add Name" + list with delete icon + Done. Delete is immediate, with a snackbar and no undo (`:115-119`). Title "Names: <group>".
- **Selection/draw** (`selection/SelectionActivity.kt`): GridView of CheckedTextView cards, 1/2 columns toggled by a toolbar icon (`:150-161`); toolbar sort-alpha icon (`menu_selection.xml`); bottom bar: counter "n out of t names selected", Select All/Clear All button, spinner 1..selected, Draw button (`layout/activity_selection.xml`; text from `core/.../SelectionUseCase.kt:35-37`). Draw is enabled only at ≥2 selected (`SelectionUseCase.kt:38`). Result: AlertDialog "The selected name(s) is/are…" + "Okay" (`SelectionActivity.kt:174-185`). Empty: "This draw contains no names… use the draw's menu button on the Overview screen" (`strings.xml:30`). Back always rebuilds to Overview (`:137-142`).
- **Launch**: Splash binds a Service, then routes to the active group's selection screen or the Overview (`SplashActivity.kt:43-55`).
- **Brand assets**: `palette_red #B32A33`, `palette_red_soft #B9414A`, `palette_grey_light #B3B5B4`, `palette_grey_dark #1F1F1A`, `background_grey #E8ECF0` (`res/values/color.xml`); launcher background `#CE3C46` (`drawable/ic_launcher_background.xml`); splash = red_soft + `ic_logo`. AppCompat Light.DarkActionBar, light only (`res/values/themes.xml`). Screen transition 200ms (`res/values/integers.xml`).
- The 2022 Compose skeleton's `ui/theme/Color.kt` (ticket's "starting point") was removed from `main` in e0aeae1 (NAM-1). It's recoverable from `e0aeae1^`. It had a Red ramp around `#DD5555` and a Green ramp around `#58A163` (M2-style 50–900 ramps).

### `main` today (what the spec designs for)
- `:app` = one `MainActivity` with a placeholder (`app/src/main/kotlin/.../MainActivity.kt`, "Placeholder until the app shell (NAM-7)"). It uses a bare `MaterialTheme`, Compose BOM `2026.09.00`, material3 only (no adaptive/navigation3 deps yet), Hilt, minSdk 26, targetSdk 36 (`gradle/libs.versions.toml`, `app/build.gradle.kts`).
- `:shared` domain, already settled and tested (NAM-4):
  - `DrawState` (`shared/.../draw/DrawState.kt:16-38`): selectedCount, totalCount, options 1..selected, allSelected, hasNames, canDraw. `MIN_NAMES_TO_DRAW = 2` (`:10`), annotated "NAM-2 and NAM-12 may lower it to 1".
  - `NameDraw` draws without replacement, never throws (`draw/NameDraw.kt`).
  - `Name(selected, drawn)` are independent flags (`model/Name.kt:9-15`). `GroupSummary(group, nameCount)` (`model/Group.kt`).
  - `validateLabel` trims, and blank → `WriteResult.Blank` for both group labels and names (`usecase/Labels.kt`). Group/name rename and delete return the deleted item for an "X deleted" message (`GroupEditor.kt`, `NameEditor.kt`). New names are selected.
  - `ActiveGroup` open/close/observe (`usecase/ActiveGroup.kt`). `GridLayout` holds 1/2 columns app-wide (`usecase/GridLayout.kt`). `SortOrder` cycles NONE→ASC→DESC→ASC per group (`usecase/SortOrder.kt`).

### What downstream tickets expect from this spec
- **NAM-7 shell**: Navigation 3 typed destinations "as listed in the design spec"; list-detail on large screens; M3 + dynamic colour 12+ + fallback palette + dark; platform splash; launch routing (default: reopen the last group); predictive back. Clearing the active group is tied to explicit back-out, not to "list visible".
- **NAM-8 groups**: create lands "where the spec says"; delete undo vs confirm; empty state; validation message "List names cannot be blank", adjusted to the spec's vocabulary.
- **NAM-9 names**: maybe merged with create; delete undo vs confirm; rename-a-name "if the spec includes it"; "Names cannot be blank"; reachable from the draw empty state; duplicates allowed unless the spec says otherwise; no bulk paste.
- **NAM-10 draw**: reveal replaces the dialog and honours "remove animations"; the count control and the 1/2 column density may be replaced; the minimum to draw is owned by the spec; the counter "n out of t names selected" must stay visible (GitHub #7).
- **NAM-12 remaining**: always-on vs a per-group choice (default always-on, reset one tap away); how drawn names look (not colour alone; TalkBack); reset confirmation or undo; un-draw a single name if the spec includes it; counter wording once some names are drawn; minimum = 1 on the remaining pool unless the spec says otherwise.

## Compose API stability (2026-11-11)
Web-checked against the androidx release notes and API reference (sources listed at the end).

**Material 3 Expressive is not in a stable material3 yet.** Stable material3 is 1.4.0 (2025-09-24), and BOM `2026.09.00` (ours, and the newest listed) maps to it. 1.5.0-rc01 shipped 2026-11-04. So the spec can design around Expressive components, but NAM-7 must either pin `material3:1.5.0-rc01` outside the BOM or wait for 1.5.0 final. **This is a constraint to hand to NAM-7, not a design blocker.**

| API | Status in 1.5 | Use in spec? |
|---|---|---|
| `MaterialTheme.motionScheme` (standard/expressive), `MaterialExpressiveTheme` | Graduated | Yes. Base the motion spec on the expressive motion scheme |
| ButtonGroup / connected toggle buttons, SplitButton | Stable | Yes. Candidate for the count control and the sort/density controls |
| FAB, **FloatingActionButtonMenu** | Stable | Yes |
| FloatingToolbar, FlexibleBottomAppBar, Medium/LargeFlexible TopAppBar | Stable | Yes. Candidate home for the draw bar |
| Expressive ListItem / SegmentedListItem, `*Emphasized` typography | Stable | Yes |
| Wavy progress indicators | Stable | Optional |
| **MaterialShapes / shape morphing, LoadingIndicator** | **Still `@ExperimentalMaterial3ExpressiveApi`** (promoted, then reverted in alpha19) | **Avoid as load-bearing.** The reveal must not depend on them; a hand-rolled `RoundedPolygon`/graphics-shapes morph is the fallback if wanted |
| Adaptive `ListDetailPaneScaffold`, `NavigableListDetailPaneScaffold`, `ListDetailSceneStrategy` (adaptive-navigation3) | Artifacts stable (adaptive 1.3.0, 2026-08-12), APIs still `@ExperimentalMaterial3AdaptiveApi` | Yes, with an opt-in. It's the only list-detail path; NAM-7 accepts the opt-in |
| `NavigationSuiteScaffold` | Stable (material3) | Not needed: there's one top-level destination, so no nav bar or rail |
| `currentWindowAdaptiveInfoV2()` | Stable; V1 is deprecated | Yes. Breakpoints (dp): width Medium 600 / Expanded 840 / Large 1200 / XLarge 1600; height Medium 480 / Expanded 900 |
| Navigation 3 | Stable (1.2.1, 2026-11-04); shared elements between scenes since 1.1 | Yes. Shared-element motion list→detail is available |
| Predictive back | Stable, on by default (Activity 1.6+, Nav3 `NavigationBackHandler`) | Yes |
| Dynamic colour | Stable, API 31+ | Fallback scheme needed for API 26–30 |
| Reduced motion | **No Compose API.** Compose animations honour the animator duration scale (0 → they finish in one frame). To *swap* a motion for a different design, read `Settings.Global.ANIMATOR_DURATION_SCALE == 0f` | The spec defines an explicit reduced-motion variant of the reveal, keyed on that setting |

Unconfirmed: whether a BOM newer than 2026.09.00 exists; the connected-button-group shape defaults by name; whether adaptive 1.4 drops the experimental annotation.

Sources: developer.android.com/jetpack/androidx/releases/{compose-material3, compose-material3-adaptive, navigation3, navigation, window, compose-ui, compose-animation, navigationevent}; …/compose/bom/bom-mapping; API reference pages for MaterialShapes, LoadingIndicator, ButtonGroup, Typography, currentWindowAdaptiveInfoV2, ListDetailPaneScaffold, ListDetailSceneStrategy, WindowSizeClass, MotionDurationScale; …/compose/system/predictive-back-setup.

## Constraints & prior art
- Behaviour that must survive: everything in the shipped screens except the 1/2 column toggle and the count spinner (NAM-2 Spec).
- Out of scope: building screens, draw history, rate prompt, nav drawer, groups mode/teams, sharing a result, bulk import, store listing art (NAM-2, NAM-9/10/12 Out of scope; memory `names-3-0-rebuild`).
- Package stays `uk.lobsterdoodle.namepicker`. In-app branding follows Northtrack Studio (`docs/play-store-compliance.md` §6 "Developer name migration", lines 100-112).
- AC: every row of the NAM-2 table covered; each screen has a phone and a large-screen mock-up in light and dark; navigation model + destination list; owner approval as a Linear comment; NAM-7..10/12 updated with section links. Section links must stay stable, so the spec needs fixed headings/anchors.
- No Figma or other design tool in the repo. Artifact types available to this account include **Design** (canvas of live HTML artboards), **Design System** and **Docs**.
- Accessibility already demanded by screen tickets: 48dp targets, content descriptions, drawn state not by colour alone, TalkBack announces the result.

## Open questions (triaged)
Shape-changing (asked and answered 2026-11-11, see the workflow.md decision log):
1. Deliverable format: repo markdown spec `docs/design/3.0-spec.md` + a Claude Design canvas for mock-ups.
2. Brand: a **fresh Northtrack look**, not an evolution of the 2.7.0 red. Propose 2–3 directions for the owner to choose from. The 2.7.0 red is reference only.

Design decisions this spec owns. Proposed in plan.md, confirmed at gate 1, not asked here: navigation model and destinations (merge create + add names?); user-facing vocabulary ("Draw" vs "List" vs "Group"); minimum to draw (1 vs 2); draw-from-remaining always on vs per group; un-draw a single name; delete group/name: undo vs confirm; reset: undo vs confirm; renaming a name; what replaces the column toggle and count spinner; the reveal and its reduced-motion fallback; launch behaviour; the dead "Settings" item and where Northtrack attribution lives (about sheet?); duplicate names (default: allowed).

Conventional defaults (recorded): Material 3 type scale and shape scale; 48dp targets; WCAG AA contrast for the fallback palette in both themes; the Compose window size classes as layout breakpoints; plurals for all counts.
