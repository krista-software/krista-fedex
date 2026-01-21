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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Unit tests for FedExResponseHelper
 */
public class FedExResponseHelperTest {

    // ========== DELIVERY DATE FORMATTING TESTS ==========

    @Test
    public void testFormatDeliveryDate_SandboxFormat() throws Exception {
        // Test sandbox format: "10/20/2025T00:00:00+00:00"
        String rawDate = "10/20/2025T00:00:00+00:00";
        String formatted = invokeFormatDeliveryDate(rawDate);

        assertEquals("10-20-2025", formatted,
            "Sandbox format should be converted to MM-DD-YYYY format");
    }

    @Test
    public void testFormatDeliveryDate_ISOFormat() throws Exception {
        // Test ISO format: "2023-08-01"
        String rawDate = "2023-08-01";
        String formatted = invokeFormatDeliveryDate(rawDate);

        assertEquals("08-01-2023", formatted,
            "ISO format should be converted to MM-DD-YYYY format");
    }

    @Test
    public void testFormatDeliveryDate_USFormat() throws Exception {
        // Test US format without time: "10/20/2025"
        String rawDate = "10/20/2025";
        String formatted = invokeFormatDeliveryDate(rawDate);

        assertEquals("10-20-2025", formatted,
            "US format should be converted to MM-DD-YYYY format");
    }

    @Test
    public void testFormatDeliveryDate_ISOWithTime() throws Exception {
        // Test ISO format with time: "2023-08-01T00:00:00+00:00"
        String rawDate = "2023-08-01T00:00:00+00:00";
        String formatted = invokeFormatDeliveryDate(rawDate);

        assertEquals("08-01-2023", formatted,
            "ISO format with time should extract date part and convert to MM-DD-YYYY");
    }

    @Test
    public void testFormatDeliveryDate_ErrorMessage() throws Exception {
        // Test error message format: "Text '10/20/2025T00:00:00+00:00' could not be parsed at index 0"
        String rawDate = "Text '10/20/2025T00:00:00+00:00' could not be parsed at index 0";
        String formatted = invokeFormatDeliveryDate(rawDate);

        assertEquals("10-20-2025", formatted,
            "Error message should be parsed and date extracted in MM-DD-YYYY format");
    }

    @Test
    public void testFormatDeliveryDate_NullInput() throws Exception {
        String formatted = invokeFormatDeliveryDate(null);
        assertNull(formatted, "Null input should return null");
    }

    @Test
    public void testFormatDeliveryDate_EmptyInput() throws Exception {
        String formatted = invokeFormatDeliveryDate("");
        assertNull(formatted, "Empty input should return null");
    }

    @Test
    public void testFormatDeliveryDate_WhitespaceInput() throws Exception {
        String formatted = invokeFormatDeliveryDate("   ");
        assertNull(formatted, "Whitespace input should return null");
    }

    @Test
    public void testFormatDeliveryDate_InvalidFormat() throws Exception {
        // Test invalid format - should return original string
        String rawDate = "invalid-date-format";
        String formatted = invokeFormatDeliveryDate(rawDate);
        
        assertEquals("invalid-date-format", formatted, 
            "Invalid format should return original string");
    }

    @Test
    public void testFormatDeliveryDate_VariousDates() throws Exception {
        // Test various valid dates
        assertEquals("01-15-2025", invokeFormatDeliveryDate("01/15/2025"));
        assertEquals("12-31-2025", invokeFormatDeliveryDate("12/31/2025"));
        assertEquals("02-29-2024", invokeFormatDeliveryDate("02/29/2024")); // Leap year
        assertEquals("03-01-2025", invokeFormatDeliveryDate("03/01/2025T12:30:45+00:00"));
    }

    // ========== EXTRACT DELIVERY DATE TESTS ==========

