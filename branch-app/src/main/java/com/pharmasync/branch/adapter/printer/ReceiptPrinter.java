package com.pharmasync.branch.adapter.printer;

/**
 * Target Interface in the Adapter Pattern.
 * Application services depend on this high-level abstraction to print receipts.
 */
public interface ReceiptPrinter {
    PrintedReceipt print(Receipt receipt);
}
