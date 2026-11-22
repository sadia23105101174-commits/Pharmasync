package com.pharmasync.domain.strategy;

import com.pharmasync.domain.batch.Batch;

import java.time.LocalDate;
import java.util.List;

/**
 * Strategy pattern interface for picking batches to fulfill a requested quantity.
 */
public interface PickingStrategy {
    /**
     * Picks batches to fulfill the requested quantity as of the given date.
     *
     * @param availableBatches candidate batches
     * @param requestedQuantity quantity to pick (in base units)
     * @param asOfDate reference date for expiry evaluation
     * @return list of batch allocations
     * @throws com.pharmasync.domain.exception.InsufficientStockException if requested quantity cannot be satisfied
     */
    List<BatchAllocation> pick(List<Batch> availableBatches, int requestedQuantity, LocalDate asOfDate);
}
