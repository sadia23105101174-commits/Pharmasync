package com.pharmasync.branch.controller;

import com.pharmasync.branch.dto.OperationLogResponse;
import com.pharmasync.branch.dto.OutboxResponse;
import com.pharmasync.branch.entity.Outbox;
import com.pharmasync.branch.exception.ResourceNotFoundException;
import com.pharmasync.branch.repository.OperationLogRepository;
import com.pharmasync.branch.repository.OutboxRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Operations & Outbox", description = "Query append-only operation log and outbox queue")
public class OperationApiController {

    private final OperationLogRepository operationLogRepository;
    private final OutboxRepository outboxRepository;

    public OperationApiController(OperationLogRepository operationLogRepository,
                                  OutboxRepository outboxRepository) {
        this.operationLogRepository = operationLogRepository;
        this.outboxRepository = outboxRepository;
    }

    @GetMapping("/operations")
    @Operation(summary = "List operations log", description = "Paginated list of historical operations stored in the append-only log")
    public ResponseEntity<Page<OperationLogResponse>> getOperations(Pageable pageable) {
        Page<OperationLogResponse> page = operationLogRepository.findAll(pageable)
                .map(OperationLogResponse::fromEntity);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/outbox")
    @Operation(summary = "List outbox operations", description = "Paginated list of outbox operations, optionally filtered by status (pending/acked)")
    public ResponseEntity<Page<OutboxResponse>> getOutbox(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<OutboxResponse> page;
        if (status != null && !status.trim().isEmpty()) {
            page = outboxRepository.findByStatus(status.trim().toLowerCase(), pageable)
                    .map(OutboxResponse::fromEntity);
        } else {
            page = outboxRepository.findAll(pageable)
                    .map(OutboxResponse::fromEntity);
        }
        return ResponseEntity.ok(page);
    }

    @PutMapping("/outbox/{id}/ack")
    @Operation(summary = "Acknowledge outbox entry", description = "Marks an outbox entry as acked after synchronization with head-office")
    public ResponseEntity<OutboxResponse> acknowledgeOutbox(@PathVariable Long id) {
        Outbox outbox = outboxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Outbox entry not found with ID: " + id));
        outbox.markAcked();
        Outbox saved = outboxRepository.save(outbox);
        return ResponseEntity.ok(OutboxResponse.fromEntity(saved));
    }
}
