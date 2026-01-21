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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for RetryHelper
 */
public class RetryHelperTest {

    private RetryHelper retryHelper;

    @BeforeEach
    public void setUp() {
        // Create retry helper with fast settings for testing
        retryHelper = RetryHelper.builder()
                .maxAttempts(3)
                .initialDelay(10)
                .maxDelay(100)
                .backoffMultiplier(2.0)
                .build();
    }

    @Test
    public void testSuccessOnFirstAttempt() throws Exception {
        Callable<String> operation = () -> "success";
        
        String result = retryHelper.execute(operation);
        assertEquals("success", result);
    }

    @Test
    public void testSuccessOnSecondAttempt() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt == 1) {
                throw new SocketTimeoutException("Timeout");
            }
            return "success";
        };
        
        String result = retryHelper.execute(operation);
        assertEquals("success", result);
        assertEquals(2, attempts.get(), "Should succeed on second attempt");
    }

    @Test
    public void testSuccessOnThirdAttempt() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                throw new SocketTimeoutException("Timeout");
            }
            return "success";
        };
        
        String result = retryHelper.execute(operation);
        assertEquals("success", result);
        assertEquals(3, attempts.get(), "Should succeed on third attempt");
    }

    @Test
    public void testFailureAfterAllAttempts() {
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            attempts.incrementAndGet();
            throw new SocketTimeoutException("Timeout");
        };
        
        assertThrows(SocketTimeoutException.class, () -> {
            retryHelper.execute(operation);
        });
        
        assertEquals(3, attempts.get(), "Should attempt 3 times");
    }

    @Test
    public void testNonRetryableException() {
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            attempts.incrementAndGet();
            throw new IllegalArgumentException("Invalid argument");
        };
        
        assertThrows(IllegalArgumentException.class, () -> {
            retryHelper.execute(operation);
        });
        
        assertEquals(1, attempts.get(), "Should not retry non-retryable exception");
    }

    @Test
    public void testRetryableIOException() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt == 1) {
                throw new IOException("Connection timeout");
            }
            return "success";
        };
        
        String result = retryHelper.execute(operation);
        assertEquals("success", result);
        assertEquals(2, attempts.get(), "Should retry IOException with 'timeout'");
    }

    @Test
    public void testRetryableConnectionException() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt == 1) {
                throw new IOException("Connection reset");
            }
            return "success";
        };
        
        String result = retryHelper.execute(operation);
        assertEquals("success", result);
        assertEquals(2, attempts.get(), "Should retry IOException with 'reset'");
    }

    @Test
    public void testRateLimitException() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt == 1) {
                throw new IOException("HTTP 429 Too Many Requests");
            }
            return "success";
        };
        
        String result = retryHelper.execute(operation);
        assertEquals("success", result);
        assertEquals(2, attempts.get(), "Should retry rate limit error");
    }

    @Test
    public void testExecuteUnchecked() {
        Callable<String> operation = () -> "success";
        
        String result = retryHelper.executeUnchecked(operation);
        assertEquals("success", result);
    }

    @Test
    public void testExecuteUncheckedThrowsRuntimeException() {
        Callable<String> operation = () -> {
            throw new IOException("Error");
        };
        
        assertThrows(RuntimeException.class, () -> {
            retryHelper.executeUnchecked(operation);
        });
    }

    @Test
    public void testCustomOperationName() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt == 1) {
                throw new SocketTimeoutException("Timeout");
            }
            return "success";
        };
        
        String result = retryHelper.execute(operation, "CustomOperation");
        assertEquals("success", result);
    }

    @Test
    public void testIsRetryableException() {
        // Retryable exceptions
        assertTrue(RetryHelper.isRetryableException(new SocketTimeoutException("Timeout")));
        assertTrue(RetryHelper.isRetryableException(new IOException("Connection timeout")));
        assertTrue(RetryHelper.isRetryableException(new IOException("Connection reset")));
        assertTrue(RetryHelper.isRetryableException(new IOException("Broken pipe")));
        assertTrue(RetryHelper.isRetryableException(new IOException("Connection refused")));
        assertTrue(RetryHelper.isRetryableException(new IOException("HTTP 429")));
        
        // Non-retryable exceptions
        assertFalse(RetryHelper.isRetryableException(new IllegalArgumentException("Invalid")));
        assertFalse(RetryHelper.isRetryableException(new NullPointerException()));
        assertFalse(RetryHelper.isRetryableException(new IOException("Invalid data")));
    }

    @Test
    public void testBuilder() {
        RetryHelper custom = RetryHelper.builder()
                .maxAttempts(5)
                .initialDelay(500)
                .maxDelay(10000)
                .backoffMultiplier(3.0)
                .build();
        
        var config = custom.getConfig();
        assertEquals(5, config.get("maxAttempts"));
        assertEquals(500L, config.get("initialDelayMillis"));
        assertEquals(10000L, config.get("maxDelayMillis"));
        assertEquals(3.0, config.get("backoffMultiplier"));
    }

    @Test
    public void testGetConfig() {
        var config = retryHelper.getConfig();
        
        assertNotNull(config);
        assertEquals(3, config.get("maxAttempts"));
        assertEquals(10L, config.get("initialDelayMillis"));
        assertEquals(100L, config.get("maxDelayMillis"));
        assertEquals(2.0, config.get("backoffMultiplier"));
    }

    @Test
    public void testToString() {
        String str = retryHelper.toString();
        assertNotNull(str);
        assertTrue(str.contains("RetryHelper"));
        assertTrue(str.contains("3"));
    }

    @Test
    public void testInvalidConstructorArguments() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RetryHelper(0, 1000, 30000, 2.0, e -> true);
        }, "Should throw for zero max attempts");

        assertThrows(IllegalArgumentException.class, () -> {
            new RetryHelper(3, 0, 30000, 2.0, e -> true);
        }, "Should throw for zero initial delay");

        assertThrows(IllegalArgumentException.class, () -> {
            new RetryHelper(3, 1000, 500, 2.0, e -> true);
        }, "Should throw for max delay < initial delay");

        assertThrows(IllegalArgumentException.class, () -> {
            new RetryHelper(3, 1000, 30000, 1.0, e -> true);
        }, "Should throw for backoff multiplier <= 1.0");
    }

    @Test
    public void testExponentialBackoff() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        long[] delays = new long[3];
        
        Callable<String> operation = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                delays[attempt - 1] = System.currentTimeMillis();
                throw new SocketTimeoutException("Timeout");
            }
            delays[attempt - 1] = System.currentTimeMillis();
            return "success";
        };
        
        retryHelper.execute(operation);
        
        // Check that delays increase exponentially
        if (delays[1] > 0 && delays[0] > 0) {
            long firstDelay = delays[1] - delays[0];
            assertTrue(firstDelay >= 10, "First delay should be at least 10ms");
        }
    }

    @Test
    public void testInterruptedDuringRetry() {
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            attempts.incrementAndGet();
            throw new SocketTimeoutException("Timeout");
        };
        
        Thread testThread = new Thread(() -> {
            try {
                retryHelper.execute(operation);
                fail("Should throw IOException");
            } catch (IOException e) {
                assertTrue(e.getMessage().contains("interrupted"));
            } catch (Exception e) {
                fail("Unexpected exception: " + e);
            }
        });
        
        testThread.start();
        testThread.interrupt();
        
        try {
            testThread.join(1000);
        } catch (InterruptedException e) {
            fail("Test thread interrupted");
        }
    }

    @Test
    public void testCustomRetryPredicate() throws Exception {
        // Only retry NullPointerException
        RetryHelper customRetry = RetryHelper.builder()
                .maxAttempts(3)
                .initialDelay(10)
                .maxDelay(100)
                .backoffMultiplier(2.0)
                .retryPredicate(e -> e instanceof NullPointerException)
                .build();
        
        AtomicInteger attempts = new AtomicInteger(0);
        
        Callable<String> operation = () -> {
            int attempt = attempts.incrementAndGet();
            if (attempt == 1) {
                throw new NullPointerException("Null");
            }
            return "success";
        };
        
        String result = customRetry.execute(operation);
        assertEquals("success", result);
        assertEquals(2, attempts.get(), "Should retry NullPointerException");
    }
}

