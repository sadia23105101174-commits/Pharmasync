package com.pharmasync.branch.service;

import com.pharmasync.branch.dto.ReceiveStockRequest;
import com.pharmasync.branch.entity.Batch;
import com.pharmasync.branch.entity.Medicine;
import com.pharmasync.branch.exception.ResourceNotFoundException;
import com.pharmasync.branch.repository.BatchRepository;
import com.pharmasync.branch.repository.MedicineRepository;
import com.pharmasync.domain.batch.BatchStatus;
import com.pharmasync.domain.operation.OperationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class StockReceivingService {

    private final MedicineRepository medicineRepository;
    private final BatchRepository batchRepository;
    private final OperationRecordingService operationRecordingService;

    public StockReceivingService(MedicineRepository medicineRepository,
                                 BatchRepository batchRepository,
                                 OperationRecordingService operationRecordingService) {
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.operationRecordingService = operationRecordingService;
    }

    @Transactional
    public Batch receiveStock(ReceiveStockRequest request) {
        Objects.requireNonNull(request, "ReceiveStockRequest must not be null");
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Received quantity must be at least 1, got: " + request.getQuantity());
        }

        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + request.getMedicineId()));

        LocalDate receivedDate = request.getReceivedDate() != null ? request.getReceivedDate() : LocalDate.now();

        Optional<Batch> existingBatchOpt = batchRepository.findByMedicineIdAndBatchNumber(
                medicine.getId(), request.getBatchNumber().trim()
        );

        Batch batch;
        if (existingBatchOpt.isPresent()) {
            batch = existingBatchOpt.get();
            batch.setQuantityOnHand(batch.getQuantityOnHand() + request.getQuantity());
            batch.setExpiryDate(request.getExpiryDate());
            if (batch.getStatus() == BatchStatus.EXPIRED && !batch.isExpired(LocalDate.now())) {
                batch.setStatus(BatchStatus.ACTIVE);
                batch.setStatusReason("Restocked with valid expiry date");
            }
        } else {
            batch = new Batch(
                    UUID.randomUUID().toString(),
                    request.getBatchNumber().trim(),
                    medicine,
                    request.getQuantity(),
                    request.getExpiryDate(),
                    receivedDate,
                    BatchStatus.ACTIVE,
                    "Initial stock receipt"
            );
        }

        Batch savedBatch = batchRepository.save(batch);

        // Record Operation in append-only log and pending outbox
        Map<String, Object> payload = Map.of(
                "batchId", savedBatch.getId(),
                "batchNumber", savedBatch.getBatchNumber(),
                "medicineId", medicine.getId(),
                "medicineName", medicine.getBrandName(),
                "quantityReceived", request.getQuantity(),
                "newTotalQuantity", savedBatch.getQuantityOnHand(),
                "expiryDate", savedBatch.getExpiryDate().toString(),
                "receivedDate", receivedDate.toString()
        );
        operationRecordingService.recordOperation(OperationType.RECEIVE, payload);

        return savedBatch;
    }
}
