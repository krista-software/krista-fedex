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

package app.krista.extensions.fedex.entity;


import app.krista.extension.impl.anno.*;


@Domain(id = "catEntryDomain_c0019254-1103-45dd-acef-1eca3463d3d3",
        name = "supply chain",
        ecosystemId = "catEntryEcosystem_753b460d-53fc-4530-b6b1-61de354f3379",
        ecosystemName = "Logistics",
        ecosystemVersion ="b1014b20-2eee-4ec9-a7d4-137ca801443b")
@Entity(name = "FedEx Label", id = "localDomainEntity_033281d6-4af1-44b2-a3c5-04cb2a6ffed2", primaryKey = "Ticket Id", supportStore = false, options = {})
public class FedExLabel {


    @Searchable
    @ToString
    @Field.Text(name = "Ticket Id", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String ticketId;

    // ========== ACCOUNT INFORMATION ==========
    @ToString
    @Field.Text(name = "Recipient Account Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
    public String recipientAccountNumber;

    @ToString
    @Field.Text(name = "Payment Type", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String paymentType;

    // ========== SHIPMENT DETAILS ==========
    @ToString
    @Field.Text(name = "Ship Date", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String shipDate;

    @ToString
    @Field.Text(name = "Service Type", required = true, attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
    public String serviceType;

    @ToString
    @Field.Text(name = "Packaging Type", required = true, attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
    public String packagingType;

    @ToString
    @Field.Text(name = "Pickup Type", required = true, attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
    public String pickupType;

    // ========== SHIPPER INFORMATION ==========
    @ToString
    @Field.Text(name = "Shipper Person Name", required = false, attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
    public String shipperPersonName;

    @ToString
    @Field.Text(name = "Shipper Company Name", required = false, attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
    public String shipperCompanyName;

    @Field.Text(name = "Shipper Phone Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String shipperPhoneNumber;

    @ToString
    @Field.Text(name = "Shipper Street Line 1", required = false, attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {})
    public String shipperStreetLine1;

    @Field.Text(name = "Shipper Street Line 2", required = false, attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {})
    public String shipperStreetLine2;

    @ToString
    @Field.Text(name = "Shipper City", required = false, attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
    public String shipperCity;

    @Field.Text(name = "Shipper State Code", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String shipperStateCode;

    @Field.Text(name = "Shipper Postal Code", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String shipperPostalCode;

    @Field.Text(name = "Shipper Country Code", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String shipperCountryCode;

    // ========== RECIPIENT INFORMATION ==========
    @ToString
    @Field.Text(name = "Recipient Person Name", required = false, attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
    public String recipientPersonName;

    @ToString
    @Field.Text(name = "Recipient Company Name", required = false, attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
    public String recipientCompanyName;

    @Field.Text(name = "Recipient Phone Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String recipientPhoneNumber;

    @ToString
    @Field.Text(name = "Recipient Street Line 1", required = false, attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {})
    public String recipientStreetLine1;

    @Field.Text(name = "Recipient Street Line 2", required = false, attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {})
    public String recipientStreetLine2;

    @ToString
    @Field.Text(name = "Recipient City", required = false, attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
    public String recipientCity;

    @Field.Text(name = "Recipient State Code", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String recipientStateCode;

    @Field.Text(name = "Recipient Postal Code", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String recipientPostalCode;

    @Field.Text(name = "Recipient Country Code", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String recipientCountryCode;

    // ========== PACKAGE INFORMATION ==========
    @ToString
    @Field(name = "Package Weight", type = "Number", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Double packageWeight;

    @Field.Text(name = "Weight Unit", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String weightUnit;

    @ToString
    @Field(name = "Package Value", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Double packageValue;

    // ========== CONSTRUCTORS ==========
    /**
     * No-argument constructor required by Krista framework for entity instantiation
     */
    public FedExLabel() {
        // Default constructor
    }

    // Getters and Setters
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getRecipientAccountNumber() { return recipientAccountNumber; }
    public void setRecipientAccountNumber(String recipientAccountNumber) { this.recipientAccountNumber = recipientAccountNumber; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getShipDate() { return shipDate; }
    public void setShipDate(String shipDate) { this.shipDate = shipDate; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getPackagingType() { return packagingType; }
    public void setPackagingType(String packagingType) { this.packagingType = packagingType; }

    public String getPickupType() { return pickupType; }
    public void setPickupType(String pickupType) { this.pickupType = pickupType; }

    // Shipper getters/setters
    public String getShipperPersonName() { return shipperPersonName; }
    public void setShipperPersonName(String shipperPersonName) { this.shipperPersonName = shipperPersonName; }

    public String getShipperCompanyName() { return shipperCompanyName; }
    public void setShipperCompanyName(String shipperCompanyName) { this.shipperCompanyName = shipperCompanyName; }

    public String getShipperPhoneNumber() { return shipperPhoneNumber; }
    public void setShipperPhoneNumber(String shipperPhoneNumber) { this.shipperPhoneNumber = shipperPhoneNumber; }

    public String getShipperStreetLine1() { return shipperStreetLine1; }
    public void setShipperStreetLine1(String shipperStreetLine1) { this.shipperStreetLine1 = shipperStreetLine1; }

    public String getShipperStreetLine2() { return shipperStreetLine2; }
    public void setShipperStreetLine2(String shipperStreetLine2) { this.shipperStreetLine2 = shipperStreetLine2; }

    public String getShipperCity() { return shipperCity; }
    public void setShipperCity(String shipperCity) { this.shipperCity = shipperCity; }

    public String getShipperStateCode() { return shipperStateCode; }
    public void setShipperStateCode(String shipperStateCode) { this.shipperStateCode = shipperStateCode; }

    public String getShipperPostalCode() { return shipperPostalCode; }
    public void setShipperPostalCode(String shipperPostalCode) { this.shipperPostalCode = shipperPostalCode; }

    public String getShipperCountryCode() { return shipperCountryCode; }
    public void setShipperCountryCode(String shipperCountryCode) { this.shipperCountryCode = shipperCountryCode; }

    // Recipient getters/setters
    public String getRecipientPersonName() { return recipientPersonName; }
    public void setRecipientPersonName(String recipientPersonName) { this.recipientPersonName = recipientPersonName; }

    public String getRecipientCompanyName() { return recipientCompanyName; }
    public void setRecipientCompanyName(String recipientCompanyName) { this.recipientCompanyName = recipientCompanyName; }

    public String getRecipientPhoneNumber() { return recipientPhoneNumber; }
    public void setRecipientPhoneNumber(String recipientPhoneNumber) { this.recipientPhoneNumber = recipientPhoneNumber; }

    public String getRecipientStreetLine1() { return recipientStreetLine1; }
    public void setRecipientStreetLine1(String recipientStreetLine1) { this.recipientStreetLine1 = recipientStreetLine1; }

    public String getRecipientStreetLine2() { return recipientStreetLine2; }
    public void setRecipientStreetLine2(String recipientStreetLine2) { this.recipientStreetLine2 = recipientStreetLine2; }

    public String getRecipientCity() { return recipientCity; }
    public void setRecipientCity(String recipientCity) { this.recipientCity = recipientCity; }

    public String getRecipientStateCode() { return recipientStateCode; }
    public void setRecipientStateCode(String recipientStateCode) { this.recipientStateCode = recipientStateCode; }

    public String getRecipientPostalCode() { return recipientPostalCode; }
    public void setRecipientPostalCode(String recipientPostalCode) { this.recipientPostalCode = recipientPostalCode; }

    public String getRecipientCountryCode() { return recipientCountryCode; }
    public void setRecipientCountryCode(String recipientCountryCode) { this.recipientCountryCode = recipientCountryCode; }

    public Double getPackageWeight() { return packageWeight; }
    public void setPackageWeight(Double packageWeight) { this.packageWeight = packageWeight; }

    public String getWeightUnit() { return weightUnit; }
    public void setWeightUnit(String weightUnit) { this.weightUnit = weightUnit; }

    public Double getPackageValue() { return packageValue; }
    public void setPackageValue(Double packageValue) { this.packageValue = packageValue; }

    @Override
    public String toString() {
        return String.format("FedExLabel[ticketId=%s, recipientAccountNumber=%s, paymentType=%s, shipperCity=%s, recipientCity=%s, serviceType=%s, weight=%s]",
                ticketId, recipientAccountNumber, paymentType, shipperCity, recipientCity, serviceType, packageWeight);
    }
}