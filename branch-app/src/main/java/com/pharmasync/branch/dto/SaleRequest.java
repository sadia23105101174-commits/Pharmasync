package com.pharmasync.branch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class SaleRequest {

    @NotBlank(message = "Medicine ID must not be blank")
    private String medicineId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private String patientName;
    private String prescriberRegNo;
    private LocalDate prescriptionDate;

    // Optional specific batch override (e.g. barcode scan of a specific box)
    private String batchId;

    public SaleRequest() {
    }

    public SaleRequest(String medicineId, int quantity) {
        this.medicineId = medicineId;
        this.quantity = quantity;
    }

    public SaleRequest(String medicineId, int quantity, String patientName, String prescriberRegNo, LocalDate prescriptionDate) {
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.patientName = patientName;
        this.prescriberRegNo = prescriberRegNo;
        this.prescriptionDate = prescriptionDate;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
}
