package com.pharmasync.branch.entity;

import com.pharmasync.domain.batch.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "batches")
public class Batch {

    @Id
    @Column(nullable = false, length = 64)
    private String id;

    @Column(nullable = false, length = 64)
    private String batchNumber;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private int quantityOnHand;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private LocalDate receivedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status = BatchStatus.ACTIVE;

    @Column
    private String statusReason;

    public Batch() {
    }

    public Batch(String id,
                 String batchNumber,
                 Medicine medicine,
                 int quantityOnHand,
                 LocalDate expiryDate,
                 LocalDate receivedDate,
                 BatchStatus status,
                 String statusReason) {
        this.id = Objects.requireNonNull(id, "Batch ID must not be null");
        this.batchNumber = Objects.requireNonNull(batchNumber, "Batch number must not be null");
        this.medicine = Objects.requireNonNull(medicine, "Medicine must not be null");
        this.quantityOnHand = quantityOnHand;
        this.expiryDate = Objects.requireNonNull(expiryDate, "Expiry date must not be null");
        this.receivedDate = Objects.requireNonNull(receivedDate, "Received date must not be null");
        this.status = status != null ? status : BatchStatus.ACTIVE;
        this.statusReason = statusReason;
    }

    public com.pharmasync.domain.batch.Batch toDomain() {
        BatchState state = switch (this.status) {
            case ACTIVE -> new ActiveBatchState();
            case QUARANTINED -> new QuarantinedBatchState();
            case RECALLED -> new RecalledBatchState();
            case EXPIRED -> new ExpiredBatchState();
        };

        return new com.pharmasync.domain.batch.Batch(
                this.id,
                this.batchNumber,
                this.medicine.toDomain(),
                this.quantityOnHand,
                this.expiryDate,
                this.receivedDate,
                state,
                this.statusReason
        );
    }

    public boolean isExpired(LocalDate asOfDate) {
        return asOfDate != null && asOfDate.isAfter(expiryDate);
    }

    public boolean canSell(LocalDate asOfDate) {
        if (status != BatchStatus.ACTIVE) {
            return false;
        }
        if (isExpired(asOfDate)) {
            return false;
        }
        return quantityOnHand > 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(int quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
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

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Batch batch = (Batch) o;
        return Objects.equals(id, batch.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
