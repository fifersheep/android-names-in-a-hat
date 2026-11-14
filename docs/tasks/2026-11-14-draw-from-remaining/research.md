# NAM-12 research: draw from remaining

Baseline: origin/main @ 42bc63a (NAM-10 merged). Verify green: app 146/0, shared 291/0, lint clean.

## Findings

### Already done by NAM-10 (drawn-aware, but nothing is ever marked)
NAM-10's decision log draws the line: "drawn-aware, no marking". Its report lists these NAM-12 hand-offs (`docs/tasks/2026-11-14-draw-screen-select-draw-reveal/report.md:83-87`):
- The **remaining pool exists in `:shared`.**
  - `MIN_NAMES_TO_DRAW = 1` and `Name.inHat = selected && !drawn` (`shared/.../draw/DrawState.kt:6,9`).
  - `DrawState.of` computes inHat, drawnCount, the tri-state box over not-drawn names, and the bar states Stepper / NothingInHat / AllDrawn (`DrawState.kt:54-77`).
  - `NameDraw.drawFrom` filters on `inHat` (`NameDraw.kt:25`), with tests `drawFrom never draws a drawn name` and `drawFrom returns k distinct…` (`NameDrawTest.kt:91,103`).
- The **counter** reads "n of t in the hat · d drawn", using plurals `draw_counter` and `draw_counter_drawn` (`strings.xml:67-74`; `counterText` at `DrawComponents.kt:75`). Spec NAM-12.5 and AC7 are satisfied by the pool. A test still needs to show the counter moving after a marked draw.
- The **Drawn chip visual** has a ticket icon (`ic_ticket.xml`), strikethrough and stateDescription "drawn" (`DrawComponents.kt:165-233`). Drawn chips are **not interactive**: the toggle Modifier is empty for DRAWN (`:193`), and `DrawViewModel.toggle` ignores drawn names (`DrawViewModel.kt:154-161`).
- **DrawBar** shows "Everyone's been drawn" for AllDrawn, but its button is a **disabled "Draw"** (`DrawComponents.kt:242-311`). The spec's filled **Reset** button there is missing.
- **Reveal:** `RevealUi.more` and `noneLeft` come from live `inHat` (`DrawViewModel.kt:126`), so once marking lands, "Draw n more" and "Everyone's been drawn" follow automatically.
- The **Lists card drawn count** (NAM-8.H1) is done: `GroupDao` sums `drawn` and the `lists_item_drawn` plural exists (`GroupDao.kt:17`, `strings.xml:16`).

### What NAM-12 must add
1. **Mark drawn in the draw step.** `DrawViewModel.drawInto` has a placeholder comment at `DrawViewModel.kt:219`, inside `writes.withLock`, before `setResult`. The ticket says the remaining-pool rule lives in `:shared` and the VM calls it. Today the VM does read → `drawFrom` → (mark) → result inline. A `:shared` use case (e.g. read the names, `drawFrom`, `markDrawn`, return the picks) would let AC1 ("draw 2 of 6 twice, disjoint, seeded") run in commonTest against `FakeNameRepository`.
2. **A per-name un-draw** (spec hand-off NAM-12.4, `3.0-spec.md:206-207`):
   - Repo: `NameRepository.kt:30-34` has only `markDrawn(ids)` and `clearDrawn(groupId)`. The DAO has the matching two queries (`NameDao.kt:41-45`).
   - Add `clearDrawn(ids: Collection<Long>)` (an overload, or a new name) to the interface, `RoomNameRepository`, the `NameDao` query, both fakes (`shared/src/commonTest/.../fakes/FakeNameRepository.kt:47-55` and `app/src/test/.../FakeNames.kt:58-66`), and `RoomRepositoryTest`.
   - Put back = clear drawn **and** set selected = true (`3.0-spec.md:205`).
3. **Reset with undo** (spec `:233-238`):
   - The top-bar icon button is enabled when any name is drawn and visible only when the list has names (mock-up `DrawScreen.dc.html:30-32`: `resetOpacity` 0.38 when nothing is drawn, inside `hasNames`).
   - It is also the AllDrawn bar's filled primary button (`DrawScreen.dc.html:156`).
   - The snackbar reads "Drawn names reset" with **Undo**, which restores the previous drawn set. There is no confirmation.
4. **Put back in the hat** on drawn chips:
   - Tapping a drawn chip opens a DropdownMenu anchored to the chip, with one item "Put back in the hat" and an undo-arrow icon (`DrawScreen.dc.html:58-66`, path `M9 14 L4 9 L9 4 M4 9 H14 A6 6 0 0 1 14 21 H11`).
   - The same item is a TalkBack custom action (`3.0-spec.md:204`, `:465`).
