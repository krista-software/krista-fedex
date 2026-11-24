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

package app.krista.extensions.fedex.auth;

import app.krista.extensions.fedex.client.FedExHttpClient;
import app.krista.extensions.fedex.config.FedExAttributes;
import app.krista.extensions.util.KeyValueStore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages OAuth token lifecycle including storage, expiry, and refresh coordination.
 */
@Service
public class FedExTokenManager {

    private static final Logger log = LoggerFactory.getLogger(FedExTokenManager.class);
    private static final int TOKEN_REFRESH_BUFFER_SECONDS = 60;
    private static final String TOKEN_KEY_PREFIX = "fedex_token_";
    private static final String OAUTH_TOKEN_ENDPOINT = "/oauth/token";
    private static final String GRANT_TYPE_FIELD = "grant_type";
    private static final String CLIENT_ID_FIELD = "client_id";
    private static final String CLIENT_SECRET_FIELD = "client_secret";
    private static final int API_KEY_MASK_LENGTH = 4;

    private final FedExAttributes attributes;
    private final KeyValueStore keyValueStore;
    private final FedExHttpClient httpClient;
    private final Gson gson;
    private final ReentrantLock tokenLock;

    /**
     * Constructs FedExTokenManager with required dependencies.
     *
     * @param attributes FedEx configuration attributes
     * @param keyValueStore key-value store for token persistence
     * @param httpClient HTTP client for OAuth requests
     */
    @Inject
    public FedExTokenManager(FedExAttributes attributes, KeyValueStore keyValueStore, FedExHttpClient httpClient) {
        this.attributes = attributes;
        this.keyValueStore = keyValueStore;
        this.httpClient = httpClient;
        this.gson = new GsonBuilder().create();
        this.tokenLock = new ReentrantLock();

        logInitialization();
    }

