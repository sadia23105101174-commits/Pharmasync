package com.pharmasync.branch.service;

import com.pharmasync.branch.adapter.printer.Receipt;
import com.pharmasync.branch.adapter.printer.ReceiptItem;
import com.pharmasync.branch.adapter.printer.ReceiptPrinter;
import com.pharmasync.branch.dto.SaleRequest;
import com.pharmasync.branch.entity.Batch;
import com.pharmasync.branch.entity.Medicine;
import com.pharmasync.branch.entity.Sale;
import com.pharmasync.branch.entity.SaleLine;
import com.pharmasync.branch.exception.PrescriptionRequiredException;
import com.pharmasync.branch.exception.ResourceNotFoundException;
import com.pharmasync.branch.repository.BatchRepository;
import com.pharmasync.branch.repository.MedicineRepository;
import com.pharmasync.branch.repository.SaleRepository;
import com.pharmasync.domain.batch.BatchStatus;
import com.pharmasync.domain.exception.InsufficientStockException;
import com.pharmasync.domain.exception.InvalidBatchStateException;
import com.pharmasync.domain.operation.OperationType;
import com.pharmasync.domain.strategy.BatchAllocation;
import com.pharmasync.domain.strategy.FefoPickingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class SaleService {

    private final MedicineRepository medicineRepository;
    private final BatchRepository batchRepository;
    private final SaleRepository saleRepository;
    private final FefoPickingStrategy fefoPickingStrategy;
    private final ReceiptPrinter receiptPrinter;
    private final OperationRecordingService operationRecordingService;
    private final RecallService recallService;
    private final String branchId;

    public SaleService(MedicineRepository medicineRepository,
                       BatchRepository batchRepository,
                       SaleRepository saleRepository,
                       FefoPickingStrategy fefoPickingStrategy,
                       ReceiptPrinter receiptPrinter,
                       OperationRecordingService operationRecordingService,
                       RecallService recallService,
                       @Value("${branch.id:BRANCH-001}") String branchId) {
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.saleRepository = saleRepository;
        this.fefoPickingStrategy = fefoPickingStrategy;
        this.receiptPrinter = receiptPrinter;
        this.operationRecordingService = operationRecordingService;
        this.recallService = recallService;
        this.branchId = branchId;
    }

    @Transactional
    public Sale processSale(SaleRequest request) {
        Objects.requireNonNull(request, "SaleRequest must not be null");
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Sale quantity must be at least 1, got: " + request.getQuantity());
        }

        // 1. Validate Medicine existence
        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + request.getMedicineId()));

        // 2. Validate Prescription requirement
        if (medicine.isPrescriptionRequired()) {
            boolean hasPrescriber = request.getPrescriberRegNo() != null && !request.getPrescriberRegNo().trim().isEmpty();
            boolean hasPatient = request.getPatientName() != null && !request.getPatientName().trim().isEmpty();
            boolean hasRxDate = request.getPrescriptionDate() != null;

            if (!hasPrescriber || !hasPatient || !hasRxDate) {
                throw new PrescriptionRequiredException(
                        "Prescription-only medicine '" + medicine.getBrandName() +
                                "' cannot be sold without prescriber registration number, patient name, and prescription date."
                );
            }
        }

        LocalDate today = LocalDate.now();
        List<BatchAllocation> allocations;

        // 3. Batch allocation: specific batch or FEFO Strategy
        if (request.getBatchId() != null && !request.getBatchId().trim().isEmpty()) {
            Batch batch = batchRepository.findById(request.getBatchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + request.getBatchId()));

            if (!batch.getMedicine().getId().equals(medicine.getId())) {
                throw new IllegalArgumentException("Batch " + batch.getBatchNumber() + " does not belong to medicine " + medicine.getBrandName());
            }

            if (batch.getStatus() == BatchStatus.RECALLED) {
                throw new InvalidBatchStateException("Cannot sell recalled batch: " + batch.getBatchNumber());
            }
            if (batch.getStatus() == BatchStatus.QUARANTINED) {
                throw new InvalidBatchStateException("Cannot sell quarantined batch: " + batch.getBatchNumber());
            }
            if (batch.getStatus() == BatchStatus.EXPIRED || batch.isExpired(today)) {
                throw new InvalidBatchStateException("Cannot sell expired batch: " + batch.getBatchNumber());
            }
            if (batch.getQuantityOnHand() < request.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock in batch " + batch.getBatchNumber() +
                        ". Requested: " + request.getQuantity() + ", available: " + batch.getQuantityOnHand());
            }

            allocations = List.of(new BatchAllocation(
                    batch.getId(),
                    batch.getBatchNumber(),
                    request.getQuantity(),
                    batch.getExpiryDate()
            ));
        } else {
            List<Batch> batches = batchRepository.findByMedicineId(medicine.getId());
            if (batches.isEmpty()) {
                throw new InsufficientStockException("No stock available for medicine: " + medicine.getBrandName());
            }

            // Check if all batches are expired or recalled
            boolean hasSellable = batches.stream().anyMatch(b -> b.canSell(today));
            if (!hasSellable) {
                boolean allRecalled = batches.stream().allMatch(b -> b.getStatus() == BatchStatus.RECALLED);
                if (allRecalled) {
                    throw new InvalidBatchStateException("Cannot sell recalled batch stock for: " + medicine.getBrandName());
                }
                boolean allExpired = batches.stream().allMatch(b -> b.getStatus() == BatchStatus.EXPIRED || b.isExpired(today));
                if (allExpired) {
                    throw new InvalidBatchStateException("Cannot sell expired batch stock for: " + medicine.getBrandName());
                }
            }

            List<com.pharmasync.domain.batch.Batch> domainBatches = batches.stream()
                    .map(Batch::toDomain)
                    .toList();

            allocations = fefoPickingStrategy.pick(domainBatches, request.getQuantity(), today);
        }

        // 4. Create Sale
        String saleId = UUID.randomUUID().toString();
        String receiptNumber = generateReceiptNumber();
        Instant now = Instant.now();

        Sale sale = new Sale(
                saleId,
                receiptNumber,
                now,
                BigDecimal.ZERO,
                request.getPatientName(),
                request.getPrescriberRegNo(),
                request.getPrescriptionDate()
        );

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<ReceiptItem> receiptItems = new ArrayList<>();
        List<Map<String, Object>> allocationPayload = new ArrayList<>();

        for (BatchAllocation allocation : allocations) {
            Batch batch = batchRepository.findById(allocation.batchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + allocation.batchId()));

            batch.setQuantityOnHand(batch.getQuantityOnHand() - allocation.allocatedQuantity());
            batchRepository.save(batch);

            BigDecimal unitPrice = medicine.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(allocation.allocatedQuantity()));
            totalAmount = totalAmount.add(subtotal);

            SaleLine line = new SaleLine(
                    UUID.randomUUID().toString(),
                    sale,
                    medicine,
                    batch,
                    allocation.allocatedQuantity(),
                    unitPrice,
                    subtotal
            );
            sale.addLine(line);

            receiptItems.add(new ReceiptItem(
                    medicine.getBrandName(),
                    batch.getBatchNumber(),
                    allocation.allocatedQuantity(),
                    unitPrice,
                    subtotal
            ));

            allocationPayload.add(Map.of(
                    "batchId", batch.getId(),
                    "batchNumber", batch.getBatchNumber(),
                    "quantity", allocation.allocatedQuantity(),
                    "expiryDate", batch.getExpiryDate().toString()
            ));
        }

        sale.setTotalAmount(totalAmount);

        // Rule 3: flag sales made after a batch's recall time (offline window).
        boolean recalledSale = allocations.stream()
                .anyMatch(a -> recallService.isSaleAfterRecall(a.batchId(), now));
        sale.setRecalledBatchSale(recalledSale);

        Sale savedSale = saleRepository.save(sale);

        // 5. Print receipt via Mock Receipt Printer Adapter
        Receipt receipt = new Receipt(
                receiptNumber,
                branchId,
                now,
                sale.getPatientName(),
                sale.getPrescriberRegNo(),
                sale.getPrescriptionDate(),
                receiptItems,
                totalAmount
        );
        receiptPrinter.print(receipt);

        // 6. Record change as domain Operation in append-only log and pending outbox
        Map<String, Object> opPayload = Map.of(
                "saleId", saleId,
                "receiptNumber", receiptNumber,
                "medicineId", medicine.getId(),
                "medicineName", medicine.getBrandName(),
                "totalAmount", totalAmount.toString(),
                "patientName", sale.getPatientName() != null ? sale.getPatientName() : "",
                "prescriberRegNo", sale.getPrescriberRegNo() != null ? sale.getPrescriberRegNo() : "",
                "prescriptionDate", sale.getPrescriptionDate() != null ? sale.getPrescriptionDate().toString() : "",
                "allocations", allocationPayload
        );
        operationRecordingService.recordOperation(OperationType.SALE, opPayload);

        return savedSale;
    }

    private String generateReceiptNumber() {
        return "REC-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 9000 + 1000);
    }
}
