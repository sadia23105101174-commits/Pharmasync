# ADR-003: Append-only operation log with a transactional outbox

## Context
A sale must never be lost, even if the app crashes right after the sale, and it must reach head office later.

## Options
A. Call head office directly during the sale. Fails offline.
B. Write the sale, then publish to a message broker. Two writes can diverge; needs a broker.
C. Save the sale and its Operation in the same local database transaction (outbox), and let a scheduler send it later.

## Decision
Option C. Every sale, return, stock receipt and count writes an Operation to an append-only operation_log and an outbox row (pending or acked) in the same transaction.

## Consequences
+ Sale and operation commit atomically; works fully offline; no extra infrastructure.
- Needs a scheduler and a status field; a pending backlog can build up offline (shown on the dashboard).

## Evidence
- Branch tests: an outbox row is created per sale; outbox is marked acked after successful sync.
