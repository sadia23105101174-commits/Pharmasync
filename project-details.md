# Project 5 — PharmaSync: Offline-First Inventory and Point of Sale for Pharmacy Chains

## PROBLEM STATEMENT

Pharmacy chains and large independent pharmacies sell from branches where the internet and power drop out regularly. Cloud point-of-sale systems stop working when the connection does, so staff switch to paper and branch stock records drift away from the truth.

Medicines make this harder. Every unit belongs to a batch with an expiry date, so stock must be tracked per batch, the earliest-expiring batch should be sold first, and expired stock must never be sold. When a batch is recalled, the chain must quickly know which branches hold it and stop selling it. Some medicines can only be sold against a prescription that has to be recorded.

PharmaSync is a central inventory service plus a branch point of sale that keeps working fully offline and synchronises when the connection returns. The project succeeds when no sale is ever lost or duplicated, every branch and the head office agree on stock per batch after syncing, and no expired or recalled batch is sold.

## FUNCTIONAL REQUIREMENTS

- **FR1 Catalogue.** Medicines with generic name, brand, strength, form and pack hierarchy (box → strip → tablet) with unit conversion; price lists by region; a prescription-only flag. Catalogue and price changes flow from head office to branches.
- **FR2 Batch stock.** Every unit belongs to a batch with an expiry date. Sales pick first-expiry-first-out by default, expired batches cannot be sold, and batches within 90 days of expiry are reported for discounting or return.
- **FR3 Offline branch.** The branch point of sale (its own Spring Boot application with an embedded database) handles sales, returns, receipts (mock printer), stock receiving and stock counts with no connection for at least 72 hours.
- **FR4 Sync.** The branch records changes as operations, not overwritten rows, and sends them in idempotent, resumable batches when online; head office pushes catalogue, price and recall updates down. Each side knows exactly what the other has acknowledged.
- **FR5 Conflict rules.** Documented, deterministic outcomes for concurrent changes: a branch sold at an old price after a central price change (the sale stands, the difference is reported); stock goes negative after sync (a count task is created); a batch was recalled while a branch was offline (sales after the recall time are flagged).
- **FR6 Transfers.** Branch-to-branch transfers move through dispatched, in-transit and received states; quantity differences at receipt are flagged.
- **FR7 Recalls.** Head office marks a batch recalled; branches block it on their next sync; a recall report shows quantity on hand per branch and units sold after the recall time.
- **FR8 Prescription-only sales.** The sale cannot complete without prescriber registration number, patient name and prescription date; a register report lists these sales.
- **FR9 Reordering.** Suggested order quantities per branch and product from 30-day sales velocity, supplier lead time and safety stock.
- **FR10 Head-office dashboard.** Stock by product, batch and branch; each branch's last sync time and pending operations; value of stock expiring within 90 days.

## QUALITY ATTRIBUTES (SUCCESS MEASURES)

- **QA1 Offline continuity.** A branch runs 72 simulated offline hours with 5,000 sales; after reconnecting, everything syncs within 5 minutes with no lost or duplicated sale.
- **QA2 Convergence.** After a chaos run (3 branches, random disconnects, transfers, central price changes and a recall), all branches and head office agree on stock per batch, and stock always equals receipts − sales ± transfers ± adjustments.
- **QA3 Checkout speed.** A 5-item sale completes in under 300 ms at p95 and a barcode lookup in under 50 ms, online or offline.
- **QA4 Safety.** Zero sales of expired batches, zero sales of a recalled batch after the branch's first sync following the recall, and zero prescription-only sales without a record.
- **QA5 Low bandwidth.** A busy branch's day (800 sales) syncs within 60 seconds over a link throttled to 256 kbps, and a sync cut halfway resumes without resending acknowledged operations.

## SPRING BOOT ARCHITECTURE FOCUS

- Two deployables (head office and branch) from one multi-module Maven or Gradle build with a shared domain module; the student decides what is shared and what is not.
- Branch: embedded H2 or SQLite with Spring Data JPA, an operation log and outbox, scheduled sync with retry and backoff (RestClient), and a counter screen built with Spring MVC and Thymeleaf (MVC) or a local single-page front end (MVVM).
- Head office: PostgreSQL, idempotent sync endpoints, Spring Security per branch device, modules checked with Spring Modulith or ArchUnit.
- Data architecture: branches favour availability over consistency (CAP), operations are ordered with logical clocks, and conflicts resolve deterministically.
- Patterns: Command (operations), Strategy (batch picking and conflict rules), State (batch: active, quarantined, recalled, expired), Composite (pack hierarchy), Observer (recall and price notifications), Repository and Unit of Work, Adapter (barcode scanner and printer, mocked).

Out of scope: payment terminals, government drug-database integration, accounting, home delivery, mobile apps.

Course coverage: Lessons 3–4, 6, 8–10, 13–14, 17–18, 20.

## Requirements common to all five projects

- **C1 Stack.** Java 21 or 25 on Spring Boot 4.x, built with Maven or Gradle. The whole system starts with one command through Docker Compose on a laptop with 8 GB RAM. No paid cloud services and no paid API keys.
- **C2 Configuration.** Profiles plus environment variables; typed @ConfigurationProperties with validation; no secrets in the repository.
- **C3 API quality.** OpenAPI documentation (springdoc); Spring Security on every endpoint; Bean Validation on inputs; errors as RFC 9457 Problem Details; pagination on every list endpoint.
- **C4 Observability.** Spring Boot Actuator health and readiness endpoints; Micrometer metrics and tracing; structured JSON logs carrying a trace ID across components.
- **C5 Tests.** JUnit 5 unit and slice tests, integration tests with Testcontainers (database, broker, Redis as used), and every quality-attribute measure as a runnable script (k6, Gatling or JMeter for load). CI runs them all.
- **C6 Architecture rules as tests.** ArchUnit or Spring Modulith verification enforces the module and layer boundaries the student chose.
- **C7 Architecture documentation.** C4 context, container and component diagrams; at least 4 ADRs (context, options, decision, consequences, with measured evidence); a utility tree with at least 6 scenarios plus sensitivity and trade-off points (mini-ATAM).
- **C8 Pattern justification.** A table giving, for each pattern used, the requirement it serves, the participating classes and the alternative rejected. Patterns not tied to a requirement earn no credit.
- **C9 Evidence report.** Results for every quality attribute, with raw outputs and charts, including failed runs and what was changed in response.
- **C10 Checkpoint change request.** Implemented after the instructor issues it, with a note listing the files touched in core code versus new components.
- **C11 Process evidence.** Incremental git history (a single bulk commit is penalised) and an AI-usage log: tools used, key prompts, and at least three cases where the agent's output was wrong or incomplete, how that was found and how it was fixed.
- **C12 Live defence.** 15 minutes: walk through one ADR and make a small instructor-requested change on the spot.