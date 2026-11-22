package com.pharmasync.domain.batch;

import java.time.LocalDate;

/**
 * State pattern interface for batch lifecycle management.
 * States: Active, Quarantined, Recalled, Expired.
 */
public interface BatchState {
    BatchStatus getStatus();

    /**
     * Verifies if this batch can be sold as of the given date.
     */
    boolean canSell(Batch batch, LocalDate asOfDate);

    /**
     * Decrements stock by quantity if permitted by the current state.
     */
    void sell(Batch batch, int quantity, LocalDate asOfDate);

    /**
     * Transitions batch to QUARANTINED state.
     */
    void quarantine(Batch batch, String reason);

    /**
     * Transitions batch to RECALLED state.
     */
    void recall(Batch batch, String reason);

    /**
     * Re-activates a quarantined batch if it is not expired.
     */
    void activate(Batch batch, LocalDate asOfDate);

    /**
     * Marks the batch as EXPIRED.
     */
    void expire(Batch batch);
}
