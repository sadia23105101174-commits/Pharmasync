package com.pharmasync.domain.batch;

import com.pharmasync.domain.exception.InvalidBatchStateException;

import java.time.LocalDate;

public class ExpiredBatchState implements BatchState {

    @Override
    public BatchStatus getStatus() {
        return BatchStatus.EXPIRED;
    }

    @Override
    public boolean canSell(Batch batch, LocalDate asOfDate) {
        return false;
    }

    @Override
    public void sell(Batch batch, int quantity, LocalDate asOfDate) {
        throw new InvalidBatchStateException("Cannot sell expired batch: " + batch.getBatchNumber());
    }

    @Override
    public void quarantine(Batch batch, String reason) {
        throw new InvalidBatchStateException("Batch " + batch.getBatchNumber() + " is expired and cannot be quarantined");
    }

    @Override
    public void recall(Batch batch, String reason) {
        batch.transitionToState(new RecalledBatchState(), reason);
    }

    @Override
    public void activate(Batch batch, LocalDate asOfDate) {
        throw new InvalidBatchStateException("Cannot activate expired batch: " + batch.getBatchNumber());
    }

    @Override
    public void expire(Batch batch) {
        // already expired
    }

    @Override
    public String toString() {
        return "ExpiredBatchState";
    }
}
