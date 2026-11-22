package com.pharmasync.domain.batch;

import com.pharmasync.domain.catalogue.DosageForm;
import com.pharmasync.domain.catalogue.Medicine;
import com.pharmasync.domain.composite.PackHierarchy;
import com.pharmasync.domain.exception.InsufficientStockException;
import com.pharmasync.domain.exception.InvalidBatchStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatchStateTest {

    private Medicine paracetamol;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.of(2026, 11, 22);
        paracetamol = new Medicine(
                "MED-001",
                "Paracetamol",
                "Panadol",
                "500mg",
                DosageForm.TABLET,
                false,
                PackHierarchy.boxStripTablet(10, 10)
        );
    }

    @Test
    @DisplayName("Active batch allows sales and decrements quantity on hand")
    void testActiveBatchSale() {
        Batch batch = new Batch("B1", "BATCH-001", paracetamol, 100, today.plusMonths(6), today.minusDays(5));

        assertThat(batch.getStatus()).isEqualTo(BatchStatus.ACTIVE);
        assertThat(batch.canSell(today)).isTrue();

        batch.sell(30, today);
        assertThat(batch.getQuantityOnHand()).isEqualTo(70);

        batch.sell(70, today);
        assertThat(batch.getQuantityOnHand()).isEqualTo(0);
        assertThat(batch.canSell(today)).isFalse(); // zero stock remaining
    }

    @Test
    @DisplayName("Active batch throws InsufficientStockException when sale quantity exceeds stock")
    void testActiveBatchInsufficientStock() {
        Batch batch = new Batch("B1", "BATCH-001", paracetamol, 20, today.plusMonths(6), today.minusDays(5));

        assertThatThrownBy(() -> batch.sell(25, today))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock in batch BATCH-001");
    }

    @Test
    @DisplayName("Batch automatically transitions to EXPIRED when sale or canSell is attempted past expiry date")
    void testAutomaticExpiry() {
        LocalDate expiredDate = today.minusDays(1);
        Batch batch = new Batch("B-EXP", "BATCH-EXP", paracetamol, 50, expiredDate, today.minusMonths(6));

        assertThat(batch.canSell(today)).isFalse();
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.EXPIRED);

        assertThatThrownBy(() -> batch.sell(5, today))
                .isInstanceOf(InvalidBatchStateException.class)
                .hasMessageContaining("Cannot sell expired batch");
    }

    @Test
    @DisplayName("Quarantined batch cannot be sold until activated")
    void testQuarantinedBatchLifecycle() {
        Batch batch = new Batch("B2", "BATCH-002", paracetamol, 100, today.plusMonths(12), today.minusDays(1));

        batch.quarantine("Quality check pending");
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.QUARANTINED);
        assertThat(batch.canSell(today)).isFalse();

        assertThatThrownBy(() -> batch.sell(10, today))
                .isInstanceOf(InvalidBatchStateException.class)
                .hasMessageContaining("Cannot sell quarantined batch");

        // Reactivate
        batch.activate(today);
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.ACTIVE);
        assertThat(batch.canSell(today)).isTrue();

        batch.sell(10, today);
        assertThat(batch.getQuantityOnHand()).isEqualTo(90);
    }

    @Test
    @DisplayName("Recalled batch cannot be sold or reactivated under any circumstance")
    void testRecalledBatchCannotBeSoldOrReactivated() {
        Batch batch = new Batch("B3", "BATCH-003", paracetamol, 100, today.plusMonths(12), today.minusDays(10));

        batch.recall("Contamination detected by manufacturer");
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.RECALLED);
        assertThat(batch.canSell(today)).isFalse();

        assertThatThrownBy(() -> batch.sell(10, today))
                .isInstanceOf(InvalidBatchStateException.class)
                .hasMessageContaining("Cannot sell recalled batch");

        assertThatThrownBy(() -> batch.activate(today))
                .isInstanceOf(InvalidBatchStateException.class)
                .hasMessageContaining("Cannot re-activate recalled batch");
    }

    @Test
    @DisplayName("isExpiringWithinDays correctly identifies stock expiring within 90 days")
    void testExpiringWithinDaysWarning() {
        Batch batchExpiringIn30Days = new Batch("B-30", "BATCH-30", paracetamol, 50, today.plusDays(30), today.minusDays(10));
        Batch batchExpiringIn120Days = new Batch("B-120", "BATCH-120", paracetamol, 50, today.plusDays(120), today.minusDays(10));
        Batch alreadyExpired = new Batch("B-PAST", "BATCH-PAST", paracetamol, 50, today.minusDays(5), today.minusDays(50));

        assertThat(batchExpiringIn30Days.isExpiringWithinDays(today, 90)).isTrue();
        assertThat(batchExpiringIn120Days.isExpiringWithinDays(today, 90)).isFalse();
        assertThat(alreadyExpired.isExpiringWithinDays(today, 90)).isFalse();
    }
}
