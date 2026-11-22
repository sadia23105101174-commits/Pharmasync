package com.pharmasync.branch.service;

import com.pharmasync.branch.dto.StockCountRequest;
import com.pharmasync.branch.entity.Batch;
import com.pharmasync.branch.entity.StockCount;
import com.pharmasync.branch.exception.ResourceNotFoundException;
import com.pharmasync.branch.repository.BatchRepository;
import com.pharmasync.branch.repository.StockCountRepository;
import com.pharmasync.domain.operation.OperationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class StockCountService {

    private final BatchRepository batchRepository;
    private final StockCountRepository stockCountRepository;
    private final OperationRecordingService operationRecordingService;

    public StockCountService(BatchRepository batchRepository,
                             StockCountRepository stockCountRepository,
                             OperationRecordingService operationRecordingService) {
        this.batchRepository = batchRepository;
        this.stockCountRepository = stockCountRepository;
        this.operationRecordingService = operationRecordingService;
    }

    @Transactional
    public StockCount recordStockCount(StockCountRequest request) {
        Objects.requireNonNull(request, "StockCountRequest must not be null");
        if (request.getCountedQuantity() < 0) {
            throw new IllegalArgumentException("Counted quantity cannot be negative, got: " + request.getCountedQuantity());
        }

        Batch batch = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + request.getBatchId()));

        int expectedQuantity = batch.getQuantityOnHand();
        int countedQuantity = request.getCountedQuantity();
        int difference = countedQuantity - expectedQuantity;

        // 1. Update batch quantity to reflect physical reality
        batch.setQuantityOnHand(countedQuantity);
        batchRepository.save(batch);

        // 2. Persist StockCount audit entity
        String countId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        StockCount stockCount = new StockCount(
                countId,
                now,
                batch,
                expectedQuantity,
                countedQuantity,
                difference,
                request.getNotes()
        );
        StockCount savedCount = stockCountRepository.save(stockCount);

        // 3. Record Operation in append-only log and pending outbox
        Map<String, Object> payload = Map.of(
                "countId", countId,
                "batchId", batch.getId(),
                "batchNumber", batch.getBatchNumber(),
                "medicineId", batch.getMedicine().getId(),
                "expectedQuantity", expectedQuantity,
                "countedQuantity", countedQuantity,
                "difference", difference,
                "notes", request.getNotes() != null ? request.getNotes() : "",
                "timestamp", now.toString()
        );
        operationRecordingService.recordOperation(OperationType.STOCK_COUNT, payload);

        return savedCount;
    }
}
