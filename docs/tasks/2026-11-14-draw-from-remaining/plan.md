# Plan — Draw additional names from those remaining (NAM-12)

## Intent
Make every draw take names out of the hat for good, until the user resets the list or puts a name back. NAM-10 already made the screen drawn-aware (pool, counter, box, bar states, Drawn chip). This ticket adds the writes: mark drawn on draw, Reset with undo, and Put back in the hat. Spec: `docs/design/3.0-spec.md` "Draw from remaining" and "Chip states".

## Acceptance criteria
Ticket ACs 1–11, made checkable:
- [ ] **AC1** In `HatDrawTest` (commonTest), drawing 2 of 6 selected and then 2 again gives disjoint picks, for seeds 0..49.
- [ ] **AC2** After every selected name is drawn, `DrawState.bar == AllDrawn` (HatDraw + VM test). On device the bar shows "Everyone's been drawn" with a filled **Reset** and no Draw button.
- [ ] **AC3** Reset clears drawn for the list, and every name's `selected` is unchanged (HatDraw + VM test). **Undo** restores the previous drawn set (VM test, including after a SavedStateHandle restore).
- [ ] **AC4** Select all and Clear all leave `drawn` unchanged. This is already covered by `NameEditorTest.kt:114`, `DrawViewModelTest.kt:226` and `RoomRepositoryTest.kt:162`; cite them, no new code.
- [ ] **AC5** On `niah_debug_api36`: draw, `am force-stop`, reopen. The same chips are Drawn and the counter shows "· n drawn".
- [ ] **AC6** With one name in the hat, Draw 1 draws it (HatDraw test).
- [ ] **AC7** After a draw of 2 from 4 in the hat, the VM state shows `inHat == 2`, the stepper max is 2, and `drawnCount == 2` (VM test).
- [ ] **AC8** A name added after a draw is in the next draw's pool (HatDraw test). A deleted drawn name no longer counts as drawn (HatDraw test).
- [ ] **AC9** Upgraded names are all `drawn = false`. Already covered by `MigrationTest.kt:70`; cite it.
- [ ] **AC10** The Drawn chip keeps its ticket icon, strikethrough and stateDescription "drawn", and gains the custom action "Put back in the hat".
  - Evidence: a Robolectric Compose semantics test, `DrawChipSemanticsTest`, asserts that:
    - the drawn chip has `StateDescription == "drawn"`
    - it has a `CustomActions` entry labelled "Put back in the hat" whose action calls `onPutBack(id)`
    - In and Out chips have no such action and keep "in the hat" / "out of the hat".
  - The icon and strikethrough are checked by device screenshot. uiautomator dumps contain neither stateDescription nor custom actions (device notes).
  - A live TalkBack listen stays an owner follow-up.
- [ ] **AC11** GitHub #16 is closed with a link to NAM-12, at wrap after merge, with the user's OK.
- [ ] **Put back:** tapping a drawn chip opens a one-item menu. Choosing it leaves the name `selected = true, drawn = false`, and no other name changes (HatDraw + VM test, plus a device check).

## Approach
- **`:shared` use case `HatDraw(names: NameRepository, draw: NameDraw)`** in `shared/.../draw/HatDraw.kt` owns the writes:
  - `suspend fun draw(groupId, count): List<Name>` reads the names, runs `drawFrom`, calls `markDrawn(picked ids)` and returns the picks. If `markDrawn` throws, the exception propagates, so no result is returned (the "never show an unsaved result" rule).
  - `suspend fun reset(groupId): List<Long>` snapshots the ids currently drawn, calls `clearDrawn(groupId)` and returns the snapshot. When nothing is drawn it is a no-op and returns an empty list.
  - `suspend fun undoReset(ids)` calls `markDrawn(ids)`.
  - `suspend fun putBack(id)` calls `setSelected(id, true)` **then** `clearDrawn(listOf(id))`. In this order the chip goes Drawn → In with no intermediate Out frame, because `visual()` checks drawn first (`DrawViewModel.kt:260-264`).
- **The repo gains `clearDrawn(ids: Collection<Long>)`**, an overload of `clearDrawn(groupId: Long)`. It is added to the interface, the DAO query, `RoomNameRepository` (an empty collection is a no-op, as with `markDrawn`) and both fakes.
- **DrawViewModel** swaps `NameDraw` for `HatDraw`, called inside the existing `writes` mutex:
  - `drawInto` calls `hat.draw(listId, n)`, and the placeholder comment goes. It catches a write failure, so a failed mark shows no result and doesn't crash. Precedent: `EditNamesViewModel.kt:239` and the test `an add into a list deleted underneath does not crash`.
  - `reset()` stores a `PendingReset(token, ids)` in SavedStateHandle (`resetToken: Long`, `resetIds: LongArray`) **only when the snapshot is non-empty**, and exposes it as `DrawUiState.pendingReset`.
  - `undoReset(token)` / `dropReset(token)` act only if the token matches, so an older snackbar never undoes a newer reset.
  - `putBack(id)` acts only if that name is drawn.
