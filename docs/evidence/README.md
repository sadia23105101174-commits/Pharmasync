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

## Not run
The k6 scripts for QA1 to QA5 are written in scripts/ but were not run: k6 was not installed and the AI-agent free quota ended before they could be run. No numbers are reported for them.

## Docker run evidence
Screenshot *.png files in this folder show the branch counter screen and head-office sync status after docker compose up --build. A sale at the branch was synced and acknowledged by head office (sync-status: totalOperationsProcessed 3).

## Tests
.\mvnw.cmd clean test: BUILD SUCCESS (domain, branch-app, head-office).