    @Test
    public void testExtractDeliveryDate_ValidResponse() {
        FedExLabelResponse response = createResponseWithDeliveryDate("10/20/2025T00:00:00+00:00");
        String deliveryDate = FedExResponseHelper.extractDeliveryDate(response);

        assertEquals("10-20-2025", deliveryDate,
            "Should extract and format delivery date to MM-DD-YYYY");
    }

    @Test
    public void testExtractDeliveryDate_NullResponse() {
        String deliveryDate = FedExResponseHelper.extractDeliveryDate(null);
        assertNull(deliveryDate, "Null response should return null");
    }

    @Test
    public void testExtractDeliveryDate_NoOutput() {
        FedExLabelResponse response = new FedExLabelResponse();
        String deliveryDate = FedExResponseHelper.extractDeliveryDate(response);
        assertNull(deliveryDate, "Response without output should return null");
    }

    @Test
    public void testExtractDeliveryDate_NoShipments() {
        FedExLabelResponse response = new FedExLabelResponse();
        response.setOutput(new FedExLabelResponse.Output());
        String deliveryDate = FedExResponseHelper.extractDeliveryDate(response);
        assertNull(deliveryDate, "Response without shipments should return null");
    }

    // ========== NORMALIZE DATES TESTS ==========

    @Test
    public void testNormalizeDates_SandboxFormat() {
        FedExLabelResponse response = createResponseWithDeliveryDate("10/20/2025T00:00:00+00:00");

        // Before normalization
        String rawDate = response.getOutput().getTransactionShipments().get(0)
                .getPieceResponses().get(0).getDeliveryDatestamp();
        assertEquals("10/20/2025T00:00:00+00:00", rawDate);

        // Normalize
        FedExResponseHelper.normalizeDates(response);

        // After normalization
        String normalizedDate = response.getOutput().getTransactionShipments().get(0)
                .getPieceResponses().get(0).getDeliveryDatestamp();
        assertEquals("10-20-2025", normalizedDate,
            "Date should be normalized to MM-DD-YYYY format");
    }

    @Test
    public void testNormalizeDates_ISOFormat() {
        FedExLabelResponse response = createResponseWithDeliveryDate("2023-08-01");

        // Normalize
        FedExResponseHelper.normalizeDates(response);

        // After normalization - should be converted to MM-DD-YYYY
        String normalizedDate = response.getOutput().getTransactionShipments().get(0)
                .getPieceResponses().get(0).getDeliveryDatestamp();
        assertEquals("08-01-2023", normalizedDate,
            "ISO format should be converted to MM-DD-YYYY format");
    }

    @Test
    public void testNormalizeDates_ErrorMessage() {
        FedExLabelResponse response = createResponseWithDeliveryDate(
            "Text '10/20/2025T00:00:00+00:00' could not be parsed at index 0");

        // Normalize
        FedExResponseHelper.normalizeDates(response);

        // After normalization - should extract date from error message
        String normalizedDate = response.getOutput().getTransactionShipments().get(0)
                .getPieceResponses().get(0).getDeliveryDatestamp();
        assertEquals("10-20-2025", normalizedDate,
            "Error message should be parsed and date extracted in MM-DD-YYYY format");
    }

    @Test
    public void testNormalizeDates_NullResponse() {
        // Should not throw exception
        assertDoesNotThrow(() -> FedExResponseHelper.normalizeDates(null));
    }

    @Test
    public void testNormalizeDates_EmptyResponse() {
        FedExLabelResponse response = new FedExLabelResponse();
        // Should not throw exception
        assertDoesNotThrow(() -> FedExResponseHelper.normalizeDates(response));
    }

    // ========== EXTRACT TRACKING NUMBERS TESTS ==========

    @Test
    public void testExtractTrackingNumbers_ValidResponse() {
        FedExLabelResponse response = createResponseWithTracking("794947717776");
        List<String> trackingNumbers = FedExResponseHelper.extractTrackingNumbers(response);
        
        assertNotNull(trackingNumbers);
        assertEquals(1, trackingNumbers.size());
        assertEquals("794947717776", trackingNumbers.get(0));
    }

