# PharmaSync: offline-first inventory and point of sale for pharmacy chains

A class project (Spring Boot). A branch point of sale keeps selling with no internet and synchronises with a central head office when the connection returns, without losing or duplicating a sale.

## Modules
| Module | What it is | Port |
|---|---|---|
| `domain` | Shared business code: Medicine, pack hierarchy (Composite), Batch with State, FEFO picking (Strategy), Operation (Command), Lamport clock | none |
| `branch-app` | Branch POS: embedded H2, Thymeleaf counter screen, operation log + outbox, scheduled sync client | 8081 |
| `head-office` | Central service: PostgreSQL, idempotent sync API, per-branch device security, central stock | 8080 |

## How to run
Requirements: Java 21, Docker Desktop. Maven is not needed, the Maven wrapper is included.

```
.\mvnw.cmd clean test
docker compose up --build
```

Links after start:
- Branch counter screen: http://localhost:8081
- Branch health: http://localhost:8081/actuator/health
- Branch API docs: http://localhost:8081/swagger-ui.html
- Head office health: http://localhost:8080/actuator/health
- Head office API docs: http://localhost:8080/swagger-ui.html

Run only the branch app without Docker:
```
.\mvnw.cmd install -DskipTests
.\mvnw.cmd spring-boot:run -pl branch-app
```

## How it works
1. At the counter, the sale picks batches first-expiry-first-out. Expired and recalled batches cannot be sold. Prescription-only medicines need prescriber registration number, patient name and prescription date.
2. The sale and an Operation (UUID plus Lamport clock) are saved in one local transaction into an append-only operation log and an outbox.
3. A scheduler sends pending operations to head office in small batches with retry and exponential backoff.
4. Head office applies each operation once (idempotent by UUID), updates central stock per batch and returns the acknowledged ids.
5. The branch marks operations as acknowledged only after that reply, so a cut connection resumes without resending acknowledged operations.

Design decisions are in `docs/adr/`, diagrams in `docs/architecture-c4.md`, quality scenarios in `docs/utility-tree.md`, patterns in `docs/patterns.md`, and test evidence in `docs/evidence/README.md`.

## Status (honest)
| Plan item | Status |
|---|---|
| P1 Skeleton and domain | Done, tests green |
| P2 Branch POS and outbox | Done, tests green |
| P3 Head office sync API | Done, tests green |
| P4 Branch sync client | Done, tests green |
| P5 Partial: recall (entity, repository, service, tests) and conflict rules doc. Transfers, reorder, dashboard not finished
| P6 Not done
| P7 Not done
| P8 Documentation | Done (C4, 4 ADRs, utility tree, pattern table, evidence template) |

Unfinished items were stopped by free AI-agent quota limits, not by design choices. The Spring Boot version is 3.4.x although the brief asks for 4.x; upgrading was left out because of the time limit.

## AI usage log
Tools: Google Antigravity (Gemini 3.8 Flash) for P1 and P2, OpenCode with free models (Nemotron 3.5 Super VL, then others) for P3 to P5. Work was handed between tools using `agent.md`, `Plan.md` and `memory.md`. Every agent mistake, how it was found and how it was fixed is listed in `errors.md`. Key prompts are the task prompts for P1 to P8 (domain and skeleton, branch POS and outbox, sync API and client, recall and conflicts, tests and load scripts, docs).
