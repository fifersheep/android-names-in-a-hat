# Report: build the draw screen — select names, draw, reveal (NAM-10)

## Changed vs plan
All 9 plan tasks are done ([plan.md](plan.md); Progress is in [workflow.md](workflow.md)), plus the review fixes.

- **AC status:**
  - Met: AC2–AC11 and AC13–AC15.
  - **AC1 is partial.** Z–A from 2.7.0 wasn't checked on device; see Follow-ups.
  - **AC12 is partial.** The title morph wasn't captured, and there was no live TalkBack listen.
- **Review:** 1 Blocking and 4 Advisory. The Blocking one and 3 Advisories are fixed; the 4th is logged ([review.md](review.md)).

| Task | Commit | What |
|---|---|---|
| 1 Sort cycle | ea0f273 | `NameSortType.next()`: DESC → NONE (D17). `NameSortTypeTest`, `SortOrderTest` and the KDoc updated; `PreferencesRepositoryTest`'s concurrent split is now 17/17/16. |
| 2 Draw rules | 81cb73e | `MIN_NAMES_TO_DRAW = 1`. `DrawState` rebuilt around **in the hat** (selected and not drawn): `inHat`/`total`/`drawnCount`, a tri-state `SelectAllBox` over not-drawn names, `DrawBarState` (Stepper/NothingInHat/AllDrawn) and `clampCount`. `drawFrom` draws only from the hat. |
| 3 Grid retired | d455e70 | `GridColumns`, `GridLayout`, `PreferencesRepository.gridColumns`/`setGridColumns`, the DataStore stubs, the fakes' grid members and their tests all removed (D5). |
| 4 DrawViewModel | 2294fb7 | `ui/draw/DrawViewModel`: selection, select all, sort, the clamped count, draw, draw more, dismiss. Writes and draws share one `Mutex` and re-read the names; the count, result and played flag live in `SavedStateHandle`. DI adds `SortOrder` and `NameDraw(RandomNumberGenerator())`. |
| 5 Copy + icons | 781c29d | Copy-deck keys for Draw, chips and the reveal, with plurals. The 2.7.0 draw, dialog, grid and sort strings are removed. Vectors `ic_sort`, `ic_ticket` and `ic_remove`. |
| 6 Draw UI | baafd4d | Medium flexible top bar (sort pill, overflow), the sticky tri-state header labelled by the counter, In/Out/Drawn chips, the stepper + **Draw n** bar (a trailing panel in compact height) and the empty state → Add names. |
| 7 Reveal | 0c302da | A full-window `Dialog` over both panes: hat shake, cards on the expressive spring with the spec stagger, Done / Draw n more / "Everyone's been drawn". One crossfade under reduced motion. Plays once per draw. "Drawn: …" announced after the first frame. Predictive back and Escape. Light bar icons. |
| 8 Shell | 25c8837 | The `Draw` entry shows `DrawScreen`; `DrawPlaceholder` and `PlaceholderViewModels.kt` are gone. |
| 9 Device | 305f1c0 | Phone, tablet and the install-over on `api36_niah`. One bug found and fixed: the reveal title wasn't centred. |
| Review fixes | 7111bce, 85bd0ca | Each draw gets a `drawId`, so a repeated "Draw more" still plays and announces (Blocking). Done cancels an in-flight Draw more. The stepper's live region moved to the value. Reduced motion is read per draw. |

**Decided at gate 1 and before** (decision log): NAM-10 is drawn-aware but never marks names drawn; it waited for NAM-8 to merge; the 2026-11-12 spec deltas and the NAM-9 empty-state hand-off are in.

## Deviations
All of these were logged in workflow.md when they happened.
- **Setup: branch created in the shared checkout by mistake.** Another session's `flow/groups-screen` was checked out there. It was switched back within seconds (both branches were at ff34628, so no files changed), and this task moved to its own worktree.
- **Plan wording: the concurrent-sort test.** The plan said it "ends on ASC"; 50 cycles from NONE end on DESC. Only the split changed.
- **`PlaceholderViewModels.kt` deleted outright.** `DrawViewModel` was the only thing left in it after NAM-8. NAM-9 had already provided `NameEditor`, so DI gained only `SortOrder` and `NameDraw`.
- **New key `draw_select_all_a11y` ("%1$s, %2$s").** It isn't in the copy deck. It keeps the box's label "Select all, 15 of 24 in the hat" out of a hard-coded join.
- **The stepper isn't M3's `ButtonGroup`.** It is two `Surface`s and a value box with the mock-up's connected corner shapes, because the value segment isn't a button and the compact-height stack is vertical (`LandScreen.dc.html:116-124`).
- **The reveal hat's body uses `onPrimary`/`onPrimaryContainer`** instead of the mock-up's fixed #F4F2FF, so it follows dynamic colour. `HatIllustration` takes `hat`/`sparkle` (the band shares the sparkle colour in both mock-ups).
- **Reveal layout by window size:** compact height → landscape (2 columns); width ≥ 840 dp → large (3 columns); otherwise the phone column.
- **Reduced motion fades the whole takeover** with one alpha `Animatable` (300 ms), not `AnimatedVisibility`. Compose snaps it when the scale is 0.
- **`status` stays a separate flow** next to `uiState`, as in the NAM-7 placeholder.
- **Added `RevealStaggerTest`.** It covers the 1-name stagger (no division by zero).
- **The quick device check for task 6 moved to after task 8,** because the screen couldn't be reached before the wiring.
- **Review fix B1 adds `drawId`** to `RevealUi` and to saved state. The plan didn't have it.
- **Predictive back needed no fallback.** The dialog window passes progress to `PredictiveBackHandler`.
- **AC1 Z–A not checked on device.** The 2.7.0 data has no sort key: NAM-8's Z–A was set in its own session and discarded with `emu kill`. Auto mode blocked writing the 2.7.0 data as root, and that wasn't worked around.

