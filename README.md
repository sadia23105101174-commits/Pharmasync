# PharmaSync

PharmaSync is a modular Java Spring Boot application for pharmacy inventory and point-of-sale operations, designed with offline-first principles, multi-module Maven architecture, and full sync capabilities between branch and head office.

## Modules

| Module | Description |
|---|---|
| `domain` | Core domain models and business logic: `Medicine`, `PackHierarchy`, `BatchState`, `FefoPickingStrategy`, `Operation`, `LamportClock`, `BatchState`, `OperationLog`. Pure Java, no Spring dependencies. |
| `branch-app` | Branch Point-of-Sale application with offline-first persistence (H2 database), Thymeleaf UI, REST APIs, outbox pattern, and branch sync client. Features: sale/return/receive/count, prescription validation, FEFO picking, mock thermal printer, batch recall, transfers, reorder suggestions, prescription reports, and dashboard. |
| `head-office` | Head office sync API with PostgreSQL, idempotent sync, device security (HTTP Basic per branch), RFC 9457 Problem Details, and recall management. |

## How to Run

### Using Maven (no Docker required)

```cmd
:: Full build and test
.\mvnw.cmd clean test
```

```powershell
.\mvnw.cmd clean test
```

### With Docker Compose (head-office with PostgreSQL)

```cmd
docker compose up --build
```

## Architecture

- **Clean Architecture**: Domain layer is pure Java; JPA persistence entities in `branch-app` and `head-office` map cleanly to/from domain models.
- **Outbox Pattern**: Every mutating operation atomically persists to `operation_log` (append-only) and `outbox` (pending status) in the same transaction.
- **Lamport Clock**: Append-only event store with logical clock ordering for distributed event ordering.
- **Spring Boot 3.4.3 on Java 21**.

## Environment Variables (for head-office)

| Variable | Description | Example |
|---|---|---|
| `BRANCH_DEVICE_CREDENTIALS` | List of `branchId:secret` pairs for device auth | `branch1:secret1,branch2:secret2` |
| `BRANCH_DEVICE_SECRET` | Default device secret for sync client | `mysecret123` |

## Test Execution

- All Maven tests: `.\mvnw.cmd clean test`
- Specific module tests: `.\mvnw.cmd test -pl <module-name>`
- ArchUnit architecture tests: included in `branch-app` module (requires ArchUnit environment)
- Testcontainers integration tests: available in `head-office` module