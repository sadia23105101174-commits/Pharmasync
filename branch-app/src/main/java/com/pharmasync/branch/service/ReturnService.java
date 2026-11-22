package com.pharmasync.branch.service;

import com.pharmasync.branch.dto.ReturnRequest;
import com.pharmasync.branch.entity.Batch;
import com.pharmasync.branch.entity.Medicine;
import com.pharmasync.branch.entity.Return;
import com.pharmasync.branch.exception.ResourceNotFoundException;
import com.pharmasync.branch.repository.BatchRepository;
import com.pharmasync.branch.repository.MedicineRepository;
import com.pharmasync.branch.repository.ReturnRepository;
import com.pharmasync.domain.operation.OperationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class ReturnService {

    private final MedicineRepository medicineRepository;
    private final BatchRepository batchRepository;
    private final ReturnRepository returnRepository;
    private final OperationRecordingService operationRecordingService;

    public ReturnService(MedicineRepository medicineRepository,
                         BatchRepository batchRepository,
                         ReturnRepository returnRepository,
                         OperationRecordingService operationRecordingService) {
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.returnRepository = returnRepository;
        this.operationRecordingService = operationRecordingService;
    }

    @Transactional
    public Return processReturn(ReturnRequest request) {
        Objects.requireNonNull(request, "ReturnRequest must not be null");
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Return quantity must be at least 1, got: " + request.getQuantity());
        }

        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + request.getMedicineId()));

        Batch batch = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + request.getBatchId()));

        if (!batch.getMedicine().getId().equals(medicine.getId())) {
            throw new IllegalArgumentException("Batch " + batch.getBatchNumber() + " does not belong to medicine " + medicine.getBrandName());
        }

        // 1. Restock quantity into batch
        batch.setQuantityOnHand(batch.getQuantityOnHand() + request.getQuantity());
        batchRepository.save(batch);

        // 2. Compute refund
        BigDecimal refundAmount = request.getRefundAmount() != null
                ? request.getRefundAmount()
                : medicine.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // 3. Persist Return record
        String returnId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Return returnEntity = new Return(
                returnId,
                now,
                request.getSaleId(),
                medicine,
                batch,
                request.getQuantity(),
                request.getReason(),
                refundAmount
        );
        Return savedReturn = returnRepository.save(returnEntity);

        // 4. Record domain Operation in log and outbox
        Map<String, Object> payload = Map.of(
                "returnId", returnId,
                "saleId", request.getSaleId() != null ? request.getSaleId() : "",
                "medicineId", medicine.getId(),
                "batchId", batch.getId(),
                "batchNumber", batch.getBatchNumber(),
                "quantity", request.getQuantity(),
                "reason", request.getReason(),
                "refundAmount", refundAmount.toString(),
                "timestamp", now.toString()
        );
        operationRecordingService.recordOperation(OperationType.RETURN, payload);

        return savedReturn;
    }
}
