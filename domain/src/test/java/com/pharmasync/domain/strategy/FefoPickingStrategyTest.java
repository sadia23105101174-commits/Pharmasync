package com.pharmasync.domain.strategy;

import com.pharmasync.domain.batch.Batch;
import com.pharmasync.domain.catalogue.DosageForm;
import com.pharmasync.domain.catalogue.Medicine;
import com.pharmasync.domain.composite.PackHierarchy;
import com.pharmasync.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FefoPickingStrategyTest {

    private Medicine amoxicillin;
    private LocalDate today;
    private FefoPickingStrategy fefo;

    @BeforeEach
    void setUp() {
        today = LocalDate.of(2026, 11, 22);
        amoxicillin = new Medicine(
                "MED-AMOX",
                "Amoxicillin",
                "Amoxil",
                "500mg",
                DosageForm.CAPSULE,
                true,
                PackHierarchy.boxStripTablet(10, 10)
        );
        fefo = new FefoPickingStrategy();
    }

    @Test
    @DisplayName("FEFO picks earliest-expiring batch first and spills over to next earliest batch")
    void testFefoEarliestExpiryFirst() {
        Batch batchExpiringJan = new Batch("B1", "BATCH-JAN", amoxicillin, 20,
                LocalDate.of(2027, 1, 15), today.minusDays(30));
        Batch batchExpiringMar = new Batch("B2", "BATCH-MAR", amoxicillin, 50,
                LocalDate.of(2027, 3, 10), today.minusDays(20));
        Batch batchExpiringDec = new Batch("B3", "BATCH-DEC", amoxicillin, 100,
                LocalDate.of(2027, 12, 31), today.minusDays(10));

        List<Batch> batches = List.of(batchExpiringDec, batchExpiringJan, batchExpiringMar);

        // Request 35 units: 20 from Jan, 15 from Mar
        List<BatchAllocation> allocations = fefo.pick(batches, 35, today);

        assertThat(allocations).hasSize(2);

        assertThat(allocations.get(0).batchNumber()).isEqualTo("BATCH-JAN");
        assertThat(allocations.get(0).allocatedQuantity()).isEqualTo(20);

        assertThat(allocations.get(1).batchNumber()).isEqualTo("BATCH-MAR");
        assertThat(allocations.get(1).allocatedQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("FEFO strictly rejects and never picks expired batches")
    void testNeverPickExpiredBatches() {
        Batch expiredBatch = new Batch("B-EXP", "BATCH-EXPIRED", amoxicillin, 100,
                today.minusDays(1), today.minusMonths(6));
        Batch validBatch = new Batch("B-VAL", "BATCH-VALID", amoxicillin, 15,
                today.plusMonths(3), today.minusDays(10));

        List<Batch> batches = List.of(expiredBatch, validBatch);

        // Picking 10: only valid batch is selected
        List<BatchAllocation> allocations = fefo.pick(batches, 10, today);
        assertThat(allocations).hasSize(1);
        assertThat(allocations.get(0).batchNumber()).isEqualTo("BATCH-VALID");
        assertThat(allocations.get(0).allocatedQuantity()).isEqualTo(10);

        // Picking 20: fails with InsufficientStockException because expired stock (100) cannot be picked
        assertThatThrownBy(() -> fefo.pick(batches, 20, today))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient sellable stock. Requested: 20, available valid stock: 15");
    }

    @Test
    @DisplayName("FEFO strictly rejects and never picks recalled batches")
    void testNeverPickRecalledBatches() {
        Batch recalledBatch = new Batch("B-REC", "BATCH-RECALLED", amoxicillin, 80,
                today.plusMonths(12), today.minusDays(20));
        recalledBatch.recall("Central recall notice");

        Batch activeBatch = new Batch("B-ACT", "BATCH-ACTIVE", amoxicillin, 25,
                today.plusMonths(6), today.minusDays(5));

        List<Batch> batches = List.of(recalledBatch, activeBatch);

        List<BatchAllocation> allocations = fefo.pick(batches, 20, today);
        assertThat(allocations).hasSize(1);
        assertThat(allocations.get(0).batchNumber()).isEqualTo("BATCH-ACTIVE");
        assertThat(allocations.get(0).allocatedQuantity()).isEqualTo(20);

        // Requesting more than active stock fails even with large recalled batch present
        assertThatThrownBy(() -> fefo.pick(batches, 30, today))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("FEFO strictly rejects and never picks quarantined batches")
    void testNeverPickQuarantinedBatches() {
        Batch quarantinedBatch = new Batch("B-Q", "BATCH-QUARANTINED", amoxicillin, 50,
                today.plusMonths(4), today.minusDays(2));
        quarantinedBatch.quarantine("Under investigation");

        Batch activeBatch = new Batch("B-A", "BATCH-ACTIVE", amoxicillin, 30,
                today.plusMonths(8), today.minusDays(1));

        List<Batch> batches = List.of(quarantinedBatch, activeBatch);

        List<BatchAllocation> allocations = fefo.pick(batches, 15, today);
        assertThat(allocations).hasSize(1);
        assertThat(allocations.get(0).batchNumber()).isEqualTo("BATCH-ACTIVE");
    }

    @Test
    @DisplayName("FEFO breaks ties with receivedDate and batchNumber deterministically")
    void testTieBreakingDeterministic() {
        LocalDate sameExpiry = today.plusMonths(6);
        Batch earlierReceived = new Batch("B-E", "BATCH-A", amoxicillin, 10,
                sameExpiry, today.minusDays(20));
        Batch laterReceived = new Batch("B-L", "BATCH-B", amoxicillin, 10,
                sameExpiry, today.minusDays(10));

        List<BatchAllocation> allocations = fefo.pick(List.of(laterReceived, earlierReceived), 15, today);

        assertThat(allocations.get(0).batchNumber()).isEqualTo("BATCH-A");
        assertThat(allocations.get(0).allocatedQuantity()).isEqualTo(10);

        assertThat(allocations.get(1).batchNumber()).isEqualTo("BATCH-B");
        assertThat(allocations.get(1).allocatedQuantity()).isEqualTo(5);
    }
}
