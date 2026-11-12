# NAM-2 ticket comments — post at wind-down, after merge

These comments are posted to Linear once the spec has been approved (gate 2) and merged, so the `main` links resolve. Each one is posted verbatim. `S` = https://github.com/fifersheep/android-names-in-a-hat/blob/main/docs/design/3.0-spec.md
Canvas: https://claude.ai/artifact/V3SwNVHPd4UNUtz6YDk15q (private until it's shared from its Share menu).

---

## NAM-7 — app shell

The design spec for 3.0 is merged: [3.0-spec.md](S).

- **Destinations:** three, `Lists`, `Draw(listId)` and `EditList(listId: Long?)`. The reveal and About are in-screen layers. See [Navigation](S#navigation), including the back-stack rules: `EditList(null)` is replaced by `EditList(id)` once the list is created, and launching into Draw seeds `[Lists, Draw]`.
- **Launch and active list:** [Launch](S#launch). The active list is cleared only by an explicit back-out on a phone, or by a delete.
- **Large screens:** list-detail when the window is at least 840 dp wide **and** 480 dp tall. See [Large screens](S#large-screens). Phones in landscape stay single-pane.
- **Theme:** Top Hat. Dynamic colour on 12+; the fallback scheme and contrast are in [Theme](S#theme) and [Palette](S#palette). Type is DM Serif Display (bundled) for display and headlines; see [Type](S#type). The icon concept is in [Icon](S#icon).
- **Motion:** `MotionScheme.expressive()`. See [Motion](S#motion) and [Reduced motion](S#reduced-motion).
- ⚠ **Changes your ticket:** pin `material3:1.5.0-rc01` (or 1.5.0 final) outside the BOM. Stable material3 is 1.4.0, and the spec uses Expressive APIs that are stable only in 1.5. `ListDetailSceneStrategy` needs `@ExperimentalMaterial3AdaptiveApi`. See [API stability](S#api-stability).

## NAM-8 — groups screen

Spec: [Lists](S#screen-lists) and the list-name header of [Edit list](S#screen-edit-list).

- ⚠ **Vocabulary:** a group is a **list** in the UI ("Your lists", "New list", "Delete list"). The validation message is **"Enter a list name"**, not "List names cannot be blank". See [Vocabulary](S#vocabulary) and [States](S#states).
- ⚠ **Create and add names are one screen:** `EditList(null)`. The list is created when its name is first committed, and Done goes to Draw. NAM-8 owns the list-name header; NAM-9 owns the names section. See [Navigation](S#navigation).
- **Delete:** an undo snackbar, implemented as a **delayed delete** (no re-insert). See [Delayed delete](S#delayed-delete).
- **Empty state** and **large-screen selection:** [States](S#states), [Large screens](S#large-screens).
- **Copy:** [Copy deck](S#copy-deck).

## NAM-9 — edit names screen

Spec: [Edit list](S#screen-edit-list).

- ⚠ Merged with list creation (see NAM-8). The add field is disabled until the list exists.
- **Rename a name:** yes, inline (tap the name).
- **Delete:** an undo snackbar with a delayed delete. See [Delayed delete](S#delayed-delete).
- ⚠ **Validation:** "Enter a name" (was "Names cannot be blank"). See [States](S#states).
- **Duplicates:** allowed. Bulk paste stays out of scope. The add field is docked above the keyboard.

## NAM-10 — draw screen

Spec: [Draw](S#screen-draw), [Select-all header](S#select-header), [Chip states](S#chip-states), [Draw bar](S#draw-bar), [Reveal](S#reveal).

- ⚠ **Names are wrapping chips.** The 1/2 column toggle is retired: delete `GridLayout`/`GridColumns` and `PreferencesRepository.gridColumns` from `:shared`. AC1's column clause drops.
- ⚠ **Count control:** a connected stepper [− n +] plus **Draw n**, replacing the spinner. The edge cases are in [Draw bar](S#draw-bar).
- ⚠ **Counter:** "8 of 30 in the hat · 4 drawn", where *in the hat* means selected and not drawn. It replaces "n out of t names selected" and is the label of a **tri-state select-all checkbox**, whose "some" state is a tonal dash.
- ⚠ **Sort cycles Unsorted → A–Z → Z–A → Unsorted.** Change `NameSortType.next()` so DESC → NONE (was DESC → ASC), and update `NameSortTypeTest`.
- ⚠ **Minimum to draw = 1** (`MIN_NAMES_TO_DRAW`).
- **Reveal:** a full-window takeover with expressive motion and a reduced-motion crossfade. See [Reveal](S#reveal) and [Reduced motion](S#reduced-motion).

## NAM-12 — draw from remaining

Spec: [Draw from remaining](S#draw-from-remaining), [Chip states](S#chip-states).

- **Always on.** Reset sits in the top bar with an **undo** snackbar, and there is no confirmation.
- **Drawn names:** strikethrough, a ticket icon and the TalkBack state "drawn". They are never shown by colour alone.
- ⚠ **Put back in the hat** (undraw one name): clears drawn **and** sets selected. NAM-5 shipped `NameRepository` with only `markDrawn(ids)` and `clearDrawn(groupId)`, so this ticket adds a per-name un-draw (e.g. `clearDrawn(ids: Collection<Long>)`) to `NameRepository` and `RoomNameRepository`, with tests.
- **Counter and stepper** count *in the hat* (selected and not drawn). The minimum is 1 on the remaining pool.

## NAM-6 — DataStore

One hand-off from the 3.0 design spec: the 1/2 column toggle is retired ([Draw](S#screen-draw)). **Don't migrate the `gridColumns` preference**, and drop it from the DataStore implementation. NAM-10 removes it from `PreferencesRepository`. Sort order per list is unchanged.
