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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for RateLimiter
 */
public class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    public void setUp() {
        // Create rate limiter with 10 permits per second
        rateLimiter = new RateLimiter(10.0);
    }

    @Test
    public void testAcquireSinglePermit() throws InterruptedException {
        // Should acquire immediately
        boolean acquired = rateLimiter.acquire();
        assertTrue(acquired, "Should acquire permit");
    }

    @Test
    public void testTryAcquireSuccess() {
        // Should acquire without blocking
        boolean acquired = rateLimiter.tryAcquire();
        assertTrue(acquired, "Should acquire permit without blocking");
    }

    @Test
    public void testTryAcquireMultiple() {
        // Acquire all available permits
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.tryAcquire(), "Should acquire permit " + i);
        }
        
        // Next attempt should fail (no permits available)
        assertFalse(rateLimiter.tryAcquire(), "Should not acquire when no permits available");
    }

    @Test
    public void testTryAcquireWithTimeout() throws InterruptedException {
        // Acquire all permits
        for (int i = 0; i < 10; i++) {
            rateLimiter.tryAcquire();
        }
        
        // Try to acquire with timeout - should succeed after refill
        boolean acquired = rateLimiter.tryAcquire(200, TimeUnit.MILLISECONDS);
        assertTrue(acquired, "Should acquire after waiting for refill");
    }

    @Test
    public void testGetAvailablePermits() {
        long available = rateLimiter.getAvailablePermits();
        assertEquals(10, available, "Should have 10 permits initially");
        
        rateLimiter.tryAcquire(3);
        available = rateLimiter.getAvailablePermits();
        assertEquals(7, available, "Should have 7 permits after acquiring 3");
    }

    @Test
    public void testReset() {
        // Acquire some permits
        rateLimiter.tryAcquire(5);
        assertEquals(5, rateLimiter.getAvailablePermits());
        
        // Reset
        rateLimiter.reset();
        assertEquals(10, rateLimiter.getAvailablePermits(), "Should have full capacity after reset");
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        int numThreads = 20;
        int permitsPerThread = 2;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        // Create threads that will try to acquire permits
        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await(); // Wait for signal to start
                    if (rateLimiter.tryAcquire(permitsPerThread, 5, TimeUnit.SECONDS)) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        // Start all threads at once
        startLatch.countDown();
        
        // Wait for all threads to complete
        doneLatch.await(10, TimeUnit.SECONDS);

        // All threads should eventually succeed (with refill)
        assertTrue(successCount.get() >= 5, "At least 5 threads should succeed immediately");
    }

    @Test
    public void testGetStats() {
        var stats = rateLimiter.getStats();
        
        assertNotNull(stats);
        assertEquals(10.0, stats.get("permitsPerSecond"));
        assertEquals(10L, stats.get("maxPermits"));
        assertTrue(stats.containsKey("availablePermits"));
        assertTrue(stats.containsKey("utilizationPercent"));
    }

    @Test
    public void testToString() {
        String str = rateLimiter.toString();
        assertNotNull(str);
        assertTrue(str.contains("RateLimiter"));
        assertTrue(str.contains("10"));
    }

    @Test
    public void testInvalidConstructorArguments() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RateLimiter(0);
        }, "Should throw for zero permits per second");

        assertThrows(IllegalArgumentException.class, () -> {
            new RateLimiter(-1);
        }, "Should throw for negative permits per second");

        assertThrows(IllegalArgumentException.class, () -> {
            new RateLimiter(10, 0);
        }, "Should throw for zero max permits");
    }

    @Test
    public void testInvalidAcquireArguments() {
        assertThrows(IllegalArgumentException.class, () -> {
            rateLimiter.acquire(0);
        }, "Should throw for zero permits");

        assertThrows(IllegalArgumentException.class, () -> {
            rateLimiter.tryAcquire(-1);
        }, "Should throw for negative permits");
    }

    @Test
    public void testRefillOverTime() throws InterruptedException {
        // Acquire all permits
        for (int i = 0; i < 10; i++) {
            rateLimiter.tryAcquire();
        }
        
        assertEquals(0, rateLimiter.getAvailablePermits(), "Should have 0 permits");
        
        // Wait for refill (100ms = 1 permit at 10/sec)
        Thread.sleep(150);
        
        long available = rateLimiter.getAvailablePermits();
        assertTrue(available >= 1, "Should have at least 1 permit after 150ms");
    }

    @Test
    public void testBurstCapacity() {
        // Create rate limiter with custom burst capacity
        RateLimiter burstLimiter = new RateLimiter(5.0, 20);
        
        // Should be able to acquire up to 20 permits immediately
        for (int i = 0; i < 20; i++) {
            assertTrue(burstLimiter.tryAcquire(), "Should acquire permit " + i);
        }
        
        // Next should fail
        assertFalse(burstLimiter.tryAcquire(), "Should not acquire beyond burst capacity");
    }

    @Test
    public void testSlowRate() throws InterruptedException {
        // Create slow rate limiter (1 permit per second)
        RateLimiter slowLimiter = new RateLimiter(1.0);
        
        // First acquire should succeed
        assertTrue(slowLimiter.tryAcquire());
        
        // Second should fail immediately
        assertFalse(slowLimiter.tryAcquire());
        
        // But should succeed after waiting
        assertTrue(slowLimiter.tryAcquire(1100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testHighConcurrency() throws InterruptedException {
        int numThreads = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    if (rateLimiter.tryAcquire()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);

        // Should have some successes and some failures
        assertTrue(successCount.get() > 0, "Should have some successes");
        assertTrue(failureCount.get() > 0, "Should have some failures");
        assertEquals(numThreads, successCount.get() + failureCount.get(), 
                "All threads should complete");
    }

    @Test
    public void testUtilizationPercent() {
        rateLimiter.tryAcquire(5);
        
        var stats = rateLimiter.getStats();
        double utilization = (double) stats.get("utilizationPercent");
        
        assertEquals(50.0, utilization, 0.1, "Should be 50% utilized");
    }
}

