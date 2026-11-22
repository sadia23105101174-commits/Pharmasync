package com.pharmasync.branch.controller;

import com.pharmasync.branch.adapter.printer.EscPosThermalPrinterDevice;
import com.pharmasync.branch.dto.SaleRequest;
import com.pharmasync.branch.entity.Batch;
import com.pharmasync.branch.entity.Medicine;
import com.pharmasync.branch.entity.Sale;
import com.pharmasync.branch.repository.BatchRepository;
import com.pharmasync.branch.repository.MedicineRepository;
import com.pharmasync.branch.repository.SaleRepository;
import com.pharmasync.branch.service.SaleService;
import com.pharmasync.domain.batch.BatchStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CounterWebController {

    private final MedicineRepository medicineRepository;
    private final BatchRepository batchRepository;
    private final SaleRepository saleRepository;
    private final SaleService saleService;
    private final EscPosThermalPrinterDevice printerDevice;

    public CounterWebController(MedicineRepository medicineRepository,
                                BatchRepository batchRepository,
                                SaleRepository saleRepository,
                                SaleService saleService,
                                EscPosThermalPrinterDevice printerDevice) {
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.saleRepository = saleRepository;
        this.saleService = saleService;
        this.printerDevice = printerDevice;
    }

    @GetMapping({"/", "/counter"})
    public String showCounterScreen(Model model) {
        populateModel(model, new SaleRequest(), null, null);
        return "counter";
    }

    @PostMapping("/counter/sale")
    public String completeSale(@ModelAttribute("saleForm") SaleRequest form, Model model) {
        try {
            Sale sale = saleService.processSale(form);
            String receiptText = printerDevice.getLastPrintedReceiptText();
            String successMsg = "Sale completed successfully! Receipt #" + sale.getReceiptNumber() + " printed.";
            populateModel(model, new SaleRequest(), receiptText, successMsg);
        } catch (Exception ex) {
            populateModel(model, form, null, null);
            model.addAttribute("error", ex.getMessage());
        }
        return "counter";
    }

    private void populateModel(Model model, SaleRequest form, String receiptText, String successMessage) {
        List<Medicine> medicines = medicineRepository.findAll();
        LocalDate today = LocalDate.now();

        List<MedicineCounterItem> medicineItems = new ArrayList<>();
        for (Medicine med : medicines) {
            List<Batch> batches = batchRepository.findByMedicineId(med.getId());
            int availableStock = batches.stream()
                    .filter(b -> b.canSell(today))
                    .mapToInt(Batch::getQuantityOnHand)
                    .sum();
            medicineItems.add(new MedicineCounterItem(
                    med.getId(),
                    med.getBrandName(),
                    med.getGenericName(),
                    med.getStrength(),
                    med.getPrice(),
                    med.isPrescriptionRequired(),
                    availableStock
            ));
        }

        List<Sale> recentSales = saleRepository.findTop5ByOrderBySaleTimeDesc();

        model.addAttribute("medicines", medicineItems);
        model.addAttribute("recentSales", recentSales);
        model.addAttribute("saleForm", form);
        model.addAttribute("receiptText", receiptText);
        model.addAttribute("successMessage", successMessage);
    }

    public static class MedicineCounterItem {
        private final String id;
        private final String brandName;
        private final String genericName;
        private final String strength;
        private final java.math.BigDecimal price;
        private final boolean prescriptionRequired;
        private final int availableStock;

        public MedicineCounterItem(String id, String brandName, String genericName, String strength,
                                   java.math.BigDecimal price, boolean prescriptionRequired, int availableStock) {
            this.id = id;
            this.brandName = brandName;
            this.genericName = genericName;
            this.strength = strength;
            this.price = price;
            this.prescriptionRequired = prescriptionRequired;
            this.availableStock = availableStock;
        }

        public String getId() { return id; }
        public String getBrandName() { return brandName; }
        public String getGenericName() { return genericName; }
        public String getStrength() { return strength; }
        public java.math.BigDecimal getPrice() { return price; }
        public boolean isPrescriptionRequired() { return prescriptionRequired; }
        public int getAvailableStock() { return availableStock; }

        public String getDisplayName() {
            return String.format("%s (%s %s) - $%.2f [Stock: %d]%s",
                    brandName, genericName, strength, price, availableStock,
                    prescriptionRequired ? " [Rx Required]" : "");
        }
    }
}
