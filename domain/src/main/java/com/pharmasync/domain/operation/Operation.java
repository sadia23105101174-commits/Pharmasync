package com.pharmasync.domain.operation;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Fundamental unit of change in PharmaSync's offline-first architecture.
 * Implements the Command pattern state: recorded in an append-only log and outbox,
 * uniquely identified by a UUID for idempotency, and ordered causally by a Lamport clock.
 */
public class Operation {
    private final UUID operationId;
    private final long lamportClock;
    private final String branchId;
    private final Instant timestamp;
    private final OperationType type;
    private final String payload;
    private OperationStatus status;

    public Operation(UUID operationId,
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
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    /**
     * Factory to create a new local operation.
     */
    public static Operation create(UUID operationId,
                                  long lamportClock,
                                  String branchId,
                                  OperationType type,
                                  String payload) {
        return new Operation(
                operationId,
                lamportClock,
                branchId,
                Instant.now(),
                type,
                payload,
                OperationStatus.PENDING
        );
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
        this.status = Objects.requireNonNull(status, "Status must not be null");
    }

    public void markApplied() {
        this.status = OperationStatus.APPLIED;
    }

    public void markAcknowledged() {
        this.status = OperationStatus.ACKNOWLEDGED;
    }

    public void markFailed() {
        this.status = OperationStatus.FAILED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return Objects.equals(operationId, operation.operationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId);
    }

    @Override
    public String toString() {
        return "Operation{" +
                "id=" + operationId +
                ", clock=" + lamportClock +
                ", branch='" + branchId + '\'' +
                ", type=" + type +
                ", status=" + status +
                '}';
    }
}