    @Test
    public void testExtractTrackingNumbers_NullResponse() {
        List<String> trackingNumbers = FedExResponseHelper.extractTrackingNumbers(null);
        assertNotNull(trackingNumbers);
        assertTrue(trackingNumbers.isEmpty());
    }

    // ========== EXTRACT MASTER TRACKING NUMBER TESTS ==========

    @Test
    public void testExtractMasterTrackingNumber_ValidResponse() {
        FedExLabelResponse response = createResponseWithMasterTracking("794947717776");
        String masterTracking = FedExResponseHelper.extractMasterTrackingNumber(response);

        assertEquals("794947717776", masterTracking);
    }

    @Test
    public void testExtractMasterTrackingNumber_NullResponse() {
        String masterTracking = FedExResponseHelper.extractMasterTrackingNumber(null);
        assertNull(masterTracking);
    }

    // ========== EXTRACT SHIPPING COST TESTS ==========

    @Test
    public void testExtractTotalShippingCost_ValidResponse() {
        FedExLabelResponse response = createResponseWithShippingCost(45.67, 40.00, 5.67, "USD");
        Double cost = FedExResponseHelper.extractTotalShippingCost(response);

        assertNotNull(cost);
        assertEquals(45.67, cost, 0.001);
    }

    @Test
    public void testExtractTotalShippingCost_ZeroValue() {
        FedExLabelResponse response = createResponseWithShippingCost(0.0, 0.0, 0.0, "USD");
        Double cost = FedExResponseHelper.extractTotalShippingCost(response);

        assertNotNull(cost);
        assertEquals(0.0, cost, 0.001, "Should return 0.0 when API returns zero");
    }

    @Test
    public void testExtractTotalShippingCost_NullResponse() {
        Double cost = FedExResponseHelper.extractTotalShippingCost(null);

        assertNotNull(cost);
        assertEquals(0.0, cost, 0.001, "Should return 0.0 for null response");
    }

    @Test
    public void testExtractTotalShippingCost_NoShippingData() {
        FedExLabelResponse response = new FedExLabelResponse();
        response.setOutput(new FedExLabelResponse.Output());
        Double cost = FedExResponseHelper.extractTotalShippingCost(response);

        assertNotNull(cost);
        assertEquals(0.0, cost, 0.001, "Should return 0.0 when no shipping data available");
    }

    // ========== EXTRACT BASE RATE TESTS ==========

    @Test
    public void testExtractBaseRate_ValidResponse() {
        FedExLabelResponse response = createResponseWithShippingCost(45.67, 40.00, 5.67, "USD");
        Double baseRate = FedExResponseHelper.extractBaseRate(response);

        assertNotNull(baseRate);
        assertEquals(40.00, baseRate, 0.001);
    }

    @Test
    public void testExtractBaseRate_ZeroValue() {
        FedExLabelResponse response = createResponseWithShippingCost(0.0, 0.0, 0.0, "USD");
        Double baseRate = FedExResponseHelper.extractBaseRate(response);

        assertNotNull(baseRate);
        assertEquals(0.0, baseRate, 0.001, "Should return 0.0 when API returns zero");
    }

    @Test
    public void testExtractBaseRate_NullResponse() {
        Double baseRate = FedExResponseHelper.extractBaseRate(null);

        assertNotNull(baseRate);
        assertEquals(0.0, baseRate, 0.001, "Should return 0.0 for null response");
    }

    // ========== EXTRACT TOTAL DISCOUNT TESTS ==========

    @Test
    public void testExtractTotalDiscount_ValidResponse() {
        FedExLabelResponse response = createResponseWithShippingCost(45.67, 40.00, 5.67, "USD");
        Double discount = FedExResponseHelper.extractTotalDiscount(response);

        assertNotNull(discount);
        assertEquals(5.67, discount, 0.001);
    }

