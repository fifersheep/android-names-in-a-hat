# NAM-10 device evidence (2026-11-14)

Devices: `niah_debug_api36` (Pixel 6, API 36, gesture nav, debug build), `niah_tablet_api36` (1280×900 dp, list-detail, debug build), and `api36_niah` (2.7.0 data, release build, discarded with `emu kill`).
Test list "Draw Test" (id 9), inserted with sqlite3: zara, Ada, Tom, ben, Kai, Chloe, with ben and Chloe out.

| AC | Check | Result | Evidence |
|---|---|---|---|
| 1 | Install the 3.0.0 release (vc 2000000002) over 2.7.0 (vc 2000000000) on `api36_niah` | Launch opens `Draw(Team Red)`. The 2.7.0 data had Dana and Eli selected (read as root before the install) and no stored sort; 3.0 shows "2 of 2 in the hat" and Unsorted. **Not checked: Z–A from 2.7.0.** The device's 2.7.0 data has no sort key, and auto mode blocked writing one into the 2.7.0 data. NAM-6's `LegacyNameListMigrationTest` covers the key format. | `installover-draw-team-red.png` |
| 2 | Toggle Kai and zara, back to Lists, reopen | Selection unchanged ("2 of 6 in the hat"); the DB agrees | — |
| 3 | Counter | "4 of 6", "2 of 6 · 3 drawn", "0 of 6 · 5 drawn", "0 of 6 · 6 drawn", "0 of 6", "2 of 2" | `phone-dark-draw-*.png` |
| 4 | Select all | Some (tonal dash) → tap → "6 of 6" (✓) → tap → "0 of 6"; the DB shows drawn untouched. Drawn names are left out of the box: "Some" with Kai and zara in and ben out. The box is disabled when every name is drawn (`enabled=false`). | `phone-light-draw-all-selected.png`, `phone-dark-draw-every-name-drawn.png` |
| 5 | Sort | Unsorted → A–Z (Ada, ben, Chloe, Kai, Tom, zara) → Z–A (zara … Ada) → Unsorted. A–Z survives force-stop and relaunch. | `phone-dark-draw-za.png`, `phone-light-draw-a-z.png` |
| 6 | Stepper | + to 4, then + disabled (`enabled=false`). Deselecting zara and Ada clamps to 2; reselecting keeps 2. | `phone-dark-draw-clamped.png` |
| 7 | Draw | Draw 4 of 4, then Draw 4 more ×4: five different orders, no repeats within a draw | `reveal-phone-dark-4-end.png` |
| 8 | Nothing in the hat | "Tap names to put them in the hat" and a disabled Draw. With every selected name drawn: "Everyone's been drawn" | `phone-dark-draw-nothing-in-hat.png`, `phone-dark-draw-all-selected-drawn.png` |
| 9 | Rotation and process death | Rotating gave the same four names, laid out at once with no replay. Home, then `am kill` (`pidof` empty), then relaunch: same names | `reveal-phone-dark-rotated-immediate.png`, `reveal-phone-dark-landscape.png`, `reveal-phone-dark-after-process-death.png` |
| 10 | `animator_duration_scale 0` | Full end layout 0.3 s after the tap; the same "Drawn: …" node | `reveal-reduced-motion-0.3s.png` |
| 11 | Empty state | Green Team: the hat, "This hat is empty", body, Add names. Add names → Edit list (Green Team) → back → Draw | `phone-dark-draw-empty.png` |
| 12 | Spec match | Phone portrait and landscape and the tablet list-detail, in light and dark. The reveal covers both panes (3-column grid) and has light status-bar icons on both backdrops. Escape (`keyevent 111`) dismisses. Predictive back mid-gesture scales and fades the reveal over Draw; releasing returns to Draw. | `phone-*-draw-landscape.png`, `tablet-*-list-detail-draw.png`, `tablet-*-reveal.png`, `reveal-phone-light-*.png`, `reveal-predictive-back-midgesture.png` |
| 12 | TalkBack labels (uiautomator tree; TalkBack focus can't be driven from adb) | "Select all, 4 of 6 in the hat"; "Sort: Unsorted"; "How many to draw", "Draw fewer", "Draw more"; reveal announcer "Drawn: zara, Ada, Tom, and Kai". Chip state descriptions ("in the hat" / "out of the hat" / "drawn") are set in code; uiautomator doesn't print state descriptions. A live TalkBack listen is a manual check. | — |

Found and fixed during the checks: the reveal title was left-aligned in portrait (its Text wasn't full width). Fixed in the task 9 commit; `reveal-phone-light-1.png` is after the fix, and `reveal-phone-light-2.png` and the dark portrait shots are before it.

Not captured: the Lists → Draw title morph mid-transition (back-to-back `screencap` is too slow). The Draw title uses NAM-8's `sharedBoundsIfSinglePane(ListsSharedKeys.title(id))`, unchanged.