5. **Strings missing:** `draw_reset`, `draw_reset_a11y`, `draw_reset_done` and `chip_put_back` (copy deck `3.0-spec.md:610-612,625`; the placeholder comment is at `strings.xml:64`).
6. **Icons missing:** there is no reset icon in `res/drawable`. Mock-up path: `M5 12 A7 7 0 1 0 7.5 6.6 M5 4 V9 H10`. There is also no put-back icon.
7. **Motion:** "Draw morphs into Reset" uses a default spatial spring on size (`3.0-spec.md:445`). DrawBar already wraps in `animateContentSize(spring)` (`DrawComponents.kt:287,299`).

### Conventions and prior art
- **Undo snackbars:**
  - `ListsScreen.kt:183-201` and `EditNamesSection.kt:387+`: a `LaunchedEffect(pending?.id)` calls `showSnackbar(..., actionLabel = undo, duration = SnackbarDuration.Long)`. The duration is explicit; the M3 1.5 trap is the Indefinite default.
  - They re-show after rotation because pending state lives in the VM.
  - DrawScreen has no `SnackbarHost` yet (`DrawScreen.kt:98`).
- **Serialisation:** every DrawViewModel write goes through one `Mutex` and re-reads via `current()` (`DrawViewModel.kt:110,234`). Reset, undo and put-back should do the same.
- **Saved state:** DrawViewModel keeps the count, result and played flag in `SavedStateHandle` (`:272-276`). A pending reset-undo snapshot (the ids drawn before the reset) belongs there too, so Undo survives rotation and process death.
- **DI:** `StorageModule.kt:55,62` provides `NameEditor` and `NameDraw`; a new `:shared` use case is provided the same way.
- **Selection is independent of drawn:**
  - `NameEditor.selectAll/clearAll` → `setAllSelected` never touches drawn. This is tested in `NameEditorTest.kt:114` (shared), `DrawViewModelTest.kt:226` and `RoomRepositoryTest.kt:162` (AC4, already covered).
  - Reset must not touch selection: `clearDrawn(groupId)` is tested at `RoomRepositoryTest.kt:204`. AC3 needs the VM/use-case-level assertion.
- **Upgrades (AC9):** `MigrationTest.kt:70` asserts every migrated name has drawn = false. Covered; cite it.
- **New name joins the hat (AC8):** `NameEditor.add` stores selected = true, and the repo stores drawn = false (`NameEditor.kt:13-16`, `RoomNameRepository.kt:15`). This needs a test: draw, add, next draw's pool includes the new name.
- **Test names:** no commas in commonTest backtick names (Kotlin/Native). Verify with `--no-watch-fs` and read the XML counts.
- **Existing test to flip:** `DrawViewModelTest.kt:352` `a draw never marks names drawn` asserts the NAM-10 behaviour that NAM-12 reverses.

### Device verification (AC5 and AC10)
- Use `niah_debug_api36` for debug installs. **Never** debug-install or `pm clear` on `api36_niah`, which holds the 2.7.0 data.
- AC5: draw, then `am force-stop`, reopen, and check the drawn chips and counter.
- AC10: check the uiautomator tree for the "drawn" stateDescription and the custom action. A live TalkBack listen is the owner's job, because TalkBack can't be driven from adb.

## Constraints and prior art
- Spec decisions D2, D7, D8 and D9 are settled. The NAM-12 owner comment (2026-11-12) restates them: always on, reset with undo and no confirm, put back clears drawn and sets selected, minimum 1 on the pool, AllDrawn only when the hat is empty and a selected name is drawn.
- "Do not show a result that was not saved": `markDrawn` must complete before `setResult`. If `markDrawn` throws, no result is shown.
- Out of scope: draw history, teams, weighted draws.
- AC11 (close GitHub #16 with a link to NAM-12) is an outward action. #16 is OPEN as of 2026-11-14. Do it after merge at wrap, with the user's OK.

## Open questions (triaged; none shape-changing, so none asked)
- **Where the draw+mark step lives** (conventional default): a small `:shared` use case that reads the names, draws, marks, and returns the picks; the VM calls it inside its mutex. The ticket's implementation note puts the rule in `:shared`. → plan Assumptions.
- **Put back atomicity** (conventional default): either `clearDrawn(ids)` plus `setSelected(id, true)` as two writes under the VM mutex, or one `@Transaction` in the repo. The spec names only the `clearDrawn(ids)` hand-off. A crash between the two writes leaves the name Out, which is harmless. Planner's choice. → Assumptions.
- **Undo semantics** (conventional default): reset commits immediately, which differs from delayed delete because the spec says undo "restores". Undo snapshots the ids drawn before reset and calls `markDrawn(snapshot)`. Ids deleted meanwhile are no-ops. Names drawn after the reset stay drawn (union). → Assumptions.
- **Undo lifetime** (conventional default): the snapshot lives in SavedStateHandle and is dropped when the snackbar is dismissed or times out, or on a new reset. Leaving the screen loses Undo, as with the other snackbars. → Assumptions.
- **Put back from a menu vs. direct tap** (decided by spec D9): a menu with one item, plus a custom accessibility action.
- **AC11 timing** (deferred, owner): close #16 at wrap after merge, with the user's OK.
