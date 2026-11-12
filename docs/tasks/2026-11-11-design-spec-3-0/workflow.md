---
task: design-spec-3-0
title: Produce the 3.0 design spec
ticket: NAM-2 — Produce the 3.0 design spec (https://linear.app/northtrack/issue/NAM-2/produce-the-30-design-spec)
ticket-id: NAM-2
tracker-synced: In Engineering
mode: full
phase: implementation
branch: flow/design-spec-3-0
worktree: ../android-names-in-a-hat-nam2
created: 2026-11-11
updated: 2026-11-12
---

## Next action
Gate 1 passed 2026-11-12. Implementation, task 1: quickstart the Design type, create the canvas, then build the brand board (3 Northtrack directions plus the D18 a/b comparison). Hold for the owner's pick and log it. Baseline 2026-11-11: verify PASS @ e73220a (210 tests / 0 failures).

## Decision log
<!-- gate entries look like: "- 2026-07-05 gate 1: plan approved by user"
     answered unknowns look like: "- 2026-07-05 Q: per-user or per-team limits? A: per-user"
     log an answer the moment it is given — conversation context does not
     survive a compaction; this file does -->
- 2026-11-11 mode: full (user confirmed). Reason: nine open design questions, needs research on which M3 Expressive and adaptive components are stable, and it blocks NAM-7 to NAM-10.
- 2026-11-11 Q: isolation? A: worktree at ../android-names-in-a-hat-nam2 from origin/main, so NAM-5's in-progress checkout (flow/room-migration-to-v3) is untouched.
- 2026-11-11 Q: deliverable format? A: spec as markdown in the repo (docs/design/3.0-spec.md, stable anchors) + mock-ups on a Claude Design canvas artifact (live HTML artboards: screen × phone/large × light/dark), linked from NAM-2.
- 2026-11-11 Q: brand direction for the fallback palette? A: fresh Northtrack look, not an evolution of the 2.7.0 red. The spec proposes 2–3 directions for the owner to pick from.
- 2026-11-12 Q (gate 1 review): D17 sort? A: cycle Unsorted → A–Z → Z–A → Unsorted (a cycle, not a menu), labelled "Unsorted". Display-only, saved per list. Hand-off: NAM-10 changes NameSortType.next() DESC → NONE.
- 2026-11-12 Q (gate 1 review): D18 select all? A: the owner wants the tri-state checkbox and the text button side by side before choosing. Both go on the brand board (task 1), picked with the brand direction.
- 2026-11-12 gate 1: plan approved by user (D1–D20; D17 revised; D18 deferred to the task 1 board)

## Progress
- [ ] 1. Brand board (3 directions + D18 comparison), owner pick logged
- [ ] 2. Spec draft docs/design/3.0-spec.md
- [ ] 3. Phone portrait artboards (light + dark, states, dynamic sample)
- [ ] 4. Phone landscape + large-screen artboards
- [ ] 5. Reveal artboards (animated, reduced motion, end states)
- [ ] 6. Link and freeze; draft ticket comments

## Deviations
<!-- record at the moment they happen, never reconstructed later -->
- 2026-11-12 task 1: the Design type dictates the canvas file layout: `project/canvas.json` plus one `project/<Name>.dc.html` per artboard, each self-contained, with no shared stylesheet. The planned `tokens.css`/`components.css`/`brand-board.html` became component artboards (`Main` = the brand sheet, `DrawHero`, `SelectHeader`), with the token table copied into each and the spec as the source of truth. Sources are committed under `docs/design/3.0-mockups/project/` and published with root `docs/design/3.0-mockups`. Canvas: https://claude.ai/artifact/V3SwNVHPd4UNUtz6YDk15q
