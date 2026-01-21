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
import app.krista.extensions.fedex.config.FedExDefaults;
import app.krista.extensions.fedex.entity.FedExLabel;
import app.krista.extensions.fedex.model.FedExLabelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting FedExLabel entity to FedEx API request payload.
 */
public final class FedExLabelMapper {

    private static final Logger log = LoggerFactory.getLogger(FedExLabelMapper.class);

    /** Private constructor to prevent instantiation. */
    private FedExLabelMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts FedExLabel entity to FedExLabelRequest API payload.
     *
     * @param label FedExLabel entity from UI
     * @param attributes FedEx extension configuration attributes
     * @return FedExLabelRequest ready for API call
     * @throws IllegalArgumentException if validation fails
     */
    public static FedExLabelRequest toFedExLabelRequest(FedExLabel label, FedExAttributes attributes) {
        if (label == null) {
            throw new IllegalArgumentException("FedExLabel cannot be null");
        }
        if (attributes == null) {
            throw new IllegalArgumentException("FedExAttributes cannot be null");
        }

        validateLabel(label, attributes);

        FedExLabelRequest request = new FedExLabelRequest();
        request.setLabelResponseOptions(FedExDefaults.LABEL_RESPONSE_OPTIONS);

        // Set account number - always use master account for API authentication
        request.setAccountNumber(createAccountNumber(attributes.getMasterAccountNumber()));

        FedExLabelRequest.RequestedShipment shipment = createRequestedShipment(label, attributes);
        request.setRequestedShipment(shipment);

        return request;
    }

