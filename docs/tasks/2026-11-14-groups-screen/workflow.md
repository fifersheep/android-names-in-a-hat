---
task: groups-screen
title: Build the groups screen — list, create, rename, delete
ticket: NAM-8 — Build the groups screen: list, create, rename, delete (https://linear.app/northtrack/issue/NAM-8/build-the-groups-screen-list-create-rename-delete)
ticket-id: NAM-8
tracker-synced: In Review
mode: full
phase: reporting
branch: flow/groups-screen
worktree:
created: 2026-11-14
updated: 2026-11-14
---

## Next action
report.md written; PR #29 open (https://github.com/fifersheep/android-names-in-a-hat/pull/29); NAM-8 moved to In Review. Holding at gate 2 for merge approval.

## Decision log
- 2026-11-14 mode: full (user confirmed) — new screen spanning ViewModel, UI and data hooks; open design questions (delete undo vs confirm, create landing, list-detail).
- 2026-11-14 Q: NAM-9 runs in parallel; how to split the Edit list screen? A: NAM-8 builds the whole Edit list screen first (scaffold, top bar, Done, back/Up commit rules, list-name field + ViewModel) with a stub names area; NAM-9 waits for NAM-8 to merge, then builds the names section on top.
- 2026-11-14 Q: the About sheet (D19) on the Lists overflow has no owning ticket — build it in NAM-8? A: yes, in NAM-8.
- 2026-11-14 gate 1: plan approved by user

## Progress
- [x] 1. Drawn count in :shared + verify config (af892c3)
- [x] 2. App scope, PendingDeletes, shared UI pieces (f0db60f)
- [x] 3. ListsViewModel + tests (c71df26)
- [x] 4. EditListViewModel + tests (10bfa13)
- [x] 5. Strings + drawables (914d9a9)
- [x] 6. Lists screen + About sheet (f40122a)
- [x] 7. Edit list screen (5bb483c)
- [x] 8. Wire into the shell (c3f22f2)
- [x] 9. Device verification + evidence (07b7f16 fixes, 5624bb2 evidence)

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
- 2026-11-14 task 1: shared FakeGroupRepository unchanged — the drawnCount default (0) already passes through; plan said it would be edited.
- 2026-11-14 task 2: the app scope lives in a new di/AppModule.kt, the plan's either/or alternative to StorageModule.
- 2026-11-14 task 4: the field text and error are Compose snapshot state in the ViewModel (backed by SavedStateHandle) rather than a StateFlow, as Google recommends for TextField state; same persistence as planned.
- 2026-11-14 task 5: placeholder_create kept until task 8 (EditListPlaceholder still uses it); added ic_open_in_new for the About privacy link (not in the plan's icon list).
- 2026-11-14 task 6: no LifecycleStopEffect exists; the app-stop commit uses LifecycleStartEffect's onStopOrDispose (same trigger). Shared-bounds helper added as ui/components/SharedBounds.kt (not in the plan's file list).
- 2026-11-14 task 7: in phone landscape the name field sits in the top bar's title row (spec: same row as the title) at titleMedium size instead of headlineSmall, to fit the small bar.
- 2026-11-14 task 8: added isCompactHeight() to navigation/Layout.kt with 2 LayoutTest cases (not in the plan's file list).
- 2026-11-14 task 9: device checks found two bugs, fixed in 07b7f16: cards needed surfaceContainer fill (M3 segmented default barely shows); the New list FAB had no accessible label (material3 clears the extended FAB text semantics; the label comes from the icon).
- 2026-11-14 task 9: TalkBack focus can't be driven on the emulator (adb swipes and Alt+arrow shortcuts don't move it after 3 tries); the AC11 TalkBack walk goes to the owner as a manual check. Labels were checked through the accessibility tree instead.
- 2026-11-14 task 9: the install-over found a case outside the plan: a blank group title that 2.7.0 allowed (GitHub #6) migrates as-is and shows as an untitled card ("Options for "). Not fixed here; raised for the report and gate 2.
- 2026-11-14 task 9: 2.7.0 couldn't have names added by UI in 3.0 yet (NAM-9), so AC6 plural data was seeded with sqlite on the debug device while the app was stopped.
