# NAM-8 device evidence (2026-11-14)

Debug build at 07b7f16 on the phone and tablet. Release-signed build (same certificate as 2.7.0, SHA-256 `4fa5cd07…`) on api36_niah. DB reads used `run-as sqlite3` (debug) or `adb root` with a pulled copy (release); nothing on api36_niah was written outside the app, and it ran `-no-snapshot-save` then `emu kill`.

| AC | Device | Result | Files |
|---|---|---|---|
| AC1 migrated data | api36_niah | 2.7.0: groups 1 "Team Red" (Dana, Eli), 2 "" (blank, 2.7.0 had no validation), 3 "Newest" deleted (the newest). After install-over: Lists shows "" · 0 names and Team Red · 2 names, newest first. A list created in 3.0 got **id 4**, not the deleted 3 (`sqlite_sequence` carried). | `installover-*.png` |
| AC1b settings | api36_niah | Launch opened `Draw(1)` (active group carried). DataStore holds `group_sort_type_for_group_1 = DESC`; `name_list.xml` is gone (migrated). | `installover-launch.png` |
| AC2 live updates | phone, tablet | Create, rename, delete reflected without leaving Lists; on the tablet the Lists pane updates beside the detail. | `phone-lists-*.png`, `tablet-list-detail*.png` |
| AC3 validation | phone | Blank + Next: "Enter a list name", focus and keyboard stay, Done disabled. `"  Team  "` stored as `'Team'`. | `phone-edit-blank-error.png`, `phone-edit-created.png` |
| AC4 delayed delete | phone | Delete hides at once, DB untouched; Undo restores with names. Timeout: snackbar gone ~12 s after delete, group and names gone from DB. Delete → Home: committed while backgrounded. Delete → rotate: still hidden, snackbar re-shown, Undo works. | `phone-delete-*.png` |
| AC4 tablet | tablet | Deleting the open list → "Pick a list to draw from", `active_group_id` cleared; Undo → reopened in detail, `active_group_id` = 2 again. Delete during an unsaved edit ("Alpha HALF") → Undo → still "Alpha". | `tablet-delete-open.png` |
| AC5 empty | phone, tablet | Hat, "No lists yet", body, FAB; tablet detail shows the hat + "Pick a list to draw from". | `phone-empty-light.png`, `tablet-empty-light.png` |
| AC6 plurals | phone | "1 name", "3 names · 1 drawn", "0 names" (names seeded with sqlite while stopped). | `phone-lists-light.png` |
| AC7 rotation / death | phone | "Team Red" typed → rotate both ways: kept. Home → `am kill` (pidof empty) → relaunch: same `EditList` entry with the unsaved text; DB still had the old name. | `phone-edit-landscape-typed.png` |
| AC8 spec match | phone, tablet | Checklist items present in light and dark: large DM Serif "Your lists", overflow → About, segmented cards with overflow, FAB, empty state, compact-height small bar with ≤ 640 dp cards, 360 dp Lists pane with selected `secondaryContainer`, Edit list top bar and field, About sheet. | all |
| AC10 Edit rules | phone | Next creates once (key swap kept the same entry); Done → Draw → back → Lists. Edit → change → back: renamed ("Green Team"). Edit → clear → back: old name kept. | — |
| AC11 a11y | phone | Labels from the accessibility tree: "Options for <list>", "More options", "Back", "New list". TalkBack focus can't be driven on the emulator, so the **TalkBack walk is left to the owner**. | — |

Found and fixed during these checks (07b7f16): cards had no visible fill; the New list FAB had no accessible label. `phone-delete-rotated.png` predates that card-fill fix; `phone-lists-landscape.png` shows landscape after it.

After code review (aebc5be, f0079ac): Edit → "Alpha Prime" → Done stored at once (`5:Alpha Prime`); on the tablet, delete the open Gamma → open Beta → Undo restores Gamma and keeps Beta in the detail (`tablet-undo-keeps-detail.png`, dark because the AVD's night mode persisted).

Gate 2 change (5964626): a blank list name (inserted with sqlite to mimic 2.7.0 data) shows as "Untitled list"; the overflow reads "Options for Untitled list", the Draw title is "Untitled list", and delete shows "Untitled list deleted · Undo" (`phone-untitled-deleted.png`, `phone-untitled-list.png`).
