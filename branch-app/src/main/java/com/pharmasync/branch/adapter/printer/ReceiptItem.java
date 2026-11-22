package com.pharmasync.branch.adapter.printer;

import java.math.BigDecimal;

public class ReceiptItem {
    private final String medicineName;
    private final String batchNumber;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;

    public ReceiptItem(String medicineName, String batchNumber, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.medicineName = medicineName;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public String getMedicineName() {
        return medicineName;
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
