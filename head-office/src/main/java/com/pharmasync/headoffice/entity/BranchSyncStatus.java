package com.pharmasync.headoffice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

/**
 * Tracks the synchronization progress and metadata for each branch.
 */
@Entity
@Table(name = "branch_sync_status")
public class BranchSyncStatus {

    @Id
    @Column(name = "branch_id", nullable = false, length = 64)
    private String branchId;

    @Column(name = "last_acked_logical_clock", nullable = false)
    private long lastAckedLogicalClock;

    @Column(name = "last_sync_time", nullable = false)
    private Instant lastSyncTime;

    @Column(name = "total_operations_processed", nullable = false)
    private long totalOperationsProcessed;

    public BranchSyncStatus() {
    }

    public BranchSyncStatus(String branchId,
                            long lastAckedLogicalClock,
                            Instant lastSyncTime,
                            long totalOperationsProcessed) {
        this.branchId = Objects.requireNonNull(branchId, "branchId must not be null");
        this.lastAckedLogicalClock = lastAckedLogicalClock;
        this.lastSyncTime = lastSyncTime != null ? lastSyncTime : Instant.now();
        this.totalOperationsProcessed = totalOperationsProcessed;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public long getLastAckedLogicalClock() {
        return lastAckedLogicalClock;
    }

    public void setLastAckedLogicalClock(long lastAckedLogicalClock) {
        this.lastAckedLogicalClock = lastAckedLogicalClock;
    }

    public Instant getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Instant lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public long getTotalOperationsProcessed() {
        return totalOperationsProcessed;
    }

    public void setTotalOperationsProcessed(long totalOperationsProcessed) {
        this.totalOperationsProcessed = totalOperationsProcessed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BranchSyncStatus that = (BranchSyncStatus) o;
        return Objects.equals(branchId, that.branchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branchId);
    }
}
