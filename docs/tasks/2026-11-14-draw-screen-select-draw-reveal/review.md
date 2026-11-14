# NAM-10 code review

One `flow-reviewer` pass over `origin/main...HEAD` (main 8bae63c), against plan.md and the spec.

## Blocking
| # | Finding | Action |
|---|---|---|
| B1 | `Reveal.kt` keyed its animatables, its motion `LaunchedEffect` and the announcement on `reveal.names`, which compare by value. A "Draw more" that repeats the same names (always the case with one name in the hat, because NAM-10 doesn't mark drawn) didn't animate or announce, and never called `onRevealPlayed()`. `played` stayed false, so a later rotation replayed the sequence (AC9). | **Fixed in 7111bce.** Each draw gets a `drawId`, kept in `SavedStateHandle` (`KEY_DRAW_ID`) and exposed on `RevealUi`; the reveal is keyed on it. Tests: "draw more that repeats the same names is still a new draw" and "the draw id survives a restore". On device (`niah_debug_api36`, one name in the hat): Draw 1 → Draw 1 more replays (`evidence/review-drawmore-same-name-midway.png`: card mid-overshoot, buttons not yet in) and settles (`…-end.png`). |

## Advisory
| # | Finding | Action |
|---|---|---|
| A1 | `dismiss()` didn't guard an in-flight `drawMore()`, so a Done that landed first could have the reveal reopen. | **Fixed in 7111bce.** `drawInto` records the `drawId` it was asked against and aborts in the lock if the result has changed. Test: "done before an in-flight draw more lands keeps the reveal closed" (it fails with the guard removed). |
| A2 | The stepper's `liveRegion` was on the parent `Box`, not on the `Text` that changes. | **Fixed in 85bd0ca.** The live region is now on the value `Text`. A live TalkBack listen is still a manual check (TalkBack can't be driven from adb). |
| A3 | `reduced` was remembered once per dialog, not once per reveal. | **Fixed in 7111bce.** It is now `remember(reveal.drawId)`. |
| A4 | Minor undocumented deviations: the reduced-motion fade is an `Animatable` alpha on the whole takeover, not `AnimatedVisibility`; `status` is a separate flow from `uiState`; `HatIllustration` takes `hat`/`sparkle`, not `body`/`band`/`sparkle`. | Recorded in workflow.md Deviations. No behaviour risk. |

## Acceptance criteria
| AC | Verdict | Notes |
|---|---|---|
| 1 Upgrade | **Partial** | Selection, the stored Unsorted and the launch into the active list were checked on `api36_niah`. Z–A from 2.7.0 was not checked on device: the 2.7.0 data has no sort key, and auto mode blocked writing one. Only `LegacyNameListMigrationTest` covers DESC. Open for the user. |
| 2 Selection persists | Met | Device row 2 |
| 3 Counter | Met | `DrawStateTest`, plurals, device |
| 4 Select all | Met | Unit tests and device (drawn seeded) |
| 5 Sort | Met | Unit tests and device, including relaunch |
| 6 Stepper | Met | Unit tests (including restore and not clamping before load) and device |
| 7 Draw | Met | Seeded unit tests, no `markDrawn`, race and double-tap tests |
| 8 Minimum | Met | Unit tests and device |
| 9 Result survives | Met after B1 | Rotation and `am kill` on device; `SavedStateHandle` restore tests; the drawId fix closes the replay hole |
| 10 Reduced motion | Met (weaker check) | A screenshot at 0.3 s, not a recording |
| 11 Empty state | Met | Device and `BackStackTest` |
| 12 Spec match | Partial | Phone (portrait and landscape) and tablet in light and dark, reveal over both panes, bar icons, Escape and predictive back are evidenced. Not evidenced: the title morph mid-transition, a live TalkBack listen (labels checked in the uiautomator tree), and a 3-name reveal on the phone (1 and 4 on the phone, 3 on the tablet). |
| 13 ViewModel tests | Met | `DrawViewModelTest` 39/0 |
| 14 Grid retired | Met | The grep finds nothing |
| 15 No hard-coded copy | Met | The grep finds nothing; `ListFormatter` |

## Checked
- The reviewer read the full diff, plan, workflow, evidence README and spec sections, and found no scope creep beyond the logged deviations and no weakened tests.
- Verify after the fixes:
  - `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug` passes.
  - Test counts: `:shared` jvm 120, android host 89, iOS 82; `:app` 146 (`DrawViewModelTest` 39); 0 failures.
  - Lint: 0 errors.
- The B1 fix was checked on device. A rotation re-shot after it was discarded: the capture came before the redraw.
