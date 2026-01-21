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

package app.krista.extensions.fedex.config;

import app.krista.extension.executor.Invoker;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

/**
 * Configuration attributes for FedEx extension including API credentials and environment settings.
 */
@Service
public class FedExAttributes {

    private static final Logger log = LoggerFactory.getLogger(FedExAttributes.class);
    private static final String DEFAULT_GRANT_TYPE = "client_credentials";
    private static final String SANDBOX_IDENTIFIER = "sandbox";
    private static final String PRODUCTION_IDENTIFIER = "apis.fedex.com";

    private String baseUrl;
    private String apiKey;
    private String secretKey;
    private String grantType;
    private String masterAccountNumber;

    /**
     * Constructs FedExAttributes from invoker.
     *
     * @param invoker invoker containing attributes
     */
    @Inject
    public FedExAttributes(Invoker invoker) {
        this(invoker.getAttributes());
    }

    /**
     * Constructs FedExAttributes from attributes map.
     *
     * @param attributes map of configuration attributes
     */
    public FedExAttributes(Map<String, Object> attributes) {
        update(attributes);
    }

    /**
     * Creates FedExAttributes instance from invoker and attributes.
     *
     * @param invoker invoker containing base attributes
     * @param attributes additional attributes to apply
     * @return new FedExAttributes instance
     */
    public static FedExAttributes create(Invoker invoker, Map<String, Object> attributes) {
        FedExAttributes fedExAttributes = new FedExAttributes(invoker);
        fedExAttributes.update(attributes);
        return fedExAttributes;
    }

    /**
     * Updates configuration attributes from the provided map.
     *
     * @param newAttributes map of new attributes to apply
     */
    public void update(Map<String, Object> newAttributes) {
        this.baseUrl = getStringValue(newAttributes, FedExConstants.BASE_URL, "");
        this.apiKey = getStringValue(newAttributes, FedExConstants.API_KEY, "");
        this.secretKey = getStringValue(newAttributes, FedExConstants.SECRET_KEY, "");
        this.grantType = getStringValue(newAttributes, FedExConstants.GRANT_TYPE, DEFAULT_GRANT_TYPE);
        this.masterAccountNumber = getStringValue(newAttributes, FedExConstants.MASTER_ACCOUNT_NUMBER, "");

        logEnvironmentInfo();
        validateMasterAccountNumber();
    }

    /**
     * Returns the FedEx API base URL.
     *
     * @return base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the FedEx API key (client ID).
     *
     * @return API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Returns the FedEx secret key (client secret).
     *
     * @return secret key
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Returns the OAuth grant type.
     *
     * @return grant type
     */
    public String getGrantType() {
        return grantType;
    }

    /**
     * Returns the client ID (deprecated, use getApiKey instead).
     *
     * @return client ID
     * @deprecated use {@link #getApiKey()} instead
     */
    @Deprecated
    public String getClientId() {
        return apiKey;
    }

    /**
     * Returns the client secret (deprecated, use getSecretKey instead).
     *
     * @return client secret
     * @deprecated use {@link #getSecretKey()} instead
     */
    @Deprecated
    public String getClientSecret() {
        return secretKey;
    }

    /**
     * Returns the master account number for billing.
     *
     * @return master account number
     */
    public String getMasterAccountNumber() {
        return masterAccountNumber;
    }

    /**
     * Extracts string value from attributes map with default fallback.
     *
     * @param attributes map of attributes
     * @param key attribute key
     * @param defaultValue default value if key not found
     * @return string value or default
     */
    private String getStringValue(Map<String, Object> attributes, String key, String defaultValue) {
        Object value = attributes.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Logs environment information based on base URL.
     */
    private void logEnvironmentInfo() {
        if (baseUrl == null || baseUrl.isEmpty()) {
            log.error("FedEx Base URL is not configured");
            return;
        }

        if (baseUrl.contains(SANDBOX_IDENTIFIER)) {
            log.warn("FedEx SANDBOX environment configured: {}", baseUrl);
        } else if (baseUrl.contains(PRODUCTION_IDENTIFIER)) {
            log.info("FedEx PRODUCTION environment configured: {}", baseUrl);
        } else {
            log.warn("Unknown FedEx environment: {}", baseUrl);
        }
    }

    /**
     * Validates that master account number is configured.
     */
    private void validateMasterAccountNumber() {
        if (masterAccountNumber == null || masterAccountNumber.trim().isEmpty()) {
            log.error(" Master Account Number is not configured! This is required for billing.");
            log.error(" Please configure 'Master Account Number' in the FedEx extension settings.");
        } else {
            log.info(" Master Account Number configured: {}",
                masterAccountNumber.replaceAll("\\d(?=\\d{4})", "*")); // Mask all but last 4 digits
        }
    }
}
