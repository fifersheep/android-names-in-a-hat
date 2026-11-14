# NAM-9 device evidence (2026-11-14)

Debug build at 7278cc1:
- **Phone:** `niah_debug_api36` (Pixel 6, 1080×2400, 420 dpi).
- **Tablet:** `niah_tablet_api36` (pixel_c, list-detail).

The install-over used a local release build, signed with the same certificate as 2.7.0 (SHA-256 `4fa5cd07…`):
- It went onto `api36_niah`, which runs 2.7.0 (vc 2000000000), with `install -r` only.
- The emulator ran with `-no-snapshot-save` and was stopped with `emu kill`, so the 2.7.0 data is unchanged.
- I read that database with `adb root` and then ran `adb unroot`. On the debug devices I read with `run-as … sqlite3`.

| AC | Device | Result | Files |
|---|---|---|---|
| 1 Order | api36_niah | After install-over, Team Red (2.7.0) opens on Draw(1) → Edit list: **Dana, Eli**. Adding "Aaron" puts it **last**: `3\|1\|Aaron\|1\|0`. | `installover-*.png` |
| 1 Order | phone | "  Ada  ", "Ben", "Aaron" added in that order show in that order, with Aaron last (not A–Z). Database: `6 Ada`, `7 Ben`, `8 Aaron`. | `phone-add-blank-error.png` |
| 2 Add | phone | IME Send adds; the field clears; focus and the keyboard stay (`mInputShown=true`). Database: `selected=1` for each. | `phone-add-blank-error.png` |
| 3 Validation | phone | Blank Send ("   ") shows "Enter a name" (error colour), keeps the typed text, and leaves the database unchanged. "  Ada  " is stored as `[Ada]`. Inner line breaks and tabs: adb can't set the clipboard on this image ("No shell command implementation"), so this is covered by `LabelsTest`/`NameEditorTest` (all 3 targets) and `EditNamesViewModelTest`. | `phone-add-blank-error.png` |
| 4 Delete | phone | Delete hides Ada and shows "Ada deleted · Undo"; the database is unchanged. Undo brings back the same id (6). Delete → rotate: still hidden, the snackbar shows again, and Undo works. Delete → Home: committed (gone from the database). Snackbar timeout: committed. | `phone-delete-snackbar.png`, `phone-delete-rotated.png` |
| 4 Delete (list pending) | tablet | One script, inside the undo window: delete Cy (pending), open a rename draft "Benji", then delete the list from the Lists pane. Database straight after: Ada, Ben, Cy unchanged (no delete, no rename). Undo of the list: Ada, Ben, Cy unchanged; Alpha reopens in the detail pane. | `tablet-list-deleted-during-edit.png`, `tablet-after-list-undo.png` |
| 5 Selected | phone, api36_niah | Every name added in this run is stored with `selected=1`. | — |
| 7 Long list | phone | 51 seeded names + "Zed" with the keyboard up. Zed row `[53,1146][922,1272]` is fully visible below the header and above the add bar (`[42,1317]…`). The add bar sits on top of the keyboard. "52 names". | `phone-50-added.png` |
| 8 Saved state | phone | Add text "Half" and rename draft "Zedek" survive rotation. Home → `am kill` (`pidof` empty) → relaunch: same Edit list, "Half" in the add field, the rename row still open with "Zedek"; the database still has `Zed`. The extra "To To" in the restored draft was typed by my scroll swipes landing on the keyboard, not by the app. | `phone-restored-after-death.png` |
| Rename | phone | Tap → field with ✓/✕ and the text prefilled; IME Done saves `Benji`; blank Done shows "Enter a name" and keeps the name in the database; ✕ cancels. | `phone-rename-open.png`, `phone-rename-blank-error.png` |
| Name the list first | phone | New list: add field disabled with "Name the list first", no header, list-name field focused. Next on "Year 5 Maple" creates it (id 8), shows "0 names", and **focus moves to the add field**. Opening an existing list leaves the keyboard down. | `phone-new-disabled.png`, `phone-new-created-focus.png` |
| 9 Spec match | phone, tablet | Checklist: rows ≥ 56 dp (126 px at 420 dpi = 48 dp touch rows inside 150 px ≈ 57 dp rows) with `outlineVariant` dividers; 48 dp delete/✓/✕/Add targets (126 px); filled primary circular Add; full-width `surfaceContainer` add bar down to the bottom edge; "N names" labelSmall header with plurals ("1 name"); snackbar above the add bar; landscape uses NAM-8's small top bar with the list name on the title row; tablet detail pane shows Edit list (back returns to Draw); light and dark. | `phone-*`, `tablet-edit-light.png`, `tablet-edit-dark.png`, `phone-dark-*.png` |

## Notes from driving the emulator
- **Timing-sensitive steps need one script.** The 10 s Long snackbar is shorter than the gap between my tool calls. An earlier tablet attempt let Cy's delete, and then a list Undo, time out. Those commits were correct behaviour, but they wiped that test data. The table row above is the single-script rerun.
- **Key events move focus.** After a theme change, a stray `KEYCODE_ENTER` hit the keyboard-focused Up button and went back to Lists. Use taps for navigation.
- **Gboard on api36_niah** shows its "Try out your stylus" tutorial on first focus, which swallows `input text`. Cancel it first (already in memory).
- **Not checked by me:** the TalkBack walk (open, owner). Labels from the accessibility tree: "Delete Ada", "Save name", "Cancel rename", "Add", "Ada deleted", "Undo".
