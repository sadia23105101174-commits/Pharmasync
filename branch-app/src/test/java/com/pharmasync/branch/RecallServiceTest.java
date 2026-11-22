package com.pharmasync.branch;

import com.pharmasync.branch.entity.*;
import com.pharmasync.branch.repository.*;
import com.pharmasync.branch.service.RecallService;
import com.pharmasync.branch.service.SaleService;
import com.pharmasync.domain.batch.BatchStatus;
import com.pharmasync.domain.catalogue.DosageForm;
import com.pharmasync.domain.exception.InvalidBatchStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class RecallServiceTest {

    @Autowired
    private RecallService recallService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private RecallRepository recallRepository;

    private Medicine otcMedicine;
    private Batch batch;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        otcMedicine = new Medicine(
                "MED-RECALL-" + UUID.randomUUID(), "Paracetamol", "Panadol",
                "500mg", DosageForm.TABLET, BigDecimal.valueOf(5.00), false, 10, 10
        );
        medicineRepository.save(otcMedicine);

        batch = new Batch(
                "BATCH-RECALL-" + UUID.randomUUID(), "RECALL-001", otcMedicine,
                10, today.plusMonths(6), today.minusMonths(1), BatchStatus.ACTIVE, null
        );
        batchRepository.save(batch);
    }

    @Test
    @DisplayName("Applying a recall marks the batch RECALLED and persists the recall")
    void applyRecallMarksBatchRecalled() {
        var recall = recallService.applyRecall(batch.getId(), Instant.now(), "Safety recall");

        assertThat(recall.isApplied()).isTrue();
        var updated = batchRepository.findById(batch.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(BatchStatus.RECALLED);
        assertThat(updated.getStatusReason()).isEqualTo("Safety recall");
        assertThat(recallRepository.findByBatchId(batch.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A recalled batch is blocked from sale")
    void recalledBatchBlocked() {
        recallService.applyRecall(batch.getId(), Instant.now(), "Safety recall");

        var req = new com.pharmasync.branch.dto.SaleRequest(otcMedicine.getId(), 1);
        req.setBatchId(batch.getId());
        assertThatThrownBy(() -> saleService.processSale(req))
                .isInstanceOf(InvalidBatchStateException.class)
                .hasMessageContaining("Cannot sell recalled batch");
    }
}
