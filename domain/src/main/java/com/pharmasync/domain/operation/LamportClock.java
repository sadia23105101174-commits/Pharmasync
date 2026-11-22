package com.pharmasync.domain.operation;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe Lamport logical clock for causal ordering of offline operations across branches.
 */
public class LamportClock {
    private final AtomicLong clock;

    public LamportClock() {
        this(0L);
    }

    public LamportClock(long initialValue) {
        if (initialValue < 0) {
            throw new IllegalArgumentException("Clock value cannot be negative: " + initialValue);
        }
        this.clock = new AtomicLong(initialValue);
    }

    /**
     * Increments the clock for a local event and returns the new value.
     */
    public long tick() {
        return clock.incrementAndGet();
    }

    /**
     * Updates the clock upon receiving an operation with a remote logical timestamp.
     * New clock value is max(localClock, receivedTimestamp) + 1.
     *
     * @param receivedTimestamp remote Lamport timestamp
     * @return updated clock value
     */
    public long update(long receivedTimestamp) {
        return clock.updateAndGet(current -> Math.max(current, receivedTimestamp) + 1);
    }

    /**
     * Returns the current logical clock time without advancing it.
     */
    public long getTime() {
        return clock.get();
    }

    @Override
    public String toString() {
        return "LamportClock{" + clock.get() + "}";
    }
}
