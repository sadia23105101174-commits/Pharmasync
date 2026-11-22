# Memory
## Current state
- P1 completed: Multi-module Maven skeleton created with wrapper (`mvnw`), `domain`, `branch-app`, and `head-office`.
- Core domain patterns implemented:
  - Composite: `PackHierarchy` (Box -> Strip -> Tablet with bidirectional unit conversion).
  - State: `BatchState` (`ActiveBatchState`, `QuarantinedBatchState`, `RecalledBatchState`, `ExpiredBatchState`).
  - Strategy: `FefoPickingStrategy` (First-Expiry-First-Out, strictly excludes expired/recalled/quarantined batches).
  - Command: `Operation` and `OperationCommand` with UUID idempotency key and `LamportClock`.
- Both apps have Spring Boot Actuator health endpoints and SpringDoc OpenAPI configured.
- `docker-compose.yml` configures PostgreSQL, head-office (port 8080), and branch-app (port 8081).
- All unit and slice tests green (19 tests total across modules).

## Decisions
- Spring Boot version: Selected `3.4.3` on Java 21 since 4.x is not yet released/published on Maven Central.
- Clean Architecture: `domain` module kept as pure Java without Spring dependencies, maximizing maintainability and test execution speed.
- Test database isolation: `head-office` uses PostgreSQL for production/docker, but H2 for tests so build runs standalone without Docker dependency.

## Next step
P2: Branch POS (sale, return, receive, count, mock printer, Thymeleaf) + outbox
