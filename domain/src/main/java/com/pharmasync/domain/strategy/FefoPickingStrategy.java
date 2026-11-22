package com.pharmasync.domain.strategy;

import com.pharmasync.domain.batch.Batch;
import com.pharmasync.domain.exception.InsufficientStockException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * First-Expiry-First-Out (FEFO) batch picking strategy.
 * Selects batches with the earliest expiry dates first.
 * Strictly ignores expired, recalled, or quarantined batches.
 */
public class FefoPickingStrategy implements PickingStrategy {

    private static final Comparator<Batch> FEFO_COMPARATOR = Comparator
            .comparing(Batch::getExpiryDate)
            .thenComparing(Batch::getReceivedDate)
            .thenComparing(Batch::getBatchNumber);

    @Override
    public List<BatchAllocation> pick(List<Batch> availableBatches, int requestedQuantity, LocalDate asOfDate) {
        if (requestedQuantity <= 0) {
            throw new IllegalArgumentException("Requested quantity must be positive, got: " + requestedQuantity);
        }
        if (availableBatches == null || availableBatches.isEmpty()) {
            throw new InsufficientStockException("No batches available to fulfill requested quantity: " + requestedQuantity);
        }

        // Filter: only sellable batches with positive quantity
        List<Batch> eligibleBatches = availableBatches.stream()
                .filter(b -> b.canSell(asOfDate))
                .filter(b -> b.getQuantityOnHand() > 0)
                .sorted(FEFO_COMPARATOR)
                .toList();

        List<BatchAllocation> allocations = new ArrayList<>();
        int remainingNeeded = requestedQuantity;

        for (Batch batch : eligibleBatches) {
            int available = batch.getQuantityOnHand();
            int toTake = Math.min(remainingNeeded, available);

            allocations.add(new BatchAllocation(
                    batch.getId(),
                    batch.getBatchNumber(),
                    toTake,
                    batch.getExpiryDate()
            ));

            remainingNeeded -= toTake;
            if (remainingNeeded == 0) {
                break;
            }
        }

        if (remainingNeeded > 0) {
            int totalAllocated = requestedQuantity - remainingNeeded;
            throw new InsufficientStockException(
                    "Insufficient sellable stock. Requested: " + requestedQuantity +
                            ", available valid stock: " + totalAllocated
            );
        }

        return List.copyOf(allocations);
    }
}
