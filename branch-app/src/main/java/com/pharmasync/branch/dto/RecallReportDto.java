package com.pharmasync.branch.dto;

import java.time.Instant;

/**
 * Recall report for a batch: quantity on hand and units sold after the recall
 * time (flagged post-recall sales).
 */
public record RecallReportDto(
        String batchId,
        String batchNumber,
        Instant recallTime,
        int quantityOnHand,
        int unitsSoldAfterRecall
) {
}