    /**
     * Validates that FedExLabel entity has all required fields.
     *
     * @param label FedExLabel entity to validate
     * @param attributes FedEx extension configuration attributes
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateLabel(FedExLabel label, FedExAttributes attributes) {
        List<String> errors = new ArrayList<>();

        validateMasterAccountNumber(attributes, errors);
        validateServiceType(label, errors);
        validatePackageWeight(label, errors);
        validateShipperInformation(label, errors);
        validateRecipientInformation(label, errors);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation errors: " + String.join(", ", errors));
        }
    }

    /**
     * Creates account number object from string value.
     *
     * @param accountNumberStr account number as string
     * @return AccountNumber object
     * @throws IllegalArgumentException if account number is invalid
     */
    private static FedExLabelRequest.AccountNumber createAccountNumber(String accountNumberStr) {
        if (accountNumberStr == null || accountNumberStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number is required");
        }

        FedExLabelRequest.AccountNumber accountNumber = new FedExLabelRequest.AccountNumber();
        try {
            accountNumber.setValue(Long.parseLong(accountNumberStr.trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Account number must be a valid number: " + accountNumberStr);
        }
        return accountNumber;
    }

    /**
     * Creates requested shipment object from FedExLabel entity.
     *
     * @param label FedExLabel entity
     * @param attributes FedEx extension configuration attributes
     * @return RequestedShipment object
     */
    private static FedExLabelRequest.RequestedShipment createRequestedShipment(FedExLabel label, FedExAttributes attributes) {
        FedExLabelRequest.RequestedShipment shipment = new FedExLabelRequest.RequestedShipment();

        shipment.setShipDatestamp(getShipDate(label.getShipDate()));
        shipment.setServiceType(getServiceType(label.getServiceType()));
        shipment.setPackagingType(getPackagingType(label.getPackagingType()));
        shipment.setPickupType(getPickupType(label.getPickupType()));
        shipment.setBlockInsightVisibility(FedExDefaults.BLOCK_INSIGHT_VISIBILITY);

        shipment.setShipper(createShipper(label));
        shipment.setRecipients(createRecipients(label));
        shipment.setShippingChargesPayment(createShippingChargesPayment(label, attributes));
        shipment.setLabelSpecification(createLabelSpecification());
        shipment.setRequestedPackageLineItems(createPackageLineItems(label));

        return shipment;
    }

    /**
     * Creates recipients list from FedExLabel entity.
     *
     * @param label FedExLabel entity
     * @return list of recipients
     */
    private static List<FedExLabelRequest.Party> createRecipients(FedExLabel label) {
        List<FedExLabelRequest.Party> recipients = new ArrayList<>();
        recipients.add(createRecipient(label));
        return recipients;
    }

    /**
     * Creates shipping charges payment object with payment type based on recipient account number.
     *
     * Payment Logic:
     * - If Recipient Account Number is provided → RECIPIENT pays (customer's account)
     * - If Recipient Account Number is empty → SENDER pays (master account from extension config)
     *
     * @param label FedExLabel entity
     * @param attributes FedEx extension configuration attributes
     * @return ShippingChargesPayment object
     */
    private static FedExLabelRequest.ShippingChargesPayment createShippingChargesPayment(FedExLabel label, FedExAttributes attributes) {
        FedExLabelRequest.ShippingChargesPayment payment = new FedExLabelRequest.ShippingChargesPayment();

        String recipientAccountNumber = label.getRecipientAccountNumber();
        boolean hasRecipientAccount = recipientAccountNumber != null && !recipientAccountNumber.trim().isEmpty();

        if (hasRecipientAccount) {
            // RECIPIENT pays - use customer's FedEx account
            log.debug("Using RECIPIENT payment type with account: {}", maskAccountNumber(recipientAccountNumber));
            payment.setPaymentType(FedExDefaults.PAYMENT_TYPE_RECIPIENT);

            // Create payor structure for recipient billing
            FedExLabelRequest.Payor payor = new FedExLabelRequest.Payor();
            FedExLabelRequest.ResponsibleParty responsibleParty = new FedExLabelRequest.ResponsibleParty();
            responsibleParty.setAccountNumber(createAccountNumber(recipientAccountNumber));
            payor.setResponsibleParty(responsibleParty);
            payment.setPayor(payor);
        } else {
            // SENDER pays - use master account from extension config (no payor object needed)
            String masterAccountNumber = attributes.getMasterAccountNumber();
            log.debug("Using SENDER payment type with master account: {}", maskAccountNumber(masterAccountNumber));
            payment.setPaymentType(FedExDefaults.PAYMENT_TYPE_SENDER);
            // NOTE: For SENDER payment type, do NOT set payor object
            // The master account number is already set at root level in accountNumber field
        }

        return payment;
    }

    /**
     * Masks account number for logging (shows only last 4 digits).
     *
     * @param accountNumber account number to mask
     * @return masked account number
     */
    private static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return "****";
        }
        return accountNumber.replaceAll("\\d(?=\\d{4})", "*");
    }

    /**
     * Creates label specification object with default values.
     *
     * @return LabelSpecification object
     */
    private static FedExLabelRequest.LabelSpecification createLabelSpecification() {
        FedExLabelRequest.LabelSpecification labelSpec = new FedExLabelRequest.LabelSpecification();
        labelSpec.setImageType(FedExDefaults.LABEL_IMAGE_TYPE);
        labelSpec.setLabelStockType(FedExDefaults.LABEL_STOCK_TYPE);
        return labelSpec;
    }

    /**
     * Creates package line items list from FedExLabel entity.
     *
     * @param label FedExLabel entity
     * @return list of package line items
     */
    private static List<FedExLabelRequest.RequestedPackageLineItem> createPackageLineItems(FedExLabel label) {
        List<FedExLabelRequest.RequestedPackageLineItem> packages = new ArrayList<>();
        FedExLabelRequest.RequestedPackageLineItem packageItem = new FedExLabelRequest.RequestedPackageLineItem();
        packageItem.setGroupPackageCount(FedExDefaults.DEFAULT_PACKAGE_COUNT);
        packageItem.setWeight(createWeight(label));

        // Add declared value if package value is provided
        if (label.getPackageValue() != null && label.getPackageValue() > 0) {
            packageItem.setDeclaredValue(createDeclaredValue(label));
        }

        packages.add(packageItem);
        return packages;
    }

