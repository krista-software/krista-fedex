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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FedEx Response Helper - Utility methods for working with FedEx API responses
 *
 * This class provides convenient methods to extract common information
 * from FedEx label generation API responses
 */
public class FedExResponseHelper {

    /**
     * Extract all tracking numbers from the response
     */
    public static List<String> extractTrackingNumbers(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null || 
            response.getOutput().getTransactionShipments() == null) {
            return List.of();
        }

        return response.getOutput().getTransactionShipments().stream()
                .flatMap(shipment -> shipment.getPieceResponses().stream())
                .map(FedExLabelResponse.PieceResponse::getTrackingNumber)
                .collect(Collectors.toList());
    }

    /**
     * Extract master tracking number from the response
     */
    public static String extractMasterTrackingNumber(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null || 
            response.getOutput().getTransactionShipments() == null ||
            response.getOutput().getTransactionShipments().isEmpty()) {
            return null;
        }

        return response.getOutput().getTransactionShipments().get(0).getMasterTrackingNumber();
    }

    /**
     * Extract all label URLs from the response
     */
    public static List<String> extractLabelUrls(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null || 
            response.getOutput().getTransactionShipments() == null) {
            return List.of();
        }

        return response.getOutput().getTransactionShipments().stream()
                .flatMap(shipment -> {
                    // Get URLs from shipment documents
                    List<String> shipmentUrls = shipment.getShipmentDocuments() != null ?
                            shipment.getShipmentDocuments().stream()
                                    .map(FedExLabelResponse.ShipmentDocument::getUrl)
                                    .collect(Collectors.toList()) : List.of();

                    // Get URLs from package documents
                    List<String> packageUrls = shipment.getPieceResponses() != null ?
                            shipment.getPieceResponses().stream()
                                    .flatMap(piece -> piece.getPackageDocuments() != null ?
                                            piece.getPackageDocuments().stream() : java.util.stream.Stream.empty())
                                    .map(FedExLabelResponse.PackageDocument::getUrl)
                                    .collect(Collectors.toList()) : List.of();

                    return java.util.stream.Stream.concat(shipmentUrls.stream(), packageUrls.stream());
                })
                .collect(Collectors.toList());
    }

    /**
     * Extract delivery date from the response
     */
    public static String extractDeliveryDate(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null ||
            response.getOutput().getTransactionShipments() == null ||
            response.getOutput().getTransactionShipments().isEmpty()) {
            return null;
        }

        FedExLabelResponse.TransactionShipment shipment = response.getOutput().getTransactionShipments().get(0);
        if (shipment.getPieceResponses() != null && !shipment.getPieceResponses().isEmpty()) {
            String rawDate = shipment.getPieceResponses().get(0).getDeliveryDatestamp();
            return formatDeliveryDate(rawDate);
        }

        return null;
    }

    /**
     * Normalize all delivery dates in the response object
     * This modifies the response object in-place to ensure all dates are in ISO format
     *
     * @param response FedEx response to normalize
     */
    public static void normalizeDates(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null ||
            response.getOutput().getTransactionShipments() == null) {
            return;
        }

        for (FedExLabelResponse.TransactionShipment shipment : response.getOutput().getTransactionShipments()) {
            if (shipment.getPieceResponses() != null) {
                for (FedExLabelResponse.PieceResponse piece : shipment.getPieceResponses()) {
                    String rawDate = piece.getDeliveryDatestamp();
                    if (rawDate != null) {
                        String formattedDate = formatDeliveryDate(rawDate);
                        piece.setDeliveryDatestamp(formattedDate);
                    }
                }
            }
        }
    }

    /**
     * Format delivery date from FedEx API format to MM-DD-YYYY format
     *
     * FedEx API returns dates in various formats:
     * - "10/20/2025T00:00:00+00:00" (sandbox)
     * - "2023-08-01" (production)
     * - "MM/DD/YYYY"
     * - Error messages like "Text '10/20/2025T00:00:00+00:00' could not be parsed at index 0"
     *
     * @param rawDate Raw date string from FedEx API
     * @return Formatted date in MM-DD-YYYY format, or original string if parsing fails
     */
    private static String formatDeliveryDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return null;
        }

        try {
            // Handle error message format: "Text '10/20/2025T00:00:00+00:00' could not be parsed at index 0"
            if (rawDate.startsWith("Text '") && rawDate.contains("' could not be parsed")) {
                // Extract the date from the error message
                int startIdx = rawDate.indexOf("'") + 1;
                int endIdx = rawDate.indexOf("'", startIdx);
                if (startIdx > 0 && endIdx > startIdx) {
                    rawDate = rawDate.substring(startIdx, endIdx);
                }
            }

            // Format 1: "10/20/2025T00:00:00+00:00" (sandbox format with time)
            if (rawDate.contains("T")) {
                String datePart = rawDate.substring(0, rawDate.indexOf('T'));
                if (datePart.contains("/")) {
                    // MM/DD/YYYY format - already in correct format, just remove time
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                    LocalDate date = LocalDate.parse(datePart, inputFormatter);
                    return date.format(outputFormatter);
                } else if (datePart.contains("-")) {
                    // YYYY-MM-DD format - convert to MM-DD-YYYY
                    LocalDate date = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE);
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                    return date.format(outputFormatter);
                }
            }

            // Format 2: "2023-08-01" (ISO format) - convert to MM-DD-YYYY
            if (rawDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                LocalDate date = LocalDate.parse(rawDate, DateTimeFormatter.ISO_LOCAL_DATE);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                return date.format(outputFormatter);
            }

            // Format 3: "MM/DD/YYYY" (US format without time) - convert to MM-DD-YYYY
            if (rawDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                LocalDate date = LocalDate.parse(rawDate, inputFormatter);
                return date.format(outputFormatter);
            }

            // If no format matches, return original
            return rawDate;

        } catch (DateTimeParseException e) {
            // If parsing fails, return original string
            // This ensures we don't lose the date information even if format is unexpected
            return rawDate;
        }
    }

    /**
     * Extract service type from the response
     */
    public static String extractServiceType(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null || 
            response.getOutput().getTransactionShipments() == null ||
            response.getOutput().getTransactionShipments().isEmpty()) {
            return null;
        }

        return response.getOutput().getTransactionShipments().get(0).getServiceType();
    }

    /**
     * Extract service name from the response
     */
    public static String extractServiceName(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null || 
            response.getOutput().getTransactionShipments() == null ||
            response.getOutput().getTransactionShipments().isEmpty()) {
            return null;
        }

        return response.getOutput().getTransactionShipments().get(0).getServiceName();
    }

    /**
     * Check if the response contains any alerts
     */
    public static boolean hasAlerts(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null || 
            response.getOutput().getTransactionShipments() == null) {
            return false;
        }

        return response.getOutput().getTransactionShipments().stream()
                .anyMatch(shipment -> shipment.getAlerts() != null && !shipment.getAlerts().isEmpty());
    }

    /**
     * Extract all alert messages from the response
     */
    public static List<String> extractAlertMessages(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null || 
            response.getOutput().getTransactionShipments() == null) {
            return List.of();
        }

        return response.getOutput().getTransactionShipments().stream()
                .flatMap(shipment -> shipment.getAlerts() != null ? 
                        shipment.getAlerts().stream() : java.util.stream.Stream.empty())
                .map(FedExLabelResponse.Alert::getMessage)
                .collect(Collectors.toList());
    }

    /**
     * Check if the shipment was successful (has tracking numbers and labels)
     */
    public static boolean isSuccessful(FedExLabelResponse response) {
        List<String> trackingNumbers = extractTrackingNumbers(response);
        List<String> labelUrls = extractLabelUrls(response);
        
        return !trackingNumbers.isEmpty() && !labelUrls.isEmpty();
    }

    /**
     * Create a summary of the response for logging/display
     */
    public static String createSummary(FedExLabelResponse response) {
        if (response == null) {
            return "Response is null";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("FedEx Label Response Summary:\n");
        summary.append("Transaction ID: ").append(response.getTransactionId()).append("\n");
        
        String masterTracking = extractMasterTrackingNumber(response);
        if (masterTracking != null) {
            summary.append("Master Tracking: ").append(masterTracking).append("\n");
        }

        List<String> trackingNumbers = extractTrackingNumbers(response);
        if (!trackingNumbers.isEmpty()) {
            summary.append("Tracking Numbers: ").append(String.join(", ", trackingNumbers)).append("\n");
        }

        String serviceType = extractServiceType(response);
        if (serviceType != null) {
            summary.append("Service Type: ").append(serviceType).append("\n");
        }

        String deliveryDate = extractDeliveryDate(response);
        if (deliveryDate != null) {
            summary.append("Delivery Date: ").append(deliveryDate).append("\n");
        }

        List<String> labelUrls = extractLabelUrls(response);
        summary.append("Label URLs: ").append(labelUrls.size()).append(" found\n");

        if (hasAlerts(response)) {
            List<String> alerts = extractAlertMessages(response);
            summary.append("Alerts: ").append(String.join("; ", alerts)).append("\n");
        }

        summary.append("Status: ").append(isSuccessful(response) ? "SUCCESS" : "FAILED");

        return summary.toString();
    }

    /**
     * Extract the first/primary label URL from the response
     */
    public static String extractPrimaryLabelUrl(FedExLabelResponse response) {
        List<String> urls = extractLabelUrls(response);
        return urls.isEmpty() ? null : urls.get(0);
    }

    /**
     * Count the number of packages in the response
     */
    public static int countPackages(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null ||
            response.getOutput().getTransactionShipments() == null) {
            return 0;
        }

        return response.getOutput().getTransactionShipments().stream()
                .mapToInt(shipment -> shipment.getPieceResponses() != null ?
                        shipment.getPieceResponses().size() : 0)
                .sum();
    }

    /**
     * Extract total shipping cost from the response
     * Returns the total FedEx charge (actual amount billed to customer)
     *
     * @param response FedEx API response
     * @return Total shipping cost, or 0.0 if not available
     */
    public static Double extractTotalShippingCost(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null ||
            response.getOutput().getTransactionShipments() == null) {
            return 0.0;
        }

        return response.getOutput().getTransactionShipments().stream()
                .map(FedExLabelResponse.TransactionShipment::getCompletedShipmentDetail)
                .filter(detail -> detail != null && detail.getShipmentRating() != null)
                .map(FedExLabelResponse.CompletedShipmentDetail::getShipmentRating)
                .flatMap(rating -> rating.getShipmentRateDetails() != null ?
                        rating.getShipmentRateDetails().stream() : java.util.stream.Stream.empty())
                .map(FedExLabelResponse.ShipmentRateDetail::getTotalNetFedExCharge)
                .filter(amount -> amount != null)
                .findFirst()
                .orElse(0.0);
    }

    /**
     * Extract base rate (before discounts) from the response
     * Returns the total base charge from shipmentRating
     *
     * @param response FedEx API response
     * @return Total base rate, or 0.0 if not available
     */
    public static Double extractBaseRate(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null ||
            response.getOutput().getTransactionShipments() == null) {
            return 0.0;
        }

        return response.getOutput().getTransactionShipments().stream()
                .map(FedExLabelResponse.TransactionShipment::getCompletedShipmentDetail)
                .filter(detail -> detail != null && detail.getShipmentRating() != null)
                .map(FedExLabelResponse.CompletedShipmentDetail::getShipmentRating)
                .flatMap(rating -> rating.getShipmentRateDetails() != null ?
                        rating.getShipmentRateDetails().stream() : java.util.stream.Stream.empty())
                .map(FedExLabelResponse.ShipmentRateDetail::getTotalBaseCharge)
                .filter(amount -> amount != null)
                .findFirst()
                .orElse(0.0);
    }

    /**
     * Extract total discount amount from the response
     * Returns the total freight discounts from shipmentRating
     *
     * @param response FedEx API response
     * @return Total discount amount, or 0.0 if not available
     */
    public static Double extractTotalDiscount(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null ||
            response.getOutput().getTransactionShipments() == null) {
            return 0.0;
        }

        return response.getOutput().getTransactionShipments().stream()
                .map(FedExLabelResponse.TransactionShipment::getCompletedShipmentDetail)
                .filter(detail -> detail != null && detail.getShipmentRating() != null)
                .map(FedExLabelResponse.CompletedShipmentDetail::getShipmentRating)
                .flatMap(rating -> rating.getShipmentRateDetails() != null ?
                        rating.getShipmentRateDetails().stream() : java.util.stream.Stream.empty())
                .map(FedExLabelResponse.ShipmentRateDetail::getTotalFreightDiscounts)
                .filter(amount -> amount != null)
                .findFirst()
                .orElse(0.0);
    }

    /**
     * Extract currency code from the response
     * Returns the currency from shipmentRating, defaults to USD if not available
     *
     * @param response FedEx API response
     * @return Currency code (defaults to "USD")
     */
    public static String extractCurrencyCode(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null ||
            response.getOutput().getTransactionShipments() == null) {
            return "USD";
        }

        return response.getOutput().getTransactionShipments().stream()
                .map(FedExLabelResponse.TransactionShipment::getCompletedShipmentDetail)
                .filter(detail -> detail != null && detail.getShipmentRating() != null)
                .map(FedExLabelResponse.CompletedShipmentDetail::getShipmentRating)
                .flatMap(rating -> rating.getShipmentRateDetails() != null ?
                        rating.getShipmentRateDetails().stream() : java.util.stream.Stream.empty())
                .map(FedExLabelResponse.ShipmentRateDetail::getCurrency)
                .filter(currency -> currency != null && !currency.isEmpty())
                .findFirst()
                .orElse("USD");
    }

    /**
     * Extract total surcharges amount from the response
     * Returns the total surcharges from shipmentRating
     *
     * @param response FedEx API response
     * @return Total surcharges amount, or 0.0 if not available
     */
    public static Double extractTotalSurcharges(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null ||
            response.getOutput().getTransactionShipments() == null) {
            return 0.0;
        }

        return response.getOutput().getTransactionShipments().stream()
                .map(FedExLabelResponse.TransactionShipment::getCompletedShipmentDetail)
                .filter(detail -> detail != null && detail.getShipmentRating() != null)
                .map(FedExLabelResponse.CompletedShipmentDetail::getShipmentRating)
                .flatMap(rating -> rating.getShipmentRateDetails() != null ?
                        rating.getShipmentRateDetails().stream() : java.util.stream.Stream.empty())
                .map(FedExLabelResponse.ShipmentRateDetail::getTotalSurcharges)
                .filter(amount -> amount != null)
                .findFirst()
                .orElse(0.0);
    }

    /**
     * Extract surcharges list from the response
     * Returns the list of individual surcharges (fuel, insurance, etc.)
     *
     * @param response FedEx API response
     * @return List of surcharges, or empty list if not available
     */
    public static java.util.List<FedExLabelResponse.Surcharge> extractSurcharges(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null ||
            response.getOutput().getTransactionShipments() == null) {
            return java.util.Collections.emptyList();
        }

        return response.getOutput().getTransactionShipments().stream()
                .map(FedExLabelResponse.TransactionShipment::getCompletedShipmentDetail)
                .filter(detail -> detail != null && detail.getShipmentRating() != null)
                .map(FedExLabelResponse.CompletedShipmentDetail::getShipmentRating)
                .flatMap(rating -> rating.getShipmentRateDetails() != null ?
                        rating.getShipmentRateDetails().stream() : java.util.stream.Stream.empty())
                .map(FedExLabelResponse.ShipmentRateDetail::getSurcharges)
                .filter(surcharges -> surcharges != null && !surcharges.isEmpty())
                .findFirst()
                .orElse(java.util.Collections.emptyList());
    }

    /**
     * Extract specific surcharge amount by type (e.g., "INSURED_VALUE", "FUEL")
     * Returns the amount for the specified surcharge type
     *
     * @param response FedEx API response
     * @param surchargeType surcharge type to find (e.g., "INSURED_VALUE", "FUEL", "DEMAND")
     * @return Surcharge amount, or 0.0 if not found
     */
    public static Double extractSurchargeAmount(FedExLabelResponse response, String surchargeType) {
        if (response == null || surchargeType == null || surchargeType.isEmpty()) {
            return 0.0;
        }

        java.util.List<FedExLabelResponse.Surcharge> surcharges = extractSurcharges(response);
        return surcharges.stream()
                .filter(s -> surchargeType.equals(s.getSurchargeType()))
                .map(FedExLabelResponse.Surcharge::getAmount)
                .filter(amount -> amount != null)
                .findFirst()
                .orElse(0.0);
    }
}
