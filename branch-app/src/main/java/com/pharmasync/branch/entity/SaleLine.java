package com.pharmasync.branch.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "sale_lines")
public class SaleLine {

    @Id
    @Column(nullable = false, length = 64)
    private String id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    public SaleLine() {
    }

    public SaleLine(String id,
                    Sale sale,
                    Medicine medicine,
                    Batch batch,
                    int quantity,
                    BigDecimal unitPrice,
                    BigDecimal subtotal) {
        this.id = Objects.requireNonNull(id, "SaleLine ID must not be null");
        this.sale = sale;
        this.medicine = Objects.requireNonNull(medicine, "Medicine must not be null");
        this.batch = Objects.requireNonNull(batch, "Batch must not be null");
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice, "UnitPrice must not be null");
        this.subtotal = Objects.requireNonNull(subtotal, "Subtotal must not be null");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaleLine saleLine = (SaleLine) o;
        return Objects.equals(id, saleLine.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
