package com.pharmasync.branch.adapter.printer;

public class PrintedReceipt {
    private final String receiptNumber;
    private final String formattedText;
    private final boolean success;

    public PrintedReceipt(String receiptNumber, String formattedText, boolean success) {
        this.receiptNumber = receiptNumber;
        this.formattedText = formattedText;
        this.success = success;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public String getFormattedText() {
        return formattedText;
    }

    public boolean isSuccess() {
        return success;
    }
}
