package com.pharmasync.branch.adapter.printer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adaptee in the Adapter Pattern.
 * Simulates a physical ESC/POS thermal receipt printer hardware device.
 * Consumes raw strings or byte-streams and logs them.
 */
public class EscPosThermalPrinterDevice {
    private final List<String> printLog = new CopyOnWriteArrayList<>();
    private volatile String lastPrintedReceiptText;

    public void transmitRawText(String rawText) {
        this.lastPrintedReceiptText = rawText;
        this.printLog.add(rawText);
    }

    public String getLastPrintedReceiptText() {
        return lastPrintedReceiptText;
    }

    public List<String> getPrintLog() {
        return List.copyOf(printLog);
    }

    public void clear() {
        printLog.clear();
        lastPrintedReceiptText = null;
    }
}