    /**
     * Logs initialization status based on configuration.
     */
    private void logInitialization() {
        String baseUrl = attributes.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            log.warn("FedExTokenManager initialized with empty base URL - configuration not yet loaded");
        } else {
            log.info("FedExTokenManager initialized for base URL: {}", baseUrl);
        }
    }

    /**
     * Returns valid access token, refreshing if expired.
     *
     * @return valid access token
     * @throws IOException if token refresh fails
     */
    public String getValidAccessToken() throws IOException {
        String cacheKey = generateCacheKey();

        tokenLock.lock();
        try {
            TokenInfo tokenInfo = getTokenFromStore(cacheKey);

            if (tokenInfo == null || isTokenExpired(tokenInfo)) {
                log.info("Token expired or not found, refreshing");
                tokenInfo = refreshAccessToken();
                storeToken(cacheKey, tokenInfo);
            } else {
                log.debug("Using cached token, expires at: {}", tokenInfo.getExpiresAt());
            }

            return tokenInfo.getAccessToken();

        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * Forces token refresh by clearing cache and requesting new token.
     *
     * @return new access token
     * @throws IOException if token refresh fails
     */
    public String forceRefreshToken() throws IOException {
        String cacheKey = generateCacheKey();

        tokenLock.lock();
        try {
            log.info("Force refreshing token");
            removeTokenFromStore(cacheKey);

            TokenInfo tokenInfo = refreshAccessToken();
            storeToken(cacheKey, tokenInfo);

            return tokenInfo.getAccessToken();

        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * Clears token from cache.
     */
    public void clearToken() {
        String cacheKey = generateCacheKey();
        tokenLock.lock();
        try {
            removeTokenFromStore(cacheKey);
            log.debug("Token cleared from store: {}", cacheKey);
        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * Checks if token is expired or about to expire within buffer period.
     *
     * @param tokenInfo token information
     * @return true if expired, false otherwise
     */
    private boolean isTokenExpired(TokenInfo tokenInfo) {
        if (tokenInfo == null || tokenInfo.getExpiresAt() == null) {
            return true;
        }

        Instant now = Instant.now();
        Instant expiryWithBuffer = tokenInfo.getExpiresAt().minusSeconds(TOKEN_REFRESH_BUFFER_SECONDS);

        boolean expired = now.isAfter(expiryWithBuffer);
        if (expired) {
            log.debug("Token expired. Now: {}, Expiry with buffer: {}", now, expiryWithBuffer);
        }

        return expired;
    }

    /**
     * Refreshes access token from FedEx OAuth endpoint.
     *
     * @return new token information
     * @throws IOException if token refresh fails
     */
    private TokenInfo refreshAccessToken() throws IOException {
        log.info("Refreshing FedEx access token");

        validateConfiguration();

        Map<String, String> formData = createTokenRequestFormData();

        try {
            String responseBody = httpClient.postForToken(OAUTH_TOKEN_ENDPOINT, formData);
            TokenResponse tokenResponse = gson.fromJson(responseBody, TokenResponse.class);

            Instant expiresAt = Instant.now().plusSeconds(tokenResponse.getExpiresIn());
            TokenInfo tokenInfo = new TokenInfo(
                    tokenResponse.getAccessToken(),
                    tokenResponse.getTokenType(),
                    expiresAt,
                    tokenResponse.getScope()
            );

            log.info("Successfully refreshed access token, expires at: {}", expiresAt);
            return tokenInfo;

        } catch (IOException e) {
            log.error("Failed to refresh token: {}", e.getMessage());
            throw new IOException("Failed to refresh token: " + e.getMessage(), e);
        }
    }

    /**
     * Generates cache key based on client credentials.
     *
     * @return cache key
     */
    private String generateCacheKey() {
        return TOKEN_KEY_PREFIX + String.format("%s:%s:%s",
                attributes.getBaseUrl(),
                attributes.getApiKey(),
                attributes.getGrantType()).hashCode();
    }

    /**
     * Stores token in key-value store.
     *
     * @param key cache key
     * @param tokenInfo token information
     */
    private void storeToken(String key, TokenInfo tokenInfo) {
        try {
            String tokenJson = gson.toJson(tokenInfo);
            keyValueStore.put(key, tokenJson);
            log.debug("Token stored: {}", key);
        } catch (Exception e) {
            log.error("Failed to store token", e);
        }
    }

    /**
     * Retrieves token from key-value store.
     *
     * @param key cache key
     * @return token information or null if not found
     */
    private TokenInfo getTokenFromStore(String key) {
        try {
            Object tokenData = keyValueStore.get(key);
            if (tokenData != null) {
                String tokenJson = tokenData.toString();
                return gson.fromJson(tokenJson, TokenInfo.class);
            }
        } catch (Exception e) {
            log.error("Failed to retrieve token", e);
        }
        return null;
    }

    /**
     * Removes token from key-value store.
     *
     * @param key cache key
     */
    private void removeTokenFromStore(String key) {
        try {
            keyValueStore.remove(key);
            log.debug("Token removed: {}", key);
        } catch (Exception e) {
            log.error("Failed to remove token", e);
        }
    }

    /**
     * Returns current token information for debugging.
     *
     * @return token information or null if not found
     */
    public TokenInfo getTokenInfo() {
        String cacheKey = generateCacheKey();
        return getTokenFromStore(cacheKey);
    }

    /**
     * Returns cache statistics including token count and expiry status.
     *
     * @return map of cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        int tokenCount = countTokensInStore();
        stats.put("tokenCount", tokenCount);

        addCurrentTokenStats(stats);

        return stats;
    }

    /**
     * Counts tokens in key-value store.
     *
     * @return token count
     */
    private int countTokensInStore() {
        int tokenCount = 0;
        for (Map.Entry<String, Object> entry : keyValueStore) {
            if (entry.getKey().startsWith(TOKEN_KEY_PREFIX)) {
                tokenCount++;
            }
        }
        return tokenCount;
    }

    /**
     * Adds current token statistics to stats map.
     *
     * @param stats statistics map
     */
    private void addCurrentTokenStats(Map<String, Object> stats) {
        String currentKey = generateCacheKey();
        TokenInfo currentToken = getTokenFromStore(currentKey);
        if (currentToken != null) {
            stats.put("currentTokenExpiry", currentToken.getExpiresAt());
            stats.put("currentTokenExpired", isTokenExpired(currentToken));
        }
    }
    /**
     * Token information storage class for GSON serialization.
     */
    public static class TokenInfo {
        private String accessToken;
        private String tokenType;
        private String expiresAt;
        private String scope;

        /**
         * Default constructor for GSON deserialization.
         */
        public TokenInfo() {
        }

        /**
         * Constructs TokenInfo with token details.
         *
         * @param accessToken OAuth access token
         * @param tokenType token type (e.g., "Bearer")
         * @param expiresAt expiration timestamp
         * @param scope token scope
         */
        public TokenInfo(String accessToken, String tokenType, Instant expiresAt, String scope) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.expiresAt = expiresAt.toString();
            this.scope = scope;
        }

        /**
         * Returns access token.
         *
         * @return access token
         */
        public String getAccessToken() {
            return accessToken;
        }

        /**
         * Returns token type.
         *
         * @return token type
         */
        public String getTokenType() {
            return tokenType;
        }

        /**
         * Returns expiration timestamp.
         *
         * @return expiration instant
         */
        public Instant getExpiresAt() {
            return expiresAt != null ? Instant.parse(expiresAt) : null;
        }

        /**
         * Returns token scope.
         *
         * @return scope
         */
        public String getScope() {
            return scope;
        }

        /**
         * Sets access token (for GSON).
         *
         * @param accessToken access token
         */
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        /**
         * Sets token type (for GSON).
         *
         * @param tokenType token type
         */
        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        /**
         * Sets expiration timestamp (for GSON).
         *
         * @param expiresAt expiration timestamp as ISO string
         */
        public void setExpiresAt(String expiresAt) {
            this.expiresAt = expiresAt;
        }

        /**
         * Sets token scope (for GSON).
         *
         * @param scope token scope
         */
        public void setScope(String scope) {
            this.scope = scope;
        }

        @Override
        public String toString() {
            return String.format("TokenInfo{tokenType='%s', expiresAt=%s, scope='%s'}",
                    tokenType, expiresAt, scope);
        }
    }

    /**
     * Token response from FedEx OAuth API.
     */
    private static class TokenResponse {
        private String access_token;
        private String token_type;
        private int expires_in;
        private String scope;

        /**
         * Returns access token from response.
         *
         * @return access token
         */
        public String getAccessToken() {
            return access_token;
        }

        /**
         * Returns token type from response.
         *
         * @return token type
         */
        public String getTokenType() {
            return token_type;
        }

        /**
         * Returns expiration duration in seconds.
         *
         * @return expiration duration
         */
        public int getExpiresIn() {
            return expires_in;
        }

        /**
         * Returns token scope from response.
         *
         * @return scope
         */
        public String getScope() {
            return scope;
        }
    }

    /**
     * Tests API connectivity by attempting OAuth token request.
     *
     * @throws IOException if connection test fails
     */
    public void testConnection() throws IOException {
        long startTime = System.currentTimeMillis();
        try {
            log.info("Testing FedEx API credentials");

            testCredentialsByRequestingToken();

            long responseTime = System.currentTimeMillis() - startTime;
            log.info("FedEx credential test successful in {}ms", responseTime);

        } catch (IOException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            String errorMessage = translateErrorMessage(e.getMessage());

            log.error("FedEx credential test failed after {}ms: {}", responseTime, errorMessage);
            throw new IOException("FedEx credential test failed: " + errorMessage, e);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("FedEx credential test failed after {}ms: {}", responseTime, e.getMessage());
            throw new IOException("FedEx credential test failed: " + e.getMessage(), e);
        }
    }

    /**
     * Tests credentials by making OAuth token request without storing result.
     *
     * @throws IOException if credential test fails
     */
    private void testCredentialsByRequestingToken() throws IOException {
        validateConfiguration();

        Map<String, String> formData = createTokenRequestFormData();
        httpClient.postForToken(OAUTH_TOKEN_ENDPOINT, formData);

        log.debug("Credential test successful - received token response");
    }

    /**
     * Translates technical error messages to user-friendly messages.
     *
     * @param errorMessage original error message
     * @return user-friendly error message
     */
    private String translateErrorMessage(String errorMessage) {
        if (errorMessage.contains("Target host is not specified")) {
            return "FedEx API Base URL is missing or invalid. Please verify your Base URL in the extension configuration. Use 'https://apis-sandbox.fedex.com' for testing or 'https://apis.fedex.com' for production.";
        } else if (errorMessage.contains("401") || errorMessage.contains("unauthorized")) {
            return "FedEx API authentication failed. Your API Key or Secret Key is incorrect. Please verify your credentials in the FedEx Developer Portal and update them in the extension configuration. Make sure you're using the correct credentials for your environment (sandbox vs production).";
        } else if (errorMessage.contains("400") || errorMessage.contains("bad request")) {
            return "FedEx API request format is invalid. This usually indicates a configuration issue. Please verify your API Key, Secret Key, and Grant Type are correctly entered. The Grant Type should be 'client_credentials'.";
        } else if (errorMessage.contains("403") || errorMessage.contains("forbidden")) {
            return "FedEx API access denied. Your account may not have permission to use the Ship API, or your credentials may be for a different environment. Please contact FedEx support to verify your API access permissions.";
        } else if (errorMessage.contains("429") || errorMessage.contains("rate limit")) {
            return "FedEx API rate limit exceeded. Too many requests were made in a short time. Please wait a few moments and try again. The extension will automatically retry with delays.";
        } else if (errorMessage.contains("500") || errorMessage.contains("internal server error")) {
            return "FedEx API is experiencing technical difficulties. This is a temporary issue on FedEx's side. Please wait a few minutes and try again. The extension will automatically retry failed requests.";
        } else if (errorMessage.contains("503") || errorMessage.contains("service unavailable")) {
            return "FedEx API is temporarily unavailable for maintenance. Please wait a few minutes and try again. Check the FedEx Developer Portal for any announced maintenance windows.";
        } else if (errorMessage.contains("Connection refused") || errorMessage.contains("timeout")) {
            return "Cannot connect to FedEx API. Please check your internet connection and verify the Base URL is correct. If using a corporate network, check if firewall rules allow access to FedEx APIs.";
        } else if (errorMessage.contains("SSL") || errorMessage.contains("certificate")) {
            return "SSL/Certificate error connecting to FedEx API. This may be due to network security settings. Please contact your IT administrator to ensure SSL certificates are properly configured for FedEx API access.";
        } else if (errorMessage.contains("invalid_client")) {
            return "FedEx API Key is invalid or not recognized. Please verify your API Key is correct and hasn't been revoked. You can regenerate your API Key in the FedEx Developer Portal if needed.";
        } else if (errorMessage.contains("invalid_grant")) {
            return "FedEx authentication grant type is invalid. Please ensure the Grant Type is set to 'client_credentials' in your extension configuration.";
        }

        // If no specific translation found, provide a generic helpful message
        return "FedEx API error: " + errorMessage + ". Please check your extension configuration, verify your credentials in the FedEx Developer Portal, and ensure your network can access FedEx APIs. Contact support if the issue persists.";
    }

    /**
     * Validates FedEx configuration before making requests.
     *
     * @throws IOException if configuration is invalid
     */
    private void validateConfiguration() throws IOException {
        log.debug("Validating FedEx configuration - BaseURL: '{}', ApiKey: '{}', SecretKey: '{}', GrantType: '{}'",
                attributes.getBaseUrl(),
                maskApiKey(attributes.getApiKey()),
                attributes.getSecretKey() != null ? "***" : "null",
                attributes.getGrantType());

        validateBaseUrl();
        validateApiKey();
        validateSecretKey();
        validateGrantType();

        log.debug("FedEx configuration validation passed - Base URL: {}, API Key: {}, Grant Type: {}",
                attributes.getBaseUrl(),
                maskApiKey(attributes.getApiKey()),
                attributes.getGrantType());
    }

    /**
     * Validates base URL is configured.
     *
     * @throws IOException if base URL is not configured
     */
    private void validateBaseUrl() throws IOException {
        if (attributes.getBaseUrl() == null || attributes.getBaseUrl().trim().isEmpty()) {
            log.error("Base URL is null or empty: '{}'", attributes.getBaseUrl());
            throw new IOException("Base URL is not configured. Please set the FedEx API Base URL in the extension settings.");
        }
    }

    /**
     * Validates API key is configured.
     *
     * @throws IOException if API key is not configured
     */
    private void validateApiKey() throws IOException {
        if (attributes.getApiKey() == null || attributes.getApiKey().trim().isEmpty()) {
            log.error("API Key is null or empty");
            throw new IOException("API Key is not configured. Please set the FedEx API Key in the extension settings.");
        }
    }

    /**
     * Validates secret key is configured.
     *
     * @throws IOException if secret key is not configured
     */
    private void validateSecretKey() throws IOException {
        if (attributes.getSecretKey() == null || attributes.getSecretKey().trim().isEmpty()) {
            log.error("Secret Key is null or empty");
            throw new IOException("Secret Key is not configured. Please set the FedEx Secret Key in the extension settings.");
        }
    }

    /**
     * Validates grant type is configured.
     *
     * @throws IOException if grant type is not configured
     */
    private void validateGrantType() throws IOException {
        if (attributes.getGrantType() == null || attributes.getGrantType().trim().isEmpty()) {
            log.error("Grant Type is null or empty: '{}'", attributes.getGrantType());
            throw new IOException("Grant Type is not configured. Please set the OAuth Grant Type (usually 'client_credentials').");
        }
    }

    /**
     * Masks API key for logging purposes.
     *
     * @param apiKey API key to mask
     * @return masked API key
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null) {
            return "null";
        }
        if (apiKey.length() > API_KEY_MASK_LENGTH) {
            return apiKey.substring(0, API_KEY_MASK_LENGTH) + "***";
        }
        return "***";
    }

    /**
     * Creates form data for token requests.
     *
     * @return form data map
     */
    private Map<String, String> createTokenRequestFormData() {
        Map<String, String> formData = new HashMap<>();
        formData.put(GRANT_TYPE_FIELD, attributes.getGrantType().trim());
        formData.put(CLIENT_ID_FIELD, attributes.getApiKey().trim());
        formData.put(CLIENT_SECRET_FIELD, attributes.getSecretKey().trim());
        return formData;
    }

    /**
     * Forces token refresh (public method for FedExService).
     *
     * @throws IOException if token refresh fails
     */
    public void refreshToken() throws IOException {
        forceRefreshToken();
    }

    /**
     * Clears all tokens (public method for FedExService).
     */
    public void clearTokens() {
        clearToken();
    }

    /**
     * Returns token statistics (public method for FedExService).
     *
     * @return map of token statistics
     */
    public Map<String, Object> getTokenStats() {
        return getCacheStats();
    }
}
