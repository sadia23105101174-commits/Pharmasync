package com.pharmasync.branch.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class ReturnRequest {

    @NotBlank(message = "Medicine ID must not be blank")
    private String medicineId;

    @NotBlank(message = "Batch ID must not be blank")
    private String batchId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotBlank(message = "Return reason must not be blank")
    private String reason;

    private String saleId;

    @DecimalMin(value = "0.00", message = "Refund amount must not be negative")
    private BigDecimal refundAmount;

    public ReturnRequest() {
    }

    public ReturnRequest(String medicineId, String batchId, int quantity, String reason, String saleId, BigDecimal refundAmount) {
        this.medicineId = medicineId;
        this.batchId = batchId;
        this.quantity = quantity;
        this.reason = reason;
        this.saleId = saleId;
        this.refundAmount = refundAmount;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
}