## Evidence
**Verify** (`--rerun-tasks`, at c77199d):
```
$ ./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug --rerun-tasks
BUILD SUCCESSFUL in 58s
117 actionable tasks: 117 executed
lint: 0 errors, 4 warnings   (OldTargetApi, RedundantLabel, AndroidGradlePluginVersion, NewerVersionAvailable — all pre-existing)
shared/build/test-results/iosSimulatorArm64Test/   tests 82  failures 0 errors 0
shared/build/test-results/jvmTest/                 tests 120 failures 0 errors 0
shared/build/test-results/testAndroidHostTest/     tests 89  failures 0 errors 0
app/build/test-results/testDebugUnitTest/          tests 146 failures 0 errors 0   (DrawViewModelTest 39)
```
- **Baseline at ff34628:** `:app` 45, `:shared` 255 (before NAM-8 and NAM-9).
- **Mutation checks:**
  - The toggle-then-draw race test fails when the in-lock re-read is removed.
  - The Done-vs-Draw-more test fails when the `drawId` guard is removed.

**Greps** (AC14 and AC15): `grep -rnE "GridColumns|GridLayout|setGridColumns|\.gridColumns|DEFAULT_GRID_COLUMNS" shared/src app/src` finds nothing. The hard-coded copy grep over `ui/draw/` finds nothing. The legacy draw strings are gone from `strings.xml`.

**Device** (the full table and screenshots are in [evidence/README.md](evidence/README.md)):
- **Install over 2.7.0 on `api36_niah`** (release 3.0.0, vc 2000000002, over vc 2000000000). Launch opens Draw for Team Red with Dana and Eli selected ("2 of 2 in the hat") and Unsorted, as stored. Then `emu kill`.
- **Phone, `niah_debug_api36`:**
  - Sort cycles Unsorted → A–Z (Ada, ben, Chloe, Kai, Tom, zara) → Z–A → Unsorted, and A–Z survives force-stop.
  - The stepper stops at 4 (+ disabled), clamps 4 → 2 on deselect and stays at 2 on reselect.
  - Five draws of 4 came out in five different orders.
  - With the reveal open, rotation and `am kill` (`pidof` empty) both show the same names, with no replay.
  - `animator_duration_scale 0` gives the end layout at 0.3 s.
  - Escape dismisses. The predictive-back preview runs over Draw.
  - Empty state → Add names → Edit list → back.
  - With drawn names seeded via sqlite3: the ticket chips with strikethrough, "2 of 6 in the hat · 3 drawn", "Everyone's been drawn", and the box disabled when every name is drawn.
  - Light and dark, portrait and landscape (trailing panel).
  - After the B1 fix, a repeated "Draw 1 more" replays (`review-drawmore-same-name-midway.png`).
- **Tablet, `niah_tablet_api36`:** list-detail Draw in light and dark; the reveal covers both panes in a 3-column grid.
- **TalkBack labels** (uiautomator tree): "Select all, 4 of 6 in the hat", "Sort: A–Z", "How many to draw" / "Draw fewer" / "Draw more", "Drawn: zara, Ada, Tom, and Kai".

## Follow-ups
- **AC1 Z–A from 2.7.0 on device (owner's call).** Set Z–A in the 2.7.0 app on `api36_niah` (or allow a root write to `name_list.xml` there), install the release over it and check that Draw opens on Z–A. It's covered only by `LegacyNameListMigrationTest` today.
- **A live TalkBack listen (owner):** the "Drawn: …" announcement on open, the chip state descriptions and the stepper's polite announcements. TalkBack can't be driven from adb.
- **The title morph** wasn't captured mid-transition. The Draw title keeps NAM-8's shared key and modifier unchanged.
- **NAM-12 hand-offs:**
  - Add `markDrawn(picked)` inside `DrawViewModel.drawInto`'s lock, where the comment marks it.
  - Add the top-bar Reset, the AllDrawn bar's Reset button and the "Draw morphs into Reset" motion (spec :221, :300, :445).
  - Add Put back on drawn chips. They render already but aren't interactive.
  - The pool, counter, box, bar states and reveal endings are already drawn-aware.
- **The compact-height panel** stops above the gesture inset instead of running under it. This is cosmetic.
