package com.pharmasync.domain.sync;

import java.time.Instant;
import java.util.Objects;

/**
 * Summary record of synchronization progress for a given branch.
 */
public record SyncStatusDto(
        String branchId,
        long lastAckedLogicalClock,
        Instant lastSyncTime,
        long totalOperationsProcessed
) {
    public SyncStatusDto {
        Objects.requireNonNull(branchId, "branchId must not be null");
    }
}
