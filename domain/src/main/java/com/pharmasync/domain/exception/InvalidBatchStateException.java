package com.pharmasync.domain.exception;

public class InvalidBatchStateException extends RuntimeException {
    public InvalidBatchStateException(String message) {
        super(message);
    }
}
