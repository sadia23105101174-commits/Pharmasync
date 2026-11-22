package com.pharmasync.branch.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Prescription register entry: a sale of a prescription-only medicine.
 */
public record PrescriptionSaleDto(
        String saleId,
        String receiptNumber,
        Instant saleTime,
        String patientName,
        String prescriberRegNo,
        LocalDate prescriptionDate,
        String medicineName,
        String batchNumber,
        int quantity,
        BigDecimal subtotal
) {
}
