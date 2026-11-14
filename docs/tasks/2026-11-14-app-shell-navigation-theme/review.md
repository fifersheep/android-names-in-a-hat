# Review — NAM-7 app shell

**Reviewer:** one flow-reviewer pass, 2026-11-14.

**Range:** `c25a883..HEAD` at 089795b. The local `main` ref was at 271fd7b, so `main...HEAD` would also have pulled in NAM-6. The reviewer used the intended range.

**Verdict:** 0 Blocking, 6 Advisory. Four were fixed (three in code, one by a device check), one is closed here, and one is pending the report.

## Blocking
None.

## Advisory
1. **A popped Draw could re-store its list as active.**
   - Where: `PlaceholderViewModels.kt:73-79`.
   - The problem: `DrawViewModel` opened the list from its own collector. That collector keeps running through the entry's exit animation.
   - Failure case: back out quickly during a slow first Room open (a migration). The `Present` arrives late and re-stores the list, so the next launch opens Draw despite the back-out. The same can happen in two-pane when list A is quickly replaced by B.
   - **Fixed in 3ce8801.** The entry now calls `onShown()` from a `LifecycleStartEffect`. Navigation 3 caps popped entries at CREATED (`BackStackAwareLifecycleNavEntryDecorator.kt:45`), so a leaving entry never opens its list. `onShown` still does nothing until the list is `Present`.
   - Tests: `DrawViewModelTest` is now 6/0 (opened only once shown, never while loading, once only).
2. **`listCreated` ignored the session.**
   - Where: `BackStack.kt:36-40`.
   - Failure case: a late create from an earlier visit would rewrite a newer `EditList(null)`.
   - **Fixed in ea41560.** It is now `listCreated(id, session)` and matches the visit.
   - Tests: two new `BackStackTest` cases, a late create left alone and rule 3's back path. Now 20/0.
3. **DM Serif reached `headlineSmall`.**
   - Where: `Type.kt:23`. The spec limits it to displayLarge–Small and headlineLarge–Medium (`3.0-spec.md:410-415`). M3 dialog titles use headlineSmall.
   - **Fixed in 7d0a49e.**
   - The 1.5 `*Emphasized` display and headline styles keep the default font, as noted in the KDoc. **Report follow-up:** whichever later ticket uses an emphasized display style (for example for the reveal) sets the family there.
4. **AC4 was checked with the tall cutout, not corner or punch-hole.** **Closed by a device check.**
   - On niah_debug_api36 with the `corner` and `hole` overlays, Lists and Draw were checked in portrait, landscape and reverse landscape, with nothing hidden (`screens/cutout-api36-*`).
   - Caveat: `screencap` doesn't paint the cutout shape, so the evidence is the content's inset padding: the extra right padding in reverse landscape.
5. **AC2 rule 4 (Lists → Edit list → back) wasn't walked on a device.** **Closed.**
   - Walked on niah_debug_api36 after the fixes: Lists → Edit list → back → Lists.
   - It is also unit-tested now (`BackStackTest`, "back from an edit opened from Lists returns to Lists").
6. **The font size cost isn't in report.md yet.** **Pending the report phase.**
   - The figure is the font's size in the APK: 41,259 B compressed, 76,580 B installed, plus OFL 2,140 B. It is not a before/after APK delta, because task 1 added the libraries before the font landed; report.md will say so.

## Acceptance criteria (after the fixes)
| Criterion | Status | Evidence |
|---|---|---|
| AC1 One activity, no Service | MET | Release merged manifest: 1 `<activity`, 0 `<service`. Room's unused service removed (gate 1). |
| AC2 Destinations and rules 1–5 | MET | BackStackTest 20/0. Rules 1–5 walked on device (rule 4 after the review). |
| AC3 Back and list-detail | MET | Predictive back mid-gesture (`screens/predictive-back-api36-mid.png`). Tablet list-detail and two-pane behaviours (`screens/tablet-*`). A phone in landscape stays single pane. Checked on niah_debug_api36 instead of Px7_API_34 (logged). |
| AC4 Insets | MET | API 26, 30 and 36; portrait and landscape; gesture and 3-button nav; tall, corner and punch-hole cutouts. |
| AC5 Colour | MET | API 36 dynamic; API 26 and 30 use the fallback, exact. Night mode switches live. primaryContainer was sampled, not primary (logged). |
| AC6 Launch routing | MET | Unit tests, plus device checks before and after fix 1 (relaunch into Draw; back-out then relaunch → Lists; stale id → Lists). |
| AC7 Process death | MET (one gap logged) | EditList and its unsaved text restored on the phone and in two-pane. Back after restore was captured on the phone only. |
| AC8 No deprecated navigation | MET | The grep finds nothing. |
| Splash | MET | API 26, 30 and 36 screenshots. Light bar icons (9bd0dd4). |
| Icon | PARTIAL | Adaptive icon correct on API 26 and 36. The themed icon couldn't be switched on via adb, so it's left for the owner. |
| Font | MET in code; size goes in report.md | DM Serif on the spec's styles only, after fix 3. |
| Build | MET | `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug :app:processReleaseMainManifest` PASS at 7d0a49e: shared 255/0, app 45/0, lint clean. |

## Checked (by the reviewer)
- **Diff and verification:** the diff range, and that verification wasn't weakened. No CI or `:shared` source changes, and no tests removed or loosened.
- **Scope:** every file maps to a plan task or a logged deviation. `back()` returns `Long?` instead of the plan's `BackResult`; this is behaviourally the same and isn't logged elsewhere, so it is noted here.
- **Back stack:** every BackStack operation against `NavDisplay`'s onBack, and the list-detail strategy (PopLatest, single-pane null return; adaptive-navigation3 1.3.0 bytecode).
- **App behaviour:** the active-list rules; EditList contentKey continuity; launch and splash; Hilt `@Singleton` scoping.
- **Theme, manifest and tests:** the palette value by value against the spec; the manifest changes; test quality.
