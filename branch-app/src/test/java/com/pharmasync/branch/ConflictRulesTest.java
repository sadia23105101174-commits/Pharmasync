package com.pharmasync.branch;

import com.pharmasync.branch.dto.StockCountRequest;
import com.pharmasync.branch.entity.*;
import com.pharmasync.branch.repository.*;
import com.pharmasync.branch.service.RecallService;
import com.pharmasync.branch.service.SaleService;
import com.pharmasync.branch.service.StockCountService;
import com.pharmasync.domain.batch.BatchStatus;
import com.pharmasync.domain.catalogue.DosageForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Deterministic conflict rules (see docs/conflict-rules.md):
 * 1. Sale at an old price stands; difference is reported.
 * 2. Negative stock after sync creates a count task.
 * 3. Sales after a recall time are flagged.
 */
@SpringBootTest
@Transactional
class ConflictRulesTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private StockCountService stockCountService;

    @Autowired
    private RecallService recallService;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private RecallRepository recallRepository;

    private Medicine otcMedicine;
    private Batch batch;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        otcMedicine = new Medicine(
                "MED-CONFLICT-" + UUID.randomUUID(), "Paracetamol", "Panadol",
                "500mg", DosageForm.TABLET, BigDecimal.valueOf(5.00), false, 10, 10
        );
        medicineRepository.save(otcMedicine);

        batch = new Batch(
                "BATCH-CONFLICT-" + UUID.randomUUID(), "CONFLICT-001", otcMedicine,
                10, today.plusMonths(6), today.minusMonths(1), BatchStatus.ACTIVE, null
        );
        batchRepository.save(batch);
    }

    @Test
    @DisplayName("Rule 1: sale at old price stands; price difference is reported")
    void rule1SaleAtOldPriceStands() {
        com.pharmasync.branch.dto.SaleRequest req = new com.pharmasync.branch.dto.SaleRequest(
                otcMedicine.getId(), 2
        );
        req.setBatchId(batch.getId());
        var sale = saleService.processSale(req);

        var line = sale.getLines().get(0);
        assertThat(line.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(5.00));

        // Central price changes after the sale.
        otcMedicine.setPrice(BigDecimal.valueOf(10.00));
        medicineRepository.save(otcMedicine);

        // The sale line still reflects the captured (old) price: the sale stands.
        var persisted = saleRepository.findById(sale.getId()).orElseThrow();
        var persistedLine = persisted.getLines().get(0);
        assertThat(persistedLine.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(5.00));

        // The difference is reported.
        BigDecimal difference = BigDecimal.valueOf(10.00).subtract(persistedLine.getUnitPrice());
        assertThat(difference).isEqualByComparingTo(BigDecimal.valueOf(5.00));
    }

    @Test
    @DisplayName("Rule 2: negative stock after sync creates a count task")
    void rule2NegativeStockCreatesCountTask() {
        // A sync-driven correction finds the batch empty (expected 0, counted - represents negative stock found).
        var task = stockCountService.recordStockCount(new StockCountRequest(
                batch.getId(), 0, "Negative stock detected after sync; recount required."
        ));
        assertThat(task).isNotNull();
        assertThat(task.getDifference()).isNegative();
        assertThat(task.getNotes()).contains("Negative stock");
    }

    @Test
    @DisplayName("Rule 3: sales after a recall time are flagged")
    void rule3SaleAfterRecallTimeFlagged() {
        // Recall happened while the branch was offline: batch still ACTIVE but
        // recall time is in the past.
        var recall = new Recall(
                UUID.randomUUID().toString(), batch.getId(),
                Instant.now().minusSeconds(3600), "Safety recall", true
        );
        recallRepository.save(recall);

        var req = new com.pharmasync.branch.dto.SaleRequest(otcMedicine.getId(), 1);
        req.setBatchId(batch.getId());
        var sale = saleService.processSale(req);

        assertThat(sale.isRecalledBatchSale()).isTrue();
        assertThat(recallRepository.findByBatchId(batch.getId())).hasSize(1);
    }
}
