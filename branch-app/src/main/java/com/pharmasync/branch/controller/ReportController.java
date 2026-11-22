package com.pharmasync.branch.controller;

import com.pharmasync.branch.dto.DashboardSummary;
import com.pharmasync.branch.dto.PrescriptionSaleDto;
import com.pharmasync.branch.dto.RecallReportDto;
import com.pharmasync.branch.entity.Batch;
import com.pharmasync.branch.entity.Medicine;
import com.pharmasync.branch.entity.Recall;
import com.pharmasync.branch.entity.Sale;
import com.pharmasync.branch.entity.SaleLine;
import com.pharmasync.branch.repository.BatchRepository;
import com.pharmasync.branch.repository.MedicineRepository;
import com.pharmasync.branch.repository.OutboxRepository;
import com.pharmasync.branch.repository.RecallRepository;
import com.pharmasync.branch.repository.SaleLineRepository;
import com.pharmasync.branch.repository.SaleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Prescription register, recall report and dashboard")
public class ReportController {

    private final SaleRepository saleRepository;
    private final SaleLineRepository saleLineRepository;
    private final BatchRepository batchRepository;
    private final MedicineRepository medicineRepository;
    private final RecallRepository recallRepository;
    private final OutboxRepository outboxRepository;

    public ReportController(SaleRepository saleRepository,
                            SaleLineRepository saleLineRepository,
                            BatchRepository batchRepository,
                            MedicineRepository medicineRepository,
                            RecallRepository recallRepository,
                            OutboxRepository outboxRepository) {
        this.saleRepository = saleRepository;
        this.saleLineRepository = saleLineRepository;
        this.batchRepository = batchRepository;
        this.medicineRepository = medicineRepository;
        this.recallRepository = recallRepository;
        this.outboxRepository = outboxRepository;
    }

    @GetMapping("/prescriptions")
    @Operation(summary = "Prescription register", description = "Lists sales of prescription-only medicines")
    public ResponseEntity<List<PrescriptionSaleDto>> prescriptionRegister(Pageable pageable) {
        List<Sale> rxSales = saleRepository.findAll().stream()
                .filter(sale -> sale.getPrescriberRegNo() != null && !sale.getPrescriberRegNo().isBlank())
                .collect(Collectors.toList());

        List<PrescriptionSaleDto> dtos = rxSales.stream()
                .flatMap(sale -> sale.getLines().stream()
                        .filter(line -> line.getMedicine().isPrescriptionRequired())
                        .map(line -> new PrescriptionSaleDto(
                                sale.getId(), sale.getReceiptNumber(), sale.getSaleTime(),
                                sale.getPatientName(), sale.getPrescriberRegNo(), sale.getPrescriptionDate(),
                                line.getMedicine().getBrandName(), line.getBatch().getBatchNumber(),
                                line.getQuantity(), line.getSubtotal())))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/recalls/{batchId}")
    @Operation(summary = "Recall report for a batch", description = "Quantity on hand and units sold after recall time")
    public ResponseEntity<RecallReportDto> recallReport(@PathVariable String batchId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));

        List<Recall> recalls = recallRepository.findByBatchId(batchId);
        Instant recallTime = recalls.stream()
                .filter(Recall::isApplied)
                .map(Recall::getRecallTime)
                .min(Instant::compareTo)
                .orElse(null);

        int unitsSoldAfterRecall = 0;
        if (recallTime != null) {
            unitsSoldAfterRecall = saleLineRepository.findAll().stream()
                    .filter(line -> batchId.equals(line.getBatch().getId()))
                    .filter(line -> line.getSale().getSaleTime().isAfter(recallTime))
                    .mapToInt(SaleLine::getQuantity)
                    .sum();
        }

        return ResponseEntity.ok(new RecallReportDto(
                batchId, batch.getBatchNumber(), recallTime,
                batch.getQuantityOnHand(), unitsSoldAfterRecall
        ));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard summary", description = "Stock by batch, last sync time, pending ops, expiring value")
    public ResponseEntity<DashboardSummary> dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        LocalDate today = asOf != null ? asOf : LocalDate.now();
        LocalDate ninetyDays = today.plusDays(90);

        List<Batch> batches = batchRepository.findAll();
        String branchId = "BRANCH-001";

        List<DashboardSummary.StockLine> lines = batches.stream()
                .map(batch -> {
                    Medicine medicine = batch.getMedicine();
                    BigDecimal price = medicine.getPrice();
                    BigDecimal lineValue = BigDecimal.valueOf(batch.getQuantityOnHand())
                            .multiply(price);
                    return new DashboardSummary.StockLine(
                            branchId, medicine.getId(), medicine.getBrandName(),
                            batch.getBatchNumber(), batch.getExpiryDate(),
                            batch.getQuantityOnHand(), price, lineValue);
                })
                .sorted((a, b) -> a.batchNumber().compareTo(b.batchNumber()))
                .toList();

        BigDecimal expiringValue = lines.stream()
                .filter(line -> !line.expiryDate().isBefore(today) && !line.expiryDate().isAfter(ninetyDays))
                .map(DashboardSummary.StockLine::lineValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Instant lastSync = outboxRepository.findAll().stream()
                .map(com.pharmasync.branch.entity.Outbox::getTimestamp)
                .max(Instant::compareTo)
                .orElse(Instant.now());

        long pendingOps = outboxRepository.countByStatus(com.pharmasync.branch.entity.Outbox.STATUS_PENDING);

        return ResponseEntity.ok(new DashboardSummary(lines, lastSync, pendingOps, expiringValue));
    }
}
