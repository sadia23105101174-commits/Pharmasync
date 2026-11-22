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

## Next step
P3: Head office sync API (idempotent, ack, device security)
