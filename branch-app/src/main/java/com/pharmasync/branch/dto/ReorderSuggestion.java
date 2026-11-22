package com.pharmasync.branch.dto;

import java.math.BigDecimal;

/**
 * Suggested reorder quantity for a medicine at a branch, based on 30-day sales
 * velocity, supplier lead time and safety stock.
 */
public record ReorderSuggestion(
        String medicineId,
        String medicineName,
        int quantityOnHand,
        double velocityPerDay,
        int leadTimeDays,
        int safetyStock,
        int suggestedOrderQuantity
) {
    public ReorderSuggestion {
        if (suggestedOrderQuantity < 0) {
            suggestedOrderQuantity = 0;
        }
    }
}
