package com.pharmasync.branch.adapter.printer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class Receipt {
    private final String receiptNumber;
    private final String branchId;
    private final Instant timestamp;
    private final String patientName;
    private final String prescriberRegNo;
    private final LocalDate prescriptionDate;
    private final List<ReceiptItem> items;
    private final BigDecimal totalAmount;

    public Receipt(String receiptNumber,
                   String branchId,
                   Instant timestamp,
                   String patientName,
                   String prescriberRegNo,
                   LocalDate prescriptionDate,
                   List<ReceiptItem> items,
                   BigDecimal totalAmount) {
        this.receiptNumber = receiptNumber;
        this.branchId = branchId;
        this.timestamp = timestamp;
        this.patientName = patientName;
        this.prescriberRegNo = prescriberRegNo;
        this.prescriptionDate = prescriptionDate;
        this.items = items != null ? items : Collections.emptyList();
        this.totalAmount = totalAmount;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public String getBranchId() {
        return branchId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPrescriberRegNo() {
        return prescriberRegNo;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public List<ReceiptItem> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
