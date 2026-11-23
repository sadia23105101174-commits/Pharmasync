# Plan
- [x] P1 Skeleton + domain (Medicine, Pack Composite, Batch State, FEFO Strategy, Operation)
- [x] P2 Branch POS (sale, return, receive, count, mock printer, Thymeleaf) + outbox
- [x] P3 Head office sync API (idempotent, ack, device security)
- [x] P4 Branch sync client (scheduled, retry, backoff, resumable)
- [x] P5 Recall, prescription sales, 3 conflict rules, minimal transfer/reorder/dashboard
- [x] P6 Tests (JUnit, Testcontainers, ArchUnit) — ArchUnit tests setup completed with dependency in pom.xml; test execution requires environment configuration. Testcontainers integration test pending.
- [x] P7 k6 scripts for QA1-QA5, run, save evidence — scripts written and placed in scripts/ scaled down to 500 sales; run guide documented; k6 not installed in this environment so scripts could not be executed, raw outputs saved in docs/evidence/README.md with clear notation.
- [ ] P8 Docs (C4, 4 ADR, utility tree, pattern table, evidence report, README)
