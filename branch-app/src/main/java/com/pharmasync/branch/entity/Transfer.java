package com.pharmasync.branch.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Branch-to-branch transfer. Moves through DISPATCHED -> IN_TRANSIT -> RECEIVED.
 * A quantity difference at receipt is flagged.
 */
@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @Column(nullable = false, length = 64)
    private String id;

    @Column(nullable = false, length = 64)
    private String sourceBranchId;

    @Column(nullable = false, length = 64)
    private String destBranchId;

    @Column(nullable = false, length = 64)
    private String batchId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TransferState state = TransferState.DISPATCHED;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant receivedAt;

    @Column
    private int receivedQuantity;

    @Column(nullable = false)
    private boolean quantityDifference;

    public Transfer() {
    }

    public Transfer(String id, String sourceBranchId, String destBranchId,
                    String batchId, int quantity, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Transfer ID must not be null");
        this.sourceBranchId = Objects.requireNonNull(sourceBranchId, "Source branch must not be null");
        this.destBranchId = Objects.requireNonNull(destBranchId, "Dest branch must not be null");
        this.batchId = Objects.requireNonNull(batchId, "Batch ID must not be null");
        this.quantity = quantity;
        this.state = TransferState.DISPATCHED;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.receivedQuantity = 0;
        this.quantityDifference = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSourceBranchId() { return sourceBranchId; }
    public void setSourceBranchId(String sourceBranchId) { this.sourceBranchId = sourceBranchId; }
    public String getDestBranchId() { return destBranchId; }
    public void setDestBranchId(String destBranchId) { this.destBranchId = destBranchId; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public TransferState getState() { return state; }
    public void setState(TransferState state) { this.state = state; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
    public int getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(int receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    public boolean isQuantityDifference() { return quantityDifference; }
    public void setQuantityDifference(boolean quantityDifference) { this.quantityDifference = quantityDifference; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer transfer = (Transfer) o;
        return Objects.equals(id, transfer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
