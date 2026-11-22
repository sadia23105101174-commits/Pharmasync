package com.pharmasync.headoffice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmasync.domain.sync.SyncBatchRequest;
import com.pharmasync.domain.sync.SyncBatchResponse;
import com.pharmasync.domain.sync.SyncOperationDto;
import com.pharmasync.domain.sync.SyncStatusDto;
import com.pharmasync.headoffice.entity.BranchSyncStatus;
import com.pharmasync.headoffice.entity.CentralBatchStock;
import com.pharmasync.headoffice.entity.ProcessedOperation;
import com.pharmasync.domain.operation.OperationType;
import com.pharmasync.headoffice.repository.BranchSyncStatusRepository;
import com.pharmasync.headoffice.repository.CentralBatchStockRepository;
import com.pharmasync.headoffice.repository.ProcessedOperationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

/**
 * Applies a batch of branch operations to central head-office state.
 * Idempotent: the same operation UUID is applied once thanks to the
 * ProcessedOperation registry. Central stock is updated per batch and the
 * per-branch last acknowledged logical clock / last sync time are tracked.
 */
@Service
public class SyncService {

    private final ProcessedOperationRepository processedOperationRepository;
    private final CentralBatchStockRepository centralBatchStockRepository;
    private final BranchSyncStatusRepository branchSyncStatusRepository;
    private final ObjectMapper objectMapper;

    public SyncService(ProcessedOperationRepository processedOperationRepository,
                       CentralBatchStockRepository centralBatchStockRepository,
                       BranchSyncStatusRepository branchSyncStatusRepository,
                       ObjectMapper objectMapper) {
        this.processedOperationRepository = processedOperationRepository;
        this.centralBatchStockRepository = centralBatchStockRepository;
        this.branchSyncStatusRepository = branchSyncStatusRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SyncBatchResponse applyBatch(SyncBatchRequest request) {
        List<SyncOperationDto> operations = request.operations() != null ? request.operations() : List.of();
        List<UUID> acknowledgedIds = new ArrayList<>();

        for (SyncOperationDto op : operations) {
            if (op.operationId() == null) {
                continue;
            }
            boolean alreadyProcessed = processedOperationRepository.existsByOperationId(op.operationId());
            if (!alreadyProcessed) {
                ProcessedOperation record = new ProcessedOperation(
                        op.operationId(), op.branchId(), op.lamportClock(), op.type(), Instant.now());
                processedOperationRepository.save(record);
                applyStockDelta(op.branchId(), op.type(), op.payload());
                updateSyncStatus(op.branchId(), op.lamportClock(), 1);
            } else {
                updateSyncStatus(op.branchId(), op.lamportClock(), 0);
            }
            acknowledgedIds.add(op.operationId());
        }

        return SyncBatchResponse.of(acknowledgedIds);
    }

    @Transactional(readOnly = true)
    public Page<CentralBatchStock> getStock(String branchId, Pageable pageable) {
        if (branchId != null && !branchId.isBlank()) {
            return centralBatchStockRepository.findByBranchId(branchId, pageable);
        }
        return centralBatchStockRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<BranchSyncStatus> getSyncStatus(Pageable pageable) {
        return branchSyncStatusRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public SyncStatusDto getSyncStatus(String branchId) {
        BranchSyncStatus status = branchSyncStatusRepository.findByBranchId(branchId)
                .orElseGet(() -> new BranchSyncStatus(branchId, 0L, Instant.now(), 0L));
        return new SyncStatusDto(status.getBranchId(), status.getLastAckedLogicalClock(),
                status.getLastSyncTime(), status.getTotalOperationsProcessed());
    }

    private void applyStockDelta(String branchId, OperationType type, String payload) {
        if (payload == null || payload.isBlank()) {
            return;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> root = objectMapper.readValue(payload, Map.class);
            List<Map<String, Object>> allocations = asList(root.get("allocations"));
            if (allocations == null || allocations.isEmpty()) {
                applySingleDelta(branchId, type, root);
                return;
            }
            for (Map<String, Object> alloc : allocations) {
                applyAllocationDelta(branchId, type, alloc);
            }
        } catch (Exception ignored) {
            // Malformed payload: operation is still recorded as processed; stock cannot be derived.
        }
    }

    private void applyAllocationDelta(String branchId, OperationType type, Map<String, Object> alloc) {
        String medicineId = asString(alloc.get("medicineId"));
        String batchNumber = asString(alloc.get("batchNumber"));
        int quantity = asInt(alloc.get("quantity"));
        if (medicineId == null || batchNumber == null || quantity <= 0) {
            return;
        }
        CentralBatchStock stock = centralBatchStockRepository
                .findByBranchIdAndMedicineIdAndBatchNumber(branchId, medicineId, batchNumber)
                .orElseGet(() -> new CentralBatchStock(branchId, medicineId, "Unknown Medicine",
                        batchNumber, null, 0, Instant.now()));
        int delta = (type == OperationType.RECEIVE) ? quantity : -quantity;
        stock.setQuantityOnHand(Math.max(0, stock.getQuantityOnHand() + delta));
        stock.setLastUpdated(Instant.now());
        centralBatchStockRepository.save(stock);
    }

    private void applySingleDelta(String branchId, OperationType type, Map<String, Object> root) {
        String medicineId = asString(root.get("medicineId"));
        String batchNumber = asString(root.get("batchNumber"));
        int quantity = asInt(root.get("quantity"));
        if (medicineId == null || batchNumber == null || quantity <= 0) {
            return;
        }
        CentralBatchStock stock = centralBatchStockRepository
                .findByBranchIdAndMedicineIdAndBatchNumber(branchId, medicineId, batchNumber)
                .orElseGet(() -> new CentralBatchStock(branchId, medicineId, "Unknown Medicine",
                        batchNumber, null, 0, Instant.now()));
        int delta = (type == OperationType.RECEIVE) ? quantity : -quantity;
        stock.setQuantityOnHand(Math.max(0, stock.getQuantityOnHand() + delta));
        stock.setLastUpdated(Instant.now());
        centralBatchStockRepository.save(stock);
    }

    private void updateSyncStatus(String branchId, long lamportClock, int newlyProcessed) {
        BranchSyncStatus status = branchSyncStatusRepository.findByBranchId(branchId)
                .orElseGet(() -> new BranchSyncStatus(branchId, 0L, Instant.now(), 0L));
        status.setLastAckedLogicalClock(Math.max(status.getLastAckedLogicalClock(), lamportClock));
        status.setLastSyncTime(Instant.now());
        status.setTotalOperationsProcessed(status.getTotalOperationsProcessed() + newlyProcessed);
        branchSyncStatusRepository.save(status);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asList(Object value) {
        if (value instanceof List<?> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> m) {
                    result.add((Map<String, Object>) m);
                }
            }
            return result;
        }
        return null;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int asInt(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