    /**
     * Creates weight object from FedExLabel entity.
     *
     * @param label FedExLabel entity
     * @return Weight object
     * @throws IllegalArgumentException if weight is invalid
     */
    private static FedExLabelRequest.RequestedPackageLineItem.Weight createWeight(FedExLabel label) {
        if (label.getPackageWeight() == null || label.getPackageWeight() <= 0) {
            throw new IllegalArgumentException("Package weight is required and must be greater than 0");
        }

        FedExLabelRequest.RequestedPackageLineItem.Weight weight =
                new FedExLabelRequest.RequestedPackageLineItem.Weight();
        weight.setValue(label.getPackageWeight());

        String weightUnit = (label.getWeightUnit() != null && !label.getWeightUnit().trim().isEmpty())
                ? label.getWeightUnit()
                : FedExDefaults.WEIGHT_UNITS;
        weight.setUnits(weightUnit);

        return weight;
    }

    /**
     * Creates declared value object from FedExLabel entity.
     * Currency is always set to USD.
     *
     * @param label FedExLabel entity
     * @return DeclaredValue object
     * @throws IllegalArgumentException if package value is invalid
     */
    private static FedExLabelRequest.RequestedPackageLineItem.DeclaredValue createDeclaredValue(FedExLabel label) {
        if (label.getPackageValue() == null || label.getPackageValue() <= 0) {
            throw new IllegalArgumentException("Package value must be greater than 0 when provided");
        }

        FedExLabelRequest.RequestedPackageLineItem.DeclaredValue declaredValue =
                new FedExLabelRequest.RequestedPackageLineItem.DeclaredValue();
        declaredValue.setAmount(label.getPackageValue());
        declaredValue.setCurrency("USD");

        return declaredValue;
    }

    /**
     * Creates shipper party object from FedExLabel entity.
     *
     * @param label FedExLabel entity
     * @return Party object for shipper
     */
    private static FedExLabelRequest.Party createShipper(FedExLabel label) {
        FedExLabelRequest.Party shipper = new FedExLabelRequest.Party();
        shipper.setContact(createContact(
                label.getShipperPersonName(),
                label.getShipperPhoneNumber(),
                label.getShipperCompanyName(),
                "Shipper"
        ));
        shipper.setAddress(createAddress(
                label.getShipperStreetLine1(),
                label.getShipperStreetLine2(),
                label.getShipperCity(),
                label.getShipperStateCode(),
                label.getShipperPostalCode(),
                label.getShipperCountryCode(),
                "Shipper"
        ));
        return shipper;
    }

    /**
     * Creates recipient party object from FedExLabel entity.
     *
     * @param label FedExLabel entity
     * @return Party object for recipient
     */
    private static FedExLabelRequest.Party createRecipient(FedExLabel label) {
        FedExLabelRequest.Party recipient = new FedExLabelRequest.Party();
        recipient.setContact(createContact(
                label.getRecipientPersonName(),
                label.getRecipientPhoneNumber(),
                label.getRecipientCompanyName(),
                "Recipient"
        ));
        recipient.setAddress(createAddress(
                label.getRecipientStreetLine1(),
                label.getRecipientStreetLine2(),
                label.getRecipientCity(),
                label.getRecipientStateCode(),
                label.getRecipientPostalCode(),
                label.getRecipientCountryCode(),
                "Recipient"
        ));
        return recipient;
    }

