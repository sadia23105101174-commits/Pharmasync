package com.pharmasync.branch.entity;

import com.pharmasync.domain.catalogue.DosageForm;
import com.pharmasync.domain.composite.PackHierarchy;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @Column(nullable = false, length = 64)
    private String id;

    @Column(nullable = false)
    private String genericName;

    @Column(nullable = false)
    private String brandName;

    @Column(nullable = false)
    private String strength;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DosageForm form;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean prescriptionRequired;

    @Column(nullable = false)
    private int stripsPerBox = 10;

    @Column(nullable = false)
    private int tabletsPerStrip = 10;

    public Medicine() {
    }

    public Medicine(String id,
                    String genericName,
                    String brandName,
                    String strength,
                    DosageForm form,
                    BigDecimal price,
                    boolean prescriptionRequired,
                    int stripsPerBox,
                    int tabletsPerStrip) {
        this.id = Objects.requireNonNull(id, "Medicine ID must not be null");
        this.genericName = Objects.requireNonNull(genericName, "Generic name must not be null");
        this.brandName = Objects.requireNonNull(brandName, "Brand name must not be null");
        this.strength = Objects.requireNonNull(strength, "Strength must not be null");
        this.form = Objects.requireNonNull(form, "Dosage form must not be null");
        this.price = Objects.requireNonNull(price, "Price must not be null");
        this.prescriptionRequired = prescriptionRequired;
        this.stripsPerBox = stripsPerBox > 0 ? stripsPerBox : 10;
        this.tabletsPerStrip = tabletsPerStrip > 0 ? tabletsPerStrip : 10;
    }

    public com.pharmasync.domain.catalogue.Medicine toDomain() {
        PackHierarchy hierarchy = PackHierarchy.boxStripTablet(
                stripsPerBox > 0 ? stripsPerBox : 10,
                tabletsPerStrip > 0 ? tabletsPerStrip : 10
        );
        return new com.pharmasync.domain.catalogue.Medicine(
                this.id,
                this.genericName,
                this.brandName,
                this.strength,
                this.form,
                this.prescriptionRequired,
                hierarchy
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public DosageForm getForm() {
        return form;
    }

    public void setForm(DosageForm form) {
        this.form = form;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isPrescriptionRequired() {
        return prescriptionRequired;
    }

    public void setPrescriptionRequired(boolean prescriptionRequired) {
        this.prescriptionRequired = prescriptionRequired;
    }

    public int getStripsPerBox() {
        return stripsPerBox;
    }

    public void setStripsPerBox(int stripsPerBox) {
        this.stripsPerBox = stripsPerBox;
    }

    public int getTabletsPerStrip() {
        return tabletsPerStrip;
    }

    public void setTabletsPerStrip(int tabletsPerStrip) {
        this.tabletsPerStrip = tabletsPerStrip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medicine medicine = (Medicine) o;
        return Objects.equals(id, medicine.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
