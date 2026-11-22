package com.pharmasync.branch.dto;

import com.pharmasync.branch.entity.Outbox;
import com.pharmasync.domain.operation.OperationType;

import java.time.Instant;
import java.util.UUID;

public class OutboxResponse {
    private Long id;
    private UUID operationId;
    private long lamportClock;
    private String branchId;
    private Instant timestamp;
    private OperationType type;
    private String payload;
    private String status;

    public OutboxResponse() {
    }

    public static OutboxResponse fromEntity(Outbox outbox) {
        OutboxResponse res = new OutboxResponse();
        res.id = outbox.getId();
        res.operationId = outbox.getOperationId();
        res.lamportClock = outbox.getLamportClock();
        res.branchId = outbox.getBranchId();
        res.timestamp = outbox.getTimestamp();
        res.type = outbox.getType();
        res.payload = outbox.getPayload();
        res.status = outbox.getStatus();
        return res;
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

    public String getStatus() {
        return status;
    }
}