    /**
     * Creates contact object from contact information.
     *
     * @param personName person name
     * @param phoneNumber phone number as string
     * @param companyName company name
     * @param partyType party type for error messages (Shipper/Recipient)
     * @return Contact object
     */
    private static FedExLabelRequest.Party.Contact createContact(String personName, String phoneNumber,
                                                                  String companyName, String partyType) {
        FedExLabelRequest.Party.Contact contact = new FedExLabelRequest.Party.Contact();
        contact.setPersonName(personName);
        contact.setCompanyName(companyName);

        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            try {
                contact.setPhoneNumber(Long.parseLong(phoneNumber.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(partyType + " phone number must be a valid number: " + phoneNumber);
            }
        }

        return contact;
    }

    /**
     * Creates address object from address information.
     *
     * @param streetLine1 street line 1
     * @param streetLine2 street line 2
     * @param city city
     * @param stateCode state or province code
     * @param postalCode postal code as string
     * @param countryCode country code
     * @param partyType party type for error messages (Shipper/Recipient)
     * @return Address object
     */
    private static FedExLabelRequest.Party.Address createAddress(String streetLine1, String streetLine2,
                                                                  String city, String stateCode,
                                                                  String postalCode, String countryCode,
                                                                  String partyType) {
        FedExLabelRequest.Party.Address address = new FedExLabelRequest.Party.Address();

        List<String> streetLines = new ArrayList<>();
        if (streetLine1 != null && !streetLine1.trim().isEmpty()) {
            streetLines.add(streetLine1);
        }
        if (streetLine2 != null && !streetLine2.trim().isEmpty()) {
            streetLines.add(streetLine2);
        }
        address.setStreetLines(streetLines.toArray(new String[0]));

        address.setCity(city);
        address.setStateOrProvinceCode(stateCode);

        if (postalCode != null && !postalCode.trim().isEmpty()) {
            try {
                address.setPostalCode(Integer.parseInt(postalCode.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(partyType + " postal code must be a valid number: " + postalCode);
            }
        }

        String country = (countryCode != null && !countryCode.trim().isEmpty())
                ? countryCode
                : FedExDefaults.COUNTRY_CODE_US;
        address.setCountryCode(country);

        return address;
    }

    /**
     * Returns ship date from label or default to tomorrow.
     *
     * @param shipDate ship date from label
     * @return ship date or default
     */
    private static String getShipDate(String shipDate) {
        return (shipDate != null && !shipDate.trim().isEmpty())
                ? shipDate
                : FedExDefaults.getDefaultShipDate();
    }

    /**
     * Returns service type from label or throws exception if not provided.
     *
     * @param serviceType service type from label
     * @return service type
     * @throws IllegalArgumentException if service type is not provided
     */
    private static String getServiceType(String serviceType) {
        if (serviceType != null && !serviceType.trim().isEmpty()) {
            return serviceType;
        }
        throw new IllegalArgumentException("Service type is required");
    }

    /**
     * Returns packaging type from label or default.
     *
     * @param packagingType packaging type from label
     * @return packaging type or default
     */
    private static String getPackagingType(String packagingType) {
        return (packagingType != null && !packagingType.trim().isEmpty())
                ? packagingType
                : FedExDefaults.PACKAGING_TYPE;
    }

    /**
     * Returns pickup type from label or default.
     *
     * @param pickupType pickup type from label
     * @return pickup type or default
     */
    private static String getPickupType(String pickupType) {
        return (pickupType != null && !pickupType.trim().isEmpty())
                ? pickupType
                : FedExDefaults.PICKUP_TYPE;
    }

    /**
     * Validates master account number from extension configuration.
     *
     * @param attributes FedEx extension configuration attributes
     * @param errors list to collect validation errors
     */
    private static void validateMasterAccountNumber(FedExAttributes attributes, List<String> errors) {
        if (isNullOrEmpty(attributes.getMasterAccountNumber())) {
            errors.add("Master Account Number is not configured in the FedEx extension settings. Please configure your 9-digit FedEx master account number in the extension configuration (found on your FedEx invoice or account dashboard). Example: '123456789'");
        }
    }

    /**
     * Validates service type field.
     *
     * @param label FedExLabel entity
     * @param errors list to collect validation errors
     */
    private static void validateServiceType(FedExLabel label, List<String> errors) {
        if (isNullOrEmpty(label.getServiceType())) {
            errors.add("Shipping Service Type is missing. Please select a FedEx service such as 'FEDEX_GROUND' (most economical), 'STANDARD_OVERNIGHT' (next day), or 'FEDEX_2_DAY'. Check the documentation for all available service types.");
        }
    }

    /**
     * Validates package weight field.
     *
     * @param label FedExLabel entity
     * @param errors list to collect validation errors
     */
    private static void validatePackageWeight(FedExLabel label, List<String> errors) {
        if (label.getPackageWeight() == null || label.getPackageWeight() <= 0) {
            errors.add("Package Weight is missing or invalid. Please enter the actual weight of your package in pounds (e.g., 5.5 for 5.5 lbs). Weight must be greater than 0. Accurate weight is important for correct shipping costs and delivery.");
        }
    }

    /**
     * Validates shipper information fields.
     *
     * @param label FedExLabel entity
     * @param errors list to collect validation errors
     */
    private static void validateShipperInformation(FedExLabel label, List<String> errors) {
        if (isNullOrEmpty(label.getShipperPersonName())) {
            errors.add("Shipper Contact Name is missing. Please enter the full name of the person sending the package (e.g., 'John Smith'). This helps FedEx contact the sender if needed.");
        }
        if (isNullOrEmpty(label.getShipperStreetLine1())) {
            errors.add("Shipper Street Address is missing. Please enter the complete street address where the package will be picked up (e.g., '123 Main Street'). Include building numbers and street names.");
        }
        if (isNullOrEmpty(label.getShipperCity())) {
            errors.add("Shipper City is missing. Please enter the city name for the pickup location (e.g., 'San Francisco').");
        }
        if (isNullOrEmpty(label.getShipperStateCode())) {
            errors.add("Shipper State is missing. Please enter the 2-letter state code for the pickup location (e.g., 'CA' for California, 'NY' for New York).");
        }
        if (isNullOrEmpty(label.getShipperPostalCode())) {
            errors.add("Shipper ZIP Code is missing. Please enter the 5-digit ZIP code for the pickup location (e.g., '94102'). This ensures accurate pickup and pricing.");
        }
        if (isNullOrEmpty(label.getShipperPhoneNumber())) {
            errors.add("Shipper Phone Number is missing. Please enter a valid phone number (e.g., '555-123-4567'). FedEx uses this to contact the sender about pickup or delivery issues.");
        }
    }

    /**
     * Validates recipient information fields.
     *
     * @param label FedExLabel entity
     * @param errors list to collect validation errors
     */
    private static void validateRecipientInformation(FedExLabel label, List<String> errors) {
        if (isNullOrEmpty(label.getRecipientPersonName())) {
            errors.add("Recipient Contact Name is missing. Please enter the full name of the person receiving the package (e.g., 'Jane Doe'). This helps FedEx deliver to the correct person.");
        }
        if (isNullOrEmpty(label.getRecipientStreetLine1())) {
            errors.add("Recipient Street Address is missing. Please enter the complete delivery address (e.g., '456 Oak Avenue'). Include building numbers, street names, and any apartment or suite numbers.");
        }
        if (isNullOrEmpty(label.getRecipientCity())) {
            errors.add("Recipient City is missing. Please enter the city name for the delivery location (e.g., 'New York').");
        }
        if (isNullOrEmpty(label.getRecipientStateCode())) {
            errors.add("Recipient State is missing. Please enter the 2-letter state code for the delivery location (e.g., 'NY' for New York, 'TX' for Texas).");
        }
        if (isNullOrEmpty(label.getRecipientPostalCode())) {
            errors.add("Recipient ZIP Code is missing. Please enter the 5-digit ZIP code for the delivery location (e.g., '10001'). This ensures accurate delivery and pricing.");
        }
        if (isNullOrEmpty(label.getRecipientPhoneNumber())) {
            errors.add("Recipient Phone Number is missing. Please enter a valid phone number (e.g., '555-987-6543'). FedEx uses this to contact the recipient about delivery attempts or issues.");
        }
    }

    /**
     * Checks if a string is null or empty after trimming.
     *
     * @param value string to check
     * @return true if null or empty, false otherwise
     */
    private static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
