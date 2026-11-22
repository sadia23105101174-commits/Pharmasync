# ADR-001: Idempotent sync using operation UUIDs

## Context
Branch links drop or cut mid-transfer. The same batch of operations can arrive twice. Requirement: no sale lost or duplicated (QA1), and a cut sync resumes without resending acknowledged operations (QA5).

## Options
A. Send state snapshots and overwrite rows at head office. Simple, but concurrent changes are lost.
B. At-least-once delivery plus server-side deduplication by operation UUID.
C. Exactly-once using a distributed transaction (two-phase commit). Not workable on unreliable links.

## Decision
Option B. Every change is an Operation with a UUID. Head office applies an operation only if its UUID is new, and always returns the list of acknowledged ids.

## Consequences
+ Safe to retry at any time; the branch needs no special recovery logic.
+ Each side knows what the other acknowledged.
- Head office must store applied operation ids (storage grows, can be pruned later).

## Evidence
- Head-office test: the same operation sent twice is applied once.
- Branch test: outbox is marked acked only after the server acknowledges; retry then ack succeeds.
- All 29 tests green across domain, branch-app and head-office at the end of P4.
- Load results for QA1 and QA5: see docs/evidence/README.md.
