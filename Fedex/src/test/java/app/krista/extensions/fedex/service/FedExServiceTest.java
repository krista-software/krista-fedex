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

package app.krista.extensions.fedex.service;

import app.krista.extensions.fedex.auth.FedExTokenManager;
import app.krista.extensions.fedex.client.FedExHttpClient;
import app.krista.extensions.fedex.config.FedExAttributes;
import app.krista.extensions.fedex.entity.FedExLabel;
import app.krista.extensions.fedex.model.FedExLabelResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for FedExService
 * 
 * Tests cover:
 * - Successful shipment creation
 * - Validation failures
 * - API failures and retries
 * - Rate limiting
 * - Connection testing
 * - Statistics and monitoring
 */
@ExtendWith(MockitoExtension.class)
public class FedExServiceTest {

    @Mock
    private FedExTokenManager tokenManager;

    @Mock
    private FedExHttpClient httpClient;

    @Mock
    private FedExAttributes attributes;

    private FedExService fedExService;
    private FedExLabel validLabel;

    @BeforeEach
    public void setUp() {
        // Mock FedExAttributes to return master account number (lenient to avoid UnnecessaryStubbingException)
        lenient().when(attributes.getMasterAccountNumber()).thenReturn("999888777");

        fedExService = new FedExService(tokenManager, httpClient, attributes);
        validLabel = createValidLabel();
    }

    // ========== HELPER METHODS ==========

    private FedExLabel createValidLabel() {
        FedExLabel label = new FedExLabel();
        label.ticketId = "TEST-123";
        // Note: recipientAccountNumber is now optional - leave it null to use master account (SENDER payment)

        // Shipper info
        label.setShipperPersonName("John Doe");
        label.setShipperPhoneNumber(String.valueOf(1234567890L));
        label.setShipperStreetLine1("123 Main St");
        label.setShipperCity("New York");
        label.setShipperStateCode("NY");
        label.setShipperPostalCode(String.valueOf(10001));

        // Recipient info
        label.setRecipientPersonName("Jane Smith");
        label.setRecipientPhoneNumber(String.valueOf(9876543210L));
        label.setRecipientStreetLine1("456 Oak Ave");
        label.setRecipientCity("Los Angeles");
        label.setRecipientStateCode("CA");
        label.setRecipientPostalCode(String.valueOf(90001));

        // Package info
        label.setServiceType("FEDEX_GROUND");
        label.setPackageWeight(10.0);

        return label;
    }

    private String createSuccessResponseJson() {
        return "{\n" +
                "  \"transactionId\": \"624deea6-b709-470c-8c39-4b5511281492\",\n" +
                "  \"output\": {\n" +
                "    \"transactionShipments\": [{\n" +
                "      \"masterTrackingNumber\": \"794947717776\",\n" +
                "      \"pieceResponses\": [{\n" +
                "        \"trackingNumber\": \"794947717776\",\n" +
                "        \"packageDocuments\": [{\n" +
                "          \"url\": \"https://example.com/label.pdf\"\n" +
                "        }]\n" +
                "      }]\n" +
                "    }]\n" +
                "  }\n" +
                "}";
    }

    // ========== TEST CONNECTION ==========

    @Test
    public void testConnectionSuccess() throws IOException {
        doNothing().when(tokenManager).testConnection();
        
        assertDoesNotThrow(() -> {
            fedExService.testConnection();
        });
        
        verify(tokenManager, times(1)).testConnection();
    }

    @Test
    public void testConnectionFailure() throws IOException {
        doThrow(new IOException("Connection failed")).when(tokenManager).testConnection();
        
        assertThrows(IOException.class, () -> {
            fedExService.testConnection();
        });
        
        verify(tokenManager, times(1)).testConnection();
    }

    // ========== CREATE SHIPMENT - SUCCESS CASES ==========

    @Test
    public void testCreateShipmentSuccess() throws Exception {
        // Mock token manager
        when(tokenManager.getValidAccessToken()).thenReturn("test-token");

        // Mock HTTP client (now accepts 4 parameters including headers Map)
        String responseJson = createSuccessResponseJson();
        when(httpClient.post(anyString(), any(), anyString(), anyMap())).thenReturn(responseJson);
        
        // Execute
        FedExLabelResponse response = fedExService.createShipment(validLabel);
        
        // Verify
        assertNotNull(response);
        assertNotNull(response.getTransactionId());
        assertEquals("624deea6-b709-470c-8c39-4b5511281492", response.getTransactionId());
        
        // Verify interactions
        verify(tokenManager, times(1)).getValidAccessToken();
        verify(httpClient, times(1)).post(eq("/ship/v1/shipments"), any(), eq("test-token"), anyMap());
    }

