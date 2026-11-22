# Conflict Rules (deterministic outcomes)

PharmaSync resolves concurrent branch/head-office changes deterministically.
All rules are applied at head-office when a batch of operations is synchronized,
and the outcome is reported back to the branch.

## Rule 1 — Sale at an old price stands; difference is reported

If a branch sells at a price that predates a central price change, the sale
stands (the customer already paid). The price difference is reported to head
office so the branch can be reconciled, but the sale is never reversed.

- Outcome: sale stands; `priceDifference = currentPrice - salePrice` reported.
- Rationale: preserves offline sale continuity (FR3, QA1); avoids clawing back
  completed customer transactions.

## Rule 2 — Negative stock after sync creates a count task

If applying synchronized operations drives a batch's `quantityOnHand` below zero
at head office (e.g. a sale was made offline against stale stock), stock is
clamped to zero and a `StockCount` task is created for the affected branch/batch
so a physical recount can correct the record.

- Outcome: `quantityOnHand = max(0, computed)`; a pending `StockCount` task is
  created; the branch is notified on next sync.
- Rationale: stock must never be negative; physical audit restores truth.

## Rule 3 — Sales after a recall time are flagged

If a branch sells a batch after its recall time but before the branch learned
about the recall (offline window), the sale stands but is flagged as a
post-recall sale. Once the recall is applied (batch status `RECALLED`), further
sales of that batch are blocked.

- Outcome: sale stands; `recalledBatchSale = true` flag set; appears in the
  recall report (units sold after recall time). Subsequent sales blocked while
  status is `RECALLED`.
- Rationale: FR7 / QA4 — flagged for recall reporting, never silently dropped.