    @Test
    public void testExtractTotalDiscount_ZeroValue() {
        FedExLabelResponse response = createResponseWithShippingCost(0.0, 0.0, 0.0, "USD");
        Double discount = FedExResponseHelper.extractTotalDiscount(response);

        assertNotNull(discount);
        assertEquals(0.0, discount, 0.001, "Should return 0.0 when API returns zero");
    }

    @Test
    public void testExtractTotalDiscount_NullResponse() {
        Double discount = FedExResponseHelper.extractTotalDiscount(null);

        assertNotNull(discount);
        assertEquals(0.0, discount, 0.001, "Should return 0.0 for null response");
    }

    // ========== EXTRACT CURRENCY CODE TESTS ==========

    @Test
    public void testExtractCurrencyCode_ValidResponse() {
        FedExLabelResponse response = createResponseWithShippingCost(45.67, 40.00, 5.67, "EUR");
        String currency = FedExResponseHelper.extractCurrencyCode(response);

        assertEquals("EUR", currency);
    }

    @Test
    public void testExtractCurrencyCode_DefaultUSD() {
        FedExLabelResponse response = new FedExLabelResponse();
        response.setOutput(new FedExLabelResponse.Output());
        String currency = FedExResponseHelper.extractCurrencyCode(response);

        assertEquals("USD", currency, "Should default to USD when no currency available");
    }

    @Test
    public void testExtractCurrencyCode_NullResponse() {
        String currency = FedExResponseHelper.extractCurrencyCode(null);

        assertEquals("USD", currency, "Should default to USD for null response");
    }

    // ========== HELPER METHODS ==========

    /**
     * Use reflection to invoke private formatDeliveryDate method
     */
    private String invokeFormatDeliveryDate(String rawDate) throws Exception {
        Method method = FedExResponseHelper.class.getDeclaredMethod("formatDeliveryDate", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, rawDate);
    }

    /**
     * Create a response with delivery date
     */
    private FedExLabelResponse createResponseWithDeliveryDate(String deliveryDate) {
        FedExLabelResponse response = new FedExLabelResponse();
        FedExLabelResponse.Output output = new FedExLabelResponse.Output();
        FedExLabelResponse.TransactionShipment shipment = new FedExLabelResponse.TransactionShipment();
        FedExLabelResponse.PieceResponse piece = new FedExLabelResponse.PieceResponse();
        
        piece.setDeliveryDatestamp(deliveryDate);
        shipment.setPieceResponses(List.of(piece));
        output.setTransactionShipments(List.of(shipment));
        response.setOutput(output);
        
        return response;
    }

    /**
     * Create a response with tracking number
     */
    private FedExLabelResponse createResponseWithTracking(String trackingNumber) {
        FedExLabelResponse response = new FedExLabelResponse();
        FedExLabelResponse.Output output = new FedExLabelResponse.Output();
        FedExLabelResponse.TransactionShipment shipment = new FedExLabelResponse.TransactionShipment();
        FedExLabelResponse.PieceResponse piece = new FedExLabelResponse.PieceResponse();
        
        piece.setTrackingNumber(trackingNumber);
        shipment.setPieceResponses(List.of(piece));
        output.setTransactionShipments(List.of(shipment));
        response.setOutput(output);
        
        return response;
    }

    /**
     * Create a response with master tracking number
     */
    private FedExLabelResponse createResponseWithMasterTracking(String masterTracking) {
        FedExLabelResponse response = new FedExLabelResponse();
        FedExLabelResponse.Output output = new FedExLabelResponse.Output();
        FedExLabelResponse.TransactionShipment shipment = new FedExLabelResponse.TransactionShipment();

        shipment.setMasterTrackingNumber(masterTracking);
        output.setTransactionShipments(List.of(shipment));
        response.setOutput(output);

        return response;
    }

