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

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Retry helper with exponential backoff for handling transient failures.
 */
public final class RetryHelper {

    private static final Logger log = LoggerFactory.getLogger(RetryHelper.class);

    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_INITIAL_DELAY_MILLIS = 1000L;
    private static final long DEFAULT_MAX_DELAY_MILLIS = 30000L;
    private static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;
    private static final double MIN_BACKOFF_MULTIPLIER = 1.0;

    private final int maxAttempts;
    private final long initialDelayMillis;
    private final long maxDelayMillis;
    private final double backoffMultiplier;
    private final Predicate<Exception> retryPredicate;

    /**
     * Creates a retry helper with default settings (3 attempts, 1s initial delay, 30s max delay, 2.0 backoff).
     */
    public RetryHelper() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_INITIAL_DELAY_MILLIS, DEFAULT_MAX_DELAY_MILLIS,
                DEFAULT_BACKOFF_MULTIPLIER, RetryHelper::isRetryableException);
    }

    /**
     * Creates a retry helper with custom settings.
     *
     * @param maxAttempts maximum number of attempts (must be positive)
     * @param initialDelayMillis initial delay in milliseconds (must be positive)
     * @param maxDelayMillis maximum delay in milliseconds (must be >= initial delay)
     * @param backoffMultiplier multiplier for exponential backoff (must be > 1.0)
     * @param retryPredicate predicate to determine if exception is retryable
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public RetryHelper(int maxAttempts, long initialDelayMillis, long maxDelayMillis,
                       double backoffMultiplier, Predicate<Exception> retryPredicate) {
        validateParameters(maxAttempts, initialDelayMillis, maxDelayMillis, backoffMultiplier);

        this.maxAttempts = maxAttempts;
        this.initialDelayMillis = initialDelayMillis;
        this.maxDelayMillis = maxDelayMillis;
        this.backoffMultiplier = backoffMultiplier;
        this.retryPredicate = retryPredicate;

        log.debug("RetryHelper initialized: maxAttempts={}, initialDelay={}ms, maxDelay={}ms, backoff={}",
                maxAttempts, initialDelayMillis, maxDelayMillis, backoffMultiplier);
    }

    /**
     * Executes an operation with retry logic using default operation name.
     *
     * @param operation operation to execute
     * @param <T> return type
     * @return result of operation
     * @throws Exception if all retries fail
     */
    public <T> T execute(Callable<T> operation) throws Exception {
        return execute(operation, "Operation");
    }

    /**
     * Executes an operation with retry logic and custom operation name for logging.
     *
     * @param operation operation to execute
     * @param operationName name for logging purposes
     * @param <T> return type
     * @return result of operation
     * @throws Exception if all retries fail or exception is non-retryable
     */
    public <T> T execute(Callable<T> operation, String operationName) throws Exception {
        Exception lastException = null;
        long delay = initialDelayMillis;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug("{}: Attempt {}/{}", operationName, attempt, maxAttempts);
                T result = operation.call();

                if (attempt > 1) {
                    log.info("{}: Succeeded on attempt {}/{}", operationName, attempt, maxAttempts);
                }

                return result;

            } catch (Exception cause) {
                lastException = cause;

                if (!retryPredicate.test(cause)) {
                    log.warn("{}: Non-retryable exception on attempt {}/{}: {}",
                            operationName, attempt, maxAttempts, cause.getMessage());
                    throw cause;
                }

                if (attempt >= maxAttempts) {
                    log.error("{}: All {} attempts failed, last error: {}",
                            operationName, maxAttempts, cause.getMessage());
                    throw cause;
                }

                log.warn("{}: Attempt {}/{} failed: {}, retrying in {}ms",
                        operationName, attempt, maxAttempts, cause.getMessage(), delay);

                sleepWithInterruptHandling(delay, operationName);
                delay = calculateNextDelay(delay);
            }
        }

        throw lastException != null ? lastException : new IOException("Retry failed");
    }

    /**
     * Executes an operation with retry logic, wrapping checked exceptions in RuntimeException.
     *
     * @param operation operation to execute
     * @param <T> return type
     * @return result of operation
     * @throws RuntimeException if all retries fail
     */
    public <T> T executeUnchecked(Callable<T> operation) {
        return executeUnchecked(operation, "Operation");
    }

    /**
     * Executes an operation with retry logic, wrapping checked exceptions in RuntimeException.
     *
     * @param operation operation to execute
     * @param operationName name for logging purposes
     * @param <T> return type
     * @return result of operation
     * @throws RuntimeException if all retries fail
     */
    public <T> T executeUnchecked(Callable<T> operation, String operationName) {
        try {
            return execute(operation, operationName);
        } catch (Exception cause) {
            throw new RuntimeException("Retry failed: " + cause.getMessage(), cause);
        }
    }

    /**
     * Determines if an exception is retryable based on type and message content.
     *
     * @param cause exception to check
     * @return true if exception is retryable, false otherwise
     */
    public static boolean isRetryableException(Exception cause) {
        if (cause instanceof SocketTimeoutException) {
            return true;
        }

        if (cause instanceof IOException) {
            return isRetryableIOException((IOException) cause);
        }

        return false;
    }

    /**
     * Creates a builder for custom retry configuration.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the retry configuration as a map.
     *
     * @return map containing retry configuration
     */
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxAttempts", maxAttempts);
        config.put("initialDelayMillis", initialDelayMillis);
        config.put("maxDelayMillis", maxDelayMillis);
        config.put("backoffMultiplier", backoffMultiplier);
        return config;
    }

    /**
     * Validates retry helper parameters.
     *
     * @param maxAttempts maximum number of attempts
     * @param initialDelayMillis initial delay in milliseconds
     * @param maxDelayMillis maximum delay in milliseconds
     * @param backoffMultiplier backoff multiplier
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private static void validateParameters(int maxAttempts, long initialDelayMillis,
                                           long maxDelayMillis, double backoffMultiplier) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("Max attempts must be positive");
        }
        if (initialDelayMillis <= 0) {
            throw new IllegalArgumentException("Initial delay must be positive");
        }
        if (maxDelayMillis < initialDelayMillis) {
            throw new IllegalArgumentException("Max delay must be >= initial delay");
        }
        if (backoffMultiplier <= MIN_BACKOFF_MULTIPLIER) {
            throw new IllegalArgumentException("Backoff multiplier must be > 1.0");
        }
    }

    /**
     * Checks if an IOException is retryable based on message content.
     *
     * @param cause IOException to check
     * @return true if retryable, false otherwise
     */
    private static boolean isRetryableIOException(IOException cause) {
        String message = cause.getMessage();
        if (message == null) {
            return false;
        }

        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("timeout") ||
               lowerMessage.contains("connection") ||
               lowerMessage.contains("reset") ||
               lowerMessage.contains("broken pipe") ||
               lowerMessage.contains("connection refused") ||
               message.contains("429");
    }

    /**
     * Sleeps for the specified delay, handling interrupts appropriately.
     *
     * @param delay delay in milliseconds
     * @param operationName operation name for logging
     * @throws IOException if interrupted during sleep
     */
    private void sleepWithInterruptHandling(long delay, String operationName) throws IOException {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("{}: Interrupted during retry delay", operationName);
            throw new IOException("Retry interrupted", ie);
        }
    }

    /**
     * Calculates the next delay using exponential backoff.
     *
     * @param currentDelay current delay in milliseconds
     * @return next delay in milliseconds, capped at maxDelayMillis
     */
    private long calculateNextDelay(long currentDelay) {
        return Math.min((long) (currentDelay * backoffMultiplier), maxDelayMillis);
    }

    @Override
    public String toString() {
        return String.format("RetryHelper[maxAttempts=%d, initialDelay=%dms, maxDelay=%dms, backoff=%.1f]",
                maxAttempts, initialDelayMillis, maxDelayMillis, backoffMultiplier);
    }

    /**
     * Builder for creating RetryHelper instances with custom configuration.
     */
    public static final class Builder {
        private int maxAttempts = DEFAULT_MAX_ATTEMPTS;
        private long initialDelayMillis = DEFAULT_INITIAL_DELAY_MILLIS;
        private long maxDelayMillis = DEFAULT_MAX_DELAY_MILLIS;
        private double backoffMultiplier = DEFAULT_BACKOFF_MULTIPLIER;
        private Predicate<Exception> retryPredicate = RetryHelper::isRetryableException;

        /** Sets the maximum number of attempts. */
        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        /** Sets the initial delay in milliseconds. */
        public Builder initialDelay(long delayMillis) {
            this.initialDelayMillis = delayMillis;
            return this;
        }

        /** Sets the maximum delay in milliseconds. */
        public Builder maxDelay(long delayMillis) {
            this.maxDelayMillis = delayMillis;
            return this;
        }

        /** Sets the backoff multiplier. */
        public Builder backoffMultiplier(double multiplier) {
            this.backoffMultiplier = multiplier;
            return this;
        }

        /** Sets the retry predicate. */
        public Builder retryPredicate(Predicate<Exception> predicate) {
            this.retryPredicate = predicate;
            return this;
        }

        /** Builds the RetryHelper instance. */
        public RetryHelper build() {
            return new RetryHelper(maxAttempts, initialDelayMillis, maxDelayMillis,
                    backoffMultiplier, retryPredicate);
        }
    }
}

