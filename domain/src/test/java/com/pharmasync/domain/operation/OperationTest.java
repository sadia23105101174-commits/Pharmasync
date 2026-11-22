package com.pharmasync.domain.operation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationTest {

    @Test
    @DisplayName("LamportClock increments sequentially on tick and advances past received timestamp on update")
    void testLamportClockAdvancement() {
        LamportClock clock = new LamportClock();

        assertThat(clock.getTime()).isEqualTo(0L);
        assertThat(clock.tick()).isEqualTo(1L);
        assertThat(clock.tick()).isEqualTo(2L);

        // Receive message with clock 10 -> new local clock should be max(2, 10) + 1 = 11
        long updated = clock.update(10L);
        assertThat(updated).isEqualTo(11L);
        assertThat(clock.getTime()).isEqualTo(11L);

        // Receive message with smaller clock 5 -> new local clock should be max(11, 5) + 1 = 12
        assertThat(clock.update(5L)).isEqualTo(12L);
    }

    @Test
    @DisplayName("Operation holds UUID, logical clock, and lifecycle status changes")
    void testOperationLifecycle() {
        UUID opId = UUID.randomUUID();
        Operation op = Operation.create(
                opId,
                15L,
                "BRANCH-001",
                OperationType.SALE,
                "{\"items\":[{\"medicineId\":\"MED-001\",\"qty\":2}]}"
        );

        assertThat(op.getOperationId()).isEqualTo(opId);
        assertThat(op.getLamportClock()).isEqualTo(15L);
        assertThat(op.getBranchId()).isEqualTo("BRANCH-001");
        assertThat(op.getType()).isEqualTo(OperationType.SALE);
        assertThat(op.getStatus()).isEqualTo(OperationStatus.PENDING);
        assertThat(op.getPayload()).contains("MED-001");

        op.markApplied();
        assertThat(op.getStatus()).isEqualTo(OperationStatus.APPLIED);

        op.markAcknowledged();
        assertThat(op.getStatus()).isEqualTo(OperationStatus.ACKNOWLEDGED);
    }
}
