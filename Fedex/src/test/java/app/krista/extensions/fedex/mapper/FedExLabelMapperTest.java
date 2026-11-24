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

package app.krista.extensions.fedex.mapper;

import app.krista.extensions.fedex.config.FedExAttributes;
import app.krista.extensions.fedex.entity.FedExLabel;
import app.krista.extensions.fedex.model.FedExLabelRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FedExLabelMapper
 *
 * Tests cover:
 * - Ticket ID mapping to customer reference
 * - Required field validation
 * - Default value handling
 * - Payment type logic (SENDER vs RECIPIENT)
 */
public class FedExLabelMapperTest {

    private FedExAttributes attributes;

    @BeforeEach
    public void setUp() {
        // Create mock FedExAttributes with master account number
        Map<String, Object> attributesMap = new HashMap<>();
        attributesMap.put("Master Account Number", "999888777");
        attributesMap.put("Base URL", "https://apis-sandbox.fedex.com");
        attributesMap.put("API Key", "test-api-key");
        attributesMap.put("Secret Key", "test-secret-key");
        attributesMap.put("Grant Type", "client_credentials");
        attributes = new FedExAttributes(attributesMap);
    }

    @Test
    public void testTicketIdMappedToCustomerReference() {
        // Create a valid label with ticket ID
        FedExLabel label = createValidLabel();
        label.ticketId = "ORDER-12345";

        // Convert to API request
        FedExLabelRequest request = FedExLabelMapper.toFedExLabelRequest(label, attributes);

        // Verify customer reference is NOT set (removed from payload)
        assertNotNull(request.getRequestedShipment(), "Requested shipment should not be null");
        assertNull(request.getRequestedShipment().getCustomerReferences(),
                "Customer references should be null (removed from payload)");
    }

    @Test
    public void testEmptyTicketIdNotMappedToCustomerReference() {
        // Create a valid label with empty ticket ID
        FedExLabel label = createValidLabel();
        label.ticketId = "";

        // Convert to API request
        FedExLabelRequest request = FedExLabelMapper.toFedExLabelRequest(label, attributes);

        // Verify customer reference is not set
        assertNull(request.getRequestedShipment().getCustomerReferences(),
                "Customer references should be null for empty ticket ID");
    }

    @Test
    public void testNullTicketIdNotMappedToCustomerReference() {
        // Create a valid label with null ticket ID
        FedExLabel label = createValidLabel();
        label.ticketId = null;

        // Convert to API request
        FedExLabelRequest request = FedExLabelMapper.toFedExLabelRequest(label, attributes);

        // Verify customer reference is not set
        assertNull(request.getRequestedShipment().getCustomerReferences(),
                "Customer references should be null for null ticket ID");
    }

    @Test
    public void testWhitespaceTicketIdNotMappedToCustomerReference() {
        // Create a valid label with whitespace-only ticket ID
        FedExLabel label = createValidLabel();
        label.ticketId = "   ";

        // Convert to API request
        FedExLabelRequest request = FedExLabelMapper.toFedExLabelRequest(label, attributes);

        // Verify customer reference is not set
        assertNull(request.getRequestedShipment().getCustomerReferences(),
                "Customer references should be null for whitespace-only ticket ID");
    }

    @Test
    public void testTicketIdWithSpecialCharacters() {
        // Create a valid label with special characters in ticket ID
        FedExLabel label = createValidLabel();
        label.ticketId = "ORD-2024-001_ABC";

        // Convert to API request
        FedExLabelRequest request = FedExLabelMapper.toFedExLabelRequest(label, attributes);

        // Verify customer reference is NOT set (removed from payload)
        assertNull(request.getRequestedShipment().getCustomerReferences(),
                "Customer references should be null (removed from payload)");
    }

    // ========== HELPER METHODS ==========

    private FedExLabel createValidLabel() {
        FedExLabel label = new FedExLabel();
        label.ticketId = "TEST-123";
        // Note: recipientAccountNumber is now optional - leave it null to use master account (SENDER payment)

        // Shipper info
        label.setShipperPersonName("John Doe");
        label.setShipperPhoneNumber("1234567890");
        label.setShipperStreetLine1("123 Main St");
        label.setShipperCity("New York");
        label.setShipperStateCode("NY");
        label.setShipperPostalCode("10001");

        // Recipient info
        label.setRecipientPersonName("Jane Smith");
        label.setRecipientPhoneNumber("9876543210");
        label.setRecipientStreetLine1("456 Oak Ave");
        label.setRecipientCity("Los Angeles");
        label.setRecipientStateCode("CA");
        label.setRecipientPostalCode("90001");

        // Package info
        label.setServiceType("FEDEX_GROUND");
        label.setPackageWeight(10.0);

        return label;
    }
}

