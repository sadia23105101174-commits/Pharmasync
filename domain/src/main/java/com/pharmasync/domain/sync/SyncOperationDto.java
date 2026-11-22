package com.pharmasync.domain.sync;

import com.pharmasync.domain.operation.OperationType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Data transfer record representing an operation sent in a sync batch.
 */
public record SyncOperationDto(
        UUID operationId,
        long lamportClock,
        String branchId,
        Instant timestamp,
        OperationType type,
        String payload
) {
    public SyncOperationDto {
        Objects.requireNonNull(operationId, "operationId must not be null");
        Objects.requireNonNull(branchId, "branchId must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (payload == null) {
            payload = "{}";
        }
    }
}
