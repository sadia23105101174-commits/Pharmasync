package com.pharmasync.domain.sync;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Request payload containing a batch of operations to synchronize to head-office.
 */
public record SyncBatchRequest(
        List<SyncOperationDto> operations
) {
    public SyncBatchRequest {
        if (operations == null) {
            operations = Collections.emptyList();
        } else {
            operations = List.copyOf(operations);
        }
    }
}
