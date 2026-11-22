# ADR-002: Lamport logical clock for ordering operations

## Context
Branch device clocks can be wrong or change while offline, so wall-clock timestamps cannot order operations reliably across branches.

## Options
A. Wall-clock timestamps.
B. Vector clocks (full causality, larger metadata per operation).
C. Lamport logical clock (one counter per operation).

## Decision
Option C. Each operation carries a Lamport clock value. The branch sends pending operations ordered by this value; ties are broken deterministically by operation UUID (verify in code).

## Consequences
+ Deterministic, small metadata, no dependence on device time.
- Gives a total order but not full concurrency detection, so conflicts are handled by explicit documented rules (FR5) rather than by detection.

## Evidence
- Domain unit tests cover LamportClock advancement and the Operation class.
- BranchSyncClient sends pending operations ordered by Lamport clock.
