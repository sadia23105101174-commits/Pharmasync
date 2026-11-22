package com.pharmasync.domain.sync;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Response payload acknowledging received and applied operations.
 */
public record SyncBatchResponse(
        List<UUID> acknowledgedIds,
        int acknowledgedCount,
        Instant processedAt
) {
    public SyncBatchResponse {
        if (acknowledgedIds == null) {
            acknowledgedIds = Collections.emptyList();
        } else {
            acknowledgedIds = List.copyOf(acknowledgedIds);
        }
        if (processedAt == null) {
            processedAt = Instant.now();
        }
    }

    public static SyncBatchResponse of(List<UUID> acknowledgedIds) {
        List<UUID> safeList = acknowledgedIds != null ? List.copyOf(acknowledgedIds) : Collections.emptyList();
        return new SyncBatchResponse(safeList, safeList.size(), Instant.now());
    }
}
