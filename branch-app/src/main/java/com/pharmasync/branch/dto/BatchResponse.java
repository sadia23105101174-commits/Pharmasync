package com.pharmasync.branch.dto;

import com.pharmasync.branch.entity.Batch;
import com.pharmasync.domain.batch.BatchStatus;

import java.time.LocalDate;

public class BatchResponse {
    private String id;
    private String batchNumber;
    private String medicineId;
    private String medicineBrandName;
    private int quantityOnHand;
    private LocalDate expiryDate;
    private LocalDate receivedDate;
    private BatchStatus status;
    private String statusReason;
    private boolean sellable;

    public BatchResponse() {
    }

    public static BatchResponse fromEntity(Batch b) {
        BatchResponse res = new BatchResponse();
        res.id = b.getId();
        res.batchNumber = b.getBatchNumber();
        if (b.getMedicine() != null) {
            res.medicineId = b.getMedicine().getId();
            res.medicineBrandName = b.getMedicine().getBrandName();
        }
        res.quantityOnHand = b.getQuantityOnHand();
        res.expiryDate = b.getExpiryDate();
        res.receivedDate = b.getReceivedDate();
        res.status = b.getStatus();
        res.statusReason = b.getStatusReason();
        res.sellable = b.canSell(LocalDate.now());
        return res;
    }

    public String getId() {
        return id;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public String getMedicineBrandName() {
        return medicineBrandName;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public boolean isSellable() {
        return sellable;
    }
}
