package com.pharmasync.domain.strategy;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Result of picking quantity from a specific batch.
 */
public record BatchAllocation(
        String batchId,
        String batchNumber,
        int allocatedQuantity,
        LocalDate expiryDate
) {
    public BatchAllocation {
        Objects.requireNonNull(batchId, "batchId must not be null");
        Objects.requireNonNull(batchNumber, "batchNumber must not be null");
        Objects.requireNonNull(expiryDate, "expiryDate must not be null");
        if (allocatedQuantity <= 0) {
            throw new IllegalArgumentException("Allocated quantity must be positive: " + allocatedQuantity);
        }
    }
}
