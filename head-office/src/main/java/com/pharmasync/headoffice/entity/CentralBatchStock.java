package com.pharmasync.headoffice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Head-office central inventory record tracking stock quantity per branch, product, and batch.
 */
@Entity
@Table(name = "central_batch_stock", uniqueConstraints = {
        @UniqueConstraint(name = "uk_central_stock_branch_med_batch", columnNames = {"branch_id", "medicine_id", "batch_number"})
})
public class CentralBatchStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id", nullable = false, length = 64)
    private String branchId;

    @Column(name = "medicine_id", nullable = false, length = 64)
    private String medicineId;

    @Column(name = "medicine_name", nullable = false)
    private String medicineName;

    @Column(name = "batch_number", nullable = false, length = 64)
    private String batchNumber;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "quantity_on_hand", nullable = false)
    private int quantityOnHand;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    public CentralBatchStock() {
    }

    public CentralBatchStock(String branchId,
                             String medicineId,
                             String medicineName,
                             String batchNumber,
                             LocalDate expiryDate,
                             int quantityOnHand,
                             Instant lastUpdated) {
        this.branchId = Objects.requireNonNull(branchId, "branchId must not be null");
        this.medicineId = Objects.requireNonNull(medicineId, "medicineId must not be null");
        this.medicineName = medicineName != null ? medicineName : "Unknown Medicine";
        this.batchNumber = Objects.requireNonNull(batchNumber, "batchNumber must not be null");
        this.expiryDate = expiryDate != null ? expiryDate : LocalDate.now().plusYears(1);
        this.quantityOnHand = quantityOnHand;
        this.lastUpdated = lastUpdated != null ? lastUpdated : Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(int quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CentralBatchStock that = (CentralBatchStock) o;
        return Objects.equals(branchId, that.branchId) &&
                Objects.equals(medicineId, that.medicineId) &&
                Objects.equals(batchNumber, that.batchNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branchId, medicineId, batchNumber);
    }
}
