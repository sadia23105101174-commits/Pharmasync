package com.pharmasync.branch.dto;

import com.pharmasync.branch.entity.StockCount;

import java.time.Instant;

public class StockCountResponse {
    private String id;
    private Instant countTime;
    private String batchId;
    private String batchNumber;
    private String medicineName;
    private int expectedQuantity;
    private int countedQuantity;
    private int difference;
    private String notes;

    public StockCountResponse() {
    }

    public static StockCountResponse fromEntity(StockCount sc) {
        StockCountResponse res = new StockCountResponse();
        res.id = sc.getId();
        res.countTime = sc.getCountTime();
        if (sc.getBatch() != null) {
            res.batchId = sc.getBatch().getId();
            res.batchNumber = sc.getBatch().getBatchNumber();
            if (sc.getBatch().getMedicine() != null) {
                res.medicineName = sc.getBatch().getMedicine().getBrandName();
            }
        }
        res.expectedQuantity = sc.getExpectedQuantity();
        res.countedQuantity = sc.getCountedQuantity();
        res.difference = sc.getDifference();
        res.notes = sc.getNotes();
        return res;
    }

    public String getId() {
        return id;
    }

    public Instant getCountTime() {
        return countTime;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public int getExpectedQuantity() {
        return expectedQuantity;
    }

    public int getCountedQuantity() {
        return countedQuantity;
    }

    public int getDifference() {
        return difference;
    }

    public String getNotes() {
        return notes;
    }
}
