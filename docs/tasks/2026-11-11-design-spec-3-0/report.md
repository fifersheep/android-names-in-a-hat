# Report: Produce the 3.0 design spec (NAM-2)

## Changed vs plan
All 6 plan tasks are done ([plan.md](plan.md); Progress in [workflow.md](workflow.md)). The change is docs only: 45 files and 3441 insertions in `e73220a..HEAD`, all under `docs/`.

| Task | Commit(s) | What |
|---|---|---|
| 1 Brand board | dd1d1f4, ebaad3d | Three Northtrack directions (Ticket, Top Hat, Chalkboard) with palettes, type, icon concepts and a Draw screen in each, plus the D18 comparison and five mixed-box options. **Owner picked Top Hat, the tri-state checkbox, and the tonal-dash mixed box** |
| 2 Spec | 5667e25 | [`docs/design/3.0-spec.md`](../../design/3.0-spec.md): coverage, D1–D20, vocabulary, navigation and back stack, launch, Lists / Edit list / Draw, select header, chip states, draw bar, draw from remaining, reveal, large screens, states, delayed delete, About, theme (palette and contrast), type, shape, icon, motion, reduced motion, accessibility, API stability, ticket index, artboards, copy deck |
| 3 Phone portrait | d488434 | Draw (sorts, put back, nothing in the hat, all drawn, empty), Lists (populated, empty, About), Edit list (new, edit, rename, blank error and undo), and a dynamic-colour sample. Light and dark |
| 4 Landscape and large | e609765 | Phone landscape for every screen; 1280×800 list-detail with Draw, with Edit list, with no list open and with no lists. Light and dark |
| 5 Reveal | b3b50b9 | Animated reveal in phone, landscape and large layouts; reduced-motion crossfade; end states (more left, fewer than n, none left). Light and dark |
| 6 Link and freeze | 64ce613, 45ef713 | Artboard checklist matches the canvas; the canvas and spec are linked on NAM-2; [ticket-comments.md](ticket-comments.md) drafted |
| Review | f282ba7, 6c96785 | 19 advisory fixes (see [review.md](review.md)), plus one mock-up fix found during the outcome check |

**Design canvas:** https://claude.ai/artifact/V3SwNVHPd4UNUtz6YDk15q. It's private: share it from its Share menu if anyone else needs it. Sources are under `docs/design/3.0-mockups/project/`.

## Deviations
- **Canvas file layout is set by the Design type.** The plan had shared `tokens.css`/`components.css` files. The type needs self-contained `.dc.html` artboards plus `canvas.json`, so shared building blocks became component artboards (`DrawScreen`, `ListsScreen`, `EditScreen`, `LargeScreen`, `LandScreen`, `Reveal`, …) and the token table is copied into each. The spec's palette table is the source of truth.
- **Extra `MixedOptions` artboard.** The owner asked for alternatives to the mixed-state box after picking the tri-state checkbox. Option 2, the tonal dash, was chosen.
- **Check scripts live in the task folder** (`checks/`) instead of the scratchpad, so review and gate 2 can re-run them. Also, the reveal's sparkle colours changed: gold on indigo was 1.10:1, so the sparkles now use `tertiaryContainer` on `primary` (light) and `tertiary` on `primaryContainer` (dark).
- **Artboards are grouped by screen, not one per state.** Edit list also gained a separate "rename" state. The spec checklist matches.
- **NAM-5 merged mid-task without a per-name un-draw.** The hand-off D9 needs ("Put back in the hat") moved from NAM-5 to NAM-12, and there is no NAM-5 comment.
- **Reveal end states and the reduced-motion variant are drawn in portrait only.** The animated reveal is drawn in all three layouts. The same component serves every layout, and the reveal AC is met.
- **Review refinements to agreed decisions (for confirmation at gate 2):**
  - D18: the select-all box state is computed over **not-drawn** names, so drawn chips, which hide their selection, can't change it invisibly. It gains a Disabled state when every name is drawn.
  - D6: "Everyone's been drawn" + Reset shows only when the hat is empty **and** a selected name is drawn. Otherwise the bar shows the "Tap names" hint.
  - D3: back on a **new** list before its name is committed discards it and creates nothing.