    @Test
    public void testCreateShipmentWithAllOptionalFields() throws Exception {
        // Add optional fields
        validLabel.setShipperCompanyName("Shipper Corp");
        validLabel.setShipperStreetLine2("Suite 100");
        validLabel.setRecipientCompanyName("Recipient Inc");
        validLabel.setRecipientStreetLine2("Apt 5B");
        validLabel.setShipDate("2024-12-31");
        
        // Mock dependencies
        when(tokenManager.getValidAccessToken()).thenReturn("test-token");
        when(httpClient.post(anyString(), any(), anyString(), anyMap())).thenReturn(createSuccessResponseJson());
        
        // Execute
        FedExLabelResponse response = fedExService.createShipment(validLabel);
        
        // Verify
        assertNotNull(response);
        verify(httpClient, times(1)).post(anyString(), any(), anyString(), anyMap());
    }

    // ========== CREATE SHIPMENT - VALIDATION FAILURES ==========

    @Test
    public void testCreateShipmentWithNullLable() throws Exception {
        // Null label will cause NullPointerException during validation
        assertThrows(Exception.class, () -> {
            fedExService.createShipment(null);
        });

        // Should not call API
        verify(httpClient, never()).post(anyString(), any(), anyString());
    }

    @Test
    public void testCreateShipmentWithMissingMasterAccountNumber() throws Exception {
        // Mock attributes to return null/empty master account number
        when(attributes.getMasterAccountNumber()).thenReturn(null);

        assertThrows(IOException.class, () -> {
            fedExService.createShipment(validLabel);
        });

        verify(httpClient, never()).post(anyString(), any(), anyString());
    }

    @Test
    public void testCreateShipmentWithMissingShipperName() throws Exception {
        validLabel.setShipperPersonName(null);

        assertThrows(IOException.class, () -> {
            fedExService.createShipment(validLabel);
        });

        verify(httpClient, never()).post(anyString(), any(), anyString());
    }

    @Test
    public void testCreateShipmentWithMissingRecipientInfo() throws Exception {
        validLabel.setRecipientPersonName(null);

        assertThrows(IOException.class, () -> {
            fedExService.createShipment(validLabel);
        });

        verify(httpClient, never()).post(anyString(), any(), anyString());
    }

    @Test
    public void testCreateShipmentWithInvalidWeight() throws Exception {
        validLabel.setPackageWeight(0.0);

        assertThrows(IOException.class, () -> {
            fedExService.createShipment(validLabel);
        });

        verify(httpClient, never()).post(anyString(), any(), anyString());
    }

    @Test
    public void testCreateShipmentWithNegativeWeight() throws Exception {
        validLabel.setPackageWeight(-5.0);

        assertThrows(IOException.class, () -> {
            fedExService.createShipment(validLabel);
        });

        verify(httpClient, never()).post(anyString(), any(), anyString());
    }

    // ========== CREATE SHIPMENT - API FAILURES ==========

    @Test
    public void testCreateShipmentWithTokenFailure() throws Exception {
        when(tokenManager.getValidAccessToken()).thenThrow(new IOException("Token refresh failed"));
        
        assertThrows(IOException.class, () -> {
            fedExService.createShipment(validLabel);
        });
        
        verify(tokenManager, atLeastOnce()).getValidAccessToken();
        verify(httpClient, never()).post(anyString(), any(), anyString());
    }

    @Test
    public void testCreateShipmentWithHttpClientFailure() throws Exception {
        when(tokenManager.getValidAccessToken()).thenReturn("test-token");
        // Use a retryable exception pattern
        when(httpClient.post(anyString(), any(), anyString(), anyMap()))
                .thenThrow(new IOException("Connection timeout"));

        assertThrows(IOException.class, () -> {
            fedExService.createShipment(validLabel);
        });

        // Should retry 3 times for retryable exceptions
        verify(httpClient, times(3)).post(anyString(), any(), anyString(), anyMap());
    }

    @Test
    public void testCreateShipmentWithInvalidResponse() throws Exception {
        when(tokenManager.getValidAccessToken()).thenReturn("test-token");
        when(httpClient.post(anyString(), any(), anyString(), anyMap())).thenReturn("invalid json");

        assertThrows(IOException.class, () -> {
            fedExService.createShipment(validLabel);
        });
    }

