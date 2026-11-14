---
task: draw-screen-select-draw-reveal
title: Build the draw screen: select names, draw, reveal
ticket: NAM-10 — Build the draw screen: select names, draw, reveal (https://linear.app/northtrack/issue/NAM-10/build-the-draw-screen-select-names-draw-reveal)
ticket-id: NAM-10
tracker-synced: In Engineering
mode: full
phase: implementation
branch: flow/draw-screen-select-draw-reveal
worktree: /Users/scoot/dev/names-in-a-hat/android-names-in-a-hat-nam10
created: 2026-11-14
updated: 2026-11-14
---

## Next action
Implementation: rebased onto main 8bae63c (NAM-8 #29 and NAM-9 #30 merged), and the parts the plan reuses are there. Working through Progress from task 1.

## Decision log
<!-- gate entries look like: "- 2026-07-05 gate 1: plan approved by user"
     answered unknowns look like: "- 2026-07-05 Q: per-user or per-team limits? A: per-user"
     log an answer the moment it is given — conversation context does not
     survive a compaction; this file does -->
- 2026-11-14 gate 1: plan approved by user
- 2026-11-14 mode: full (user confirmed). Reason: the app's main screen, with many files; a full-window reveal with motion and a reduced-motion version; a draw result that must survive process death; :shared changes (sort cycle, grid columns retired).
- 2026-11-14 scope: the ticket's 2026-11-12 spec comment overrides the description. Names are wrapping chips (delete GridLayout/GridColumns and PreferencesRepository.gridColumns; AC1's column clause drops). The count control is a stepper [− n +] plus "Draw n". The counter reads "8 of 30 in the hat · 4 drawn" and labels a tri-state select-all checkbox computed over not-drawn names. Sort cycles NONE → ASC → DESC → NONE (update NameSortTypeTest, SortOrderTest, the SortOrder KDoc). MIN_NAMES_TO_DRAW = 1. The reveal is a full-window takeover with a reduced-motion crossfade.
- 2026-11-14 scope: NAM-10 owns NAM-9's AC6 (hand-off comment 2026-11-14, flagged by user). The Draw empty state shows the hat illustration, "This hat is empty", "Add some names to start drawing." and a filled "Add names" button (draw_empty_title / draw_empty_body / draw_empty_action). The button pushes EditList(id) via `backStack.editList(id, fromDraw = true)` in navigation/BackStack.kt. NAM-9 doesn't touch Draw.
- 2026-11-14 Q: where does the NAM-10/NAM-12 line fall? A: drawn-aware, no marking. NAM-10 builds all drawn-aware state and UI: the in-the-hat pool, the "· n drawn" suffix, the tri-state over not-drawn names, the Drawn chip visual, the bar states and the reveal endings. Draw does NOT call markDrawn, so nothing becomes drawn yet. NAM-12 adds markDrawn in the draw step, Reset with undo, and Put back.
- 2026-11-14 Q: how to sequence against the parallel NAM-8 and NAM-9? A: wait for NAM-8. Plan now; start implementation only after NAM-8 (flow/groups-screen) merges, since it owns ic_hat, the ListStatus move and the app-scope DI. Then rebase onto main.

## Progress
- [x] 1. Sort cycle (ea0f273): :shared jvm 116/0, android host 84/0, iOS 77/0
- [x] 2. Draw rules (81cb73e): :shared jvm 128/0, android host 96/0, iOS 89/0
- [x] 3. Retire the grid (d455e70): AC14 grep is empty; :shared jvm 120/0, host 89/0, iOS 82/0; :app 110/0
- [x] 4. DrawViewModel + tests (2294fb7): DrawViewModelTest 36/0 (the toggle-then-draw race test fails if the in-lock re-read is removed); :app 140/0
- [x] 5. Copy + icons (781c29d): :app:clean :app:assembleDebug passes
- [x] 6. Draw screen UI (baafd4d): assembleDebug passes; lintDebug has 0 errors (the new warnings are only the reveal strings task 7 uses); the device look is checked with task 8's wiring, before task 9
- [x] 7. Reveal (0c302da): assembleDebug passes; lintDebug 0 errors (only the 4 warnings that were already there); RevealStaggerTest 3/0; :app 143/0; checked on device after task 8
- [ ] 8. Wire into the shell
- [ ] 9. Device verification + evidence

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
- 2026-11-14 setup: created the branch in the shared main checkout by mistake. Another session's flow/groups-screen was checked out there, with an untracked task folder. Switched it back within seconds (both branches at ff34628, so no files changed) and moved this task to its own worktree, android-names-in-a-hat-nam10. Other sessions share this repo: work only in this worktree.
- 2026-11-14 task 1: plan.md said the concurrent-sort test "ends on ASC". 50 cycles from NONE actually end on DESC (50 mod 3 = 2), so the final assertion is unchanged; only the split changed, to 17/17/16.
- 2026-11-14 task 4: PlaceholderViewModels.kt is deleted outright, since DrawViewModel was the only thing left in it after NAM-8. `DrawPlaceholder` imports the new `ui.draw.DrawViewModel` until task 8 replaces it. NAM-9 had already provided `NameEditor`, so DI only gains `SortOrder` and `NameDraw`.
- 2026-11-14 task 5: added `draw_select_all_a11y` ("%1$s, %2$s"), a key that isn't in the copy deck, so the box's TalkBack label "Select all, 15 of 24 in the hat" isn't a hard-coded join. The chip check reuses NAM-9's `ic_check` (Material); `ic_sort`, `ic_ticket` and `ic_remove` are stroke vectors from the mock-up.
- 2026-11-14 task 6: the quick device install planned for task 6 moved to after task 8, because DrawScreen can't be reached until the shell entry is wired. The stepper is two Surfaces and a value box with the mock-up's connected corner shapes (`LandScreen.dc.html:116-124` for the vertical one), not M3's `ButtonGroup`: the value segment isn't a button, and the vertical stack has no M3 equivalent.
- 2026-11-14 task 7: the reveal's hat body uses `onPrimary`/`onPrimaryContainer` (the colour on the backdrop) instead of the mock-up's fixed #F4F2FF, so it follows dynamic colour. The reveal layout is chosen from the window: compact height → landscape; width ≥ 840 dp → the large grid (3 columns); otherwise the phone column. Added `RevealStaggerTest`, which isn't in the plan.