## Evidence
**Verify** at 6f3f46e (re-run for this report):
```
$ ./gradlew --no-watch-fs :app:assembleDebug :shared:allTests
BUILD SUCCESSFUL in 16s
$ (sum of shared/build/test-results/*/*.xml)
tests 210 failures 0                      # same as the baseline: docs-only change
$ git diff --name-only e73220a..HEAD | grep -vc '^docs/'
0
```

**Spec checks** (`checks/check_spec.py docs/design/3.0-spec.md <(git show origin/master:app/src/main/res/values/strings.xml)`):
```
anchors: 31 defined, 24 linked, missing: none
coverage rows: 9 (expect 9), without a link: 0
ticket index per ticket: {'7': 7, '8': 4, '9': 7, '10': 4, '12': 6, '6': 1}; mismatches: none
copy deck: 29 2.7.0 keys + 8 hard-coded; unaccounted: none
stability note for MaterialShapes: 1 mentions, ok
stability note for LoadingIndicator: 1 mentions, ok
stability note for ListDetailPaneScaffold: 1 mentions, ok
stability note for NavigableListDetailPaneScaffold: 1 mentions, ok
stability note for ListDetailSceneStrategy: 2 mentions, ok
stability note for currentWindowAdaptiveInfo`: 1 mentions, ok
material3 1.5.0-rc01 pin stated in API stability: True

0 gaps
```

**Contrast:** `checks/spec_contrast.py` (Top Hat, 30 pairs, light and dark) → `0 failures`; `checks/contrast.py` (all three directions) → `0 failures`. The full table is in the spec under [Contrast](../../design/3.0-spec.md#contrast).

**Brand checkpoint order:** ebaad3d (the owner's picks) comes before d488434 (the first phone artboard).

**Outcome check on the live canvas** (Chrome, 2026-11-12; screenshots in [`screens/`](screens/)):
- All artboards render.
- Draw shows the chip states, the tonal mixed box, the counter, the sort labels, the put-back menu and the stepper, in light and dark ([draw-phone-grid.jpg](screens/draw-phone-grid.jpg), [draw-phone-a-z-light.png](screens/draw-phone-a-z-light.png)).
- The large-screen list-detail is correct ([large-draw.jpg](screens/large-draw.jpg)).
- The reveal end layout is correct ([reveal-phone.jpg](screens/reveal-phone.jpg)).
- The check **found one mismatch**: the "Everyone drawn" box showed the mixed dash, against the review's D18 refinement ([draw-phone-states-light.png](screens/draw-phone-states-light.png) was captured *before* the fix). `DrawScreen` was fixed in 6c96785 and re-checked.
- **Not verified:** the reveal animation's timing. Screenshots can't show motion, and two frames a second apart were identical. Play the `RevealPhone` artboard to see it.

## Follow-ups
- **Post the ticket comments** in [ticket-comments.md](ticket-comments.md) to NAM-6/7/8/9/10/12 after merge, so the `main` links resolve. This happens at wind-down.
- **Owner approval comment on NAM-2** (NAM-2 AC4). The owner posts it.
- **Hand-offs carried in the spec's ticket index:**
  - NAM-7: pin material3 1.5.0-rc01 (or 1.5.0 final); accept the adaptive opt-in.
  - NAM-8: add a drawn count to `GroupSummary`.
  - NAM-9: normalise inner line breaks before validating.
  - NAM-10: retire `GridLayout`/`GridColumns`; `MIN_NAMES_TO_DRAW = 1`; `NameSortType.next()` DESC → NONE, with its tests and KDoc.
  - NAM-12: remaining-pool `DrawState` and `drawFrom`; per-name un-draw on `NameRepository` and `RoomNameRepository`; select-all over not-drawn names.
  - NAM-6: don't migrate `gridColumns`.
- **Optional:** reveal end-state and reduced-motion artboards in landscape and large, if the owner wants them drawn rather than implied by the component.
- **Optional:** NAM-7 decides whether to bundle DM Serif Display and measures its APK cost.
