# NAM-12 device check: niah_api30 (debug build at 9a4bc2f), 2026-11-14

Seeded list "Year 6 Oak" with Ada, Ben, Cat, Dev, Eve, Fin, all selected and none drawn. DB lines are `name:selected drawn`, read with `run-as … sqlite3` after each step.

| # | Step | Result |
|---|---|---|
| 01 | Open the list | "6 of 6 in the hat"; the Reset icon is dimmed (nothing drawn) |
| 02 | Stepper to 2, Draw 2 | Reveal shows 2 names. DB: Ada:11 Dev:11, the rest :10 |
| 03 | Done | Counter "4 of 6 in the hat · 2 drawn"; the button reads "Draw 2" |
| 04 | Draw 2 again | DB: Ben:11 Cat:11 added. **Disjoint from the first draw (AC1)** |
| 05 | `am force-stop` (pidof empty), cold launch | Reopens Year 6 Oak. **4 Drawn chips (ticket icon + strikethrough), "2 of 6 in the hat · 4 drawn" (AC5, AC10 visuals)** |
| 06 | Tap drawn Ben | Anchored menu "Put back in the hat" with the undo-arrow icon |
| 07 | Choose it | DB Ben:10 (selected, not drawn); "3 of 6 in the hat · 3 drawn" |
| 08 | Stepper to 3, Draw 3 | Reveal: "Everyone's been drawn" replaces "Draw n more" |
| 09 | Done | Bar: "Everyone's been drawn" + filled **Reset**. Box shows empty; "0 of 6 in the hat · 6 drawn" **(AC2)** |
| 10 | Bar Reset (one script with 11) | Snackbar "Drawn names reset" / Undo. All chips In. DB: every name :10, **selection unchanged (AC3)** |
| 11 | Undo at t+7 s | DB: every name :11 again (the drawn set is restored) |
| 12 | Landscape: top-bar Reset | The snackbar sits in the names area, clear of the trailing panel's Draw button. Left to time out |

Not checkable here: TalkBack speech for the "drawn" state and the Put back action (covered by `DrawChipSemanticsTest`; the owner does a live listen).
