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
import app.krista.extensions.fedex.mapper.FedExLabelMapper;
import app.krista.extensions.fedex.model.FedExLabelRequest;
import app.krista.extensions.fedex.model.FedExLabelResponse;
import app.krista.extensions.fedex.model.FedExResponseHelper;
import app.krista.extensions.fedex.model.FedExResponseParser;
import app.krista.extensions.fedex.util.RateLimiter;
import app.krista.extensions.fedex.util.RetryHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for FedEx business operations including shipment creation, tracking, cancellation, and rate calculations.
 */
@Service
public class FedExService {

    private static final Logger log = LoggerFactory.getLogger(FedExService.class);

    private static final double REQUESTS_PER_SECOND = 10.0;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000L;
    private static final long MAX_RETRY_DELAY_MS = 30000L;
    private static final double BACKOFF_MULTIPLIER = 2.0;
    private static final String OPERATION_CREATE_SHIPMENT = "CreateShipment";

    /**
     * Standard headers for FedEx Ship API requests.
     * X-locale header is required by FedEx API to specify language/locale.
     */
    private static final Map<String, String> FEDEX_API_HEADERS;

    static {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-locale", "en_US");
        FEDEX_API_HEADERS = Collections.unmodifiableMap(headers);
    }

    private final FedExTokenManager tokenManager;
    private final FedExHttpClient httpClient;
    private final FedExAttributes attributes;
    private final Gson gson;
    private final FedExResponseParser responseParser;
    private final RateLimiter rateLimiter;
    private final RetryHelper retryHelper;

    /**
     * Constructs FedExService with required dependencies.
     *
     * @param tokenManager token manager for OAuth authentication
     * @param httpClient HTTP client for API communication
     * @param attributes FedEx extension configuration attributes
     */
    @Inject
    public FedExService(FedExTokenManager tokenManager, FedExHttpClient httpClient, FedExAttributes attributes) {
        this.tokenManager = tokenManager;
        this.httpClient = httpClient;
        this.attributes = attributes;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.responseParser = new FedExResponseParser();
        this.rateLimiter = new RateLimiter(REQUESTS_PER_SECOND);
        this.retryHelper = RetryHelper.builder()
                .maxAttempts(MAX_RETRY_ATTEMPTS)
                .initialDelay(INITIAL_RETRY_DELAY_MS)
                .maxDelay(MAX_RETRY_DELAY_MS)
                .backoffMultiplier(BACKOFF_MULTIPLIER)
                .build();

        log.info("FedExService initialized: {} req/sec, {} retry attempts", REQUESTS_PER_SECOND, MAX_RETRY_ATTEMPTS);
    }

