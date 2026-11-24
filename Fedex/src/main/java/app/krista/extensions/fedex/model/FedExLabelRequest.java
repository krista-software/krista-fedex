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

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * FedEx Label Generation Request - Complete API model for creating shipping labels
 *
 * This class represents the complete payload for FedEx label generation API
 * Maps to the JSON structure required by FedEx Ship API
 * Contains all necessary inner classes for the API request
 */
public class FedExLabelRequest {

    @SerializedName("labelResponseOptions")
    private String labelResponseOptions;

    @SerializedName("requestedShipment")
    private RequestedShipment requestedShipment;

    @SerializedName("accountNumber")
    private AccountNumber accountNumber;

    // Constructors
    public FedExLabelRequest() {}

    public FedExLabelRequest(String labelResponseOptions, RequestedShipment requestedShipment, AccountNumber accountNumber) {
        this.labelResponseOptions = labelResponseOptions;
        this.requestedShipment = requestedShipment;
        this.accountNumber = accountNumber;
    }

    // Getters and Setters
    public String getLabelResponseOptions() {
        return labelResponseOptions;
    }

    public void setLabelResponseOptions(String labelResponseOptions) {
        this.labelResponseOptions = labelResponseOptions;
    }

    public RequestedShipment getRequestedShipment() {
        return requestedShipment;
    }

    public void setRequestedShipment(RequestedShipment requestedShipment) {
        this.requestedShipment = requestedShipment;
    }

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(AccountNumber accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public String toString() {
        return "FedExLabelRequest{" +
                "labelResponseOptions='" + labelResponseOptions + '\'' +
                ", requestedShipment=" + requestedShipment +
                ", accountNumber=" + accountNumber +
                '}';
    }

    /**
     * Account Number wrapper class
     */
    public static class AccountNumber {
        @SerializedName("value")
        private Long value;

        public AccountNumber() {}

        public AccountNumber(Long value) {
            this.value = value;
        }

        public Long getValue() {
            return value;
        }

        public void setValue(Long value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "AccountNumber{value=" + value + '}';
        }
    }

    /**
     * Requested Shipment - Core shipment details
     */
    public static class RequestedShipment {
        @SerializedName("shipper")
        private Party shipper;

        @SerializedName("recipients")
        private List<Party> recipients;

        @SerializedName("shipDatestamp")
        private String shipDatestamp;

        @SerializedName("serviceType")
        private String serviceType;

        @SerializedName("packagingType")
        private String packagingType;

        @SerializedName("pickupType")
        private String pickupType;

        @SerializedName("blockInsightVisibility")
        private Boolean blockInsightVisibility;

        @SerializedName("shippingChargesPayment")
        private ShippingChargesPayment shippingChargesPayment;

        @SerializedName("labelSpecification")
        private LabelSpecification labelSpecification;

        @SerializedName("requestedPackageLineItems")
        private List<RequestedPackageLineItem> requestedPackageLineItems;

        @SerializedName("customerReferences")
        private List<CustomerReference> customerReferences;

        // Constructors
        public RequestedShipment() {}

        // Getters and Setters
        public Party getShipper() { return shipper; }
        public void setShipper(Party shipper) { this.shipper = shipper; }

        public List<Party> getRecipients() { return recipients; }
        public void setRecipients(List<Party> recipients) { this.recipients = recipients; }

        public String getShipDatestamp() { return shipDatestamp; }
        public void setShipDatestamp(String shipDatestamp) { this.shipDatestamp = shipDatestamp; }

        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }

        public String getPackagingType() { return packagingType; }
        public void setPackagingType(String packagingType) { this.packagingType = packagingType; }

        public String getPickupType() { return pickupType; }
        public void setPickupType(String pickupType) { this.pickupType = pickupType; }

        public Boolean getBlockInsightVisibility() { return blockInsightVisibility; }
        public void setBlockInsightVisibility(Boolean blockInsightVisibility) { this.blockInsightVisibility = blockInsightVisibility; }

        public ShippingChargesPayment getShippingChargesPayment() { return shippingChargesPayment; }
        public void setShippingChargesPayment(ShippingChargesPayment shippingChargesPayment) { this.shippingChargesPayment = shippingChargesPayment; }

        public LabelSpecification getLabelSpecification() { return labelSpecification; }
        public void setLabelSpecification(LabelSpecification labelSpecification) { this.labelSpecification = labelSpecification; }

        public List<RequestedPackageLineItem> getRequestedPackageLineItems() { return requestedPackageLineItems; }
        public void setRequestedPackageLineItems(List<RequestedPackageLineItem> requestedPackageLineItems) { this.requestedPackageLineItems = requestedPackageLineItems; }

        public List<CustomerReference> getCustomerReferences() { return customerReferences; }
        public void setCustomerReferences(List<CustomerReference> customerReferences) { this.customerReferences = customerReferences; }
    }

