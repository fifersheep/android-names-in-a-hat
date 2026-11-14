# NAM-12 code review

One flow-reviewer pass over `origin/main...HEAD` at 1d93882, against plan.md.

## Blocking
None.

## Advisory
- **A1: the reset's Undo came back after visiting Edit.**
  - Where: `DrawScreen.kt` `ResetSnackbar`, with `DrawViewModel.dropReset`.
  - Repro: Reset, then the overflow → Edit list within 10 s.
  - Why it happened: Draw's ViewModel stays alive under Edit. Leaving composition cancels `showSnackbar` instead of returning Dismissed, so the pending reset survives, and on Back "Drawn names reset · Undo" shows again for 10 s, possibly minutes later.
  - Plan conflict: the plan's assumption said "leaving Draw drops Undo along with the screen".
  - **Fixed in 6e1af72.** It uses the established pattern (`ListsScreen.kt:95-97`, `EditNamesSection.kt:123-125`): a `LifecycleStartEffect` whose `onStopOrDispose` calls the new `DrawViewModel.dropPendingReset()` unless `isChangingConfigurations`, so rotation still re-shows Undo. Test: `leaving draw ends the reset undo`. Checked on device (see below).
- **A2: `drawMore()` was never asserted disjoint directly.** The planned "draw more draws only from remaining" test was written as draw → dismiss → draw. **Fixed in 6e1af72** with the test `draw more draws only from the names left in the hat`.

## Acceptance criteria
| AC | Status | Evidence |
|---|---|---|
| 1 Disjoint second draw | MET | `HatDrawTest` (seeds 0..49); VM `a second draw never repeats…`; device step 04 |
| 2 Everyone drawn → Reset | MET | `HatDrawTest` and VM `drawing everyone leaves the bar on everyone drawn`; `DrawComponents.kt` AllDrawn → Reset; screenshot 09 |
| 3 Reset keeps selection; Undo restores, also after a restore | MET | `HatDrawTest` reset/undo; VM reset/undo/restore tests; device steps 10-11 |
| 4 Select all / clear all leave drawn | MET (cited) | `NameEditorTest:114`, `DrawViewModelTest:227`, `RoomRepositoryTest:162` |
| 5 Survives force-stop | MET | Device step 05 (niah_api30; deviation recorded) |
| 6 One name left can be drawn | MET | `HatDrawTest` `the last name in the hat can be drawn` |
| 7 Counter and stepper show the remaining pool | MET | VM `the counter and stepper follow what is left after a draw`; device step 03 |
| 8 Added name joins; deleted name drops its drawn state | MET | `HatDrawTest`, two tests |
| 9 Upgraded names not drawn | MET (cited) | `MigrationTest:70` |
| 10 Drawn not by colour alone; TalkBack | MET | `DrawChipSemanticsTest` (4 tests; a mutation check made the 2 drawn-chip tests fail); screenshots 05/09. The owner does a live TalkBack listen |
| 11 Close GitHub #16 | NOT MET (by design) | At wrap, after merge, with the user's OK |
| Put back | MET | `HatDrawTest`, two tests; VM two tests; device steps 06-07 |

## Checked (reviewer)
- **Test changes:** five existing VM tests were rewritten exactly as plan task 3 listed. None were deleted or loosened.
- **Shared tests:** there are no commas in commonTest names, and iOS tests ran (94).
- **HatDraw ordering:** the mark completes before `setResult`, inside the Mutex. A failed mark is caught, with CancellationException rethrown. A draw cancelled by Done marks nothing. `putBack` selects, then clears drawn.
- **Concurrency:** reset, undo and put back all go through the one Mutex. The token is persisted and increases, and stale tokens are no-ops. `drawing` is cleared in `finally`.
- **Compose:**
  - The snackbar has an explicit `Long` duration, and a newer token cancels the older snackbar.
  - The drawn chip is `clickable` with `Role.Button` and has a custom action in the merged node.
  - The menu is anchored to the chip.
  - Reset is enabled when `drawnCount > 0`, and the copy matches the copy deck.
- **Scope:** every file traces to a plan task or a recorded deviation.
