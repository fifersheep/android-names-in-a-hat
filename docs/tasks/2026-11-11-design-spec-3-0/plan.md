# Plan — Produce the 3.0 design spec

## Intent
Produce the design spec that NAM-7 (shell), NAM-8/9/10 (screens) and NAM-12 (draw from remaining) build against. It settles every row of the NAM-2 table and every per-ticket "per the design spec" decision found in research. The output is a repo spec and a Design canvas of mock-ups. The approach is decide first, then draw: the product decisions below are proposed here and confirmed at gate 1, so the mock-ups don't encode guesses.

## Acceptance criteria
- [ ] **Coverage.** `docs/design/3.0-spec.md` opens with a coverage table that maps each of the 9 NAM-2 table rows to an anchor. Check: the anchor script reports 9/9 rows mapped and 0 unresolved anchors.
- [ ] **Navigation.** The spec lists the destinations with typed arguments and back-stack rules: `Lists`, `Draw(listId)`, `EditList(listId: Long?)`. It includes the create → `EditList(id)` replacement (D3) and the back/active-list rules (D4).
- [ ] **Ticket index.** The spec has a per-ticket index (NAM-5/6/7/8/9/10/12). Every item listed under research.md "What downstream tickets expect" appears there with an anchor to its answer. Check: the script counts the research items against the index rows, and they must match exactly.
- [ ] **Copy deck.** The copy deck is the only place user-facing strings are defined; other sections reference its keys. Every 2.7.0 string (`origin/master:app/src/main/res/values/strings.xml`, plus the hard-coded "Edit: ", "Names: ", "… deleted", "n out of t names selected", "Select All"/"Clear All") has a successor key or a "retired" row. Check: a script diffs the 2.7.0 string list against the deck, and 0 may be unaccounted.
- [ ] **Mock-ups.** A Claude Design canvas is published and linked from the spec and from NAM-2. For each of Lists, EditList, Draw and Reveal it has a phone portrait, a phone landscape and a large-screen artboard, each in light **and** dark. It also has these states:
  - Lists: empty, About sheet.
  - EditList: create, rename name inline, blank error, undo snackbar.
  - Draw: nothing in the hat, mixed chip states (in / out / drawn-selected / drawn-unselected), put-back menu, all drawn, empty list, each sort order.
  - Large screen: no list open.
  - One Draw artboard under a sample dynamic-colour scheme.
  Check: the artboard checklist in the spec is ticked against the canvas.
- [ ] **Brand checkpoint.** The canvas has a brand board with 2–3 Northtrack directions. The owner's pick, and their D18 variant pick, are committed to the workflow.md decision log in a commit that comes before the first `docs/design/3.0-mockups/phone-*.html` commit. Check: `git log --oneline`.
- [ ] **Reveal.** The reveal is animated on the canvas, with a reduced-motion variant alongside and the D10 edge cases (remaining < n, remaining = 0).
- [ ] **Contrast.** The fallback palette passes WCAG AA: 4.5:1 for body text, 3:1 for large text and UI. This covers each on-/container pair used, **including drawn-chip text on its chip**, in light and dark. Check: contrast script output is pasted into the spec, with 0 failures.
- [ ] **API stability.** The spec names only APIs that are stable in material3 1.5 / Navigation 3 1.2 / adaptive 1.3, or flags them. Check: grep the spec for `MaterialShapes|LoadingIndicator|ListDetailPaneScaffold|NavigableListDetailPaneScaffold|ListDetailSceneStrategy|currentWindowAdaptiveInfo\b`. Each hit sits next to a stability note, and the section "API stability" lists the material3 1.5.0-rc01 pin.
- [ ] **Docs only.** Baseline verify still passes, because the change touches docs only.

## Approach
Write the decisions into a markdown spec with stable anchors. Draw the mock-ups as live HTML artboards on one Design canvas. One shared token/component stylesheet means light and dark are a class swap and the brand pick is a token swap. The brand board comes first, as a checkpoint, because the palette colours every later artboard. Ticket comments wait until after the owner approves the spec and it is merged, so links resolve and don't go stale.
- Alt: a clickable HTML prototype. Rejected at research: harder to review every variant side by side.
- Alt: Compose `@Preview` mock-ups. Rejected: that's building screens, which belongs to NAM-7..10.