    /**
     * Party - Represents shipper or recipient information
     */
    public static class Party {
        @SerializedName("contact")
        private Contact contact;

        @SerializedName("address")
        private Address address;

        public Party() {}
        public Party(Contact contact, Address address) {
            this.contact = contact;
            this.address = address;
        }

        public Contact getContact() { return contact; }
        public void setContact(Contact contact) { this.contact = contact; }

        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }

        /**
         * Contact information
         */
        public static class Contact {
            @SerializedName("personName")
            private String personName;

            @SerializedName("phoneNumber")
            private Long phoneNumber;

            @SerializedName("companyName")
            private String companyName;

            public Contact() {}
            public Contact(String personName, Long phoneNumber, String companyName) {
                this.personName = personName;
                this.phoneNumber = phoneNumber;
                this.companyName = companyName;
            }

            public String getPersonName() { return personName; }
            public void setPersonName(String personName) { this.personName = personName; }

            public Long getPhoneNumber() { return phoneNumber; }
            public void setPhoneNumber(Long phoneNumber) { this.phoneNumber = phoneNumber; }

            public String getCompanyName() { return companyName; }
            public void setCompanyName(String companyName) { this.companyName = companyName; }
        }

        /**
         * Address information
         */
        public static class Address {
            @SerializedName("streetLines")
            private String[] streetLines;

            @SerializedName("city")
            private String city;

            @SerializedName("stateOrProvinceCode")
            private String stateOrProvinceCode;

            @SerializedName("postalCode")
            private Integer postalCode;

            @SerializedName("countryCode")
            private String countryCode;

            public Address() {}
            public Address(String[] streetLines, String city, String stateOrProvinceCode, Integer postalCode, String countryCode) {
                this.streetLines = streetLines;
                this.city = city;
                this.stateOrProvinceCode = stateOrProvinceCode;
                this.postalCode = postalCode;
                this.countryCode = countryCode;
            }

            public String[] getStreetLines() { return streetLines; }
            public void setStreetLines(String[] streetLines) { this.streetLines = streetLines; }

            public String getCity() { return city; }
            public void setCity(String city) { this.city = city; }

            public String getStateOrProvinceCode() { return stateOrProvinceCode; }
            public void setStateOrProvinceCode(String stateOrProvinceCode) { this.stateOrProvinceCode = stateOrProvinceCode; }

            public Integer getPostalCode() { return postalCode; }
            public void setPostalCode(Integer postalCode) { this.postalCode = postalCode; }

