package com.pharmasync.branch.adapter.printer;

import org.springframework.stereotype.Component;

/**
 * Adapter in the Adapter Pattern.
 * Adapts high-level domain {@link Receipt} objects into formatted plain text
 * consumable by the physical thermal printer device {@link EscPosThermalPrinterDevice}.
 */
@Component
public class MockReceiptPrinterAdapter implements ReceiptPrinter {

    private final EscPosThermalPrinterDevice hardwareDevice;

    public MockReceiptPrinterAdapter(EscPosThermalPrinterDevice hardwareDevice) {
        this.hardwareDevice = hardwareDevice;
    }

    @Override
    public PrintedReceipt print(Receipt receipt) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("         PHARMASYNC BRANCH POS          \n");
        sb.append("               RECEIPT                  \n");
        sb.append("========================================\n");
        sb.append(String.format("Receipt No: %s\n", receipt.getReceiptNumber()));
        sb.append(String.format("Branch ID:  %s\n", receipt.getBranchId()));
        sb.append(String.format("Date/Time:  %s\n", receipt.getTimestamp()));
        if (receipt.getPatientName() != null && !receipt.getPatientName().isBlank()) {
            sb.append(String.format("Patient:    %s\n", receipt.getPatientName()));
        }
        if (receipt.getPrescriberRegNo() != null && !receipt.getPrescriberRegNo().isBlank()) {
            sb.append(String.format("Prescriber: %s\n", receipt.getPrescriberRegNo()));
        }
        if (receipt.getPrescriptionDate() != null) {
            sb.append(String.format("Rx Date:    %s\n", receipt.getPrescriptionDate()));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-18s %4s %7s %8s\n", "Item (Batch)", "Qty", "Price", "Total"));
        sb.append("----------------------------------------\n");

        for (ReceiptItem item : receipt.getItems()) {
            String nameAndBatch = item.getMedicineName() + " (" + item.getBatchNumber() + ")";
            if (nameAndBatch.length() > 18) {
                nameAndBatch = nameAndBatch.substring(0, 18);
            }
            sb.append(String.format("%-18s %4d %7.2f %8.2f\n",
                    nameAndBatch,
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()));
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("TOTAL: %33.2f\n", receipt.getTotalAmount()));
        sb.append("========================================\n");
        sb.append("      Thank you for your visit!         \n");
        sb.append("========================================\n");

        String formattedReceipt = sb.toString();
        hardwareDevice.transmitRawText(formattedReceipt);
        return new PrintedReceipt(receipt.getReceiptNumber(), formattedReceipt, true);
    }

    public EscPosThermalPrinterDevice getHardwareDevice() {
        return hardwareDevice;
    }
}
