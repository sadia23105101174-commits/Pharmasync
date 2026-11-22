package com.pharmasync.branch.dto;

import com.pharmasync.branch.entity.SaleLine;

import java.math.BigDecimal;

public class SaleLineResponse {
    private String id;
    private String medicineId;
    private String medicineName;
    private String batchId;
    private String batchNumber;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public SaleLineResponse() {
    }

    public static SaleLineResponse fromEntity(SaleLine line) {
        SaleLineResponse res = new SaleLineResponse();
        res.id = line.getId();
        if (line.getMedicine() != null) {
            res.medicineId = line.getMedicine().getId();
            res.medicineName = line.getMedicine().getBrandName();
        }
        if (line.getBatch() != null) {
            res.batchId = line.getBatch().getId();
            res.batchNumber = line.getBatch().getBatchNumber();
        }
        res.quantity = line.getQuantity();
        res.unitPrice = line.getUnitPrice();
        res.subtotal = line.getSubtotal();
        return res;
    }

    public String getId() {
        return id;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
