package com.pharmasync.branch;

import com.pharmasync.branch.dto.DashboardSummary;
import com.pharmasync.branch.dto.PrescriptionSaleDto;
import com.pharmasync.branch.dto.RecallReportDto;
import com.pharmasync.branch.entity.*;
import com.pharmasync.branch.repository.*;
import com.pharmasync.domain.batch.BatchStatus;
import com.pharmasync.domain.catalogue.DosageForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleLineRepository saleLineRepository;

    @Autowired
    private RecallRepository recallRepository;

    private Medicine rxMedicine;
    private Batch rxBatch;
    private Medicine otcMedicine;
    private Batch otcBatch;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();

        rxMedicine = new Medicine(
                "MED-RX-REPORT-" + UUID.randomUUID(), "Ciprofloxacin", "Cipro",
                "500mg", DosageForm.TABLET, BigDecimal.valueOf(18.00), true, 10, 10
        );
        medicineRepository.save(rxMedicine);
        rxBatch = new Batch(
                "BATCH-RX-REPORT-" + UUID.randomUUID(), "RX-001", rxMedicine,
                20, today.plusMonths(6), today.minusMonths(1), BatchStatus.ACTIVE, null
        );
        batchRepository.save(rxBatch);

        otcMedicine = new Medicine(
                "MED-OTC-REPORT-" + UUID.randomUUID(), "Ibuprofen", "Advil",
                "400mg", DosageForm.TABLET, BigDecimal.valueOf(5.00), false, 10, 10
        );
        medicineRepository.save(otcMedicine);
        otcBatch = new Batch(
                "BATCH-OTC-REPORT-" + UUID.randomUUID(), "OTC-001", otcMedicine,
                50, today.plusMonths(6), today.minusMonths(1), BatchStatus.ACTIVE, null
        );
        batchRepository.save(otcBatch);
    }

    @Test
    @DisplayName("Prescription register lists Rx sales with prescriber details")
    void prescriptionRegister() throws Exception {
        var sale = new Sale("S-RX-1", "R-RX-1", Instant.now(), BigDecimal.valueOf(36.00),
                "Alice Smith", "DOC-777", LocalDate.now());
        saleRepository.save(sale);
        sale.addLine(new SaleLine("L-RX-1", sale, rxMedicine, rxBatch, 2,
                BigDecimal.valueOf(18.00), BigDecimal.valueOf(36.00)));
        saleRepository.save(sale);

        // Non-Rx sale must not appear.
        var otcSale = new Sale("S-OTC-1", "R-OTC-1", Instant.now(), BigDecimal.valueOf(10.00),
                null, null, null);
        saleRepository.save(otcSale);
        sale.addLine(new SaleLine("L-OTC-1", otcSale, otcMedicine, otcBatch, 2,
                BigDecimal.valueOf(5.00), BigDecimal.valueOf(10.00)));
        saleRepository.save(otcSale);

        mockMvc.perform(get("/api/v1/reports/prescriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientName").value("Alice Smith"))
                .andExpect(jsonPath("$[0].prescriberRegNo").value("DOC-777"))
                .andExpect(jsonPath("$[0].medicineName").value("Cipro"));
    }

    @Test
    @DisplayName("Recall report returns quantity on hand and units sold after recall time")
    void recallReport() throws Exception {
        var recall = new Recall("R-1", rxBatch.getId(), Instant.now().minusSeconds(3600), "Safety", true);
        recallRepository.save(recall);

        // A sale after the recall time.
        var sale = new Sale("S-RX-2", "R-RX-2", Instant.now(), BigDecimal.valueOf(18.00),
                null, null, null);
        saleRepository.save(sale);
        sale.addLine(new SaleLine("L-RX-2", sale, rxMedicine, rxBatch, 3,
                BigDecimal.valueOf(18.00), BigDecimal.valueOf(54.00)));
        saleRepository.save(sale);

        mockMvc.perform(get("/api/v1/reports/recalls/{batchId}", rxBatch.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batchId").value(rxBatch.getId()))
                .andExpect(jsonPath("$.quantityOnHand").value(20))
                .andExpect(jsonPath("$.unitsSoldAfterRecall").value(3));
    }

    @Test
    @DisplayName("Dashboard returns stock lines, last sync time, pending ops and expiring value")
    void dashboard() throws Exception {
        mockMvc.perform(get("/api/v1/reports/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockByBranchBatch").isArray())
                .andExpect(jsonPath("$.pendingOperations").isNumber())
                .andExpect(jsonPath("$.valueExpiringWithin90Days").isNumber());
    }
}
