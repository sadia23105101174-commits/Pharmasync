package com.pharmasync.branch.entity;

import com.pharmasync.domain.operation.Operation;
import com.pharmasync.domain.operation.OperationType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Outbox entity holding operations waiting to be synchronised to head-office.
 * Status values: "pending" or "acked".
 */
@Entity
@Table(name = "outbox")
public class Outbox {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACKED = "acked";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_id", nullable = false, unique = true)
    private UUID operationId;

    @Column(name = "lamport_clock", nullable = false)
    private long lamportClock;

    @Column(name = "branch_id", nullable = false, length = 64)
    private String branchId;

    @Column(nullable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OperationType type;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false, length = 32)
    private String status = STATUS_PENDING;

    public Outbox() {
    }

    public Outbox(UUID operationId,
                  long lamportClock,
                  String branchId,
                  Instant timestamp,
                  OperationType type,
                  String payload,
                  String status) {
        this.operationId = Objects.requireNonNull(operationId, "operationId must not be null");
        this.lamportClock = lamportClock;
        this.branchId = Objects.requireNonNull(branchId, "branchId must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.payload = payload != null ? payload : "{}";
        this.status = status != null ? status : STATUS_PENDING;
    }

    public static Outbox fromDomain(Operation op) {
        return new Outbox(
                op.getOperationId(),
                op.getLamportClock(),
                op.getBranchId(),
                op.getTimestamp(),
                op.getType(),
                op.getPayload(),
                STATUS_PENDING
        );
    }

    public void markAcked() {
        this.status = STATUS_ACKED;
    }

    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(this.status);
    }

    public Long getId() {
        return id;
    }

    public UUID getOperationId() {
        return operationId;
    }

    public void setOperationId(UUID operationId) {
        this.operationId = operationId;
    }

    public long getLamportClock() {
        return lamportClock;
    }

    public void setLamportClock(long lamportClock) {
        this.lamportClock = lamportClock;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Outbox outbox = (Outbox) o;
        return Objects.equals(operationId, outbox.operationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId);
    }
}
