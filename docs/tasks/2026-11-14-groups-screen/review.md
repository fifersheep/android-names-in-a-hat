# NAM-8 code review

One flow-reviewer pass over `main...HEAD` at 7a08256, against plan.md. **0 Blocking, 6 Advisory.** Two advisories were fixed. The other four are report follow-ups, plus one evidence gap that is now filled.

## Blocking
None.

## Advisory
| # | Finding | Action |
|---|---|---|
| A1 | `ListsScreen.kt:94-96`. On list-detail, delete A with Draw(B) open, then resize or rotate to single pane before the snackbar ends. Lists is no longer composed, so leaving the app doesn't commit A. A stays hidden, and isn't deleted, until the user returns to Lists, where the snackbar re-shows. There is no data loss. | **Follow-up.** It needs a commit trigger outside the Lists composition, such as process lifecycle or the shell. |
| A2 | `EditListViewModel.kt:136-141`. The rename for Done or Next ran on `viewModelScope`. An immediate pop could cancel it, and `onCleared` then treated the name as already stored, so the rename was lost silently. | **Fixed in aebc5be.** Renames run on the app scope. New test: `done keeps the rename when the screen is cleared before the write runs` (red before the fix, green after). Device: Edit → "Alpha Prime" → Done → DB `5:Alpha Prime`. |
| A3 | `EditListViewModel.kt:103-111`. `onCreated` is captured from the composition at the time of Next. An activity recreation during the create, within a few milliseconds, leaves the key as `EditList(null)` while the ViewModel holds the id. Done then pops instead of going to Draw, and the names slot sees null. | **Follow-up (NAM-9).** Take the slot's `listId` from the ViewModel, not the key, and consider re-issuing `listCreated` once the screen is recomposed. |
| A4 | `ListsScreen.kt:194`. Undo after opening another list replaced that list's Draw with the restored one. | **Fixed in f0079ac.** Undo reopens only into an empty detail pane. Tablet: delete Gamma (open) → open Beta → Undo → Gamma restored and the detail stays on Beta (`tablet-undo-keeps-detail.png`). The baseline still works: delete the open list → Undo → reopened. |
| A5 | A blank title migrated from 2.7.0 shows an untitled card, "Options for " and " deleted". | **Follow-up for gate 2** (workflow.md deviation). Needs a product decision, e.g. an "Untitled list" fallback. |
| A6 | The phone-landscape Lists evidence predated the card-fill fix. | **Filled:** `phone-lists-landscape.png` taken after the fix. |

## Acceptance criteria
| AC | Verdict | Evidence |
|---|---|---|
| AC1 migrated data | MET | install-over on api36_niah; the new list got id 4, the deleted id 3 wasn't reused |
| AC1b settings | MET | launch → `Draw(1)`; DataStore `group_sort_type_for_group_1 = DESC` |
| AC2 live updates | MET | test `lists update live`; phone and tablet walks |
| AC3 validation | MET | ViewModel tests; `phone-edit-blank-error.png`; DB `'Team'` |
| AC4 delayed delete | MET | 7 ViewModel cases; phone timeout, Home, rotate; tablet close and reopen, unsaved edit + Undo (A1 is an edge outside these) |
| AC5 empty state | MET | `phone-empty-light.png`, `tablet-empty-light.png` |
| AC6 plurals | MET | Room jvmTest; device "1 name", "3 names · 1 drawn" |
| AC7 rotation, death | MET | recreation tests; device rotate + `am kill` |
| AC8 spec match | MET | checklist screenshots, light and dark, including `phone-lists-landscape.png` |
| AC9 VM tests | MET | `ListsViewModelTest`, `EditListViewModelTest` (71 app tests) |
| AC10 Edit rules | MET | tests + phone walk |
| AC11 accessibility | PARTIAL | labels wired and checked in the a11y tree; **TalkBack walk is the owner's** (emulator can't drive it); A5 affects blank titles |
| AC12 copy | MET | grep clean; superseded keys removed; lint in verify |

## Checked (reviewer)
- **Conformance:** `.flow.md` verify strengthened; the deleted placeholder test is superseded case for case; every changed file traces to a plan task or a logged deviation.
- **Correctness:**
  - ListsViewModel / ListsScreen delete lifecycle and its id guards.
  - `AppNavigation` `openId`; BackStack ops; `ShellViewModel.onListGone`.
  - EditListViewModel commit, rename-on-leave and saved state.
  - SharedBounds (single pane only); HatIllustration; About; the shared drawn-count SQL.

## Re-verify
`./gradlew --no-watch-fs :app:assembleDebug :shared:allTests :app:testDebugUnitTest :app:lintDebug` exited 0 after the fixes. Counts from the XML: app 71, shared 256 (jvm 109, iOS sim 70, Android host 77), 0 failures. Outcome: both fixes were driven on device as described above.
