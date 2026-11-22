package com.pharmasync.branch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class StockCountRequest {

    @NotBlank(message = "Batch ID must not be blank")
    private String batchId;

    @Min(value = 0, message = "Counted quantity cannot be negative")
    private int countedQuantity;

    private String notes;

    public StockCountRequest() {
    }

    public StockCountRequest(String batchId, int countedQuantity, String notes) {
        this.batchId = batchId;
        this.countedQuantity = countedQuantity;
        this.notes = notes;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public int getCountedQuantity() {
        return countedQuantity;
    }

    public void setCountedQuantity(int countedQuantity) {
        this.countedQuantity = countedQuantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
