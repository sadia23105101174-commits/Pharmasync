package com.pharmasync.branch;

import com.pharmasync.branch.adapter.printer.EscPosThermalPrinterDevice;
import com.pharmasync.branch.dto.SaleRequest;
import com.pharmasync.branch.entity.*;
import com.pharmasync.branch.exception.PrescriptionRequiredException;
import com.pharmasync.branch.repository.*;
import com.pharmasync.branch.service.SaleService;
import com.pharmasync.domain.batch.BatchStatus;
import com.pharmasync.domain.catalogue.DosageForm;
import com.pharmasync.domain.exception.InvalidBatchStateException;
import com.pharmasync.domain.operation.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BranchSaleServiceTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Autowired
    private EscPosThermalPrinterDevice printerDevice;

    private Medicine testOtcMedicine;
    private Batch earlierBatch;
    private Batch laterBatch;
    private Medicine testRxMedicine;
    private Batch rxBatch;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();

        // Dedicated OTC medicine for deterministic tests
        testOtcMedicine = new Medicine(
                "TEST-MED-OTC-" + UUID.randomUUID(),
                "Ibuprofen",
                "AdvilTest",
                "400mg",
                DosageForm.TABLET,
                BigDecimal.valueOf(5.00),
                false,
                10,
                10
        );
        medicineRepository.save(testOtcMedicine);

        // Batch 1: Expiring earlier (in 2 months), stock = 10
        earlierBatch = new Batch(
                "TEST-BATCH-EARLY-" + UUID.randomUUID(),
                "ADV-EARLY",
                testOtcMedicine,
                10,
                today.plusMonths(2),
                today.minusMonths(1),
                BatchStatus.ACTIVE,
                "Early batch"
        );

        // Batch 2: Expiring later (in 10 months), stock = 10
        laterBatch = new Batch(
                "TEST-BATCH-LATER-" + UUID.randomUUID(),
                "ADV-LATER",
                testOtcMedicine,
                10,
                today.plusMonths(10),
                today.minusMonths(1),
                BatchStatus.ACTIVE,
                "Later batch"
        );
        batchRepository.saveAll(List.of(earlierBatch, laterBatch));

        // Dedicated Rx medicine
        testRxMedicine = new Medicine(
                "TEST-MED-RX-" + UUID.randomUUID(),
                "Ciprofloxacin",
                "CiproTest",
                "500mg",
                DosageForm.TABLET,
                BigDecimal.valueOf(18.00),
                true,
                10,
                10
        );
        medicineRepository.save(testRxMedicine);

        rxBatch = new Batch(
                "TEST-BATCH-RX-" + UUID.randomUUID(),
                "CIP-001",
                testRxMedicine,
                20,
                today.plusMonths(8),
                today.minusMonths(1),
                BatchStatus.ACTIVE,
                "Rx Batch"
        );
        batchRepository.save(rxBatch);
    }

    @Test
    @DisplayName("Sale uses earliest-expiry batch first (FEFO Strategy)")
    @Transactional
    void testSaleUsesEarliestExpiryBatch() {
        // Sale 1: Request 6 units (less than earlierBatch quantity of 10)
        SaleRequest req1 = new SaleRequest(testOtcMedicine.getId(), 6);
        Sale sale1 = saleService.processSale(req1);

        assertThat(sale1).isNotNull();
        assertThat(sale1.getLines()).hasSize(1);
        assertThat(sale1.getLines().get(0).getBatch().getId()).isEqualTo(earlierBatch.getId());
        assertThat(sale1.getLines().get(0).getQuantity()).isEqualTo(6);

        // Verify batch balances
        Batch updatedEarly = batchRepository.findById(earlierBatch.getId()).orElseThrow();
        Batch updatedLater = batchRepository.findById(laterBatch.getId()).orElseThrow();
        assertThat(updatedEarly.getQuantityOnHand()).isEqualTo(4);
        assertThat(updatedLater.getQuantityOnHand()).isEqualTo(10);

        // Sale 2: Request 8 units (crosses batches: remaining 4 from earlierBatch + 4 from laterBatch)
        SaleRequest req2 = new SaleRequest(testOtcMedicine.getId(), 8);
        Sale sale2 = saleService.processSale(req2);

        assertThat(sale2.getLines()).hasSize(2);
        assertThat(sale2.getLines().get(0).getBatch().getId()).isEqualTo(earlierBatch.getId());
        assertThat(sale2.getLines().get(0).getQuantity()).isEqualTo(4);
        assertThat(sale2.getLines().get(1).getBatch().getId()).isEqualTo(laterBatch.getId());
        assertThat(sale2.getLines().get(1).getQuantity()).isEqualTo(4);

        // Final balances
        updatedEarly = batchRepository.findById(earlierBatch.getId()).orElseThrow();
        updatedLater = batchRepository.findById(laterBatch.getId()).orElseThrow();
        assertThat(updatedEarly.getQuantityOnHand()).isEqualTo(0);
        assertThat(updatedLater.getQuantityOnHand()).isEqualTo(6);
    }

    @Test
    @DisplayName("Expired batch is blocked from sale")
    @Transactional
    void testExpiredBatchBlocked() {
        LocalDate today = LocalDate.now();

        // Create a medicine whose only batch is expired
        Medicine expiredMed = new Medicine(
                "MED-EXP-" + UUID.randomUUID(),
                "OldDrug",
                "OldBrand",
                "100mg",
                DosageForm.TABLET,
                BigDecimal.valueOf(2.00),
                false,
                10,
                10
        );
        medicineRepository.save(expiredMed);

        Batch expiredBatch = new Batch(
                "BATCH-EXPIRED-" + UUID.randomUUID(),
                "EXP-999",
                expiredMed,
                15,
                today.minusDays(10), // Expired 10 days ago!
                today.minusYears(1),
                BatchStatus.EXPIRED,
                "Expired stock"
        );
        batchRepository.save(expiredBatch);

        // Attempt FEFO sale -> Must be blocked
        SaleRequest fefoReq = new SaleRequest(expiredMed.getId(), 1);
        assertThatThrownBy(() -> saleService.processSale(fefoReq))
                .isInstanceOf(RuntimeException.class);

        // Attempt direct batch scan of expired batch -> Must be blocked
        SaleRequest directReq = new SaleRequest(expiredMed.getId(), 1);
        directReq.setBatchId(expiredBatch.getId());
        assertThatThrownBy(() -> saleService.processSale(directReq))
                .isInstanceOf(InvalidBatchStateException.class)
                .hasMessageContaining("Cannot sell expired batch");

        // Verify stock is untouched
        Batch checkBatch = batchRepository.findById(expiredBatch.getId()).orElseThrow();
        assertThat(checkBatch.getQuantityOnHand()).isEqualTo(15);
    }

    @Test
    @DisplayName("Rx sale without record rejected; succeeds with record")
    @Transactional
    void testRxSaleWithoutRecordRejected() {
        // 1. Missing prescriber details -> Rejected
        SaleRequest missingPrescriber = new SaleRequest(testRxMedicine.getId(), 2);
        missingPrescriber.setPatientName("Alice Smith");
        missingPrescriber.setPrescriptionDate(LocalDate.now());
        // prescriberRegNo is null

        assertThatThrownBy(() -> saleService.processSale(missingPrescriber))
                .isInstanceOf(PrescriptionRequiredException.class)
                .hasMessageContaining("without prescriber registration number");

        // 2. Missing patient name -> Rejected
        SaleRequest missingPatient = new SaleRequest(testRxMedicine.getId(), 2);
        missingPatient.setPrescriberRegNo("DOC-777");
        missingPatient.setPrescriptionDate(LocalDate.now());

        assertThatThrownBy(() -> saleService.processSale(missingPatient))
                .isInstanceOf(PrescriptionRequiredException.class);

        // 3. Missing prescription date -> Rejected
        SaleRequest missingDate = new SaleRequest(testRxMedicine.getId(), 2);
        missingDate.setPatientName("Alice Smith");
        missingDate.setPrescriberRegNo("DOC-777");

        assertThatThrownBy(() -> saleService.processSale(missingDate))
                .isInstanceOf(PrescriptionRequiredException.class);

        // 4. Valid prescription sale -> Succeeds
        SaleRequest validRxSale = new SaleRequest(
                testRxMedicine.getId(),
                2,
                "Alice Smith",
                "DOC-777",
                LocalDate.now()
        );

        Sale sale = saleService.processSale(validRxSale);
        assertThat(sale).isNotNull();
        assertThat(sale.getPatientName()).isEqualTo("Alice Smith");
        assertThat(sale.getPrescriberRegNo()).isEqualTo("DOC-777");
        assertThat(sale.getPrescriptionDate()).isEqualTo(LocalDate.now());

        Batch updatedRxBatch = batchRepository.findById(rxBatch.getId()).orElseThrow();
        assertThat(updatedRxBatch.getQuantityOnHand()).isEqualTo(18);
    }

    @Test
    @DisplayName("Outbox row created per sale with status pending and Lamport clock")
    @Transactional
    void testOutboxRowCreatedPerSale() {
        long outboxCountBefore = outboxRepository.count();
        long logCountBefore = operationLogRepository.count();

        SaleRequest req = new SaleRequest(testOtcMedicine.getId(), 2);
        Sale sale = saleService.processSale(req);

        // Verify outbox entry
        long outboxCountAfter = outboxRepository.count();
        long logCountAfter = operationLogRepository.count();

        assertThat(outboxCountAfter).isEqualTo(outboxCountBefore + 1);
        assertThat(logCountAfter).isEqualTo(logCountBefore + 1);

        List<Outbox> pendingOutbox = outboxRepository.findByStatus("pending");
        assertThat(pendingOutbox).isNotEmpty();

        Outbox latestOutbox = pendingOutbox.get(pendingOutbox.size() - 1);
        assertThat(latestOutbox.getType()).isEqualTo(OperationType.SALE);
        assertThat(latestOutbox.getStatus()).isEqualTo(Outbox.STATUS_PENDING);
        assertThat(latestOutbox.getLamportClock()).isPositive();
        assertThat(latestOutbox.getPayload()).contains(sale.getReceiptNumber());
        assertThat(latestOutbox.getPayload()).contains(testOtcMedicine.getBrandName());

        // Verify mock printer also received the receipt
        assertThat(printerDevice.getLastPrintedReceiptText()).isNotNull();
        assertThat(printerDevice.getLastPrintedReceiptText()).contains(sale.getReceiptNumber());
    }
}
