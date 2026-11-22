package com.pharmasync.branch.exception;

public class PrescriptionRequiredException extends RuntimeException {
    public PrescriptionRequiredException(String message) {
        super(message);
    }
}
