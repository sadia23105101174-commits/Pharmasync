package com.pharmasync.branch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmasync.branch.entity.OperationLog;
import com.pharmasync.branch.entity.Outbox;
import com.pharmasync.branch.repository.OperationLogRepository;
import com.pharmasync.branch.repository.OutboxRepository;
import com.pharmasync.domain.operation.LamportClock;
import com.pharmasync.domain.operation.Operation;
import com.pharmasync.domain.operation.OperationStatus;
import com.pharmasync.domain.operation.OperationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Service responsible for appending every state change to the operation_log
 * and creating a corresponding pending row in the outbox table.
 * Implements the Command pattern record store with causal Lamport logical ordering.
 */
@Service
public class OperationRecordingService {

    private final OperationLogRepository operationLogRepository;
    private final OutboxRepository outboxRepository;
    private final LamportClock lamportClock;
    private final ObjectMapper objectMapper;
    private final String branchId;

    public OperationRecordingService(OperationLogRepository operationLogRepository,
                                     OutboxRepository outboxRepository,
                                     LamportClock lamportClock,
                                     ObjectMapper objectMapper,
                                     @Value("${branch.id:BRANCH-001}") String branchId) {
        this.operationLogRepository = operationLogRepository;
        this.outboxRepository = outboxRepository;
        this.lamportClock = lamportClock;
        this.objectMapper = objectMapper;
        this.branchId = branchId;
    }

    @Transactional
    public Operation recordOperation(OperationType type, Object payloadData) {
        Objects.requireNonNull(type, "OperationType must not be null");

        String payloadJson = "{}";
        if (payloadData != null) {
            try {
                payloadJson = objectMapper.writeValueAsString(payloadData);
            } catch (JsonProcessingException e) {
                payloadJson = String.valueOf(payloadData);
            }
        }

        UUID operationId = UUID.randomUUID();
        long logicalClock = lamportClock.tick();
        Instant now = Instant.now();

        Operation domainOp = new Operation(
                operationId,
                logicalClock,
                branchId,
                now,
                type,
                payloadJson,
                OperationStatus.APPLIED
        );

        // 1. Append to operation_log (append-only)
        OperationLog logEntry = new OperationLog(
                operationId,
                logicalClock,
                branchId,
                now,
                type,
                payloadJson,
                OperationStatus.APPLIED
        );
        operationLogRepository.save(logEntry);

        // 2. Insert into outbox (status = "pending")
        Outbox outboxEntry = new Outbox(
                operationId,
                logicalClock,
                branchId,
                now,
                type,
                payloadJson,
                Outbox.STATUS_PENDING
        );
        outboxRepository.save(outboxEntry);

        return domainOp;
    }

    public LamportClock getLamportClock() {
        return lamportClock;
    }

    public String getBranchId() {
        return branchId;
    }
}
