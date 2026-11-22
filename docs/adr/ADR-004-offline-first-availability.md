# ADR-004: Branch favours availability over consistency (embedded database)

## Context
Branches must sell with no connection for at least 72 hours (FR3, QA1). Under the CAP theorem, a branch cannot be both always available and always consistent with head office.

## Options
A. Thin client that always calls the central database. Consistent, but stops when offline.
B. Local read-only cache. Can look up medicines but cannot sell offline.
C. Full local application with an embedded H2 database, syncing later.

## Decision
Option C. The branch is its own Spring Boot application with embedded H2. Consistency is eventual and restored by sync.

## Consequences
+ Selling never stops.
- Concurrent changes can conflict, so we need deterministic rules: sale at an old price stands and the difference is reported; negative stock after sync creates a count task; sales after a recall time are flagged.
- Embedded H2 is simple but less durable than a server database; acceptable for this project scope.

## Evidence
- Branch sale tests run without head office.
- Sync tests show head office unavailable, then available, and the outbox still drains.
- Conflict rule tests (P5), see docs/conflict-rules.md (verify it exists).
