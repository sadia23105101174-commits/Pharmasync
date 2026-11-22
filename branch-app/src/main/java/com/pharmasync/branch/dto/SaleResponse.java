package com.pharmasync.branch.dto;

import com.pharmasync.branch.entity.Sale;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class SaleResponse {
    private String id;
    private String receiptNumber;
    private Instant saleTime;
    private BigDecimal totalAmount;
    private String patientName;
    private String prescriberRegNo;
    private LocalDate prescriptionDate;
    private List<SaleLineResponse> lines;

    public SaleResponse() {
    }

    public static SaleResponse fromEntity(Sale sale) {
        SaleResponse res = new SaleResponse();
        res.id = sale.getId();
        res.receiptNumber = sale.getReceiptNumber();
        res.saleTime = sale.getSaleTime();
        res.totalAmount = sale.getTotalAmount();
        res.patientName = sale.getPatientName();
        res.prescriberRegNo = sale.getPrescriberRegNo();
        res.prescriptionDate = sale.getPrescriptionDate();
        if (sale.getLines() != null) {
            res.lines = sale.getLines().stream().map(SaleLineResponse::fromEntity).toList();
        } else {
            res.lines = Collections.emptyList();
        }
        return res;
    }

    public String getId() {
        return id;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public Instant getSaleTime() {
        return saleTime;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPrescriberRegNo() {
        return prescriberRegNo;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public List<SaleLineResponse> getLines() {
        return lines;
    }
}
