package com.pharmasync.domain.batch;

import com.pharmasync.domain.exception.InvalidBatchStateException;

import java.time.LocalDate;

public class RecalledBatchState implements BatchState {

    @Override
    public BatchStatus getStatus() {
        return BatchStatus.RECALLED;
    }

    @Override
    public boolean canSell(Batch batch, LocalDate asOfDate) {
        return false;
    }

    @Override
    public void sell(Batch batch, int quantity, LocalDate asOfDate) {
        throw new InvalidBatchStateException("Cannot sell recalled batch: " + batch.getBatchNumber());
    }

    @Override
    public void quarantine(Batch batch, String reason) {
        throw new InvalidBatchStateException("Batch " + batch.getBatchNumber() + " is already recalled");
    }

    @Override
    public void recall(Batch batch, String reason) {
        // already recalled
    }

    @Override
    public void activate(Batch batch, LocalDate asOfDate) {
        throw new InvalidBatchStateException("Cannot re-activate recalled batch: " + batch.getBatchNumber());
    }

    @Override
    public void expire(Batch batch) {
        // recalled takes precedence, remain recalled
    }

    @Override
    public String toString() {
        return "RecalledBatchState";
    }
}