    /**
     * Create a response with shipping cost information
     */
    private FedExLabelResponse createResponseWithShippingCost(Double totalNetCharge, Double totalBaseCharge,
                                                               Double totalFreightDiscounts, String currency) {
        FedExLabelResponse response = new FedExLabelResponse();
        FedExLabelResponse.Output output = new FedExLabelResponse.Output();
        FedExLabelResponse.TransactionShipment shipment = new FedExLabelResponse.TransactionShipment();
        FedExLabelResponse.CompletedShipmentDetail completedDetail = new FedExLabelResponse.CompletedShipmentDetail();
        FedExLabelResponse.ShipmentRating shipmentRating = new FedExLabelResponse.ShipmentRating();
        FedExLabelResponse.ShipmentRateDetail rateDetail = new FedExLabelResponse.ShipmentRateDetail();

        // Set shipping cost values
        rateDetail.setTotalNetCharge(totalNetCharge);
        rateDetail.setTotalNetFedExCharge(totalNetCharge); // For domestic shipments, same as totalNetCharge
        rateDetail.setTotalBaseCharge(totalBaseCharge);
        rateDetail.setTotalFreightDiscounts(totalFreightDiscounts);
        rateDetail.setCurrency(currency);

        // Build the response structure
        shipmentRating.setShipmentRateDetails(List.of(rateDetail));
        completedDetail.setShipmentRating(shipmentRating);
        shipment.setCompletedShipmentDetail(completedDetail);
        output.setTransactionShipments(List.of(shipment));
        response.setOutput(output);

        return response;
    }

    // ========== EXTRACT SURCHARGES TESTS ==========

    @Test
    public void testExtractTotalSurcharges_ValidResponse() {
        FedExLabelResponse response = createResponseWithSurcharges(14.86);
        Double totalSurcharges = FedExResponseHelper.extractTotalSurcharges(response);

        assertNotNull(totalSurcharges);
        assertEquals(14.86, totalSurcharges, 0.001);
    }

    @Test
    public void testExtractTotalSurcharges_ZeroValue() {
        FedExLabelResponse response = createResponseWithSurcharges(0.0);
        Double totalSurcharges = FedExResponseHelper.extractTotalSurcharges(response);

        assertNotNull(totalSurcharges);
        assertEquals(0.0, totalSurcharges, 0.001);
    }

    @Test
    public void testExtractTotalSurcharges_NullResponse() {
        Double totalSurcharges = FedExResponseHelper.extractTotalSurcharges(null);

        assertNotNull(totalSurcharges);
        assertEquals(0.0, totalSurcharges, 0.001);
    }

    @Test
    public void testExtractSurcharges_ValidResponse() {
        FedExLabelResponse response = createResponseWithSurchargesList();
        List<FedExLabelResponse.Surcharge> surcharges = FedExResponseHelper.extractSurcharges(response);

        assertNotNull(surcharges);
        assertEquals(2, surcharges.size());
        assertEquals("INSURED_VALUE", surcharges.get(0).getSurchargeType());
        assertEquals(4.5, surcharges.get(0).getAmount(), 0.001);
        assertEquals("FUEL", surcharges.get(1).getSurchargeType());
        assertEquals(10.36, surcharges.get(1).getAmount(), 0.001);
    }

    @Test
    public void testExtractSurcharges_EmptyList() {
        FedExLabelResponse response = createResponseWithShippingCost(67.98, 53.12, 0.0, "USD");
        List<FedExLabelResponse.Surcharge> surcharges = FedExResponseHelper.extractSurcharges(response);

        assertNotNull(surcharges);
        assertTrue(surcharges.isEmpty());
    }

    @Test
    public void testExtractSurcharges_NullResponse() {
        List<FedExLabelResponse.Surcharge> surcharges = FedExResponseHelper.extractSurcharges(null);

        assertNotNull(surcharges);
        assertTrue(surcharges.isEmpty());
    }

    @Test
    public void testExtractSurchargeAmount_InsuredValue() {
        FedExLabelResponse response = createResponseWithSurchargesList();
        Double amount = FedExResponseHelper.extractSurchargeAmount(response, "INSURED_VALUE");

        assertNotNull(amount);
        assertEquals(4.5, amount, 0.001);
    }

