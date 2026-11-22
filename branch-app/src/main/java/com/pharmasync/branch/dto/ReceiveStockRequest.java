package com.pharmasync.branch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ReceiveStockRequest {

    @NotBlank(message = "Medicine ID must not be blank")
    private String medicineId;

    @NotBlank(message = "Batch number must not be blank")
    private String batchNumber;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotNull(message = "Expiry date must not be null")
    private LocalDate expiryDate;

    private LocalDate receivedDate;

    public ReceiveStockRequest() {
    }

    public ReceiveStockRequest(String medicineId, String batchNumber, int quantity, LocalDate expiryDate, LocalDate receivedDate) {
        this.medicineId = medicineId;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.receivedDate = receivedDate != null ? receivedDate : LocalDate.now();
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }
}
