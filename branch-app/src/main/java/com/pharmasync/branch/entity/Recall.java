package com.pharmasync.branch.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * A batch recall applied at this branch. Once applied the batch status becomes
 * RECALLED and sales of the batch are blocked. Sales made after the recall time
 * but before the branch learned of the recall are flagged.
 */
@Entity
@Table(name = "recalls")
public class Recall {

    @Id
    @Column(nullable = false, length = 64)
    private String id;

    @Column(nullable = false, length = 64)
    private String batchId;

    @Column(nullable = false)
    private Instant recallTime;

    @Column(nullable = false, length = 256)
    private String reason;

    @Column(nullable = false)
    private boolean applied = false;

    public Recall() {
    }

    public Recall(String id, String batchId, Instant recallTime, String reason, boolean applied) {
        this.id = Objects.requireNonNull(id, "Recall ID must not be null");
        this.batchId = Objects.requireNonNull(batchId, "Batch ID must not be null");
        this.recallTime = Objects.requireNonNull(recallTime, "Recall time must not be null");
        this.reason = Objects.requireNonNull(reason, "Reason must not be null");
        this.applied = applied;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public Instant getRecallTime() { return recallTime; }
    public void setRecallTime(Instant recallTime) { this.recallTime = recallTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public boolean isApplied() { return applied; }
    public void setApplied(boolean applied) { this.applied = applied; }

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
