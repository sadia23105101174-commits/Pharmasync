# Evidence report (C9)

## Unit and integration tests
- End of P4: 29 tests green across domain, branch-app and head-office (Maven: .\mvnw.cmd clean test).
- Covered: FEFO picking, expired batch blocked, batch state transitions, pack conversion, Lamport clock, duplicate operation applied once, unauthenticated request rejected (401), outbox marked acked after sync, retry then ack, paginated GETs.
- End of P5: Additional tests green - ConflictRulesTest (3 rules), RecallServiceTest, ReportControllerTest, TransferServiceTest, ReorderServiceTest, HeadOfficeRecallControllerTest.
- Later phases (P6, P7): update this section with the final test count.

## Quality attribute runs
For each run paste: date, command, raw output file name, result, and what was changed after a failure. Keep failed runs too.

| QA | Script | Scale used | Target | Result | Raw output | Notes / changes after failure |
|---|---|---|---|---|---|---|
| QA1 offline continuity | scripts/qa1_offline_continuity.js | 5 virtual users, 5 sales | 5 sales under 30 s | N/A — k6 not installed in this environment | | Script written; requires k6 and running server |
| QA2 convergence | scripts/qa2_convergence.js | 5 virtual users | all nodes agree | N/A — k6 not installed in this environment | | Script written; requires k6 and running server |
| Q3 checkout speed | scripts/qa3_checkout_speed.js | 5 virtual users | p95 under 300 ms | N/A — k6 not installed in this environment | | Script written; requires k6 and running server |
| Q4 safety | scripts/qa4_safety.js | 5 virtual users | zero unsafe sales | N/A — k6 not installed in this environment | | Script written; requires k6 and running server |
| Q5 low bandwidth | scripts/qa5_low_bandwidth.js | 5 virtual users | 800 sales under 60 s | N/A — k6 not installed in this environment | | Script written; requires k6 and running server |

If a script could not be run in time, write exactly that and why. Do not invent numbers.
