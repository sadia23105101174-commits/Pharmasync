package com.pharmasync.branch.dto;

import com.pharmasync.branch.entity.OperationLog;
import com.pharmasync.domain.operation.OperationStatus;
import com.pharmasync.domain.operation.OperationType;

import java.time.Instant;
import java.util.UUID;

public class OperationLogResponse {
    private Long id;
    private UUID operationId;
    private long lamportClock;
    private String branchId;
    private Instant timestamp;
    private OperationType type;
    private String payload;
    private OperationStatus status;

    public OperationLogResponse() {
    }

    public static OperationLogResponse fromEntity(OperationLog op) {
        OperationLogResponse res = new OperationLogResponse();
        res.id = op.getId();
        res.operationId = op.getOperationId();
        res.lamportClock = op.getLamportClock();
        res.branchId = op.getBranchId();
        res.timestamp = op.getTimestamp();
        res.type = op.getType();
        res.payload = op.getPayload();
        res.status = op.getStatus();
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

    public OperationStatus getStatus() {
        return status;
    }
}
