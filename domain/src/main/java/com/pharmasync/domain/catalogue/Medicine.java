package com.pharmasync.domain.catalogue;

import com.pharmasync.domain.composite.PackHierarchy;

import java.util.Objects;

/**
 * Catalogue Medicine entity representing a drug product.
 * Holds generic name, brand name, strength, dosage form, prescription-only flag, and pack hierarchy.
 */
public class Medicine {
    private final String id;
    private final String genericName;
    private final String brandName;
    private final String strength;
    private final DosageForm form;
    private final boolean prescriptionRequired;
    private final PackHierarchy packHierarchy;

    public Medicine(String id,
                    String genericName,
                    String brandName,
                    String strength,
                    DosageForm form,
                    boolean prescriptionRequired,
                    PackHierarchy packHierarchy) {
        this.id = Objects.requireNonNull(id, "Medicine ID must not be null");
        this.genericName = Objects.requireNonNull(genericName, "Generic name must not be null");
        this.brandName = Objects.requireNonNull(brandName, "Brand name must not be null");
        this.strength = Objects.requireNonNull(strength, "Strength must not be null");
        this.form = Objects.requireNonNull(form, "Dosage form must not be null");
        this.prescriptionRequired = prescriptionRequired;
        this.packHierarchy = Objects.requireNonNull(packHierarchy, "Pack hierarchy must not be null");
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

    public boolean isPrescriptionRequired() {
        return prescriptionRequired;
    }

    public PackHierarchy getPackHierarchy() {
        return packHierarchy;
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

    @Override
    public String toString() {
        return "Medicine{" +
                "id='" + id + '\'' +
                ", genericName='" + genericName + '\'' +
                ", brandName='" + brandName + '\'' +
                ", strength='" + strength + '\'' +
                ", form=" + form +
                ", prescriptionRequired=" + prescriptionRequired +
                '}';
    }
}
