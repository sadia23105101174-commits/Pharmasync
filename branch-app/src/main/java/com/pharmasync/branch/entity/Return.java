package com.pharmasync.branch.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "returns")
public class Return {

    @Id
    @Column(nullable = false, length = 64)
    private String id;

    @Column(nullable = false)
    private Instant returnTime;

    @Column(length = 64)
    private String saleId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    public Return() {
    }

    public Return(String id,
                  Instant returnTime,
                  String saleId,
                  Medicine medicine,
                  Batch batch,
                  int quantity,
                  String reason,
                  BigDecimal refundAmount) {
        this.id = Objects.requireNonNull(id, "Return ID must not be null");
        this.returnTime = Objects.requireNonNull(returnTime, "Return time must not be null");
        this.saleId = saleId;
        this.medicine = Objects.requireNonNull(medicine, "Medicine must not be null");
        this.batch = Objects.requireNonNull(batch, "Batch must not be null");
        this.quantity = quantity;
        this.reason = Objects.requireNonNull(reason, "Reason must not be null");
        this.refundAmount = Objects.requireNonNull(refundAmount, "Refund amount must not be null");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(Instant returnTime) {
        this.returnTime = returnTime;
    }

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
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

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Return aReturn = (Return) o;
        return Objects.equals(id, aReturn.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
