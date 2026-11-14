# Report — Draw additional names from those remaining (NAM-12)

## Changed vs plan
All five plan tasks are done, as in [plan.md](plan.md). Commits, in order:
1. `3b0c1ef`: **per-name un-draw.**
   - `NameRepository.clearDrawn(ids: Collection<Long>)`, with the matching DAO query and Room implementation (empty ids is a no-op).
   - Both fakes gain it, plus failure/gate hooks for draw writes.
   - Covered by `RoomRepositoryTest` and `DatabaseThreadingTest`.
2. `28dd911`: **the `HatDraw` use case in `:shared`.**
   - `draw` reads the names, draws from the hat, and marks the picks drawn before returning them.
   - `reset` returns the undo snapshot and writes nothing when nothing is drawn.
   - `undoReset` re-marks the snapshot.
   - `putBack` selects first, then clears drawn.
   - 12 commonTest tests run on jvm, iOS and androidHost.
3. `8e9ca3c`: **DrawViewModel on HatDraw.**
   - A draw marks the picks drawn and catches a failed write (no result, no crash).
   - Reset stores a token and snapshot in SavedStateHandle. Undo and drop are checked against the token.
   - `putBack(id)` covers the Put back action.
   - The five tests that marking breaks were rewritten as planned, and 8 + 5 tests were added.
4. `9a4bc2f`: **UI.**
   - A Reset icon in the top bar, enabled once anything is drawn.
   - When everyone's been drawn, the bar's Draw morphs into a filled **Reset**.
   - "Drawn names reset" / Undo snackbar, with an explicit `Long` duration.
   - A drawn chip opens an anchored "Put back in the hat" menu, which is also a custom accessibility action.
   - New strings and the `ic_reset` / `ic_put_back` vectors.
   - `DrawChipSemanticsTest` (Robolectric Compose, 4 tests) covers AC10.
5. `f3f32b4`: **device check** on `niah_api30`, recorded in [evidence/README.md](evidence/README.md).

Then the review fix `6e1af72` (see [review.md](review.md)): leaving Draw ends the reset's Undo, and a direct `drawMore` disjointness test was added.

Diff: 41 files, +1071 / −30, including docs and evidence.

## Deviations
From workflow.md, with the reason for each:
- **The compact-height SnackbarHost went in the names area up front.**
  - The plan said to check on device first. The trailing draw panel is part of the content, not the Scaffold's bottom bar, so a Scaffold snackbar would cover its button.
  - Placement confirmed on device (evidence 12, 14).
- **`:app` lint shows a new NewerVersionAvailable warning (robolectric 4.17 → 4.18).**
  - `:app` now uses the catalog entry that `:shared` pins.
  - Kept at 4.17 so both modules stay on one version. It's a warning only; lint has 0 errors.
- **`unitTests.isIncludeAndroidResources = true` in `:app`.**
  - The Robolectric semantics test needs resources to resolve strings. The plan didn't name it.
- **Device check on `niah_api30` (API 30), not `niah_debug_api36`.**
  - `niah_debug_api36` was running with a recently updated app while another names-in-a-hat session was busy, so it was left alone.
  - `niah_api30` first hung and then refused to boot because the disk was full. The user freed space, and the rerun was clean.
  - Only a phone was checked, portrait and landscape; there was no tablet run (not in the plan).
- **Review fix A1 adds `DrawViewModel.dropPendingReset()` and a stop-time `LifecycleStartEffect`.** Neither was in plan.md.
  - The plan assumed leaving Draw dropped Undo. The reviewer showed that Edit pushed over Draw keeps the ViewModel, so the snackbar came back on return.
  - The fix follows the Lists and Edit-names pattern (commit on stop, except for a rotation). As a result, backgrounding the app also ends Undo, as it does on those screens.

## Evidence
**Verify** at `4017cf3`: `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug`, counted from the result XMLs:
```
exit 0
BUILD SUCCESSFUL
app/build/test-results/testDebugUnitTest: 165 tests, 0 failures      (baseline 146)
shared/build/test-results/iosSimulatorArm64Test: 94 tests, 0 failures (baseline 82)
shared/build/test-results/jvmTest: 133 tests, 0 failures              (baseline 120)
shared/build/test-results/testAndroidHostTest: 101 tests, 0 failures  (baseline 89)
lint: 0 errors, 4 warnings
```
The 4 warnings are OldTargetApi, AndroidGradlePluginVersion and RedundantLabel, all from before this change, plus NewerVersionAvailable for robolectric (see Deviations).

**Mutation check on AC10:** with the drawn chip's custom action and menu disabled, `DrawChipSemanticsTest` failed 2 of 4 (the two drawn-chip tests). The code was restored afterwards.

**Device**, `niah_api30`, debug build. Full table in [evidence/README.md](evidence/README.md); each line below gives the DB state as `name:selected drawn`.
- **Draws:**
  - Draw 2 marked Ada and Dev; the next Draw 2 marked Ben and Cat, disjoint (AC1). Screenshots 02–04.
  - The counter read "4 of 6 in the hat · 2 drawn" (AC7).
- **Force-stop** (`pidof` empty), then a cold launch: 4 Drawn chips with the ticket icon and strikethrough, and "2 of 6 in the hat · 4 drawn" (AC5, AC10 visuals). Screenshot 05.
- **Put back** on Ben: an anchored menu, then Ben:10 and "3 of 6 in the hat · 3 drawn". Screenshots 06–07.
- **Everyone drawn:** the reveal shows "Everyone's been drawn". The bar shows "Everyone's been drawn" and a filled Reset (AC2). Screenshots 08–09.
- **Reset → Undo**, in one script:
  - After Reset, every name is :10 and the selection is unchanged (AC3). The snackbar reads "Drawn names reset" / Undo (screenshot 10).
  - Undo at t+7 s brought every name back to :11 (screenshot 11).
- **Landscape:** the snackbar sits over the names, clear of the trailing panel (screenshot 12).
- **After the review fix:**
  - Reset, then Edit list, then Up, at t+13 s: no snackbar, and the reset stands (screenshot 13).
  - Reset, then rotate: Undo still shows (screenshot 14).

**AC table:** in [review.md](review.md). All are MET except AC11 (closing GitHub #16), which happens at wrap.

## Follow-ups
- **AC11 (owner's OK at wrap):** close GitHub #16 with a link to NAM-12.
- **A live TalkBack listen (owner):** the "drawn" state, the "Put back in the hat" action and the reset snackbar. TalkBack can't be driven from adb; the semantics are covered by `DrawChipSemanticsTest`.
- **Tablet check (optional):** Reset and Put back in list-detail on `niah_tablet_api36`, which wasn't run.
- **robolectric 4.18:** bump `:shared` and `:app` together in a deps pass.
