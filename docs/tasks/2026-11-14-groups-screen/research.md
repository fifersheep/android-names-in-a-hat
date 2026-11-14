# NAM-8 research — Lists screen (groups) + Edit list's list-name header

Baseline: main@ff34628, verify green (255 tests). All paths are repo-relative. `app/…/` = `app/src/main/kotlin/uk/lobsterdoodle/namepicker/`, `shared/…/` = `shared/src/commonMain/kotlin/uk/lobsterdoodle/namepicker/shared/`.

## Findings

### The spec answers the ticket's open items
- **Scope** (`docs/tasks/2026-11-11-design-spec-3-0/ticket-comments.md:19-28`): the [Lists](../../design/3.0-spec.md#screen-lists) screen **plus the list-name header of Edit list**. NAM-9 owns the Edit list names section (`3.0-spec.md:87`).
- **Ticket index** (`3.0-spec.md:507-510,519`):
  - NAM-8.1 create lands on `EditList`, created on the first name commit (rules 1–2, `:79-80`).
  - NAM-8.2 delete uses an undo snackbar as a **delayed delete**, with no re-insert (`:309-318`).
  - NAM-8.3 empty state: a hat illustration, "No lists yet", a body line and the FAB (`:297`).
  - NAM-8.4 validation says "Enter a list name", inline, and focus stays (`:301`, D15).
  - NAM-8.H1 hand-off: add a **drawn count** to `GroupSummary` and the groups query, for "24 names · 4 drawn" (`:109`).
- **Lists, portrait** (`3.0-spec.md:102-117`):
  - Top bar: large "Your lists" in the display face, with an overflow holding **About**.
  - Cards are newest first. Each has a titleLarge title, a plurals supporting line and a 48 dp trailing overflow ("Options for %s") with Edit list and Delete list.
  - An extended FAB "New list" with a plus icon.
  - Landscape (compact height): a small app bar, and cards in a single column, max 640 dp wide and centred.
- **Edit list header** (`3.0-spec.md:125-138`):
  - Top bar: Up, "New list" or "Edit list", and **Done**, enabled once the list exists.
  - An outlined "List name" field, placeholder "e.g. Year 5 Maple", headlineSmall, IME **Next**.
  - **Commit** is Next, or focus moving to another field with non-blank text. It creates or renames the list.
  - Back or Up on a new, uncommitted list discards everything. Focus lost because of back never commits.
  - Back on an existing list commits a non-blank change; a blank one is discarded and the old name kept.
- **Large screens** (`3.0-spec.md:281-288`):
  - The selected card gets a `secondaryContainer` fill.
  - Deleting the open list closes the detail to "Pick a list" at once and clears the active list. **Undo restores it and reopens it in the detail.**
  - No lists at all: empty state plus the placeholder.
- **Motion** (`3.0-spec.md:441-446`): card title → Draw title shared bounds; FAB → Edit list shared bounds; delete collapses with `animateItem`.
- **Copy deck** (`3.0-spec.md:558-583`): every Lists and Edit-header key: `lists_*`, `list_*`, `action_undo`, `about*`, `edit_*` for the header, and `action_more`.
- **Mock-up** (`docs/design/3.0-mockups/project/ListsScreen.dc.html:41-58`): the empty-state hat is a 108×108 SVG (3 sparkles plus hat/band/brim paths), coloured `tertiary`/`primary`. It ports directly to a vector drawable with themed tints. The About sheet markup is at `:66-84`.

### What NAM-7 left to build on
- **Destinations and back stack.** `app/…/navigation/AppKey.kt:10-31`, `BackStack.kt:17-64`.
  - `openList`, `newList`, `editList`, `listCreated(id, session)`, `doneEditing`, `back(singlePane)` and `listGone(id)` already implement rules 1–5.
  - The entries are wired in `AppNavigation.kt:90-123`.
- **Placeholders to replace.** `app/…/ui/placeholder/Placeholders.kt:51-156` and `PlaceholderViewModels.kt:28-128`.
  - `ListsViewModel.delete` is immediate. It is marked "NAM-8 replaces it with the delayed delete (D12)" (`:53-56`).
  - `EditListViewModel` creates once, guarded by `creating` (`:108-122`), but has no rename and no validation-error state.
  - The `ListStatus` Gone handling and Draw's `onShown` active-list rule must stay (NAM-7 report follow-ups).
- **Saved state.** `rememberSaveableStateHolderNavEntryDecorator` plus the per-visit `EditList.session` content key keep typed text and the ViewModel across the `EditList(null)`→`EditList(id)` swap, and across process death. NAM-7 checked this on device (NAM-7 report AC7). That covers AC7's mechanism; NAM-8 must keep the text in `rememberSaveable`/`SavedStateHandle`.
- **Shared transitions.** `LocalSharedTransitionScope` is provided (`AppNavigation.kt:34-35,77-78`); no morphs yet.
- **Up is hidden in list-detail** (`Placeholders.kt:170-178`).
- **Strings.** The copy-deck keys from NAM-7 are in `res/values/strings.xml:5-21`, with 2.7.0 leftovers below. `lists_item_names` plurals already exist. `placeholder_create` goes when the header commits on Next.
- **Icons.** Only `ic_arrow_back` and the launcher drawables are in `res/drawable`. No material-icons dependency is in `app/build.gradle.kts`, so plus, more-vert and the hat are new vector drawables.
- **Tests.** JVM ViewModel tests use `FakeGroups` and `FakePreferences` (`app/src/test/…/FakeGroups.kt`) and `UnconfinedTestDispatcher`. Existing: `EditListViewModelTest`, `DrawViewModelTest`, `ShellViewModelTest`, `BackStackTest`. There are no Compose UI tests and no androidTest source set.

### Shared domain
- **`GroupEditor`** (`shared/…/usecase/GroupEditor.kt:13-34`):
  - `create` and `rename` call `validateLabel`, which trims and rejects blanks, and return `WriteResult.Blank`. This satisfies AC3, `"  Team  "`→`"Team"`, without UI logic.
  - `delete` removes the group (names by CASCADE), `removeSortType` and `clearActiveGroupIf`. That covers the "remove stored sort order + clear active" note.
- **`GroupRepository.observeGroups()`** is a live `Flow`, newest first (`GroupDao.kt:12-17`, `ORDER BY g.id DESC`).
  - `GroupSummary(group, nameCount)` is at `shared/…/model/Group.kt:7`, and `GroupSummaryRow` at `GroupEntity.kt:17-21`.
  - `names.drawn` is `INTEGER NOT NULL DEFAULT 0`, set to 0 by the migration (`Migrations.kt:58,69`). A `SUM(n.drawn)` with a 0 default (the LEFT JOIN gives NULL for empty groups) is safe, with no legacy text-boolean quirk.
- **Drawn-count ripple.** Adding `drawnCount` with a default of 0 keeps these compiling unchanged:
  - `RoomRepositoryTest.kt:53,80,208,213`
  - `MigrationTest.kt:97`
  - shared `FakeGroupRepository.kt:16-17`
  - app `FakeGroups.kt`

  Add a Room test for the drawn count.

## Constraints & prior art
- **Inherited device checks.** User decisions moved two install-over checks to NAM-8:
  - NAM-5 AC7: 2.7.0 data migrates, and a new group doesn't reuse a deleted id (`room-migration-to-v3/report.md:71`).
  - NAM-6 AC6: a Z–A sort and the active group carry over (`datastore-preferences-migration/report.md:67-70`).

  AC1 here is the same check from the UI side.
- **How to run them.** `api36_niah` holds release-signed 2.7.0 (vc 2000000000) with data and runs `-no-snapshot-save`, so changes are discarded on `emu kill` (memory: device verification).
  - Debug builds can't install over it (signature).
  - Local release signing is configured: `gradle.properties:12` plus 3 secrets in `~/.gradle/gradle.properties`, keystore at `../android-keystore`. `versionCode = 2000000002` > 2000000000.
  - So a local `assembleRelease` can be installed over it. Never `pm clear` there.
- **Delayed delete** is ViewModel-owned (`3.0-spec.md:318`), and "the real delete commits on snackbar timeout or dismiss, or when the screen leaves composition" (`:311`).
  - Pitfall: composition also leaves on rotation, so a naive `DisposableEffect` commit would lose Undo on every config change.
  - Pitfall: `viewModelScope` is cancelled in `onCleared`, so a commit there needs a scope that outlives the ViewModel.
- **Large-screen delete must drive navigation before the row is gone.** The Draw/EditList placeholders only drop their entry when their list's status becomes `Gone` (`Placeholders.kt:89,121`). Under a delayed delete the row still exists, so the Lists screen must call `listGone`-like navigation itself on hide, then `openList(id)` on Undo.
- **Parallel NAM-9.** It runs in a worktree at `../android-names-in-a-hat-nam-9` (`flow/edit-names-screen`, phase research, same base ff34628). Its next action explicitly says "settle the scope seam with NAM-8".
  - Overlap: `EditListPlaceholder`/`EditListViewModel`, `strings.xml`, `FakeGroups`, and the delayed-delete pattern, which both ViewModels own.
  - Whichever merges second rebases.
- **Build gotchas** (memory): `--no-watch-fs` and XML counts; no commas in KMP test names; `:app` tests need `kotlin-test-junit` (already present); `:app:clean` after deleting resource directories.

## Open questions (triaged)

| # | Question | Class | Disposition |
|---|---|---|---|
| 1 | Seam with NAM-9 on the Edit list screen: who builds the scaffold and header, who the names section; which merges first | shape-changing | **Answered 2026-11-14:** NAM-8 builds the whole Edit list screen first, with a stub names area; NAM-9 waits for NAM-8's merge |
| 2 | The About sheet (D19) sits on the Lists overflow, but no ticket owns it | shape-changing | **Answered 2026-11-14:** build it in NAM-8 |
| 3 | Shared-element morphs while Draw is still a placeholder | conventional default | Record: NAM-8 does FAB→Edit list and tags the card title with a shared key; the Draw-side title morph is NAM-10's |
| 4 | Delayed-delete commit triggers vs rotation and `onCleared` | code-resolvable/design | Plan: pending state in the ViewModel (in `SavedStateHandle`); the snackbar re-shows after rotation; commit on snackbar timeout or dismiss, before navigating away, and in `onCleared` on an app-lifetime scope. Process death while pending = not deleted (safe direction) |
| 5 | Undo on large screens when the open list is hidden | code-resolvable | Plan: on hide, if the id is on the back stack, call `listGone(id)` and clear the active list; on Undo, `openList(id)` |
| 6 | Install-over method | code-resolvable | Release-signed local build over 2.7.0 on `api36_niah` (`-no-snapshot-save`); never `pm clear` |
| 7 | Hat illustration asset | conventional default | Port the mock-up SVG to a vector drawable, themed `primary`/`tertiary` |
| 8 | Back on an existing list commits a non-blank rename | code-resolvable | The Edit list entry intercepts back/Up to commit, then calls the shell's back |
