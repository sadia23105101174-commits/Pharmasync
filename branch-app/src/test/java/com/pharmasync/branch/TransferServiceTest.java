package com.pharmasync.branch;

import com.pharmasync.branch.entity.TransferState;
import com.pharmasync.branch.entity.*;
import com.pharmasync.branch.repository.*;
import com.pharmasync.branch.service.TransferService;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private BatchRepository batchRepository;

    private Medicine medicine;
    private Batch batch;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        medicine = new Medicine(
                "MED-TRANSFER-" + UUID.randomUUID(), "Ibuprofen", "Advil",
                "400mg", DosageForm.TABLET, BigDecimal.valueOf(5.00), false, 10, 10
        );
        medicineRepository.save(medicine);

        batch = new Batch(
                "BATCH-TRANSFER-" + UUID.randomUUID(), "TR-001", medicine,
                100, today.plusMonths(6), today.minusMonths(1),
                com.pharmasync.domain.batch.BatchStatus.ACTIVE, null
        );
        batchRepository.save(batch);
    }

    @Test
    @DisplayName("Transfer moves DISPATCHED -> IN_TRANSIT -> RECEIVED and flags quantity difference")
    void transferLifecycleFlagsQuantityDifference() {
        var transfer = transferService.createTransfer("BRANCH-A", "BRANCH-B", batch.getId(), 100);
        assertThat(transfer.getState()).isEqualTo(TransferState.DISPATCHED);
        assertThat(transfer.isQuantityDifference()).isFalse();

        transfer = transferService.markInTransit(transfer.getId());
        assertThat(transfer.getState()).isEqualTo(TransferState.IN_TRANSIT);

        // Receipt quantity differs from dispatched quantity -> flagged.
        transfer = transferService.receiveTransfer(transfer.getId(), 95);
        assertThat(transfer.getState()).isEqualTo(TransferState.RECEIVED);
        assertThat(transfer.getReceivedQuantity()).isEqualTo(95);
        assertThat(transfer.isQuantityDifference()).isTrue();

        var stored = transferRepository.findById(transfer.getId()).orElseThrow();
        assertThat(stored.isQuantityDifference()).isTrue();
    }
}
