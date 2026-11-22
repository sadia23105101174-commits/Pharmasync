# Utility tree and mini-ATAM

| # | Quality attribute | Scenario | Measure | Importance / Difficulty |
|---|---|---|---|---|
| 1 | Availability | A branch sells for 72 hours with no connection | 5,000 sales, all synced within 5 min after reconnect (QA1) | High / High |
| 2 | Consistency | After random disconnects, transfers, price change and a recall, all nodes agree | Stock per batch equal everywhere (QA2) | High / High |
| 3 | Performance | Cashier completes a 5-item sale | p95 under 300 ms; barcode lookup under 50 ms (QA3) | High / Medium |
| 4 | Safety | Cashier tries to sell an expired batch, a recalled batch, or a prescription medicine without record | Zero such sales (QA4) | High / Medium |
| 5 | Efficiency | A busy day (800 sales) syncs over a 256 kbps link, cut halfway | Under 60 s, resume without resending acked operations (QA5) | Medium / Medium |
| 6 | Security | An unknown device calls the sync API | Rejected with 401; each branch has its own credential | Medium / Low |
| 7 | Modifiability | Pharmacy wants a different picking rule | New Strategy class, sale service unchanged | Medium / Low |

## Sensitivity points
- Sync batch size: bigger batches use bandwidth better, smaller batches lose less when a link is cut.
- Retry backoff settings: too short floods a recovering server, too long delays convergence.
- Idempotency store at head office: size grows with number of operations.

## Trade-off points
- Availability vs consistency (ADR-004): selling never stops, but stock can briefly disagree and may go negative.
- Lamport clock vs vector clock (ADR-002): small and simple, but conflicts need explicit rules.
- Embedded H2 vs server database at the branch: easy setup vs durability.
- Scaled-down load tests (for example 500 instead of 5,000 sales) because of laptop and time limits: documented in the evidence report.

## Risks
- Free AI-agent output may contain mistakes; every module is covered by tests and mistakes are logged in errors.md.
