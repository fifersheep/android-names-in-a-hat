# NAM-2 code review

One `flow-reviewer` pass, 2026-11-12, on `e73220a..45ef713` against plan.md. It found **0 blocking** and **20 advisory** findings: 19 fixed and 1 recorded as a deviation.

## Blocking
None.

## Advisory

| # | Finding | Done |
|---|---|---|
| 1 | "Everyone's been drawn" shows when the hat is empty and *anything* is drawn. That's false after drawing 1 of 24 and then tapping Clear all | **Fixed:** the line and the Reset action show only when the hat is empty **and** a selected name is drawn; otherwise the "Tap names" hint shows. Spec [Draw bar] and [States], NAM-12 comment |
| 2 | The reveal's button styles didn't match the mock-up (spec said tonal/filled, mock-up is outlined/white), and the buttons had no colour roles | **Fixed:** the spec names the roles (Done outlined `onPrimary`/`onPrimaryContainer`; Draw n more `surfaceContainerLowest`+`primary` light, `primary`+`onPrimary` dark). Contrast rows added |
| 3 | The spec said reduced motion "skips the hat", but the artboard keeps it | **Fixed:** spec says the hat stays but doesn't shake |
| 4 | "≤ 1.5 s total" isn't true: the last card *starts* by 1.5 s | **Fixed:** spec says the last card starts by 1.5 s and the reveal is done in under 2 s |
| 5 | Plan D2's hand-off (`DrawState`/`drawFrom` count drawn-but-selected names) was missing from the spec | **Fixed:** an explicit ⇢ in [Draw from remaining] and in the NAM-12 comment |
| 6 | The Lists "· 4 drawn" count has no data source (`GroupSummary` has only `nameCount`) | **Fixed:** ⇢ to NAM-8 (spec [Lists], index row NAM-8.H1, NAM-8 comment) |
| 7 | The D17 hand-off missed `SortOrderTest` and the `SortOrder` KDoc | **Fixed:** in the ticket index row NAM-10.4 and the NAM-10 comment |
| 8 | `validateLabel` doesn't remove inner line breaks, but the spec said it did | **Fixed:** spec corrected; ⇢ NAM-9 normalises line breaks and tabs to spaces (index NAM-9.H1, comment) |
| 9 | Wrong 2.7.0 line refs (`OverviewCard.kt:36`, `strings.xml:4-23`) | **Fixed:** now `:50` and `:7-28` |
| 10 | Unchecked-box role was `outline` in the contrast table but `onSurfaceVariant` elsewhere | **Fixed:** separate contrast rows; `onSurfaceVariant` is the box |
| 11 | The contrast table missed pairs the artboards use | **Fixed:** 13 rows added; script re-run, still 0 failures |
| 12 | Content descriptions used in the mock-ups had no copy keys | **Fixed:** added `action_back`, `action_more`, `draw_back`, `draw_count_group`, `edit_rename_save`, `edit_rename_cancel` |
| 13 | The select-header section pointed NAM-10 at `SelectCompare` (old palette and old dash) | **Fixed:** points to `PhoneDraw`/`DrawScreen`; SelectCompare and MixedOptions are labelled superseded |
| 14 | Landscape chips were 36 px; the spec says ≥ 40 dp | **Fixed:** `LandScreen.dc.html` uses 40 px; republished |
| 15 | Task 5 end states and the reduced variant are portrait only | **Recorded** as a deviation in workflow.md; the AC is still met |
| 16 | `check_spec.py` output wasn't in the spec | **Fixed:** new [Spec checks] section. The script ignores that section, so it doesn't check its own output |
| 17 | The pin named `material3-adaptive-navigation-suite`, which isn't used | **Fixed:** the adaptive 1.3.0 artifacts are named; no navigation suite |
| 18 | Large screens: deleting the open list was undefined | **Fixed:** the detail closes to "Pick a list"; Undo reopens it |
| 19 | Back on a new list with a typed name was undefined | **Fixed:** back discards the name and creates nothing; on an existing list, back commits a non-blank rename |
| 20 | The tri-state box could change with no visible cause, because drawn chips hide their selection | **Fixed:** the box state is computed over not-drawn names, with a new Disabled state when every name is drawn; ⇢ NAM-10/12. **A refinement of D18 for the owner to confirm at gate 2** |

## Acceptance criteria

| AC | Verdict | Evidence |
|---|---|---|
| Coverage | MET | check_spec: 9/9 rows, 0 missing anchors |
| Navigation | MET | Spec [Navigation]: destinations, typed args, rules 1–6; [Launch] |
| Ticket index | MET | Counts 7/4/7/4/6 + NAM-6 match research.md. The NAM-5 hand-off moved to NAM-12 (deviation) |
| Copy deck | MET (after fix 12) | 29 keys + 8 hard-coded strings, 0 unaccounted; the a11y keys are added. Other sections quote the text for readability, and the deck notes this |
| Mock-ups | MET | 30 boards; every screen in portrait, landscape and large, light and dark; all states; dynamic sample |
| Brand checkpoint | MET | ebaad3d (picks) comes before d488434 (first phone artboard) |
| Reveal | MET | Animated in 3 layouts; reduced variant; fewer and none endings |
| Contrast | MET (after fix 11) | `spec_contrast.py`: 0 failures, 30 rows |
| API stability | MET | Every experimental mention has a note; the 1.5.0-rc01 pin is stated |
| Docs only | MET | `git diff --name-only e73220a..HEAD` is all under `docs/` |

## Checked
The reviewer read plan, workflow, research, the spec, the ticket comments, the check scripts and all component and wrapper artboards. They checked the code claims against `:shared` at HEAD, origin/master (2.7.0) and origin/main (NAM-5), and the role→hex mappings against the palette (0 mismatches). They re-ran all three scripts and checked commit order.