    @Test
    public void testExtractSurchargeAmount_Fuel() {
        FedExLabelResponse response = createResponseWithSurchargesList();
        Double amount = FedExResponseHelper.extractSurchargeAmount(response, "FUEL");

        assertNotNull(amount);
        assertEquals(10.36, amount, 0.001);
    }

    @Test
    public void testExtractSurchargeAmount_NotFound() {
        FedExLabelResponse response = createResponseWithSurchargesList();
        Double amount = FedExResponseHelper.extractSurchargeAmount(response, "DEMAND");

        assertNotNull(amount);
        assertEquals(0.0, amount, 0.001);
    }

    @Test
    public void testExtractSurchargeAmount_NullResponse() {
        Double amount = FedExResponseHelper.extractSurchargeAmount(null, "INSURED_VALUE");

        assertNotNull(amount);
        assertEquals(0.0, amount, 0.001);
    }

    @Test
    public void testExtractSurchargeAmount_NullType() {
        FedExLabelResponse response = createResponseWithSurchargesList();
        Double amount = FedExResponseHelper.extractSurchargeAmount(response, null);

        assertNotNull(amount);
        assertEquals(0.0, amount, 0.001);
    }

    /**
     * Create a response with total surcharges
     */
    private FedExLabelResponse createResponseWithSurcharges(Double totalSurcharges) {
        FedExLabelResponse response = new FedExLabelResponse();
        FedExLabelResponse.Output output = new FedExLabelResponse.Output();
        FedExLabelResponse.TransactionShipment shipment = new FedExLabelResponse.TransactionShipment();
        FedExLabelResponse.CompletedShipmentDetail completedDetail = new FedExLabelResponse.CompletedShipmentDetail();
        FedExLabelResponse.ShipmentRating shipmentRating = new FedExLabelResponse.ShipmentRating();
        FedExLabelResponse.ShipmentRateDetail rateDetail = new FedExLabelResponse.ShipmentRateDetail();

        rateDetail.setTotalSurcharges(totalSurcharges);

        shipmentRating.setShipmentRateDetails(List.of(rateDetail));
        completedDetail.setShipmentRating(shipmentRating);
        shipment.setCompletedShipmentDetail(completedDetail);
        output.setTransactionShipments(List.of(shipment));
        response.setOutput(output);

        return response;
    }

    /**
     * Create a response with surcharges list (INSURED_VALUE and FUEL)
     */
    private FedExLabelResponse createResponseWithSurchargesList() {
        FedExLabelResponse response = new FedExLabelResponse();
        FedExLabelResponse.Output output = new FedExLabelResponse.Output();
        FedExLabelResponse.TransactionShipment shipment = new FedExLabelResponse.TransactionShipment();
        FedExLabelResponse.CompletedShipmentDetail completedDetail = new FedExLabelResponse.CompletedShipmentDetail();
        FedExLabelResponse.ShipmentRating shipmentRating = new FedExLabelResponse.ShipmentRating();
        FedExLabelResponse.ShipmentRateDetail rateDetail = new FedExLabelResponse.ShipmentRateDetail();

        // Create surcharges list
        FedExLabelResponse.Surcharge insuredValue = new FedExLabelResponse.Surcharge("INSURED_VALUE", "Insured value", 4.5);
        FedExLabelResponse.Surcharge fuel = new FedExLabelResponse.Surcharge("FUEL", "Fuel", 10.36);
        rateDetail.setSurcharges(List.of(insuredValue, fuel));
        rateDetail.setTotalSurcharges(14.86);

        shipmentRating.setShipmentRateDetails(List.of(rateDetail));
        completedDetail.setShipmentRating(shipmentRating);
        shipment.setCompletedShipmentDetail(completedDetail);
        output.setTransactionShipments(List.of(shipment));
        response.setOutput(output);

        return response;
    }
}

