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

package app.krista.extensions.fedex.client;

import app.krista.extensions.fedex.config.FedExAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP client for FedEx API communication with connection pooling and timeout management.
 */
@Service
public class FedExHttpClient {

    private static final Logger log = LoggerFactory.getLogger(FedExHttpClient.class);

    private static final int MAX_TOTAL_CONNECTIONS = 100;
    private static final int MAX_PER_ROUTE_CONNECTIONS = 20;
    private static final int CONNECT_TIMEOUT_SECONDS = 10;
    private static final int RESPONSE_TIMEOUT_SECONDS = 30;
    private static final int CONNECTION_REQUEST_TIMEOUT_SECONDS = 5;
    private static final int IDLE_CONNECTION_EVICTION_SECONDS = 30;
    private static final int HTTP_SUCCESS_MIN = 200;
    private static final int HTTP_SUCCESS_MAX = 300;
    private static final String VIRTUAL_RESPONSE_INDICATOR = "VIRTUAL.RESPONSE";
    private static final String BEARER_PREFIX = "Bearer ";

    private final FedExAttributes attributes;
    private final CloseableHttpClient httpClient;
    private final PoolingHttpClientConnectionManager connectionManager;
    private final Gson gson;

    /**
     * Constructs FedExHttpClient with configuration and connection pooling.
     *
     * @param attributes FedEx configuration attributes
     */
    @Inject
    public FedExHttpClient(FedExAttributes attributes) {
        this.attributes = requireNonNull(attributes, "FedExAttributes cannot be null");
        this.connectionManager = createConnectionManager();
        this.httpClient = createHttpClient();
        this.gson = new GsonBuilder().create();

        log.info("FedExHttpClient initialized: {}, pool: {}/{}, timeouts: {}s/{}s/{}s",
                attributes.getBaseUrl(), MAX_TOTAL_CONNECTIONS, MAX_PER_ROUTE_CONNECTIONS,
                CONNECT_TIMEOUT_SECONDS, RESPONSE_TIMEOUT_SECONDS, CONNECTION_REQUEST_TIMEOUT_SECONDS);
    }

    /**
     * Executes authenticated GET request.
     *
     * @param endpoint API endpoint
     * @param authToken authentication token
     * @return response body
     * @throws IOException if request fails
     */
    public String get(String endpoint, String authToken) throws IOException {
        return get(endpoint, authToken, null);
    }

    /**
     * Executes authenticated GET request with custom headers.
     *
     * @param endpoint API endpoint
     * @param authToken authentication token
     * @param headers custom headers
     * @return response body
     * @throws IOException if request fails
     */
    public String get(String endpoint, String authToken, Map<String, String> headers) throws IOException {
        HttpGet request = createGetRequest(endpoint, authToken, headers);
        return executeHttpRequest(request);
    }

    /**
     * Executes authenticated POST request with JSON body.
     *
     * @param endpoint API endpoint
     * @param requestBody request body object
     * @param authToken authentication token
     * @return response body
     * @throws IOException if request fails
     */
    public String post(String endpoint, Object requestBody, String authToken) throws IOException {
        return post(endpoint, requestBody, authToken, null);
    }

    /**
     * Executes authenticated POST request with JSON body and custom headers.
     *
     * @param endpoint API endpoint
     * @param requestBody request body object
     * @param authToken authentication token
     * @param headers custom headers
     * @return response body
     * @throws IOException if request fails
     */
    public String post(String endpoint, Object requestBody, String authToken, Map<String, String> headers) throws IOException {
        HttpPost request = createPostRequest(endpoint, requestBody, authToken, headers);
        return executeHttpRequest(request);
    }

    /**
     * Executes OAuth token request without authentication.
     *
     * @param endpoint API endpoint
     * @param formData form data for token request
     * @return response body
     * @throws IOException if request fails
     */
    public String postForToken(String endpoint, Map<String, String> formData) throws IOException {
        HttpPost request = createTokenRequest(endpoint, formData);
        return executeHttpRequest(request);
    }
    /**
     * Creates connection manager with pooling configuration.
     *
     * @return configured connection manager
     */
    private PoolingHttpClientConnectionManager createConnectionManager() {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        manager.setDefaultMaxPerRoute(MAX_PER_ROUTE_CONNECTIONS);
        return manager;
    }

