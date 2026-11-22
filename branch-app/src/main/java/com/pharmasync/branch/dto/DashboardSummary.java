package com.pharmasync.branch.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Head-office / branch dashboard summary: stock by branch and batch, last sync
 * time, pending operations, and value of stock expiring within 90 days.
 */
public record DashboardSummary(
        List<StockLine> stockByBranchBatch,
        Instant lastSyncTime,
        long pendingOperations,
        BigDecimal valueExpiringWithin90Days
) {
    public record StockLine(String branchId, String medicineId, String medicineName,
                            String batchNumber, LocalDate expiryDate, int quantityOnHand,
                            BigDecimal unitPrice, BigDecimal lineValue) {
    }
}