    /**
     * Tests FedEx API connectivity and validates credentials.
     *
     * @throws IOException if connection test fails
     */
    public void testConnection() throws IOException {
        log.info("Testing FedEx API connection");
        tokenManager.testConnection();
    }
    /**
     * Creates a FedEx shipment with label generation from FedExLabel entity.
     *
     * @param fedExLable FedExLabel entity with shipment details
     * @return FedExLabelResponse with tracking numbers, label URLs, and shipping costs
     * @throws IOException if API call fails after all retries
     */
    public FedExLabelResponse createShipment(FedExLabel fedExLable) throws IOException {
        log.info("Creating FedEx shipment, ticket: {}", fedExLable.getTicketId());

        try {
            rateLimiter.acquire();
            return retryHelper.execute(() -> executeShipmentCreation(fedExLable), OPERATION_CREATE_SHIPMENT);

        } catch (FedExResponseParser.FedExResponseParseException e) {
            log.error("Failed to parse FedEx response: {}", e.getMessage());
            throw new IOException("Unable to process FedEx API response. The response format was unexpected or incomplete. This may indicate a temporary issue with the FedEx API. Please try again, and if the problem persists, contact support with the transaction details. Error: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.error("Validation failed: {}", e.getMessage());
            throw new IOException("Shipment information validation failed. Please review and correct the following issues: " + e.getMessage() + ". Ensure all required fields are complete and properly formatted before trying again.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for rate limiter");
            throw new IOException("Shipment creation was interrupted while managing request rate limits. This is usually temporary. Please try your request again. If you're making many requests quickly, consider adding small delays between them.", e);
        } catch (Exception e) {
            log.error("Unexpected error creating shipment: {}", e.getMessage(), e);
            throw new IOException("Shipment creation failed due to an unexpected error. Please verify your FedEx configuration, check your network connection, and ensure the FedEx API is accessible. If the issue continues, contact support with these details: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a FedEx shipment and returns raw JSON response for backward compatibility.
     *
     * @param fedExLable FedExLabel entity with shipment details
     * @return raw JSON response string
     * @throws IOException if API call fails
     */
    public String createShipmentRaw(FedExLabel fedExLable) throws IOException {
        log.info("Creating FedEx shipment (raw response)");

        FedExLabelMapper.validateLabel(fedExLable, attributes);
        FedExLabelRequest labelRequest = FedExLabelMapper.toFedExLabelRequest(fedExLable, attributes);
        String token = tokenManager.getValidAccessToken();

        String response = httpClient.post("/ship/v1/shipments", labelRequest, token, FEDEX_API_HEADERS);

        log.info("Shipment created successfully (raw response)");
        return response;
    }

    /**
     * Tracks a FedEx shipment by tracking number.
     *
     * @param trackingNumber tracking number to query
     * @return JSON response with tracking information
     * @throws IOException if API call fails
     */
    public String trackShipment(String trackingNumber) throws IOException {
        log.info("Tracking shipment: {}", trackingNumber);
        validateTrackingNumber(trackingNumber);

        Object trackingRequest = createTrackingRequest(trackingNumber);
        String token = tokenManager.getValidAccessToken();
        String response = httpClient.post("/track/v1/trackingnumbers", trackingRequest, token);

        log.info("Tracking information retrieved");
        return response;
    }

    /**
     * Cancels a FedEx shipment by tracking number.
     *
     * @param trackingNumber tracking number to cancel
     * @return JSON response with cancellation confirmation
     * @throws IOException if API call fails
     */
    public String cancelShipment(String trackingNumber) throws IOException {
        log.info("Cancelling shipment: {}", trackingNumber);
        validateTrackingNumber(trackingNumber);

        Object cancelRequest = createCancelRequest(trackingNumber);
        String token = tokenManager.getValidAccessToken();
        String response = httpClient.post("/ship/v1/shipments/cancel", cancelRequest, token);

        log.info("Shipment cancelled successfully");
        return response;
    }

    /**
     * Retrieves shipping rates for a shipment.
     *
     * @param rateRequest rate request object
     * @return JSON response with shipping rates
     * @throws IOException if API call fails
     */
    public String getShippingRates(Object rateRequest) throws IOException {
        log.info("Retrieving shipping rates");
        validateRateRequest(rateRequest);

        String token = tokenManager.getValidAccessToken();
        String response = httpClient.post("/rate/v1/rates/quotes", rateRequest, token);

        log.info("Shipping rates retrieved successfully");
        return response;
    }

    /**
     * Validates an address using FedEx API.
     *
     * @param addressRequest address request object
     * @return JSON response with validation results
     * @throws IOException if API call fails
     */
    public String validateAddress(Object addressRequest) throws IOException {
        log.info("Validating address");
        validateAddressRequest(addressRequest);

        String token = tokenManager.getValidAccessToken();
        String response = httpClient.post("/address/v1/addresses/resolve", addressRequest, token);

        log.info("Address validation completed");
        return response;
    }
    /**
     * Forces a token refresh.
     *
     * @throws IOException if token refresh fails
     */
    public void refreshToken() throws IOException {
        log.info("Forcing token refresh");
        tokenManager.refreshToken();
    }

    /**
     * Clears all stored tokens.
     */
    public void clearTokens() {
        log.info("Clearing stored tokens");
        tokenManager.clearTokens();
    }

    /**
     * Returns token statistics.
     *
     * @return map containing token statistics
     */
    public Map<String, Object> getTokenStats() {
        return tokenManager.getTokenStats();
    }

    /**
     * Executes the shipment creation logic with validation and API call.
     *
     * @param fedExLable FedExLabel entity with shipment details
     * @return FedExLabelResponse with shipment details
     * @throws Exception if any step fails
     */
    private FedExLabelResponse executeShipmentCreation(FedExLabel fedExLable) throws Exception {
        FedExLabelMapper.validateLabel(fedExLable, attributes);
        FedExLabelRequest labelRequest = FedExLabelMapper.toFedExLabelRequest(fedExLable, attributes);

        log.debug("Generated FedEx API payload: {}", gson.toJson(labelRequest));

        String token = tokenManager.getValidAccessToken();

        String responseJson = httpClient.post("/ship/v1/shipments", labelRequest, token, FEDEX_API_HEADERS);

        // Log the full JSON response from FedEx Ship API
        log.info("FedEx Ship API Response JSON:\n{}", responseJson);

        FedExLabelResponse response = responseParser.parseAndValidateResponse(responseJson);

        // Normalize dates in the response object to ensure consistent formatting
        FedExResponseHelper.normalizeDates(response);

        log.info("Shipment created successfully\n{}", FedExResponseHelper.createSummary(response));
        return response;
    }

    /**
     * Creates a tracking request object for FedEx API.
     *
     * @param trackingNumber tracking number
     * @return tracking request object
     */
    private Object createTrackingRequest(String trackingNumber) {
        return new TrackingRequest(trackingNumber);
    }

    /**
     * Creates a cancel request object for FedEx API.
     *
     * @param trackingNumber tracking number
     * @return cancel request object
     */
    private Object createCancelRequest(String trackingNumber) {
        return new CancelRequest(trackingNumber);
    }

    /**
     * Validates that tracking number is not null or empty.
     *
     * @param trackingNumber tracking number to validate
     * @throws IllegalArgumentException if tracking number is invalid
     */
    private void validateTrackingNumber(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Tracking number cannot be null or empty");
        }
    }

    /**
     * Validates that rate request is not null.
     *
     * @param rateRequest rate request to validate
     * @throws IllegalArgumentException if rate request is null
     */
    private void validateRateRequest(Object rateRequest) {
        if (rateRequest == null) {
            throw new IllegalArgumentException("Rate request cannot be null");
        }
    }

    /**
     * Validates that address request is not null.
     *
     * @param addressRequest address request to validate
     * @throws IllegalArgumentException if address request is null
     */
    private void validateAddressRequest(Object addressRequest) {
        if (addressRequest == null) {
            throw new IllegalArgumentException("Address request cannot be null");
        }
    }
    /**
     * Returns comprehensive service statistics including rate limiter, retry config, connection pool, and token cache.
     *
     * @return map containing service statistics
     */
    public Map<String, Object> getServiceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("rateLimiter", rateLimiter.getStats());
        stats.put("retryConfig", retryHelper.getConfig());
        stats.put("connectionPool", httpClient.getConnectionPoolStats());
        stats.put("tokenCache", tokenManager.getCacheStats());
        return stats;
    }

    /**
     * Returns rate limiter statistics.
     *
     * @return map containing rate limiter statistics
     */
    public Map<String, Object> getRateLimiterStats() {
        return rateLimiter.getStats();
    }

    /**
     * Resets rate limiter to full capacity.
     */
    public void resetRateLimiter() {
        rateLimiter.reset();
        log.info("Rate limiter reset to full capacity");
    }

    /** Tracking request structure for FedEx API. */
    private static final class TrackingRequest {
        public final TrackingInfo[] trackingInfo;

        public TrackingRequest(String trackingNumber) {
            this.trackingInfo = new TrackingInfo[]{new TrackingInfo(trackingNumber)};
        }

        private static final class TrackingInfo {
            public final String trackingNumberInfo;

            public TrackingInfo(String trackingNumber) {
                this.trackingNumberInfo = trackingNumber;
            }
        }
    }

    /** Cancel request structure for FedEx API. */
    private static final class CancelRequest {
        public final String trackingNumber;

        public CancelRequest(String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }
    }
}