            public String getCountryCode() { return countryCode; }
            public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        }
    }

    /**
     * Shipping Charges Payment
     */
    public static class ShippingChargesPayment {
        @SerializedName("paymentType")
        private String paymentType;

        @SerializedName("payor")
        private Payor payor;

        public ShippingChargesPayment() {}
        public ShippingChargesPayment(String paymentType) {
            this.paymentType = paymentType;
        }

        public String getPaymentType() { return paymentType; }
        public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

        public Payor getPayor() { return payor; }
        public void setPayor(Payor payor) { this.payor = payor; }
    }

    /**
     * Payor - Identifies who is responsible for payment
     */
    public static class Payor {
        @SerializedName("responsibleParty")
        private ResponsibleParty responsibleParty;

        public Payor() {}

        public ResponsibleParty getResponsibleParty() { return responsibleParty; }
        public void setResponsibleParty(ResponsibleParty responsibleParty) { this.responsibleParty = responsibleParty; }
    }

    /**
     * Responsible Party - Contains account number of the party responsible for payment
     */
    public static class ResponsibleParty {
        @SerializedName("accountNumber")
        private AccountNumber accountNumber;

        public ResponsibleParty() {}

        public AccountNumber getAccountNumber() { return accountNumber; }
        public void setAccountNumber(AccountNumber accountNumber) { this.accountNumber = accountNumber; }
    }

    /**
     * Label Specification
     */
    public static class LabelSpecification {
        @SerializedName("imageType")
        private String imageType;

        @SerializedName("labelStockType")
        private String labelStockType;

        public LabelSpecification() {}
        public LabelSpecification(String imageType, String labelStockType) {
            this.imageType = imageType;
            this.labelStockType = labelStockType;
        }

        public String getImageType() { return imageType; }
        public void setImageType(String imageType) { this.imageType = imageType; }

        public String getLabelStockType() { return labelStockType; }
        public void setLabelStockType(String labelStockType) { this.labelStockType = labelStockType; }
    }

    /**
     * Requested Package Line Item
     */
    public static class RequestedPackageLineItem {
        @SerializedName("groupPackageCount")
        private Integer groupPackageCount;

        @SerializedName("weight")
        private Weight weight;

        @SerializedName("declaredValue")
        private DeclaredValue declaredValue;

        public RequestedPackageLineItem() {}
        public RequestedPackageLineItem(Integer groupPackageCount, Weight weight) {
            this.groupPackageCount = groupPackageCount;
            this.weight = weight;
        }

        public Integer getGroupPackageCount() { return groupPackageCount; }
        public void setGroupPackageCount(Integer groupPackageCount) { this.groupPackageCount = groupPackageCount; }

        public Weight getWeight() { return weight; }
        public void setWeight(Weight weight) { this.weight = weight; }

        public DeclaredValue getDeclaredValue() { return declaredValue; }
        public void setDeclaredValue(DeclaredValue declaredValue) { this.declaredValue = declaredValue; }

        /**
         * Weight information
         */
        public static class Weight {
            @SerializedName("value")
            private Double value;

            @SerializedName("units")
            private String units;

            public Weight() {}
            public Weight(Double value, String units) {
                this.value = value;
                this.units = units;
            }

            public Double getValue() { return value; }
            public void setValue(Double value) { this.value = value; }

            public String getUnits() { return units; }
            public void setUnits(String units) { this.units = units; }
        }

        /**
         * Declared Value - Insurance value for the package
         */
        public static class DeclaredValue {
            @SerializedName("amount")
            private Double amount;

            @SerializedName("currency")
            private String currency;

            public DeclaredValue() {}
            public DeclaredValue(Double amount, String currency) {
                this.amount = amount;
                this.currency = currency;
            }

            public Double getAmount() { return amount; }
            public void setAmount(Double amount) { this.amount = amount; }

            public String getCurrency() { return currency; }
            public void setCurrency(String currency) { this.currency = currency; }
        }
    }

    /**
     * Customer Reference - For tracking internal order/ticket IDs with FedEx shipment
     *
     * This allows you to associate your internal reference numbers (order ID, ticket ID, etc.)
     * with the FedEx shipment for easier tracking and reconciliation.
     */
    public static class CustomerReference {
        @SerializedName("customerReferenceType")
        private String customerReferenceType;

        @SerializedName("value")
        private String value;

        // Constructors
        public CustomerReference() {}

        public CustomerReference(String customerReferenceType, String value) {
            this.customerReferenceType = customerReferenceType;
            this.value = value;
        }

        // Getters and Setters
        public String getCustomerReferenceType() { return customerReferenceType; }
        public void setCustomerReferenceType(String customerReferenceType) { this.customerReferenceType = customerReferenceType; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        @Override
        public String toString() {
            return "CustomerReference{" +
                    "customerReferenceType='" + customerReferenceType + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}