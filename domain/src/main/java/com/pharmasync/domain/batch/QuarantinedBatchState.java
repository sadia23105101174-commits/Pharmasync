package com.pharmasync.domain.batch;

import com.pharmasync.domain.exception.InvalidBatchStateException;

import java.time.LocalDate;

public class QuarantinedBatchState implements BatchState {

    @Override
    public BatchStatus getStatus() {
        return BatchStatus.QUARANTINED;
    }

    @Override
    public boolean canSell(Batch batch, LocalDate asOfDate) {
        return false;
    }

    @Override
    public void sell(Batch batch, int quantity, LocalDate asOfDate) {
        throw new InvalidBatchStateException("Cannot sell quarantined batch: " + batch.getBatchNumber());
    }

    @Override
    public void quarantine(Batch batch, String reason) {
        // already quarantined
    }

    @Override
    public void recall(Batch batch, String reason) {
        batch.transitionToState(new RecalledBatchState(), reason);
    }

    @Override
    public void activate(Batch batch, LocalDate asOfDate) {
        if (asOfDate != null && batch.getExpiryDate() != null && asOfDate.isAfter(batch.getExpiryDate())) {
            batch.expire();
            throw new InvalidBatchStateException("Cannot activate batch " + batch.getBatchNumber() + " because it is expired");
        }
        batch.transitionToState(new ActiveBatchState(), "Released from quarantine");
    }

    @Override
    public void expire(Batch batch) {
        batch.transitionToState(new ExpiredBatchState(), "Expired while in quarantine");
    }

    @Override
    public String toString() {
        return "QuarantinedBatchState";
    }
}
