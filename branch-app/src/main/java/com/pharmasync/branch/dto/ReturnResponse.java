package com.pharmasync.branch.dto;

import com.pharmasync.branch.entity.Return;

import java.math.BigDecimal;
import java.time.Instant;

public class ReturnResponse {
    private String id;
    private Instant returnTime;
    private String saleId;
    private String medicineId;
    private String medicineName;
    private String batchId;
    private String batchNumber;
    private int quantity;
    private String reason;
    private BigDecimal refundAmount;

    public ReturnResponse() {
    }

    public static ReturnResponse fromEntity(Return r) {
        ReturnResponse res = new ReturnResponse();
        res.id = r.getId();
        res.returnTime = r.getReturnTime();
        res.saleId = r.getSaleId();
        if (r.getMedicine() != null) {
            res.medicineId = r.getMedicine().getId();
            res.medicineName = r.getMedicine().getBrandName();
        }
        if (r.getBatch() != null) {
            res.batchId = r.getBatch().getId();
            res.batchNumber = r.getBatch().getBatchNumber();
        }
        res.quantity = r.getQuantity();
        res.reason = r.getReason();
        res.refundAmount = r.getRefundAmount();
        return res;
    }

    public String getId() {
        return id;
    }

    public Instant getReturnTime() {
        return returnTime;
    }

    public String getSaleId() {
        return saleId;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getReason() {
        return reason;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
}
