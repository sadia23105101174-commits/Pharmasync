package com.pharmasync.headoffice.entity;

import com.pharmasync.domain.operation.OperationType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Idempotency registry recording every operation successfully applied at head-office.
 */
@Entity
@Table(name = "processed_operations", indexes = {
        @Index(name = "idx_processed_op_id", columnList = "operation_id", unique = true)
})
public class ProcessedOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_id", nullable = false, unique = true, updatable = false)
    private UUID operationId;

    @Column(name = "branch_id", nullable = false, length = 64)
    private String branchId;

    @Column(name = "lamport_clock", nullable = false)
    private long lamportClock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OperationType type;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public ProcessedOperation() {
    }

    public ProcessedOperation(UUID operationId,
                              String branchId,
                              long lamportClock,
                              OperationType type,
                              Instant processedAt) {
        this.operationId = Objects.requireNonNull(operationId, "operationId must not be null");
        this.branchId = Objects.requireNonNull(branchId, "branchId must not be null");
        this.lamportClock = lamportClock;
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.processedAt = processedAt != null ? processedAt : Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UUID getOperationId() {
        return operationId;
    }

    public String getBranchId() {
        return branchId;
    }

    public long getLamportClock() {
        return lamportClock;
    }

    public OperationType getType() {
        return type;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessedOperation that = (ProcessedOperation) o;
        return Objects.equals(operationId, that.operationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId);
    }
}
