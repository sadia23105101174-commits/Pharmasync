package com.pharmasync.headoffice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * A batch recall issued by head-office. The recall is pushed to branches and
 * the central stock report shows quantity on hand per branch.
 */
@Entity
@Table(name = "ho_recalls")
public class Recall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String batchNumber;

    @Column(nullable = false, length = 64)
    private String medicineId;

    @Column(nullable = false)
    private Instant recallTime;

    @Column(nullable = false, length = 256)
    private String reason;

    public Recall() {
    }

    public Recall(String batchNumber, String medicineId, Instant recallTime, String reason) {
        this.batchNumber = Objects.requireNonNull(batchNumber, "batchNumber must not be null");
        this.medicineId = Objects.requireNonNull(medicineId, "medicineId must not be null");
        this.recallTime = recallTime != null ? recallTime : Instant.now();
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }
    public Instant getRecallTime() { return recallTime; }
    public void setRecallTime(Instant recallTime) { this.recallTime = recallTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recall recall = (Recall) o;
        return Objects.equals(id, recall.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
