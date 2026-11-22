# Memory
## Current state
- P1 completed: Multi-module Maven skeleton created with wrapper (`mvnw`), `domain`, `branch-app`, and `head-office`.
  - Core domain patterns implemented: Composite (`PackHierarchy`), State (`BatchState`), Strategy (`FefoPickingStrategy`), Command (`Operation`, `LamportClock`).
- P2 completed: Branch POS with offline-first persistence and outbox:
  - Spring Data JPA + H2 entities: `Medicine`, `Batch`, `Sale`, `SaleLine`, `Return`, `StockCount`, `OperationLog`, `Outbox`.
  - Core Services:
    - `SaleService`: FEFO picking with `FefoPickingStrategy`, blocks expired and recalled batches, validates prescription rules for Rx medicines (`patientName`, `prescriberRegNo`, `prescriptionDate`).
    - `ReturnService`: Restores inventory to batch and records return transaction.
    - `StockReceivingService`: Creates or increments batch inventory.
    - `StockCountService`: Performs physical stock audits and records difference.
  - Adapter Pattern: `MockReceiptPrinterAdapter` adapting high-level domain `Receipt` to simulated thermal receipt printer hardware (`EscPosThermalPrinterDevice`).
  - Append-only event store & Outbox: Every state change persists to `operation_log` and `outbox` with logical `LamportClock` ordering and pending status.
  - REST & Web UI:
    - Counter screen in Thymeleaf (`/counter`) for picking medicine, entering quantity and prescription details, completing sale, and viewing formatted thermal receipt.
    - REST API controllers with Bean Validation, RFC 9457 Problem Details (`GlobalExceptionHandler`), and pagination on list endpoints.
  - Demo Data Seeding: Auto-seeds 4 medicines and 6 batches (active, expired, recalled, Rx, OTC) on startup.
  - Tests: All green (23 total tests across modules), including FEFO earliest expiry allocation, expired batch blocking, Rx rule validation, and outbox creation per sale.

## Decisions
- Spring Boot version: `3.4.3` on Java 21.
- Clean Architecture: Domain remains pure Java; JPA persistence entities in `branch-app` map cleanly to/from domain models.
- Outbox pattern: Every mutating operation saves atomically to `operation_log` (append-only) and `outbox` (pending status) in the same transaction.

- P3 completed: Head office sync API (idempotent, ack, device security)
  - Entities: `CentralBatchStock` (stock per branch/product/batch), `BranchSyncStatus` (last acked logical clock, last sync time, total ops), `ProcessedOperation` (idempotency registry, unique operation_id)
  - `SyncService`: applies a batch idempotently (skips already-processed UUIDs, updates central stock once per op), tracks per-branch sync status
  - `SyncController`: `POST /api/sync/operations` returns acknowledged ids; `GET /api/sync/stock` and `GET /api/sync/sync-status` with pagination; `GET /api/sync/sync-status/{branchId}`
  - `BranchDeviceSecurityConfig` + `BranchSecurityProperties`: HTTP Basic on `/api/sync/**` with per-branch device credentials from `BRANCH_DEVICE_CREDENTIALS` env var (`branchId:secret` list), BRANCH role, stateless
  - RFC 9457 Problem Details via `spring.mvc.problemdetails.enabled`
  - Tests: duplicate op applied once, unauthenticated rejected (401), paginated GET stock/sync-status (4 green tests)
- P4 completed: Branch sync client (scheduled, retry, backoff, resumable)
  - `SyncClientProperties`: server url, batch size, exponential backoff, max retries, fixed delay, device secret (env `BRANCH_DEVICE_SECRET`)
  - `BranchSyncClient`: RestClient, fetches pending outbox ordered by lamport clock in small batches, sends with basic auth, retries with exponential backoff, marks acked only after server ack; acked entries never re-sent (only `status=pending` fetched)
  - `SyncScheduler` + `SyncClientConfig` (`@EnableScheduling`, `@ConfigurationProperties`): fixed-delay scheduled sync
  - Tests: outbox marked acked after successful sync, retry then ack on success (2 green tests)

## Next step
P5: Recall, prescription sales, 3 conflict rules, minimal transfer/reorder/dashboard