- **UI:**
  - DrawScreen gains a `SnackbarHost`, plus a `LaunchedEffect(pendingReset?.token)` that shows "Drawn names reset" / Undo with `SnackbarDuration.Long`. This is the same pattern as `ListsScreen.kt:194-200`.
  - A Reset `IconButton` goes in the top-bar actions after the sort pill. It shows when the list has names and is enabled when `drawnCount > 0`.
  - `DrawBar` gains `onReset`. In AllDrawn its button is a filled **Reset** rather than a disabled Draw, and the existing `animateContentSize` gives the morph.
  - `NameChip` gains `onPutBack` and becomes `internal` so the semantics test can host it. A Drawn chip becomes `clickable`: it opens an anchored `DropdownMenu` with "Put back in the hat", and carries `customActions = [Put back]`.
- **Alternatives:**
  - Mark inline in the VM. Rejected: the ticket puts the rule in `:shared`, and AC1 belongs in commonTest.
  - A single combined DAO `putBack` query. Rejected: the repo contract keeps the flags' writes separate (`NameRepository.kt:11`).
  - A delayed reset like delayed delete. Rejected: the spec says Undo "restores the previous drawn set" (`3.0-spec.md:237`).
  - AC10 left only to the owner's TalkBack check. Rejected because a semantics test gives real evidence, but it is kept as the fallback.

## Assumptions & open questions
- [assumed] The draw+mark step lives in a new `:shared` `HatDraw`, and the VM calls it. Evidence: the ticket's implementation note ("remaining-pool rule belongs in `:shared` … ViewModel … calls it"); `NameEditor`/`SortOrder` are provided the same way (`StorageModule.kt:55,58`).
- [assumed] The un-draw API is a `clearDrawn(ids: Collection<Long>)` overload.
  - The spec hand-off (`3.0-spec.md:206`) asks only for "clear drawn for given ids", with no signature.
  - The owner's NAM-12 Linear comment suggests `clearDrawn(ids: Collection<Long>)`.
  - Only `RoomNameRepository` and the two fakes implement the interface.
- [assumed] Put back is two ordered writes (`setSelected`, then `clearDrawn(ids)`) under the VM mutex, not a transaction. Evidence: `NameRepository.kt:11` keeps flag writes independent. A crash between them leaves the name selected and drawn, which is still a valid Drawn chip, and retrying works.
- [assumed] Reset commits at once, and Undo re-marks the snapshot.
  - Names deleted in between are no-ops.
  - Names drawn after the reset stay drawn (union).
  - Evidence: spec `:237` "restores the previous drawn set"; `markDrawn` on missing ids is a harmless `UPDATE … IN` (`NameDao.kt:41-42`).
