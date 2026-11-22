package com.pharmasync.branch.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @Column(nullable = false, length = 64)
    private String id;

    @Column(nullable = false, unique = true, length = 64)
    private String receiptNumber;

    @Column(nullable = false)
    private Instant saleTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 128)
    private String patientName;

    @Column(length = 64)
    private String prescriberRegNo;

    @Column
    private LocalDate prescriptionDate;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SaleLine> lines = new ArrayList<>();

    public Sale() {
    }

    public Sale(String id,
                String receiptNumber,
                Instant saleTime,
                BigDecimal totalAmount,
                String patientName,
                String prescriberRegNo,
                LocalDate prescriptionDate) {
        this.id = Objects.requireNonNull(id, "Sale ID must not be null");
        this.receiptNumber = Objects.requireNonNull(receiptNumber, "Receipt number must not be null");
        this.saleTime = Objects.requireNonNull(saleTime, "Sale time must not be null");
        this.totalAmount = Objects.requireNonNull(totalAmount, "Total amount must not be null");
        this.patientName = patientName;
        this.prescriberRegNo = prescriberRegNo;
        this.prescriptionDate = prescriptionDate;
    }

    public void addLine(SaleLine line) {
        lines.add(line);
        line.setSale(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public Instant getSaleTime() {
        return saleTime;
    }

    public void setSaleTime(Instant saleTime) {
        this.saleTime = saleTime;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPrescriberRegNo() {
        return prescriberRegNo;
    }

    public void setPrescriberRegNo(String prescriberRegNo) {
        this.prescriberRegNo = prescriberRegNo;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public List<SaleLine> getLines() {
        return lines;
    }

    public void setLines(List<SaleLine> lines) {
        this.lines = lines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sale sale = (Sale) o;
        return Objects.equals(id, sale.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
