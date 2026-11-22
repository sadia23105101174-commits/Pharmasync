package com.pharmasync.domain.batch;

import com.pharmasync.domain.exception.InsufficientStockException;
import com.pharmasync.domain.exception.InvalidBatchStateException;

import java.time.LocalDate;

public class ActiveBatchState implements BatchState {

    @Override
    public BatchStatus getStatus() {
        return BatchStatus.ACTIVE;
    }

    @Override
    public boolean canSell(Batch batch, LocalDate asOfDate) {
        if (asOfDate != null && batch.getExpiryDate() != null && asOfDate.isAfter(batch.getExpiryDate())) {
            batch.expire();
            return false;
        }
        return batch.getQuantityOnHand() > 0;
    }

    @Override
    public void sell(Batch batch, int quantity, LocalDate asOfDate) {
        if (!canSell(batch, asOfDate)) {
            if (asOfDate != null && batch.getExpiryDate() != null && asOfDate.isAfter(batch.getExpiryDate())) {
                throw new InvalidBatchStateException("Cannot sell expired batch: " + batch.getBatchNumber());
            }
            if (batch.getQuantityOnHand() <= 0) {
                throw new InsufficientStockException("Batch " + batch.getBatchNumber() + " has zero stock available");
            }
            throw new InvalidBatchStateException("Batch " + batch.getBatchNumber() + " cannot be sold in current state: " + getStatus());
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Sale quantity must be positive, got: " + quantity);
        }
        if (quantity > batch.getQuantityOnHand()) {
            throw new InsufficientStockException("Insufficient stock in batch " + batch.getBatchNumber() +
                    ". Requested: " + quantity + ", available: " + batch.getQuantityOnHand());
        }
        batch.setQuantityOnHand(batch.getQuantityOnHand() - quantity);
    }

    @Override
    public void quarantine(Batch batch, String reason) {
        batch.transitionToState(new QuarantinedBatchState(), reason);
    }

    @Override
    public void recall(Batch batch, String reason) {
        batch.transitionToState(new RecalledBatchState(), reason);
    }

    @Override
    public void activate(Batch batch, LocalDate asOfDate) {
        // already active
    }

    @Override
    public void expire(Batch batch) {
        batch.transitionToState(new ExpiredBatchState(), "Expired on " + batch.getExpiryDate());
    }

    @Override
    public String toString() {
        return "ActiveBatchState";
    }
}