    /**
     * Creates HTTP client with timeouts and connection pooling.
     *
     * @return configured HTTP client
     */
    private CloseableHttpClient createHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(CONNECT_TIMEOUT_SECONDS))
                .setResponseTimeout(Timeout.ofSeconds(RESPONSE_TIMEOUT_SECONDS))
                .setConnectionRequestTimeout(Timeout.ofSeconds(CONNECTION_REQUEST_TIMEOUT_SECONDS))
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictIdleConnections(TimeValue.ofSeconds(IDLE_CONNECTION_EVICTION_SECONDS))
                .build();
    }

    /**
     * Creates GET request with authentication and headers.
     *
     * @param endpoint API endpoint
     * @param authToken authentication token
     * @param headers custom headers
     * @return configured GET request
     * @throws IOException if URL building fails
     */
    private HttpGet createGetRequest(String endpoint, String authToken, Map<String, String> headers) throws IOException {
        String url = buildUrl(endpoint);
        HttpGet request = new HttpGet(url);

        setJsonHeaders(request);
        setAuthenticationHeader(request, authToken);
        setCustomHeaders(request, headers);

        log.debug("Created GET request: {}", url);
        return request;
    }

    /**
     * Creates POST request with JSON body, authentication and headers.
     *
     * @param endpoint API endpoint
     * @param requestBody request body object
     * @param authToken authentication token
     * @param headers custom headers
     * @return configured POST request
     * @throws IOException if URL building fails
     */
    private HttpPost createPostRequest(String endpoint, Object requestBody, String authToken, Map<String, String> headers) throws IOException {
        String url = buildUrl(endpoint);
        HttpPost request = new HttpPost(url);

        setJsonHeaders(request);
        setAuthenticationHeader(request, authToken);
        setCustomHeaders(request, headers);
        setJsonBody(request, requestBody);

        log.info("POST request to FedEx: {}", url);
        return request;
    }

    /**
     * Creates OAuth token request with form data.
     *
     * @param endpoint API endpoint
     * @param formData form data for token request
     * @return configured POST request
     * @throws IOException if URL building fails
     */
    private HttpPost createTokenRequest(String endpoint, Map<String, String> formData) throws IOException {
        requireNonNull(formData, "Form data cannot be null");

        String url = buildUrl(endpoint);
        HttpPost request = new HttpPost(url);

        setFormHeaders(request);
        setFormBody(request, formData);

        log.debug("Created token request: {}", url);
        return request;
    }

    /**
     * Executes HTTP request and handles response.
     *
     * @param request HTTP request to execute
     * @return response body
     * @throws IOException if request fails
     */
    private String executeHttpRequest(HttpUriRequestBase request) throws IOException {
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return processHttpResponse(response);
        }
    }

    /**
     * Processes HTTP response and extracts body.
     *
     * @param response HTTP response
     * @return response body
     * @throws IOException if response processing fails
     */
    private String processHttpResponse(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getCode();
        String responseBody = extractResponseBody(response);

        if (isSuccessfulResponse(statusCode)) {
            logResponseEnvironment(responseBody, statusCode);
            return responseBody;
        } else {
            String errorMsg = String.format("HTTP request failed. Status: %d, Response: %s", statusCode, responseBody);
            log.error(errorMsg);
            throw new IOException(errorMsg);
        }
    }

    /**
     * Logs response environment information based on response content.
     *
     * @param responseBody response body
     * @param statusCode HTTP status code
     */
    private void logResponseEnvironment(String responseBody, int statusCode) {
        log.info("FedEx API response received. Status: {}", statusCode);

        if (responseBody.contains(VIRTUAL_RESPONSE_INDICATOR)) {
            log.warn("Response contains VIRTUAL.RESPONSE - sandbox mock data");
        } else {
            log.debug("Response appears to be from production environment");
        }
    }

    /**
     * Extracts response body from HTTP response.
     *
     * @param response HTTP response
     * @return response body as string
     * @throws IOException if parsing fails
     */
    private String extractResponseBody(CloseableHttpResponse response) throws IOException {
        try {
            return EntityUtils.toString(response.getEntity());
        } catch (ParseException e) {
            throw new IOException("Failed to parse response body", e);
        }
    }

    /**
     * Checks if HTTP status code indicates success.
     *
     * @param statusCode HTTP status code
     * @return true if successful, false otherwise
     */
    private boolean isSuccessfulResponse(int statusCode) {
        return statusCode >= HTTP_SUCCESS_MIN && statusCode < HTTP_SUCCESS_MAX;
    }
    /**
     * Sets JSON content type headers on request.
     *
     * @param request HTTP request
     */
    private void setJsonHeaders(HttpUriRequestBase request) {
        request.setHeader("Content-Type", "application/json");
    }

    /**
     * Sets form content type headers on request.
     *
     * @param request HTTP POST request
     */
    private void setFormHeaders(HttpPost request) {
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
    }

    /**
     * Sets authentication header if token is provided.
     *
     * @param request HTTP request
     * @param authToken authentication token
     */
    private void setAuthenticationHeader(HttpUriRequestBase request, String authToken) {
        if (authToken != null && !authToken.trim().isEmpty()) {
            request.setHeader("Authorization", BEARER_PREFIX + authToken);
        }
    }

    /**
     * Sets custom headers on request if provided.
     *
     * @param request HTTP request
     * @param headers custom headers map
     */
    private void setCustomHeaders(HttpUriRequestBase request, Map<String, String> headers) {
        if (headers != null) {
            headers.forEach((key, value) -> {
                if (key != null && value != null) {
                    request.setHeader(key, value);
                }
            });
        }
    }

    /**
     * Sets JSON request body on POST request.
     *
     * @param request HTTP POST request
     * @param requestBody request body object
     */
    private void setJsonBody(HttpPost request, Object requestBody) {
        if (requestBody != null) {
            String jsonBody = gson.toJson(requestBody);
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            log.debug("Set JSON body: {}", jsonBody);
        }
    }

    /**
     * Sets form request body on POST request.
     *
     * @param request HTTP POST request
     * @param formData form data map
     */
    private void setFormBody(HttpPost request, Map<String, String> formData) {
        String formBody = buildFormDataString(formData);
        request.setEntity(new StringEntity(formBody, ContentType.APPLICATION_FORM_URLENCODED));
        log.debug("Set form body with {} fields", formData.size());
    }

    /**
     * Builds form data string from map.
     *
     * @param formData form data map
     * @return form data string
     */
    private String buildFormDataString(Map<String, String> formData) {
        StringBuilder formBody = new StringBuilder();
        formData.forEach((key, value) -> {
            if (formBody.length() > 0) {
                formBody.append("&");
            }
            formBody.append(key).append("=").append(value != null ? value : "");
        });
        return formBody.toString();
    }

    /**
     * Builds full URL from endpoint.
     *
     * @param endpoint API endpoint
     * @return full URL
     */
    private String buildUrl(String endpoint) {
        requireNonNull(endpoint, "Endpoint cannot be null");

        String baseUrl = attributes.getBaseUrl().trim();
        String cleanEndpoint = endpoint.trim();

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        if (!cleanEndpoint.startsWith("/")) {
            cleanEndpoint = "/" + cleanEndpoint;
        }

        return baseUrl + cleanEndpoint;
    }

    /**
     * Validates that object is not null.
     *
     * @param obj object to validate
     * @param message error message
     * @param <T> object type
     * @return validated object
     * @throws IllegalArgumentException if object is null
     */
    private <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }

    /**
     * Shuts down HTTP client and releases resources.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down FedExHttpClient");
        try {
            if (httpClient != null) {
                httpClient.close();
                log.info("HTTP client closed");
            }
            if (connectionManager != null) {
                connectionManager.close();
                log.info("Connection manager closed");
            }
        } catch (IOException e) {
            log.error("Error closing HTTP client resources", e);
        }
    }

    /**
     * Returns connection pool statistics.
     *
     * @return map of connection pool statistics
     */
    public Map<String, Object> getConnectionPoolStats() {
        Map<String, Object> stats = new HashMap<>();
        if (connectionManager != null) {
            stats.put("maxTotal", MAX_TOTAL_CONNECTIONS);
            stats.put("maxPerRoute", MAX_PER_ROUTE_CONNECTIONS);
            stats.put("totalStats", connectionManager.getTotalStats().toString());
        }
        return stats;
    }
}
