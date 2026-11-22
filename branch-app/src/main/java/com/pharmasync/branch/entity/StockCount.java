package com.pharmasync.branch.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "stock_counts")
public class StockCount {

    @Id
    @Column(nullable = false, length = 64)
    private String id;

    @Column(nullable = false)
    private Instant countTime;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @Column(nullable = false)
    private int expectedQuantity;

    @Column(nullable = false)
    private int countedQuantity;

    @Column(nullable = false)
    private int difference;

    @Column(length = 255)
    private String notes;

    public StockCount() {
    }

    public StockCount(String id,
                      Instant countTime,
                      Batch batch,
                      int expectedQuantity,
                      int countedQuantity,
                      int difference,
                      String notes) {
        this.id = Objects.requireNonNull(id, "StockCount ID must not be null");
        this.countTime = Objects.requireNonNull(countTime, "Count time must not be null");
        this.batch = Objects.requireNonNull(batch, "Batch must not be null");
        this.expectedQuantity = expectedQuantity;
        this.countedQuantity = countedQuantity;
        this.difference = difference;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getCountTime() {
        return countTime;
    }

    public void setCountTime(Instant countTime) {
        this.countTime = countTime;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public int getExpectedQuantity() {
        return expectedQuantity;
    }

    public void setExpectedQuantity(int expectedQuantity) {
        this.expectedQuantity = expectedQuantity;
    }

    public int getCountedQuantity() {
        return countedQuantity;
    }

    public void setCountedQuantity(int countedQuantity) {
        this.countedQuantity = countedQuantity;
    }

    public int getDifference() {
        return difference;
    }

    public void setDifference(int difference) {
        this.difference = difference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockCount that = (StockCount) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
