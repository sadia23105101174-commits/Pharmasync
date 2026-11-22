package com.pharmasync.branch.init;

import com.pharmasync.branch.entity.Batch;
import com.pharmasync.branch.entity.Medicine;
import com.pharmasync.branch.repository.BatchRepository;
import com.pharmasync.branch.repository.MedicineRepository;
import com.pharmasync.domain.batch.BatchStatus;
import com.pharmasync.domain.catalogue.DosageForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds initial demo medicines and batches on branch startup if database is empty.
 */
@Component
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final MedicineRepository medicineRepository;
    private final BatchRepository batchRepository;

    public DemoDataInitializer(MedicineRepository medicineRepository, BatchRepository batchRepository) {
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
    }

    @Override
    public void run(String... args) {
        if (medicineRepository.count() > 0) {
            log.info("Database already seeded. Skipping demo data initialization.");
            return;
        }

        log.info("Seeding demo medicines and batches for Branch POS...");

        LocalDate today = LocalDate.now();

        // 1. Panadol (OTC, with early expiry batch, later expiry batch, and expired batch)
        Medicine panadol = new Medicine(
                "MED-PANADOL",
                "Paracetamol",
                "Panadol",
                "500mg",
                DosageForm.TABLET,
                BigDecimal.valueOf(4.50),
                false,
                10,
                10
        );
        medicineRepository.save(panadol);

        Batch panadolBatch1 = new Batch(
                "BATCH-PAN-01",
                "PAN-2026-A1",
                panadol,
                50,
                today.plusMonths(3), // Earliest expiry
                today.minusMonths(1),
                BatchStatus.ACTIVE,
                "Regular stock"
        );
        Batch panadolBatch2 = new Batch(
                "BATCH-PAN-02",
                "PAN-2026-A2",
                panadol,
                100,
                today.plusMonths(12), // Later expiry
                today.minusMonths(1),
                BatchStatus.ACTIVE,
                "Regular stock"
        );
        Batch panadolExpired = new Batch(
                "BATCH-PAN-EXP",
                "PAN-2025-EXP",
                panadol,
                20,
                today.minusMonths(2), // Expired!
                today.minusYears(1),
                BatchStatus.EXPIRED,
                "Expired stock awaiting return"
        );
        batchRepository.saveAll(List.of(panadolBatch1, panadolBatch2, panadolExpired));

        // 2. Amoxil (Prescription-Only Antibiotic)
        Medicine amoxil = new Medicine(
                "MED-AMOXIL",
                "Amoxicillin",
                "Amoxil",
                "500mg",
                DosageForm.CAPSULE,
                BigDecimal.valueOf(14.50),
                true,
                5,
                10
        );
        medicineRepository.save(amoxil);

        Batch amoxilBatch = new Batch(
                "BATCH-AMX-01",
                "AMX-2026-B1",
                amoxil,
                80,
                today.plusMonths(9),
                today.minusMonths(2),
                BatchStatus.ACTIVE,
                "Regular antibiotic stock"
        );
        batchRepository.save(amoxilBatch);

        // 3. Ventolin (Prescription-Only Inhaler)
        Medicine ventolin = new Medicine(
                "MED-VENTOLIN",
                "Salbutamol",
                "Ventolin",
                "100mcg",
                DosageForm.INHALER,
                BigDecimal.valueOf(22.00),
                true,
                1,
                1
        );
        medicineRepository.save(ventolin);

        Batch ventolinBatch = new Batch(
                "BATCH-VEN-01",
                "VEN-2026-C1",
                ventolin,
                35,
                today.plusMonths(18),
                today.minusMonths(1),
                BatchStatus.ACTIVE,
                "Inhaler stock"
        );
        batchRepository.save(ventolinBatch);

        // 4. CoughClear (Recalled Cough Syrup)
        Medicine coughClear = new Medicine(
                "MED-COUGHCLEAR",
                "Dextromethorphan",
                "CoughClear",
                "15mg/5ml",
                DosageForm.SYRUP,
                BigDecimal.valueOf(8.50),
                false,
                1,
                1
        );
        medicineRepository.save(coughClear);

        Batch coughClearRecalled = new Batch(
                "BATCH-CC-REC",
                "CC-2026-REC",
                coughClear,
                25,
                today.plusMonths(6),
                today.minusMonths(3),
                BatchStatus.RECALLED,
                "Central recall: manufacturer packaging defect"
        );
        batchRepository.save(coughClearRecalled);

        log.info("Demo data initialized successfully with 4 medicines and 6 batches.");
    }
}
