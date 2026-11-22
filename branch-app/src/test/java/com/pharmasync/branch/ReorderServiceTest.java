package com.pharmasync.branch;

import com.pharmasync.branch.dto.ReorderSuggestion;
import com.pharmasync.branch.entity.*;
import com.pharmasync.branch.repository.*;
import com.pharmasync.branch.service.ReorderService;
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
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReorderServiceTest {

    @Autowired
    private ReorderService reorderService;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleLineRepository saleLineRepository;

    private Medicine medicine;
    private Batch batch;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        medicine = new Medicine(
                "MED-REORDER-" + UUID.randomUUID(), "Paracetamol", "Panadol",
                "500mg", DosageForm.TABLET, BigDecimal.valueOf(5.00), false, 10, 10
        );
        medicineRepository.save(medicine);

        batch = new Batch(
                "BATCH-REORDER-" + UUID.randomUUID(), "REORDER-001", medicine,
                10, today.plusMonths(6), today.minusMonths(1),
                com.pharmasync.domain.batch.BatchStatus.ACTIVE, null
        );
        batchRepository.save(batch);
    }

    @Test
    @DisplayName("Reorder suggestion from 30-day velocity, lead time and safety stock")
    void reorderSuggestion() {
        // Record 30 units sold in the last 30 days across 2 sales of 15 each.
        Instant now = Instant.now();
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);
        Instant oldInstant = now.minus(40, ChronoUnit.DAYS);

        var sale1 = new Sale("S1", "R1", now.minus(10, ChronoUnit.DAYS), BigDecimal.valueOf(75.00),
                null, null, null);
        saleRepository.save(sale1);
        var sale2 = new Sale("S2", "R2", now.minus(5, ChronoUnit.DAYS), BigDecimal.valueOf(75.00),
                null, null, null);
        saleRepository.save(sale2);
        // Out-of-window sale should not count.
        var saleOld = new Sale("S3", "R3", oldInstant, BigDecimal.valueOf(75.00),
                null, null, null);
        saleRepository.save(saleOld);

        saleLineRepository.save(new SaleLine("L1", sale1, medicine, batch, 15,
                BigDecimal.valueOf(5.00), BigDecimal.valueOf(75.00)));
        saleLineRepository.save(new SaleLine("L2", sale2, medicine, batch, 15,
                BigDecimal.valueOf(5.00), BigDecimal.valueOf(75.00)));
        saleLineRepository.save(new SaleLine("L3", saleOld, medicine, batch, 999,
                BigDecimal.valueOf(5.00), BigDecimal.valueOf(4995.00)));

        var suggestion = reorderService.suggest("BRANCH-001", medicine.getId());

        // velocity = 30/30 = 1 per day; suggested = 1*7 + 10 - 10 = 7.
        assertThat(suggestion.velocityPerDay()).isEqualTo(1.0);
        assertThat(suggestion.quantityOnHand()).isEqualTo(10);
        assertThat(suggestion.suggestedOrderQuantity()).isEqualTo(7);
    }
}
