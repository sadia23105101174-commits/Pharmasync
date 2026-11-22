# Evidence report (C9)

## Unit and integration tests
- End of P4: 29 tests green across domain, branch-app and head-office (Maven: .\mvnw.cmd clean test).
- Covered: FEFO picking, expired batch blocked, batch state transitions, pack conversion, Lamport clock, duplicate operation applied once, unauthenticated request rejected (401), outbox marked acked after sync, retry then ack, paginated GETs.
- Later phases (P5, P6, P7): update this section with the final test count.

## Quality attribute runs
For each run paste: date, command, raw output file name, result, and what was changed after a failure. Keep failed runs too.

| QA | Script | Scale used | Target | Result | Raw output | Notes / changes after failure |
|---|---|---|---|---|---|---|
| QA1 offline continuity | scripts/ (name) | | 5,000 sales, sync under 5 min | | | |
| QA2 convergence | scripts/ (name) | | all nodes agree | | | |
| QA3 checkout speed | scripts/ (name) | | p95 under 300 ms | | | |
| QA4 safety | scripts/ (name) | | zero unsafe sales | | | |
| QA5 low bandwidth | scripts/ (name) | | 800 sales under 60 s | | | |

If a script could not be run in time, write exactly that and why. Do not invent numbers.