    // ========== CREATE SHIPMENT - RETRY LOGIC ==========

    @Test
    public void testCreateShipmentRetriesOnTimeout() throws Exception {
        when(tokenManager.getValidAccessToken()).thenReturn("test-token");
        
        // First two attempts fail with timeout, third succeeds
        when(httpClient.post(anyString(), any(), anyString(), anyMap()))
                .thenThrow(new SocketTimeoutException("Timeout"))
                .thenThrow(new SocketTimeoutException("Timeout"))
                .thenReturn(createSuccessResponseJson());

        // Execute
        FedExLabelResponse response = fedExService.createShipment(validLabel);

        // Verify success after retries
        assertNotNull(response);
        verify(httpClient, times(3)).post(anyString(), any(), anyString(), anyMap());
    }

    @Test
    public void testCreateShipmentSucceedsOnSecondAttempt() throws Exception {
        when(tokenManager.getValidAccessToken()).thenReturn("test-token");
        
        // First attempt fails, second succeeds
        when(httpClient.post(anyString(), any(), anyString(), anyMap()))
                .thenThrow(new IOException("Connection reset"))
                .thenReturn(createSuccessResponseJson());
        
        // Execute
        FedExLabelResponse response = fedExService.createShipment(validLabel);
        
        // Verify
        assertNotNull(response);
        verify(httpClient, times(2)).post(anyString(), any(), anyString(), anyMap());
    }

    @Test
    public void testCreateShipmentFailsAfterMaxRetries() throws Exception {
        when(tokenManager.getValidAccessToken()).thenReturn("test-token");
        
        // All attempts fail
        when(httpClient.post(anyString(), any(), anyString(), anyMap()))
                .thenThrow(new SocketTimeoutException("Timeout"));

        // Execute and expect failure
        assertThrows(IOException.class, () -> {
            fedExService.createShipment(validLabel);
        });

        // Verify all 3 attempts were made
        verify(httpClient, times(3)).post(anyString(), any(), anyString(), anyMap());
    }

    @Test
    public void testCreateShipmentDoesNotRetryNonRetryableException() throws Exception {
        when(tokenManager.getValidAccessToken()).thenReturn("test-token");
        
        // Non-retryable exception
        when(httpClient.post(anyString(), any(), anyString(), anyMap()))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        // Execute and expect failure
        assertThrows(Exception.class, () -> {
            fedExService.createShipment(validLabel);
        });

        // Should only attempt once (no retry for non-retryable exceptions)
        verify(httpClient, times(1)).post(anyString(), any(), anyString(), anyMap());
    }

    // ========== STATISTICS & MONITORING ==========

    @Test
    public void testGetServiceStats() {
        // Mock connection pool stats
        Map<String, Object> mockPoolStats = Map.of("maxTotal", 100, "maxPerRoute", 20);
        when(httpClient.getConnectionPoolStats()).thenReturn(mockPoolStats);
        
        // Mock token cache stats
        Map<String, Object> mockTokenStats = Map.of("tokenCount", 1);
        when(tokenManager.getCacheStats()).thenReturn(mockTokenStats);
        
        // Execute
        Map<String, Object> stats = fedExService.getServiceStats();
        
        // Verify
        assertNotNull(stats);
        assertTrue(stats.containsKey("rateLimiter"));
        assertTrue(stats.containsKey("retryConfig"));
        assertTrue(stats.containsKey("connectionPool"));
        assertTrue(stats.containsKey("tokenCache"));
        
        verify(httpClient, times(1)).getConnectionPoolStats();
        verify(tokenManager, times(1)).getCacheStats();
    }

    @Test
    public void testGetRateLimiterStats() {
        Map<String, Object> stats = fedExService.getRateLimiterStats();
        
        assertNotNull(stats);
        assertTrue(stats.containsKey("permitsPerSecond"));
        assertTrue(stats.containsKey("maxPermits"));
        assertTrue(stats.containsKey("availablePermits"));
        
        assertEquals(10.0, stats.get("permitsPerSecond"));
    }

    @Test
    public void testResetRateLimiter() {
        assertDoesNotThrow(() -> {
            fedExService.resetRateLimiter();
        });
        
        // Verify rate limiter is reset
        Map<String, Object> stats = fedExService.getRateLimiterStats();
        long availablePermits = (long) stats.get("availablePermits");
        assertEquals(10L, availablePermits);
    }
}

