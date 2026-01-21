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

package app.krista.extensions.fedex.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * FedEx Response Parser - Utility for parsing FedEx API responses
 * 
 * This class provides methods to parse JSON responses from FedEx API
 * into strongly-typed Java objects
 */
public class FedExResponseParser {

    private final Gson gson;

    public FedExResponseParser() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Parse JSON response string into FedExLabelResponse object
     * 
     * @param jsonResponse JSON response string from FedEx API
     * @return Parsed FedExLabelResponse object
     * @throws FedExResponseParseException if parsing fails
     */
    public FedExLabelResponse parseResponse(String jsonResponse) throws FedExResponseParseException {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            throw new FedExResponseParseException("Response JSON is null or empty");
        }

        try {
            FedExLabelResponse response = gson.fromJson(jsonResponse, FedExLabelResponse.class);
            
            if (response == null) {
                throw new FedExResponseParseException("Parsed response is null");
            }

            return response;

        } catch (JsonSyntaxException e) {
            throw new FedExResponseParseException("FedEx API returned invalid JSON format. This may indicate a temporary issue with the FedEx service. Please try again in a few moments. If the problem persists, contact support with the transaction details. Technical details: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FedExResponseParseException("Unable to process FedEx API response. The response structure was unexpected or incomplete. This could be due to a temporary FedEx API issue or a change in their response format. Please try again, and contact support if the issue continues. Technical details: " + e.getMessage(), e);
        }
    }

    /**
     * Parse and validate JSON response
     * 
     * @param jsonResponse JSON response string from FedEx API
     * @return Parsed and validated FedExLabelResponse object
     * @throws FedExResponseParseException if parsing or validation fails
     */
    public FedExLabelResponse parseAndValidateResponse(String jsonResponse) throws FedExResponseParseException {
        FedExLabelResponse response = parseResponse(jsonResponse);
        validateResponse(response);
        return response;
    }

    /**
     * Validate the parsed response structure
     * 
     * @param response Parsed response object
     * @throws FedExResponseParseException if validation fails
     */
    public void validateResponse(FedExLabelResponse response) throws FedExResponseParseException {
        if (response == null) {
            throw new FedExResponseParseException("FedEx API returned an empty response. This may indicate a temporary service issue. Please try your request again. If the problem persists, check the FedEx API status or contact support.");
        }

        if (response.getTransactionId() == null || response.getTransactionId().trim().isEmpty()) {
            throw new FedExResponseParseException("FedEx response is missing a Transaction ID. This is required for tracking and troubleshooting. The shipment may not have been processed correctly. Please try again or contact support if the issue continues.");
        }

        if (response.getOutput() == null) {
            throw new FedExResponseParseException("FedEx response is missing shipment output data. This indicates the shipment was not successfully created. Please verify your shipment details and try again. If the problem persists, contact support with the Transaction ID.");
        }

        if (response.getOutput().getTransactionShipments() == null ||
            response.getOutput().getTransactionShipments().isEmpty()) {
            throw new FedExResponseParseException("FedEx response contains no shipment information. The shipment creation may have failed. Please check your shipment details, verify your account permissions, and try again. Contact support if the issue continues.");
        }

        // Validate each shipment has required fields
        for (int i = 0; i < response.getOutput().getTransactionShipments().size(); i++) {
            FedExLabelResponse.TransactionShipment shipment = response.getOutput().getTransactionShipments().get(i);

            if (shipment.getMasterTrackingNumber() == null || shipment.getMasterTrackingNumber().trim().isEmpty()) {
                throw new FedExResponseParseException("FedEx did not provide a tracking number for shipment " + (i + 1) + ". This is required for package tracking and delivery. The shipment may not have been fully processed. Please try again or contact FedEx support.");
            }

            if (shipment.getPieceResponses() == null || shipment.getPieceResponses().isEmpty()) {
                throw new FedExResponseParseException("FedEx response is missing package details for shipment " + (i + 1) + ". This includes label information needed for shipping. Please try creating the shipment again. If the issue persists, contact support.");
            }
        }
    }

    /**
     * Convert FedExLabelResponse back to JSON string
     * 
     * @param response Response object to convert
     * @return JSON string representation
     */
    public String toJson(FedExLabelResponse response) {
        if (response == null) {
            return null;
        }
        return gson.toJson(response);
    }

    /**
     * Check if a JSON string represents a valid FedEx response
     * 
     * @param jsonResponse JSON string to check
     * @return true if valid, false otherwise
     */
    public boolean isValidResponse(String jsonResponse) {
        try {
            parseAndValidateResponse(jsonResponse);
            return true;
        } catch (FedExResponseParseException e) {
            return false;
        }
    }

    /**
     * Parse response with error handling and logging
     * 
     * @param jsonResponse JSON response string
     * @return Parsed response or null if parsing fails
     */
    public FedExLabelResponse parseResponseSafely(String jsonResponse) {
        try {
            return parseResponse(jsonResponse);
        } catch (FedExResponseParseException e) {
            System.err.println("Failed to parse FedEx response: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extract basic information from JSON without full parsing
     * 
     * @param jsonResponse JSON response string
     * @return Basic response info or null if extraction fails
     */
    public BasicResponseInfo extractBasicInfo(String jsonResponse) {
        try {
            FedExLabelResponse response = parseResponse(jsonResponse);
            
            String transactionId = response.getTransactionId();
            String masterTracking = FedExResponseHelper.extractMasterTrackingNumber(response);
            String serviceType = FedExResponseHelper.extractServiceType(response);
            boolean successful = FedExResponseHelper.isSuccessful(response);
            int packageCount = FedExResponseHelper.countPackages(response);

            return new BasicResponseInfo(transactionId, masterTracking, serviceType, successful, packageCount);

        } catch (FedExResponseParseException e) {
            return null;
        }
    }

    /**
     * Basic response information for quick access
     */
    public static class BasicResponseInfo {
        private final String transactionId;
        private final String masterTrackingNumber;
        private final String serviceType;
        private final boolean successful;
        private final int packageCount;

        public BasicResponseInfo(String transactionId, String masterTrackingNumber, 
                               String serviceType, boolean successful, int packageCount) {
            this.transactionId = transactionId;
            this.masterTrackingNumber = masterTrackingNumber;
            this.serviceType = serviceType;
            this.successful = successful;
            this.packageCount = packageCount;
        }

        public String getTransactionId() { return transactionId; }
        public String getMasterTrackingNumber() { return masterTrackingNumber; }
        public String getServiceType() { return serviceType; }
        public boolean isSuccessful() { return successful; }
        public int getPackageCount() { return packageCount; }

        @Override
        public String toString() {
            return "BasicResponseInfo{" +
                    "transactionId='" + transactionId + '\'' +
                    ", masterTrackingNumber='" + masterTrackingNumber + '\'' +
                    ", serviceType='" + serviceType + '\'' +
                    ", successful=" + successful +
                    ", packageCount=" + packageCount +
                    '}';
        }
    }

    /**
     * Custom exception for response parsing errors
     */
    public static class FedExResponseParseException extends Exception {
        public FedExResponseParseException(String message) {
            super(message);
        }

        public FedExResponseParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