### Proposed design decisions (to confirm at gate 1)
| # | Decision | Proposal | Why / alternative |
|---|---|---|---|
| D1 | User-facing noun for a group | **"List"**: "Your lists", "New list", "List name", "Delete list" | "Draw" collides with the Draw action. Alt: "Hat" (on-brand, but unclear for raffles) |
| D2 | "In the hat" and the counter | **In the hat = selected and not drawn**: the pool the next draw uses. Counter: **"8 of 30 in the hat"**, plus **"· 4 drawn"** once any are drawn. The stepper's max equals the "in the hat" number | One meaning, used by counter, stepper and "Put back". It replaces NAM-10's "n out of t names selected" (GitHub #7 is still met). NAM-12 adds the remaining count; `DrawState.selectedCount` counts drawn names too (`DrawState.kt:28`) |
| D3 | Navigation model | **3 destinations**: `Lists` (home), `Draw(listId)`, `EditList(listId: Long?)` (null = create). Create and add-names **merge**: the list is created when its name is first committed, and the destination is **replaced** with `EditList(id)` so restoring after process death can't create it twice. **Done** on a new list replaces EditList with `Draw(id)`; on an existing list, Done goes back. Reveal and About are in-screen layers | 2.7.0 had 4 Activities. `GroupEditor.create` always inserts (`GroupEditor.kt:13-16`). NAM-8 owns list create/rename inside EditList's header; NAM-9 owns the names part. One screen, two tickets, split by section |
| D4 | Launch and the active list | Reopen the last list (NAM-7 default). Back from a launched-into Draw goes to Lists. The active list is **set** on opening Draw and **cleared** only by an explicit back-out to Lists (phone) or by deleting it. In list-detail, picking another list replaces it, and the Lists pane being visible doesn't clear it. A deleted id routes to Lists | `ActiveGroup.kt:6`; NAM-7 rabbit hole |
| D5 | Names on the Draw screen | **Wrapping filter chips** (FlowRow) instead of a 1/2-column grid. The **column toggle and grid preference are retired**: NAM-6 drops `gridColumns`, and NAM-10 removes `GridLayout`/`GridColumns` from `:shared` | Chips adapt to width with no density setting. Alt: keep a list/grid toggle |
| D6 | Draw bar | The bottom toolbar holds a connected **stepper [− n +]** and a large **"Draw n"** button. n is clamped to 1..in-the-hat. **In the hat = 0** → the stepper hides. The button becomes **"Reset"** if anything is drawn, else it is disabled with "Tap names to put them in the hat" | Replaces the spinner |
| D7 | Minimum to draw | **1** (`MIN_NAMES_TO_DRAW` → 1, in NAM-12/10, not here) | The last name left is a legitimate draw |
| D8 | Draw from remaining | **Always on**. **Reset** sits in the Draw top bar, enabled once anything is drawn, with an **undo snackbar** | NAM-12 default. Alt: a per-list switch |
| D9 | Chip states | Four visuals for (selected, drawn):<br>• **In**: tonal fill + check.<br>• **Out**: outlined.<br>• **Drawn** (selected or not, same visual): strikethrough + "drawn" icon, text still ≥4.5:1, TalkBack "drawn".<br>Tapping In/Out toggles selection. Tapping Drawn opens **"Put back in the hat"**, which clears drawn **and** sets selected, so the copy holds. Select all / clear all change only selection (drawn chips keep the drawn visual). Reset clears drawn, and chips then show their real selection | Name.kt:6-7 keeps the flags independent; this keeps the UI truthful. **Needs a per-name un-draw** in `NameRepository` (NAM-5 implements it, NAM-12 uses it) |
| D10 | Reveal | **Full-window takeover**: the hat shakes (≤600 ms), then name cards pop out with a staggered expressive spring (≤1.5 s total, compressing for many names). It ends on large type with **"Done"** and **"Draw min(n, left) more"**. That button hides when 0 are left, and "Everyone's been drawn" shows instead. Predictive back dismisses. The result is held in saved state | Classroom projection. "Draw more" is for raffles |
| D11 | Reduced motion | `ANIMATOR_DURATION_SCALE == 0` → no shake or stagger; the result appears with one crossfade | No Compose API exists (research) |
| D12 | Deletes | List and name delete: **undo snackbar**, implemented as a **delayed delete**: the item is hidden, and committed when the snackbar times out or is dismissed. No re-insert | Re-insert loses names, sort order, drawn state and position (`GroupRepository.kt:23-24`, `NameRepository.kt:14-18`) |
| D13 | Rename a name | **Yes**: tap a name in EditList to edit it inline | `NameEditor.rename` exists |
| D14 | Duplicate names | Allowed, no warning | NAM-9: two pupils called Sam |
| D15 | Validation | Inline field error, and the field keeps focus: **"Enter a list name"** / **"Enter a name"**. A blank keyboard-action shows the error only | Replaces the tickets' "… cannot be blank" strings |
| D16 | Layout by window | **List-detail** when width ≥ 840 dp (Expanded) **and** height ≥ 480 dp (Medium); Lists pane ~360 dp; detail = Draw or EditList; empty detail = "Pick a list". **Compact height** (phone landscape): single pane, and the Draw bar becomes a trailing vertical toolbar. Otherwise single pane, with wider margins from 600 dp | Phones in landscape are >840 wide but <480 tall (critic). WindowSizeClass V2 breakpoints |
| D17 | Sort | The Draw top bar has a **sort button** that cycles **Unsorted → A–Z → Z–A → Unsorted**. The label reads **"Unsorted"**, "A–Z" or "Z–A", and it is remembered per list. Display-only: insertion order is the stored order, and `sortNames` sorts in the UI. **Hand-off to NAM-10:** `NameSortType.next()` changes DESC → NONE (was DESC → ASC), with its test (`NameSortType.kt:13-17`, `NameSortTypeTest.kt:17`) | Survives from 2.7.0, and adds the way back to unsorted (owner, 2026-11-12) |
| D18 | Select all / clear all | A **header row above the chips** carries the counter (D2) and the select-all control. It stays visible (sticky) in portrait and landscape. **The control is chosen from two variants drawn side by side on the brand board (task 1):**<br>(a) **Tri-state checkbox**: the counter is its label. ☑ all / ▣ some / ☐ none; tapping when all are selected clears all, otherwise it selects all.<br>(b) **Text button**: the counter at the start, "Select all"/"Clear all" at the end.<br>Either way the logic is `DrawState.allSelected` | Survives from 2.7.0. Keeps the counter visible (GitHub #7). The owner wants to compare (2026-11-12) |
| D19 | Settings and branding | The dead "Settings" item becomes an overflow **"About"** bottom sheet: version, "Made by Northtrack Studio", privacy policy link | A place for Northtrack without a settings screen |
| D20 | Theme | M3 + **dynamic colour on 12+** (always). Northtrack fallback palette below 12, light and dark following the system. The brand direction is picked from the board (task 1) | NAM-2 table |

## Assumptions & open questions
- [answered 2026-11-11] Mode full; worktree isolation — workflow.md decision log.
- [answered 2026-11-11] Format: repo markdown spec + Claude Design canvas — decision log.
- [answered 2026-11-11] Brand: fresh Northtrack look, 2–3 directions for the owner to pick — decision log.
- [open — owner, at gate 1] D1–D20 are proposals. Approving the plan approves them; any can be revised at the gate.
- [answered 2026-11-12] D17: the sort cycles back to "Unsorted" (cycle, not a menu) — decision log.
- [open — owner, mid-implementation] Which brand direction, and D18 variant (a) or (b), both picked at the task 1 checkpoint. Logged and committed before the screen artboards.
- [open — owner, at gate 2] Spec approval as a comment on NAM-2 (AC4 of NAM-2). The owner posts it; I don't post on their behalf.
- [assumed] Expressive components are designed for, although stable material3 is 1.4.0. NAM-7 pins `material3:1.5.0-rc01` (or 1.5.0 final) outside the BOM — research.md "Compose API stability".
- [assumed] The reveal uses no `MaterialShapes`/`LoadingIndicator` (still experimental). The hat is a hand-drawn vector animation — research.md.
- [assumed] Typeface: system default for UI. A direction may propose one bundled display face for headings and the reveal, with its size cost noted.
- [assumed] Icon: the spec supplies an icon **concept** per direction (SVG, adaptive + monochrome layers). NAM-7 makes the resources, and store art stays in NAM-13.
- [assumed] Canvas artboard sources are also committed under `docs/design/3.0-mockups/`. Adjusted if the Design type's instructions (quickstart, task 1) say otherwise.
- [assumed] Code is untouched here: `MIN_NAMES_TO_DRAW`, the grid preference and the per-name un-draw method are hand-offs to NAM-12/10/6/5, recorded in the ticket index — NAM-2 Out of scope.
- [assumed] Ticket comments (NAM-5/6/7/8/9/10/12) are posted after gate 2 approval and the merge, during wind-down, so `main` links resolve and reflect the approved spec.

## Tasks
1. **Brand board.** Quickstart the Design type and create the canvas. Publish a shared `tokens.css`/`components.css` and a brand board with 3 directions. Each direction has a seed colour, light/dark tonal roles, a type pairing, an icon concept and one hero Draw screen. Add a **D18 comparison**: the same Draw header with variant (a) tri-state checkbox and variant (b) text button side by side, each showing all / some / none, in light and dark. The owner picks a direction and a D18 variant; log both and commit. Files: `docs/design/3.0-mockups/{tokens.css,components.css,brand-board.html}`; canvas. Verify: the canvas opens, and the contrast script passes for every direction, including the drawn-chip pair.
2. **Spec draft** — `docs/design/3.0-spec.md`. Sections:
   - coverage table, §Decisions (D1–D20), vocabulary
   - navigation and back stack, launch and active list
   - one section per screen (portrait and landscape), large screens
   - draw moment, draw from remaining (chip state table), empty/error/validation states
   - theme (roles + contrast output), motion + reduced motion, accessibility
   - API stability, ticket index, artboard checklist, copy deck

   Verify: the anchor, ticket-index and copy-deck scripts (one-off Python in the scratchpad) report 0 gaps.
3. **Phone portrait artboards**, light and dark: all screens plus the Lists, EditList and Draw states in AC5, and the dynamic-colour sample. Files: `docs/design/3.0-mockups/phone-*.html`. Verify: tick the artboard checklist.
4. **Phone landscape and large-screen artboards**, light and dark: landscape for Lists, EditList and Draw. Large-screen list-detail with Draw, with EditList, with no lists, and with no list open. Files: `docs/design/3.0-mockups/{landscape,large}-*.html`. Verify: checklist.
5. **Reveal artboards**: animated (1 and 3 names), the reduced-motion variant, end states (more left / remaining < n / none left), in phone portrait, phone landscape and large, light and dark. Files: `docs/design/3.0-mockups/reveal-*.html`. Verify: plays on the canvas, and the reduced-motion variant has only one crossfade.
6. **Link and freeze.** Put the canvas URL in the spec and attach it to NAM-2 as a link. Freeze the anchors. Draft the ticket comments (NAM-5/6/7/8/9/10/12, each with section links and the decisions that change its text: D1, D2, D3, D5, D7, D9, D12, D15, D17, D18) into `docs/tasks/2026-11-11-design-spec-3-0/ticket-comments.md`, to post at wind-down. Files: the spec and that file. Verify: the anchor script passes, and `get_issue NAM-2` shows the link.

## Out of scope
- Any Kotlin or resource change: theme code, constants, repository methods and icon resources belong to NAM-5/6/7/10/12.
- Store listing art and copy (NAM-13). Draw history, teams, sharing, bulk import, a settings screen and per-list colours (NAM-2/8/9/10 Out of scope).
- A separate Design System artifact. The tokens live in the spec and the canvas stylesheet.

## Verification
- Machine:
  - `./gradlew --no-watch-fs :app:assembleDebug :shared:allTests` stays green (210 tests).
  - The anchor, ticket-index, copy-deck and contrast scripts (scratchpad) report 0 gaps or failures, and their output is recorded in the spec.
- Outcome:
  - Open the Design canvas and walk the artboard checklist: every screen × phone portrait / landscape / large × light / dark, plus the states.
  - Play the reveal and its reduced-motion variant.
  - After merge, follow each ticket comment's link to the right spec heading.
