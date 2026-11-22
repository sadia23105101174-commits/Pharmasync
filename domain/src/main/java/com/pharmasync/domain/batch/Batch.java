package com.pharmasync.domain.batch;

import com.pharmasync.domain.catalogue.Medicine;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Batch entity representing a tracked lot of medicine with State pattern lifecycle.
 * Manages quantity on hand in base units, expiry date, and status transitions.
 */
public class Batch {
    private final String id;
    private final String batchNumber;
    private final Medicine medicine;
    private final LocalDate expiryDate;
    private final LocalDate receivedDate;

    private int quantityOnHand;
    private BatchState state;
    private String stateReason;

    public Batch(String id,
                 String batchNumber,
                 Medicine medicine,
                 int initialQuantity,
                 LocalDate expiryDate,
                 LocalDate receivedDate) {
        this.id = Objects.requireNonNull(id, "Batch ID must not be null");
        this.batchNumber = Objects.requireNonNull(batchNumber, "Batch number must not be null");
        this.medicine = Objects.requireNonNull(medicine, "Medicine must not be null");
        if (initialQuantity < 0) {
            throw new IllegalArgumentException("Initial quantity cannot be negative");
        }
        this.quantityOnHand = initialQuantity;
        this.expiryDate = Objects.requireNonNull(expiryDate, "Expiry date must not be null");
        this.receivedDate = Objects.requireNonNull(receivedDate, "Received date must not be null");
        this.state = new ActiveBatchState();
        this.stateReason = "Initial receipt";
    }

    public Batch(String id,
                 String batchNumber,
                 Medicine medicine,
                 int initialQuantity,
                 LocalDate expiryDate,
                 LocalDate receivedDate,
                 BatchState initialState,
                 String stateReason) {
        this.id = Objects.requireNonNull(id, "Batch ID must not be null");
        this.batchNumber = Objects.requireNonNull(batchNumber, "Batch number must not be null");
        this.medicine = Objects.requireNonNull(medicine, "Medicine must not be null");
        if (initialQuantity < 0) {
            throw new IllegalArgumentException("Initial quantity cannot be negative");
        }
        this.quantityOnHand = initialQuantity;
        this.expiryDate = Objects.requireNonNull(expiryDate, "Expiry date must not be null");
        this.receivedDate = Objects.requireNonNull(receivedDate, "Received date must not be null");
        this.state = Objects.requireNonNull(initialState, "Initial state must not be null");
        this.stateReason = stateReason;
    }

    // State Pattern Delegation

    public boolean canSell(LocalDate asOfDate) {
        return state.canSell(this, asOfDate);
    }

    public void sell(int quantity, LocalDate asOfDate) {
        state.sell(this, quantity, asOfDate);
    }

    public void quarantine(String reason) {
        state.quarantine(this, reason);
    }

    public void recall(String reason) {
        state.recall(this, reason);
    }

    public void activate(LocalDate asOfDate) {
        state.activate(this, asOfDate);
    }

    public void expire() {
        state.expire(this);
    }

    public void receiveStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Received quantity must be positive, got: " + quantity);
        }
        this.quantityOnHand += quantity;
    }

    void transitionToState(BatchState newState, String reason) {
        this.state = Objects.requireNonNull(newState);
        this.stateReason = reason;
    }

    // Helper methods for expiry rules

    public boolean isExpired(LocalDate asOfDate) {
        return asOfDate != null && asOfDate.isAfter(expiryDate);
    }

    public long daysUntilExpiry(LocalDate asOfDate) {
        if (asOfDate == null) return 0;
        return ChronoUnit.DAYS.between(asOfDate, expiryDate);
    }

    public boolean isExpiringWithinDays(LocalDate asOfDate, int days) {
        if (isExpired(asOfDate)) {
            return false;
        }
        long remainingDays = daysUntilExpiry(asOfDate);
        return remainingDays >= 0 && remainingDays <= days;
    }

    // Getters and internal setters

    public String getId() {
        return id;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(int quantityOnHand) {
        if (quantityOnHand < 0) {
            throw new IllegalArgumentException("Quantity on hand cannot be negative");
        }
        this.quantityOnHand = quantityOnHand;
    }

    public BatchState getState() {
        return state;
    }

    public BatchStatus getStatus() {
        return state.getStatus();
    }

    public String getStateReason() {
        return stateReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Batch batch = (Batch) o;
        return Objects.equals(id, batch.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Batch{" +
                "id='" + id + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", medicine=" + medicine.getBrandName() +
                ", quantityOnHand=" + quantityOnHand +
                ", expiryDate=" + expiryDate +
                ", state=" + state.getStatus() +
                '}';
    }
}
