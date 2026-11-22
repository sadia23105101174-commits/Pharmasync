package com.pharmasync.branch.dto;

import com.pharmasync.branch.entity.Medicine;
import com.pharmasync.domain.catalogue.DosageForm;

import java.math.BigDecimal;

public class MedicineResponse {
    private String id;
    private String genericName;
    private String brandName;
    private String strength;
    private DosageForm form;
    private BigDecimal price;
    private boolean prescriptionRequired;
    private int stripsPerBox;
    private int tabletsPerStrip;

    public MedicineResponse() {
    }

    public static MedicineResponse fromEntity(Medicine m) {
        MedicineResponse res = new MedicineResponse();
        res.id = m.getId();
        res.genericName = m.getGenericName();
        res.brandName = m.getBrandName();
        res.strength = m.getStrength();
        res.form = m.getForm();
        res.price = m.getPrice();
        res.prescriptionRequired = m.isPrescriptionRequired();
        res.stripsPerBox = m.getStripsPerBox();
        res.tabletsPerStrip = m.getTabletsPerStrip();
        return res;
    }

    public String getId() {
        return id;
    }

    public String getGenericName() {
        return genericName;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getStrength() {
        return strength;
    }

    public DosageForm getForm() {
        return form;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isPrescriptionRequired() {
        return prescriptionRequired;
    }

    public int getStripsPerBox() {
        return stripsPerBox;
    }

    public int getTabletsPerStrip() {
        return tabletsPerStrip;
    }
}
