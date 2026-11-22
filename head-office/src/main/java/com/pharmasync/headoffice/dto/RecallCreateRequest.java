package com.pharmasync.headoffice.dto;

import java.time.Instant;
import java.util.Objects;

public record RecallCreateRequest(
        String batchNumber,
        String medicineId,
        Instant recallTime,
        String reason
) {
    public RecallCreateRequest {
        Objects.requireNonNull(batchNumber, "batchNumber must not be null");
        Objects.requireNonNull(medicineId, "medicineId must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
    }
}
