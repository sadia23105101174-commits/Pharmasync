package com.pharmasync.branch.entity;

import com.pharmasync.domain.operation.Operation;
import com.pharmasync.domain.operation.OperationStatus;
import com.pharmasync.domain.operation.OperationType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Append-only persistent entity for domain Operations.
 * Records the exact chronological and logical history of branch events.
 */
@Entity
@Table(name = "operation_log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_id", nullable = false, unique = true, updatable = false)
    private UUID operationId;

    @Column(name = "lamport_clock", nullable = false, updatable = false)
    private long lamportClock;

    @Column(name = "branch_id", nullable = false, updatable = false, length = 64)
    private String branchId;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 32)
    private OperationType type;

    @Lob
    @Column(nullable = false, updatable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OperationStatus status;

    public OperationLog() {
    }

    public OperationLog(UUID operationId,
                        long lamportClock,
                        String branchId,
                        Instant timestamp,
                        OperationType type,
                        String payload,
                        OperationStatus status) {
        this.operationId = Objects.requireNonNull(operationId, "operationId must not be null");
        this.lamportClock = lamportClock;
        this.branchId = Objects.requireNonNull(branchId, "branchId must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.payload = payload != null ? payload : "{}";
        this.status = status != null ? status : OperationStatus.PENDING;
    }

    public static OperationLog fromDomain(Operation op) {
        return new OperationLog(
                op.getOperationId(),
                op.getLamportClock(),
                op.getBranchId(),
                op.getTimestamp(),
                op.getType(),
                op.getPayload(),
                op.getStatus()
        );
    }

    public Operation toDomain() {
        return new Operation(
                this.operationId,
                this.lamportClock,
                this.branchId,
                this.timestamp,
                this.type,
                this.payload,
                this.status
        );
    }

    public Long getId() {
        return id;
    }

    public UUID getOperationId() {
        return operationId;
    }

    public long getLamportClock() {
        return lamportClock;
    }

    public String getBranchId() {
        return branchId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public OperationType getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationLog that = (OperationLog) o;
        return Objects.equals(operationId, that.operationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId);
    }
}
