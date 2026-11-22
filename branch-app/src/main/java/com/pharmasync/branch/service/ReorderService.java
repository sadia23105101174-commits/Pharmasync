package com.pharmasync.branch.service;

import com.pharmasync.branch.entity.Batch;
import com.pharmasync.branch.entity.Medicine;
import com.pharmasync.branch.entity.SaleLine;
import com.pharmasync.branch.dto.ReorderSuggestion;
import com.pharmasync.branch.repository.BatchRepository;
import com.pharmasync.branch.repository.MedicineRepository;
import com.pharmasync.branch.repository.SaleLineRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Suggests reorder quantities from 30-day sales velocity, supplier lead time
 * and safety stock: suggested = max(0, velocity*leadTime + safetyStock - onHand).
 */
@Service
public class ReorderService {

    private static final int DEFAULT_LEAD_TIME_DAYS = 7;
    private static final int DEFAULT_SAFETY_STOCK = 10;

    private final SaleLineRepository saleLineRepository;
    private final BatchRepository batchRepository;
    private final MedicineRepository medicineRepository;

    public ReorderService(SaleLineRepository saleLineRepository,
                          BatchRepository batchRepository,
                          MedicineRepository medicineRepository) {
        this.saleLineRepository = saleLineRepository;
        this.batchRepository = batchRepository;
        this.medicineRepository = medicineRepository;
    }

    public ReorderSuggestion suggest(String branchId, String medicineId) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found: " + medicineId));

        Instant now = Instant.now();
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);
        List<SaleLine> recentLines = saleLineRepository.findBySale_SaleTimeBetween(thirtyDaysAgo, now);

        int velocity30d = recentLines.stream()
                .filter(line -> medicineId.equals(line.getMedicine().getId()))
                .mapToInt(SaleLine::getQuantity)
                .sum();
        double velocityPerDay = velocity30d / 30.0;

        int onHand = batchRepository.findByMedicineId(medicineId).stream()
                .mapToInt(Batch::getQuantityOnHand)
                .sum();

        int suggested = (int) Math.ceil(velocityPerDay * DEFAULT_LEAD_TIME_DAYS + DEFAULT_SAFETY_STOCK - onHand);
        if (suggested < 0) suggested = 0;

        return new ReorderSuggestion(
                medicineId, medicine.getBrandName(), onHand,
                velocityPerDay, DEFAULT_LEAD_TIME_DAYS, DEFAULT_SAFETY_STOCK, suggested
        );
    }
}
