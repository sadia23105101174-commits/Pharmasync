# Pattern justification (C8)

Keep a row only if the pattern really exists in the code. Check the class names (verify).

| Pattern | Requirement served | Participating classes | Alternative rejected |
|---|---|---|---|
| Command | FR4: changes recorded as operations, sent in idempotent batches | Operation, OperationCommand, OperationType, OperationStatus | Overwriting rows and syncing state (loses concurrent changes) |
| Strategy | FR2: first-expiry-first-out, replaceable picking rule | PickingStrategy, FefoPickingStrategy | if/else inside the sale service (hard to change and test) |
| State | FR2, FR7: batch active, quarantined, recalled, expired; only active can be sold | Batch, BatchState, ActiveBatchState, QuarantinedBatchState, RecalledBatchState, ExpiredBatchState | Status string with switch statements (rules scattered) |
| Composite | FR1: pack hierarchy box, strip, tablet with unit conversion | PackComponent, BasePackUnit, CompositePackUnit, PackHierarchy | Hard-coded conversion factors per medicine |
| Adapter | FR3: mock receipt printer (and scanner) behind an interface | printer adapter in branch-app (verify class name) | Calling printing code directly from the sale service |
| Repository | Data access for batches, sales and operations | Spring Data JPA repositories | Hand-written SQL in services |

Observer (recall and price notifications) is listed in the brief; add a row only if you implemented it in P5.
