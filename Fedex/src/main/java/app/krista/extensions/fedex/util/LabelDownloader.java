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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;

/**
 * Utility class for downloading FedEx shipping label PDFs from URLs.
 */
public final class LabelDownloader {

    private static final Logger log = LoggerFactory.getLogger(LabelDownloader.class);

    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 30000;
    private static final int BUFFER_SIZE = 8192;
    private static final String CONTENT_TYPE_PDF = "application/pdf";

    private static final String FEDEX_PRODUCTION_URL = "https://www.fedex.com/document/";
    private static final String FEDEX_TEST_URL = "https://wwwtest.fedex.com/document/";
    private static final String FEDEX_DEVELOPER_URL = "https://developer.fedex.com/api/content/";

    private static final String ENV_SANDBOX = "sandbox";
    private static final String ENV_PRODUCTION = "production";
    private static final String ENV_UNKNOWN = "unknown";

    /** Private constructor to prevent instantiation. */
    private LabelDownloader() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Downloads a PDF label from the specified URL.
     *
     * @param labelUrl the URL of the label to download
     * @return byte array containing the PDF data
     * @throws IllegalArgumentException if labelUrl is null or empty
     * @throws IOException if download fails or HTTP status is not 200
     */
    public static byte[] downloadLabel(String labelUrl) throws IOException {
        validateUrl(labelUrl);

        log.debug("Downloading label from URL: {}", labelUrl);

        HttpURLConnection connection = null;
        try {
            connection = createConnection(labelUrl);
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                String errorMsg = String.format("Failed to download label, HTTP status: %d", responseCode);
                log.error(errorMsg);
                throw new IOException(errorMsg);
            }

            byte[] pdfData = readInputStream(connection.getInputStream());
            log.info("Label downloaded successfully, size: {} bytes", pdfData.length);

            return pdfData;

        } catch (IOException e) {
            log.error("Failed to download label from URL: {}", labelUrl, e);
            throw new IOException("Failed to download label: " + e.getMessage(), e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Downloads a PDF label and returns it as a Base64 encoded string.
     *
     * @param labelUrl the URL of the label to download
     * @return Base64 encoded string of the PDF
     * @throws IllegalArgumentException if labelUrl is null or empty
     * @throws IOException if download fails
     */
    public static String downloadLabelAsBase64(String labelUrl) throws IOException {
        byte[] pdfData = downloadLabel(labelUrl);
        return Base64.getEncoder().encodeToString(pdfData);
    }

    /**
     * Downloads multiple labels from the specified URLs.
     *
     * @param labelUrls list of label URLs to download
     * @return array of byte arrays, one for each label
     * @throws IllegalArgumentException if labelUrls is null or empty
     * @throws IOException if any download fails
     */
    public static byte[][] downloadLabels(List<String> labelUrls) throws IOException {
        if (labelUrls == null || labelUrls.isEmpty()) {
            throw new IllegalArgumentException("Label URLs list cannot be null or empty");
        }

        log.info("Downloading {} labels", labelUrls.size());

        byte[][] labels = new byte[labelUrls.size()][];
        for (int i = 0; i < labelUrls.size(); i++) {
            labels[i] = downloadLabel(labelUrls.get(i));
        }

        log.info("Successfully downloaded {} labels", labelUrls.size());
        return labels;
    }

    /**
     * Validates if a URL is a valid FedEx label URL.
     *
     * @param url the URL to validate
     * @return true if valid FedEx label URL, false otherwise
     */
    public static boolean isValidFedExLabelUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        return url.startsWith(FEDEX_PRODUCTION_URL) ||
               url.startsWith(FEDEX_TEST_URL) ||
               url.startsWith(FEDEX_DEVELOPER_URL);
    }

    /**
     * Determines the environment (sandbox/production) from a label URL.
     *
     * @param url the label URL
     * @return "sandbox", "production", or "unknown"
     */
    public static String getEnvironmentFromUrl(String url) {
        if (url == null) {
            return ENV_UNKNOWN;
        }

        if (url.contains("wwwtest.fedex.com") || url.contains("developer.fedex.com")) {
            return ENV_SANDBOX;
        } else if (url.contains("www.fedex.com")) {
            return ENV_PRODUCTION;
        }

        return ENV_UNKNOWN;
    }

    /**
     * Creates and configures an HTTP connection for label download.
     *
     * @param labelUrl the URL to connect to
     * @return configured HttpURLConnection
     * @throws IOException if connection cannot be established
     */
    private static HttpURLConnection createConnection(String labelUrl) throws IOException {
        URL url = new URL(labelUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("Accept", CONTENT_TYPE_PDF);

        return connection;
    }

    /**
     * Reads all bytes from an InputStream into a byte array.
     *
     * @param inputStream the input stream to read from
     * @return byte array containing all data
     * @throws IOException if read operation fails
     */
    private static byte[] readInputStream(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            buffer.flush();
            return buffer.toByteArray();
        }
    }

    /**
     * Validates that the URL is not null or empty.
     *
     * @param url the URL to validate
     * @throws IllegalArgumentException if URL is null or empty
     */
    private static void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Label URL cannot be null or empty");
        }
    }

    /**
     * Safely closes an HTTP connection.
     *
     * @param connection the connection to close, may be null
     */
    private static void closeConnection(HttpURLConnection connection) {
        if (connection != null) {
            connection.disconnect();
        }
    }
}