- [assumed] The Undo snapshot lives in SavedStateHandle. It is dropped when the snackbar is dismissed or times out, or by a newer reset. Leaving Draw drops Undo along with the screen. Evidence: the NAM-8/NAM-9 snackbars behave the same (`ListsScreen.kt:178-201`); DrawViewModel already keeps its state in SavedStateHandle (`DrawViewModel.kt:271-277`).
- [assumed] If the process dies after `markDrawn` but before `setResult`, the names stay drawn and no reveal shows. This is accepted: the ticket forbids only the reverse (a shown result that wasn't saved), and the window is a few ms.
- [assumed] A failed `markDrawn` in the VM shows no result and doesn't crash (a catch in `drawInto`), matching `EditNamesViewModel.kt:239`.
- [assumed] **Snackbar overlap.**
  - If the user draws while Undo is showing, the reveal overlay covers the snackbar, and its timeout drops Undo. This is accepted: the user chose to draw again.
  - In compact height, the snackbar must not cover the trailing panel's button. This is checked on device; if it does, the SnackbarHost moves into the names area.
- [assumed] **One Robolectric Compose semantics test is added to `:app` for AC10 only.**
  - Why: the critic's Blocking finding; no route on the device can see Compose semantics.
  - Deps: `ui-test-junit4` and `ui-test-manifest` from the compose BOM, plus `libs.robolectric`, which is already in the catalog and used by `:shared` host tests.
  - Config: `app/src/test/resources/robolectric.properties` with `sdk=34`, the JDK 17 pin (names-build-gotchas).
  - All other UI checks stay on device, as in NAM-10.
  - Fallback: if the Robolectric setup fights back for more than about 30 min, AC10 falls back to the owner's TalkBack check, recorded as a deviation.
- [assumed] New icons `ic_reset` and `ic_put_back` are vectors taken from the mock-up paths (`DrawScreen.dc.html:31,62`).
- [open — owner, at wrap] AC11: close GitHub #16. It is an outward action, so ask before doing it.
- [open — owner, follow-up] A live TalkBack listen for the "drawn" state and the Put back action.

## Tasks
1. **Per-name un-draw in the repo, plus fake hooks.**
   - Files:
     - `shared/.../repository/NameRepository.kt`, `shared/.../data/NameDao.kt`, `shared/.../data/RoomNameRepository.kt`
     - `shared/src/commonTest/.../fakes/FakeNameRepository.kt`: add `clearDrawn(ids)` and a `failMarkDrawn` hook
     - `app/src/test/.../FakeNames.kt`: add `clearDrawn(ids)`; `markDrawn` honours `failWrites` and a new `drawGate`
     - `shared/src/jvmTest/.../data/RoomRepositoryTest.kt`: a new test that it clears only the given ids and never changes selected; empty ids is a no-op
     - `shared/src/jvmTest/.../data/DatabaseThreadingTest.kt`: add the new call to its "every repository call" list
   - Verify: `./gradlew --no-watch-fs :shared:jvmTest :app:compileDebugUnitTestKotlin`.
2. **`HatDraw` use case + tests** (AC1, 2, 3, 6, 8, put back, and a failed mark returning nothing).
   - Files: `shared/src/commonMain/.../draw/HatDraw.kt`, `shared/src/commonTest/.../draw/HatDrawTest.kt`. No commas in test names.
   - Verify: `./gradlew --no-watch-fs :shared:allTests`, then read the XML counts.
3. **DrawViewModel on HatDraw: mark on draw, reset/undo, put back** (AC2, 3, 7).
   - Files: `app/.../di/StorageModule.kt`, `app/.../ui/draw/DrawViewModel.kt`, `app/src/test/.../ui/draw/DrawViewModelTest.kt`.
   - Existing tests that marking breaks, and how each is rewritten:
     - `a draw never marks names drawn` → "a draw marks its names drawn before the result shows", using `drawGate`: no reveal while the mark is pending.
     - `a draw of k returns k distinct names from the hat` → compute the in-the-hat set **before** drawing.
     - `a seeded draw is deterministic` → a fresh `FakeNames` per VM.
     - `the reveal offers the count again or as many as are left` → expect `more = 1` after drawing 3 of 4, plus a larger-pool case where `more = n`.
     - `draw more that repeats the same names is still a new draw` → on the 1-name list: draw, Put back, then Draw more.
   - New tests:
     - the counter and stepper after a draw (AC7)
     - draw more draws only from the remaining names
     - reset keeps the selection
     - reset with nothing drawn shows no undo
     - undo restores
     - undo survives a restore
     - a stale token is ignored
     - put back
     - a drawn chip still isn't toggled
     - a failed mark shows no result
   - Verify: `./gradlew --no-watch-fs :app:testDebugUnitTest`.
4. **UI: Reset (top bar and AllDrawn bar), undo snackbar, Put back menu and a11y action, semantics test** (AC2, AC10).
   - Files:
     - `app/src/main/res/values/strings.xml`: add `draw_reset`, `draw_reset_a11y`, `draw_reset_done` and `chip_put_back`; drop the "arrive with NAM-12" comment
     - `app/src/main/res/drawable/ic_reset.xml`, `app/src/main/res/drawable/ic_put_back.xml`
     - `app/.../ui/draw/DrawScreen.kt`, `app/.../ui/draw/DrawComponents.kt`
     - `app/src/test/.../ui/draw/DrawChipSemanticsTest.kt`, `app/src/test/resources/robolectric.properties`
     - `app/build.gradle.kts`, `gradle/libs.versions.toml`
   - Verify: `./gradlew --no-watch-fs :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`.
5. **Device check** (AC2, AC5, the AC10 visuals, put back) on `niah_debug_api36`.
   - Reset → Undo must happen inside the 10 s snackbar. Run it as **one** bash script, with coordinate taps and no dumps between Reset and the Undo tap (device notes, NAM-8/NAM-9).
   - Steps:
     1. Seed a 6-name list.
     2. Draw 2 twice (disjoint).
     3. Force-stop and reopen.
     4. Put back.
     5. Draw until everyone's been drawn → bar Reset → snackbar Undo.
     6. Landscape: Reset, then check the snackbar's placement.
     7. Take screenshots of the Drawn chips.
   - Save the evidence in the task folder.
   - Verify: by observation, recorded in the report.

## Out of scope
- A history of past draws, teams (GitHub #11), weighted draws.
- Changing the reveal (NAM-10), the Lists card drawn count (already done by NAM-8) or migrations (AC9 is already tested).
- Compose UI tests beyond the single AC10 semantics test.

## Verification
- **Machine:** `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug`. Confirm the counts from `*/build/test-results/**/*.xml`; the baseline is app 146 and shared 291.
- **Outcome:** on `niah_debug_api36` (never on `api36_niah`):
  1. Open a list with 6 selected names and set the stepper to 2.
  2. Draw → Done. The two chips show as Drawn and the counter reads "4 of 6 in the hat · 2 drawn".
  3. Draw again. Neither earlier name appears.
  4. Force-stop and reopen: the same 4 are Drawn.
  5. Tap a Drawn chip → Put back. It returns as In and the counter reads 3.
  6. Draw until "Everyone's been drawn" appears with Reset. In one script: tap Reset, check that the snackbar reads "Drawn names reset" and every chip is back In with the selection unchanged, then tap Undo. The Drawn chips are restored.
  7. Landscape: after Reset, the snackbar doesn't cover the trailing panel's button.
