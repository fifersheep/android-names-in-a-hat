---
task: groups-screen
title: Build the groups screen — list, create, rename, delete
ticket: NAM-8 — Build the groups screen: list, create, rename, delete (https://linear.app/northtrack/issue/NAM-8/build-the-groups-screen-list-create-rename-delete)
ticket-id: NAM-8
tracker-synced: In Engineering
mode: full
phase: implementation
branch: flow/groups-screen
worktree:
created: 2026-11-14
updated: 2026-11-14
---

## Next action
Gate 1 passed. Implement plan.md task 1 (drawn count in :shared + .flow.md verify).

## Decision log
- 2026-11-14 mode: full (user confirmed) — new screen spanning ViewModel, UI and data hooks; open design questions (delete undo vs confirm, create landing, list-detail).
- 2026-11-14 Q: NAM-9 runs in parallel; how to split the Edit list screen? A: NAM-8 builds the whole Edit list screen first (scaffold, top bar, Done, back/Up commit rules, list-name field + ViewModel) with a stub names area; NAM-9 waits for NAM-8 to merge, then builds the names section on top.
- 2026-11-14 Q: the About sheet (D19) on the Lists overflow has no owning ticket — build it in NAM-8? A: yes, in NAM-8.
- 2026-11-14 gate 1: plan approved by user

## Progress
- [ ] 1. Drawn count in :shared + verify config
- [ ] 2. App scope, PendingDeletes, shared UI pieces
- [ ] 3. ListsViewModel + tests
- [ ] 4. EditListViewModel + tests
- [ ] 5. Strings + drawables
- [ ] 6. Lists screen + About sheet
- [ ] 7. Edit list screen
- [ ] 8. Wire into the shell
- [ ] 9. Device verification + evidence

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
