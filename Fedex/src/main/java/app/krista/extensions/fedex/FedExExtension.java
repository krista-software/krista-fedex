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

package app.krista.extensions.fedex;

import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.*;
import app.krista.extensions.fedex.config.FedExAttributes;
import app.krista.extensions.fedex.config.FedExConstants;
import app.krista.extensions.fedex.service.FedExService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

/**
 * FedEx Extension - Main extension class for FedEx shipping integration
 *
 * @version 1.0.0
 * @see FedExService
 * @see FedExAttributes
 */
@Java(version = Java.Version.JAVA_21)
@Field.Text(value = FedExConstants.BASE_URL)
@Field.Text(value = FedExConstants.API_KEY)
@Field.Text(value = FedExConstants.SECRET_KEY, isSecured = true)
@Field.Text(value = FedExConstants.GRANT_TYPE)
@Field.Text(value = FedExConstants.MASTER_ACCOUNT_NUMBER)
@Extension(version = "1.0.4", name = "FedEx")
@StaticResource(path = "docs", file = "docs")
public class FedExExtension {

    private static final Logger log = LoggerFactory.getLogger(FedExExtension.class);


    private final Invoker invoker;
    private final FedExService fedexService;
    private final FedExAttributes fedExAttributes;


    /**
     * Constructor with dependency injection
     *
     * @param invoker         Krista invoker for accessing extension context
     * @param fedexService    FedEx service for API operations
     * @param fedExAttributes FedEx configuration attributes
     */
    @Inject
    public FedExExtension(Invoker invoker, FedExService fedexService, FedExAttributes fedExAttributes) {
        this.invoker = invoker;
        this.fedexService = fedexService;
        this.fedExAttributes = fedExAttributes;
        log.info("FedEx Extension initialized successfully");
    }


    /**
     * Validates extension configuration attributes
     * <p>
     * This method is called when the extension configuration is saved.
     * It validates all required fields and tests the connection to FedEx API.
     *
     * @param attributes Configuration attributes to validate
     * @throws IllegalArgumentException if any required attribute is missing or invalid
     */
    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateAttributes(Map<String, Object> attributes) {
        log.info("Validating FedEx extension attributes");

        // Create and update attributes
        FedExAttributes.create(invoker, attributes);
        fedExAttributes.update(attributes);

        // Validate the updated configuration
        validateConfiguration(fedExAttributes);

        log.info("FedEx extension attributes validated successfully");
    }

    /**
     * Provides custom tabs for the extension UI
     * <p>
     * Adds a "Documentation" tab that displays the comprehensive
     * extension documentation in the Krista UI.
     *
     * @return Map of tab names to their resource paths
     */
    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        log.debug("Providing custom tabs for FedEx extension");
        return Map.of("Documentation", "static/docs");
    }

    /**
     * Tests the connection to FedEx API
     * <p>
     * This method is called when the "Test Connection" button is clicked
     * in the extension configuration UI. It attempts to authenticate with
     * FedEx API using the configured credentials.
     *
     * @throws RuntimeException if connection test fails
     */
    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() {
        log.info("Testing FedEx API connection");

        try {
            fedexService.testConnection();
            log.info("FedEx connection test successful");
        } catch (Exception cause) {
            log.error("FedEx connection test failed: {}", cause.getMessage(), cause);
            throw new RuntimeException("FedEx connection test failed: " + cause.getMessage(), cause);
        }
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates FedEx configuration attributes
     * <p>
     * Ensures all required configuration fields are present and valid:
     * <ul>
     *   <li>API Key - Must not be null or empty</li>
     *   <li>Secret Key - Must not be null or empty</li>
     *   <li>Base URL - Must not be null or empty</li>
     *   <li>Grant Type - Must not be null or empty</li>
     * </ul>
     *
     * @param attributes FedEx configuration attributes to validate
     * @throws IllegalArgumentException if any validation fails
     */
    private void validateConfiguration(FedExAttributes attributes) {
        log.debug("Validating FedEx configuration");

        // Validate attributes object
        if (attributes == null) {
            log.error("FedEx configuration is null");
            throw new IllegalArgumentException("FedEx configuration cannot be null");
        }

        // Extract configuration values
        String apiKey = attributes.getApiKey();
        String secretKey = attributes.getSecretKey();
        String baseUrl = attributes.getBaseUrl();
        String grantType = attributes.getGrantType();

        // Validate API Key
        if (isNullOrEmpty(apiKey)) {
            log.error("API Key is missing or empty");
            throw new IllegalArgumentException("FedEx API Key is required. Please obtain your API Key from the FedEx Developer Portal (developer.fedex.com), create a new application if needed, and enter the API Key in the extension configuration. Make sure you're using the correct key for your environment (sandbox vs production).");
        }

        // Validate Secret Key
        if (isNullOrEmpty(secretKey)) {
            log.error("Secret Key is missing or empty");
            throw new IllegalArgumentException("FedEx Secret Key is required. Please obtain your Secret Key from the FedEx Developer Portal (developer.fedex.com) where you created your API Key. The Secret Key is paired with your API Key and must match the same application. Enter it in the extension configuration.");
        }

        // Validate Base URL
        if (isNullOrEmpty(baseUrl)) {
            log.error("Base URL is missing or empty");
            throw new IllegalArgumentException("FedEx API Base URL is required. Please enter the correct URL for your environment: Use 'https://apis-sandbox.fedex.com' for testing/development or 'https://apis.fedex.com' for production. Make sure the URL matches your API credentials.");
        }

        // Validate Grant Type
        if (isNullOrEmpty(grantType)) {
            log.error("Grant Type is missing or empty");
            throw new IllegalArgumentException("Grant Type is required for FedEx API authentication. Please set the Grant Type to 'client_credentials' in the extension configuration. This is the standard OAuth 2.0 grant type used by FedEx APIs.");
        }

        // Validate Grant Type value
        if (!"client_credentials".equalsIgnoreCase(grantType)) {
            log.warn("Grant Type is not 'client_credentials': {}", grantType);
            throw new IllegalArgumentException("Grant Type must be 'client_credentials' for FedEx API authentication. Please update the Grant Type in your extension configuration. This is the only supported authentication method for FedEx APIs.");
        }

        log.debug("FedEx configuration validation successful");
    }

    /**
     * Checks if a string is null or empty
     *
     * @param value String to check
     * @return true if the string is null or empty (after trimming)
     */
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
