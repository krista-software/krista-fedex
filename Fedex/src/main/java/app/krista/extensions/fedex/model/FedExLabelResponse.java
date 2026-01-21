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
 * FedEx Label Generation Response - Complete API response model
 * 
 * This class represents the complete response from FedEx label generation API
 * Maps to the JSON structure returned by FedEx Ship API
 */
public class FedExLabelResponse {

    @SerializedName("transactionId")
    private String transactionId;

    @SerializedName("output")
    private Output output;

    // Constructors
    public FedExLabelResponse() {}

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "FedExLabelResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", output=" + output +
                '}';
    }

    /**
     * Output container for transaction shipments
     */
    public static class Output {
        @SerializedName("transactionShipments")
        private List<TransactionShipment> transactionShipments;

        public Output() {}

        public List<TransactionShipment> getTransactionShipments() {
            return transactionShipments;
        }

        public void setTransactionShipments(List<TransactionShipment> transactionShipments) {
            this.transactionShipments = transactionShipments;
        }

        @Override
        public String toString() {
            return "Output{transactionShipments=" + transactionShipments + '}';
        }
    }

    /**
     * Transaction Shipment - Individual shipment details
     */
    public static class TransactionShipment {
        @SerializedName("alerts")
        private List<Alert> alerts;

        @SerializedName("masterTrackingNumber")
        private String masterTrackingNumber;

        @SerializedName("serviceType")
        private String serviceType;

        @SerializedName("shipDatestamp")
        private String shipDatestamp;

        @SerializedName("serviceName")
        private String serviceName;

        @SerializedName("shipmentDocuments")
        private List<ShipmentDocument> shipmentDocuments;

        @SerializedName("pieceResponses")
        private List<PieceResponse> pieceResponses;

        @SerializedName("shipmentAdvisoryDetails")
        private Object shipmentAdvisoryDetails;

        @SerializedName("completedShipmentDetail")
        private CompletedShipmentDetail completedShipmentDetail;

        @SerializedName("serviceCategory")
        private String serviceCategory;

        // Constructors
        public TransactionShipment() {}

        // Getters and Setters
        public List<Alert> getAlerts() { return alerts; }
        public void setAlerts(List<Alert> alerts) { this.alerts = alerts; }

        public String getMasterTrackingNumber() { return masterTrackingNumber; }
        public void setMasterTrackingNumber(String masterTrackingNumber) { this.masterTrackingNumber = masterTrackingNumber; }

        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }

        public String getShipDatestamp() { return shipDatestamp; }
        public void setShipDatestamp(String shipDatestamp) { this.shipDatestamp = shipDatestamp; }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public List<ShipmentDocument> getShipmentDocuments() { return shipmentDocuments; }
        public void setShipmentDocuments(List<ShipmentDocument> shipmentDocuments) { this.shipmentDocuments = shipmentDocuments; }

        public List<PieceResponse> getPieceResponses() { return pieceResponses; }
        public void setPieceResponses(List<PieceResponse> pieceResponses) { this.pieceResponses = pieceResponses; }

        public Object getShipmentAdvisoryDetails() { return shipmentAdvisoryDetails; }
        public void setShipmentAdvisoryDetails(Object shipmentAdvisoryDetails) { this.shipmentAdvisoryDetails = shipmentAdvisoryDetails; }

        public CompletedShipmentDetail getCompletedShipmentDetail() { return completedShipmentDetail; }
        public void setCompletedShipmentDetail(CompletedShipmentDetail completedShipmentDetail) { this.completedShipmentDetail = completedShipmentDetail; }

        public String getServiceCategory() { return serviceCategory; }
        public void setServiceCategory(String serviceCategory) { this.serviceCategory = serviceCategory; }
    }

    /**
     * Alert information
     */
    public static class Alert {
        @SerializedName("code")
        private String code;

        @SerializedName("message")
        private String message;

        @SerializedName("alertType")
        private String alertType;

        public Alert() {}

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }

        @Override
        public String toString() {
            return "Alert{code='" + code + "', message='" + message + "', alertType='" + alertType + "'}";
        }
    }

    /**
     * Shipment Document - Label URLs and document information
     */
    public static class ShipmentDocument {
        @SerializedName("url")
        private String url;

        @SerializedName("encodedLabel")
        private String encodedLabel;

        @SerializedName("contentType")
        private String contentType;

        @SerializedName("copiesToPrint")
        private Integer copiesToPrint;

        @SerializedName("trackingNumber")
        private String trackingNumber;

        @SerializedName("docType")
        private String docType;

        public ShipmentDocument() {}

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getEncodedLabel() { return encodedLabel; }
        public void setEncodedLabel(String encodedLabel) { this.encodedLabel = encodedLabel; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public Integer getCopiesToPrint() { return copiesToPrint; }
        public void setCopiesToPrint(Integer copiesToPrint) { this.copiesToPrint = copiesToPrint; }

        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

        public String getDocType() { return docType; }
        public void setDocType(String docType) { this.docType = docType; }

        @Override
        public String toString() {
            return "ShipmentDocument{url='" + url + "', encodedLabel=" + (encodedLabel != null ? "present" : "null") + ", contentType='" + contentType + "', trackingNumber='" + trackingNumber + "'}";
        }
    }

    /**
     * Piece Response - Individual package response
     */
    public static class PieceResponse {
        @SerializedName("masterTrackingNumber")
        private String masterTrackingNumber;

        @SerializedName("deliveryDatestamp")
        private String deliveryDatestamp;

        @SerializedName("packageSequenceNumber")
        private Integer packageSequenceNumber;

        @SerializedName("trackingNumber")
        private String trackingNumber;

        @SerializedName("additionalChargesDiscount")
        private Double additionalChargesDiscount;

        @SerializedName("netRateAmount")
        private Double netRateAmount;

        @SerializedName("netChargeAmount")
        private Double netChargeAmount;

        @SerializedName("netDiscountAmount")
        private Double netDiscountAmount;

        @SerializedName("packageDocuments")
        private List<PackageDocument> packageDocuments;

        @SerializedName("customerReferences")
        private List<String> customerReferences;

        @SerializedName("codcollectionAmount")
        private Double codcollectionAmount;

        @SerializedName("baseRateAmount")
        private Double baseRateAmount;

        public PieceResponse() {}

        // Getters and Setters
        public String getMasterTrackingNumber() { return masterTrackingNumber; }
        public void setMasterTrackingNumber(String masterTrackingNumber) { this.masterTrackingNumber = masterTrackingNumber; }

        public String getDeliveryDatestamp() { return deliveryDatestamp; }
        public void setDeliveryDatestamp(String deliveryDatestamp) { this.deliveryDatestamp = deliveryDatestamp; }

        public Integer getPackageSequenceNumber() { return packageSequenceNumber; }
        public void setPackageSequenceNumber(Integer packageSequenceNumber) { this.packageSequenceNumber = packageSequenceNumber; }

        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

        public Double getAdditionalChargesDiscount() { return additionalChargesDiscount; }
        public void setAdditionalChargesDiscount(Double additionalChargesDiscount) { this.additionalChargesDiscount = additionalChargesDiscount; }

        public Double getNetRateAmount() { return netRateAmount; }
        public void setNetRateAmount(Double netRateAmount) { this.netRateAmount = netRateAmount; }

        public Double getNetChargeAmount() { return netChargeAmount; }
        public void setNetChargeAmount(Double netChargeAmount) { this.netChargeAmount = netChargeAmount; }

        public Double getNetDiscountAmount() { return netDiscountAmount; }
        public void setNetDiscountAmount(Double netDiscountAmount) { this.netDiscountAmount = netDiscountAmount; }

        public List<PackageDocument> getPackageDocuments() { return packageDocuments; }
        public void setPackageDocuments(List<PackageDocument> packageDocuments) { this.packageDocuments = packageDocuments; }

        public List<String> getCustomerReferences() { return customerReferences; }
        public void setCustomerReferences(List<String> customerReferences) { this.customerReferences = customerReferences; }

        public Double getCodcollectionAmount() { return codcollectionAmount; }
        public void setCodcollectionAmount(Double codcollectionAmount) { this.codcollectionAmount = codcollectionAmount; }

        public Double getBaseRateAmount() { return baseRateAmount; }
        public void setBaseRateAmount(Double baseRateAmount) { this.baseRateAmount = baseRateAmount; }
    }

    /**
     * Package Document - Individual package label document
     */
    public static class PackageDocument {
        @SerializedName("url")
        private String url;

        @SerializedName("encodedLabel")
        private String encodedLabel;

        @SerializedName("contentType")
        private String contentType;

        @SerializedName("copiesToPrint")
        private Integer copiesToPrint;

        @SerializedName("docType")
        private String docType;

        public PackageDocument() {}

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getEncodedLabel() { return encodedLabel; }
        public void setEncodedLabel(String encodedLabel) { this.encodedLabel = encodedLabel; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public Integer getCopiesToPrint() { return copiesToPrint; }
        public void setCopiesToPrint(Integer copiesToPrint) { this.copiesToPrint = copiesToPrint; }

        public String getDocType() { return docType; }
        public void setDocType(String docType) { this.docType = docType; }

        @Override
        public String toString() {
            return "PackageDocument{url='" + url + "', encodedLabel=" + (encodedLabel != null ? "present" : "null") + ", contentType='" + contentType + "', docType='" + docType + "'}";
        }
    }

    /**
     * Completed Shipment Detail - Detailed shipment information
     */
    public static class CompletedShipmentDetail {
        @SerializedName("usDomestic")
        private Boolean usDomestic;

        @SerializedName("carrierCode")
        private String carrierCode;

        @SerializedName("masterTrackingId")
        private TrackingId masterTrackingId;

        @SerializedName("serviceDescription")
        private Object serviceDescription;

        @SerializedName("packagingDescription")
        private String packagingDescription;

        @SerializedName("operationalDetail")
        private Object operationalDetail;

        @SerializedName("shipmentRating")
        private ShipmentRating shipmentRating;

        @SerializedName("completedPackageDetails")
        private List<Object> completedPackageDetails;

        public CompletedShipmentDetail() {}

        // Getters and Setters
        public Boolean getUsDomestic() { return usDomestic; }
        public void setUsDomestic(Boolean usDomestic) { this.usDomestic = usDomestic; }

        public String getCarrierCode() { return carrierCode; }
        public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }

        public TrackingId getMasterTrackingId() { return masterTrackingId; }
        public void setMasterTrackingId(TrackingId masterTrackingId) { this.masterTrackingId = masterTrackingId; }

        public Object getServiceDescription() { return serviceDescription; }
        public void setServiceDescription(Object serviceDescription) { this.serviceDescription = serviceDescription; }

        public String getPackagingDescription() { return packagingDescription; }
        public void setPackagingDescription(String packagingDescription) { this.packagingDescription = packagingDescription; }

        public Object getOperationalDetail() { return operationalDetail; }
        public void setOperationalDetail(Object operationalDetail) { this.operationalDetail = operationalDetail; }

        public ShipmentRating getShipmentRating() { return shipmentRating; }
        public void setShipmentRating(ShipmentRating shipmentRating) { this.shipmentRating = shipmentRating; }

        public List<Object> getCompletedPackageDetails() { return completedPackageDetails; }
        public void setCompletedPackageDetails(List<Object> completedPackageDetails) { this.completedPackageDetails = completedPackageDetails; }
    }

    /**
     * Tracking ID information
     */
    public static class TrackingId {
        @SerializedName("trackingIdType")
        private String trackingIdType;

        @SerializedName("formId")
        private String formId;

        @SerializedName("trackingNumber")
        private String trackingNumber;

        public TrackingId() {}

        public String getTrackingIdType() { return trackingIdType; }
        public void setTrackingIdType(String trackingIdType) { this.trackingIdType = trackingIdType; }

        public String getFormId() { return formId; }
        public void setFormId(String formId) { this.formId = formId; }

        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

        @Override
        public String toString() {
            return "TrackingId{trackingIdType='" + trackingIdType + "', formId='" + formId + "', trackingNumber='" + trackingNumber + "'}";
        }
    }

    /**
     * Shipment Rating - Contains pricing information for the shipment
     */
    public static class ShipmentRating {
        @SerializedName("actualRateType")
        private String actualRateType;

        @SerializedName("shipmentRateDetails")
        private List<ShipmentRateDetail> shipmentRateDetails;

        public ShipmentRating() {}

        public String getActualRateType() { return actualRateType; }
        public void setActualRateType(String actualRateType) { this.actualRateType = actualRateType; }

        public List<ShipmentRateDetail> getShipmentRateDetails() { return shipmentRateDetails; }
        public void setShipmentRateDetails(List<ShipmentRateDetail> shipmentRateDetails) { this.shipmentRateDetails = shipmentRateDetails; }
    }

    /**
     * Shipment Rate Detail - Detailed pricing breakdown
     */
    public static class ShipmentRateDetail {
        @SerializedName("rateType")
        private String rateType;

        @SerializedName("totalBaseCharge")
        private Double totalBaseCharge;

        @SerializedName("totalFreightDiscounts")
        private Double totalFreightDiscounts;

        @SerializedName("totalNetCharge")
        private Double totalNetCharge;

        @SerializedName("totalNetFedExCharge")
        private Double totalNetFedExCharge;

        @SerializedName("totalSurcharges")
        private Double totalSurcharges;

        @SerializedName("surcharges")
        private List<Surcharge> surcharges;

        @SerializedName("currency")
        private String currency;

        public ShipmentRateDetail() {}

        public String getRateType() { return rateType; }
        public void setRateType(String rateType) { this.rateType = rateType; }

        public Double getTotalBaseCharge() { return totalBaseCharge; }
        public void setTotalBaseCharge(Double totalBaseCharge) { this.totalBaseCharge = totalBaseCharge; }

        public Double getTotalFreightDiscounts() { return totalFreightDiscounts; }
        public void setTotalFreightDiscounts(Double totalFreightDiscounts) { this.totalFreightDiscounts = totalFreightDiscounts; }

        public Double getTotalNetCharge() { return totalNetCharge; }
        public void setTotalNetCharge(Double totalNetCharge) { this.totalNetCharge = totalNetCharge; }

        public Double getTotalNetFedExCharge() { return totalNetFedExCharge; }
        public void setTotalNetFedExCharge(Double totalNetFedExCharge) { this.totalNetFedExCharge = totalNetFedExCharge; }

        public Double getTotalSurcharges() { return totalSurcharges; }
        public void setTotalSurcharges(Double totalSurcharges) { this.totalSurcharges = totalSurcharges; }

        public List<Surcharge> getSurcharges() { return surcharges; }
        public void setSurcharges(List<Surcharge> surcharges) { this.surcharges = surcharges; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    /**
     * Surcharge - Individual surcharge details (fuel, insurance, demand, etc.)
     */
    public static class Surcharge {
        @SerializedName("surchargeType")
        private String surchargeType;

        @SerializedName("description")
        private String description;

        @SerializedName("amount")
        private Double amount;

        public Surcharge() {}

        public Surcharge(String surchargeType, String description, Double amount) {
            this.surchargeType = surchargeType;
            this.description = description;
            this.amount = amount;
        }

        public String getSurchargeType() { return surchargeType; }
        public void setSurchargeType(String surchargeType) { this.surchargeType = surchargeType; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        @Override
        public String toString() {
            return "Surcharge{" +
                    "surchargeType='" + surchargeType + '\'' +
                    ", description='" + description + '\'' +
                    ", amount=" + amount +
                    '}';
        }
    }
}
