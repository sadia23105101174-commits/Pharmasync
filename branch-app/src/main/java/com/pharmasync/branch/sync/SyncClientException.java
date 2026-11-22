package com.pharmasync.branch.sync;

/**
 * Thrown when the branch sync client cannot reach (or gets an error from) head-office.
 */
public class SyncClientException extends RuntimeException {
    public SyncClientException(String message) {
        super(message);
    }

    public SyncClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
