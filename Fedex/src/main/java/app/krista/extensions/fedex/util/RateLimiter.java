/*
 * Fedex Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.fedex.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe rate limiter using token bucket algorithm to prevent API rate limit violations.
 */
public final class RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);
    private static final long MIN_SLEEP_MILLIS = 1L;

    private final double permitsPerSecond;
    private final long maxPermits;
    private final AtomicLong availablePermits;
    private final AtomicLong lastRefillTimestamp;
    private final long refillIntervalNanos;

    /**
     * Creates a rate limiter with the specified permits per second.
     *
     * @param permitsPerSecond number of permits per second (must be positive)
     * @throws IllegalArgumentException if permitsPerSecond is not positive
     */
    public RateLimiter(double permitsPerSecond) {
        this(permitsPerSecond, (long) permitsPerSecond);
    }

    /**
     * Creates a rate limiter with the specified permits per second and burst capacity.
     *
     * @param permitsPerSecond number of permits per second (must be positive)
     * @param maxPermits maximum burst capacity (must be positive)
     * @throws IllegalArgumentException if permitsPerSecond or maxPermits is not positive
     */
    public RateLimiter(double permitsPerSecond, long maxPermits) {
        validatePositive(permitsPerSecond, "Permits per second");
        validatePositive(maxPermits, "Max permits");

        this.permitsPerSecond = permitsPerSecond;
        this.maxPermits = maxPermits;
        this.availablePermits = new AtomicLong(maxPermits);
        this.lastRefillTimestamp = new AtomicLong(System.nanoTime());
        this.refillIntervalNanos = (long) (TimeUnit.SECONDS.toNanos(1) / permitsPerSecond);

        log.info("RateLimiter initialized: {} permits/sec, max burst: {}", permitsPerSecond, maxPermits);
    }

    /**
     * Acquires a single permit, blocking if necessary until available.
     *
     * @return true if permit acquired
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean acquire() throws InterruptedException {
        return acquire(1);
    }

    /**
     * Acquires the specified number of permits, blocking if necessary until available.
     *
     * @param permits number of permits to acquire (must be positive)
     * @return true if permits acquired
     * @throws IllegalArgumentException if permits is not positive
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean acquire(int permits) throws InterruptedException {
        validatePositive(permits, "Permits");

        while (true) {
            refillTokens();

            long available = availablePermits.get();
            if (available >= permits) {
                if (availablePermits.compareAndSet(available, available - permits)) {
                    log.debug("Acquired {} permits, {} remaining", permits, available - permits);
                    return true;
                }
            } else {
                long waitTimeMillis = calculateWaitTime(permits, available);
                log.debug("Rate limit reached, waiting {}ms", waitTimeMillis);
                Thread.sleep(waitTimeMillis);
            }
        }
    }

    /**
     * Attempts to acquire a single permit without blocking.
     *
     * @return true if permit acquired, false if not available
     */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * Attempts to acquire the specified number of permits without blocking.
     *
     * @param permits number of permits to acquire (must be positive)
     * @return true if permits acquired, false if not available
     * @throws IllegalArgumentException if permits is not positive
     */
    public boolean tryAcquire(int permits) {
        validatePositive(permits, "Permits");
        refillTokens();

        while (true) {
            long available = availablePermits.get();
            if (available >= permits) {
                if (availablePermits.compareAndSet(available, available - permits)) {
                    log.debug("Acquired {} permits (non-blocking), {} remaining", permits, available - permits);
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * Attempts to acquire a single permit within the specified timeout.
     *
     * @param timeout timeout value
     * @param unit time unit
     * @return true if permit acquired within timeout, false otherwise
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        return tryAcquire(1, timeout, unit);
    }

    /**
     * Attempts to acquire the specified number of permits within the specified timeout.
     *
     * @param permits number of permits to acquire (must be positive)
     * @param timeout timeout value
     * @param unit time unit
     * @return true if permits acquired within timeout, false otherwise
     * @throws IllegalArgumentException if permits is not positive
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit) throws InterruptedException {
        validatePositive(permits, "Permits");

        long deadlineNanos = System.nanoTime() + unit.toNanos(timeout);

        while (System.nanoTime() < deadlineNanos) {
            if (tryAcquire(permits)) {
                return true;
            }
            Thread.sleep(MIN_SLEEP_MILLIS);
        }

        log.debug("Timeout waiting for {} permits after {}ms", permits, unit.toMillis(timeout));
        return false;
    }

    /**
     * Returns the current number of available permits.
     *
     * @return number of available permits
     */
    public long getAvailablePermits() {
        refillTokens();
        return availablePermits.get();
    }

    /**
     * Returns rate limiter statistics including permits per second, max permits, available permits, and utilization.
     *
     * @return map containing rate limiter statistics
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        long available = getAvailablePermits();

        stats.put("permitsPerSecond", permitsPerSecond);
        stats.put("maxPermits", maxPermits);
        stats.put("availablePermits", available);
        stats.put("utilizationPercent", calculateUtilization(available));

        return stats;
    }

    /**
     * Resets the rate limiter to full capacity.
     */
    public void reset() {
        availablePermits.set(maxPermits);
        lastRefillTimestamp.set(System.nanoTime());
        log.info("RateLimiter reset to full capacity: {}", maxPermits);
    }

    /**
     * Refills tokens based on elapsed time since last refill.
     */
    private void refillTokens() {
        long now = System.nanoTime();
        long lastRefill = lastRefillTimestamp.get();
        long elapsedNanos = now - lastRefill;

        if (elapsedNanos > refillIntervalNanos) {
            long tokensToAdd = calculateTokensToAdd(elapsedNanos);

            if (tokensToAdd > 0) {
                updateAvailablePermits(tokensToAdd, lastRefill, now);
            }
        }
    }

    /**
     * Calculates the number of tokens to add based on elapsed time.
     *
     * @param elapsedNanos elapsed time in nanoseconds
     * @return number of tokens to add
     */
    private long calculateTokensToAdd(long elapsedNanos) {
        return (long) (elapsedNanos * permitsPerSecond / TimeUnit.SECONDS.toNanos(1));
    }

    /**
     * Updates available permits atomically with the specified tokens to add.
     *
     * @param tokensToAdd number of tokens to add
     * @param lastRefill last refill timestamp
     * @param now current timestamp
     */
    private void updateAvailablePermits(long tokensToAdd, long lastRefill, long now) {
        while (true) {
            long current = availablePermits.get();
            long newPermits = Math.min(maxPermits, current + tokensToAdd);

            if (availablePermits.compareAndSet(current, newPermits)) {
                lastRefillTimestamp.compareAndSet(lastRefill, now);
                break;
            }
        }
    }

    /**
     * Calculates wait time in milliseconds for the specified permits.
     *
     * @param permits number of permits needed
     * @param available number of permits currently available
     * @return wait time in milliseconds
     */
    private long calculateWaitTime(int permits, long available) {
        long deficit = permits - available;
        long waitTimeNanos = deficit * refillIntervalNanos;
        return Math.max(MIN_SLEEP_MILLIS, TimeUnit.NANOSECONDS.toMillis(waitTimeNanos));
    }

    /**
     * Calculates utilization percentage based on available permits.
     *
     * @param available number of available permits
     * @return utilization percentage
     */
    private double calculateUtilization(long available) {
        return (maxPermits - available) * 100.0 / maxPermits;
    }

    /**
     * Validates that a value is positive.
     *
     * @param value value to validate
     * @param name name of the parameter for error message
     * @throws IllegalArgumentException if value is not positive
     */
    private void validatePositive(double value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    /**
     * Validates that a value is positive.
     *
     * @param value value to validate
     * @param name name of the parameter for error message
     * @throws IllegalArgumentException if value is not positive
     */
    private void validatePositive(long value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    @Override
    public String toString() {
        return String.format("RateLimiter[%.2f permits/sec, %d/%d available]",
                permitsPerSecond, getAvailablePermits(), maxPermits);
    }
}

