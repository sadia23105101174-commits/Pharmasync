package com.pharmasync.branch.service;

import com.pharmasync.branch.entity.Batch;
import com.pharmasync.branch.entity.Recall;
import com.pharmasync.branch.repository.BatchRepository;
import com.pharmasync.branch.repository.OutboxRepository;
import com.pharmasync.branch.repository.RecallRepository;
import com.pharmasync.domain.operation.OperationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Applies a batch recall at this branch. Marks the batch RECALLED, persists the
 * recall record, and records a RECALL_APPLIED operation to the outbox so the
 * recall can be synchronized back to head-office.
 */
@Service
public class RecallService {

    private final RecallRepository recallRepository;
    private final BatchRepository batchRepository;
    private final OutboxRepository outboxRepository;
    private final OperationRecordingService operationRecordingService;

    public RecallService(RecallRepository recallRepository,
                         BatchRepository batchRepository,
                         OutboxRepository outboxRepository,
                         OperationRecordingService operationRecordingService) {
        this.recallRepository = recallRepository;
        this.batchRepository = batchRepository;
        this.outboxRepository = outboxRepository;
        this.operationRecordingService = operationRecordingService;
    }

    @Transactional
    public Recall applyRecall(String batchId, Instant recallTime, String reason) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));
        batch.setStatus(com.pharmasync.domain.batch.BatchStatus.RECALLED);
        batch.setStatusReason(reason);
        batchRepository.save(batch);

        Recall recall = new Recall(
                UUID.randomUUID().toString(), batchId,
                recallTime != null ? recallTime : Instant.now(), reason, true
        );
        recall = recallRepository.save(recall);

        operationRecordingService.recordOperation(
                OperationType.RECALL_APPLIED,
                java.util.Map.of("batchId", batchId, "reason", reason, "recallTime", recall.getRecallTime().toString())
        );
        return recall;
    }

    public boolean isSaleAfterRecall(String batchId, Instant saleTime) {
        return recallRepository.findByBatchIdAndRecallTimeBefore(batchId, saleTime).isPresent();
    }
}
