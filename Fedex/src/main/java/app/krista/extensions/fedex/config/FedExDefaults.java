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

package app.krista.extensions.fedex.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * FedEx Default Values - Constants and default values for FedEx API requests
 * 
 * This class contains recommended default values for FedEx label generation
 * to minimize user input while maintaining flexibility
 */
public class FedExDefaults {

    // ==================== CONSTANTS (Never Change) ====================
    
    /**
     * Label Response Options - How to receive the label
     * URL_ONLY: Returns URL to download label (recommended)
     * LABEL: Returns base64 encoded label data
     */
    public static final String LABEL_RESPONSE_OPTIONS = "URL_ONLY";
    
    /**
     * Block Insight Visibility - Whether to hide shipment from recipient tracking
     * false: Allow recipient to track (recommended)
     * true: Block recipient from seeing shipment details
     */
    public static final Boolean BLOCK_INSIGHT_VISIBILITY = false;

    // ==================== DEFAULTS (Can be Overridden) ====================
    
    /**
     * Pickup Type - How FedEx will collect the package
     * USE_SCHEDULED_PICKUP: Use existing scheduled pickup (most common)
     * DROPOFF_AT_FEDEX_LOCATION: Customer drops off at FedEx location
     * CONTACT_FEDEX_TO_SCHEDULE: Request one-time pickup
     */
    public static final String PICKUP_TYPE = "USE_SCHEDULED_PICKUP";
    
    /**
     * Payment Type - Who pays for shipping
     * SENDER: Shipper pays (most common)
     * RECIPIENT: Recipient pays (collect)
     * THIRD_PARTY: Third party pays
     */
    public static final String PAYMENT_TYPE = "SENDER";

    /**
     * Payment Type Constants - Individual payment type values
     * Use these constants instead of hardcoded strings to prevent typos
     */
    public static final String PAYMENT_TYPE_SENDER = "SENDER";
    public static final String PAYMENT_TYPE_RECIPIENT = "RECIPIENT";
    public static final String PAYMENT_TYPE_THIRD_PARTY = "THIRD_PARTY";
    
    /**
     * Label Image Type - Format of the label
     * PDF: PDF format (recommended for most printers)
     * PNG: PNG image format
     * ZPLII: Zebra printer format (for thermal printers)
     */
    public static final String LABEL_IMAGE_TYPE = "PDF";
    
    /**
     * Label Stock Type - Size/type of label paper
     * PAPER_4X6: 4x6 inch label (most common for thermal printers)
     * PAPER_85X11_TOP_HALF_LABEL: 8.5x11 inch paper, top half
     * PAPER_85X11_BOTTOM_HALF_LABEL: 8.5x11 inch paper, bottom half
     */
    public static final String LABEL_STOCK_TYPE = "PAPER_85X11_TOP_HALF_LABEL";
    
    /**
     * Packaging Type - Type of packaging used
     * YOUR_PACKAGING: Customer's own packaging (most flexible)
     * FEDEX_PAK: FedEx Pak envelope
     * FEDEX_BOX: FedEx Box
     * FEDEX_ENVELOPE: FedEx Envelope
     */
    public static final String PACKAGING_TYPE = "YOUR_PACKAGING";
    
    /**
     * Weight Units - Unit of measurement for package weight
     * LB: Pounds (US standard)
     * KG: Kilograms (International)
     */
    public static final String WEIGHT_UNITS = "LB";
    
    /**
     * Country Code - Default country for domestic shipments
     * US: United States
     */
    public static final String COUNTRY_CODE_US = "US";
    
    /**
     * Default Package Count - Number of packages in a group
     */
    public static final Integer DEFAULT_PACKAGE_COUNT = 1;

    // ==================== SERVICE TYPES ====================
    
    /**
     * Common FedEx Service Types
     */
    public static class ServiceTypes {
        public static final String STANDARD_OVERNIGHT = "STANDARD_OVERNIGHT";
        public static final String PRIORITY_OVERNIGHT = "PRIORITY_OVERNIGHT";
        public static final String FIRST_OVERNIGHT = "FIRST_OVERNIGHT";
        public static final String FEDEX_2_DAY = "FEDEX_2_DAY";
        public static final String FEDEX_2_DAY_AM = "FEDEX_2_DAY_AM";
        public static final String FEDEX_EXPRESS_SAVER = "FEDEX_EXPRESS_SAVER";
        public static final String FEDEX_GROUND = "FEDEX_GROUND";
        public static final String GROUND_HOME_DELIVERY = "GROUND_HOME_DELIVERY";
        public static final String FEDEX_INTERNATIONAL_PRIORITY = "FEDEX_INTERNATIONAL_PRIORITY";
        public static final String FEDEX_INTERNATIONAL_ECONOMY = "FEDEX_INTERNATIONAL_ECONOMY";
    }

    // ==================== PACKAGING TYPES ====================
    
    /**
     * Common FedEx Packaging Types
     */
    public static class PackagingTypes {
        public static final String YOUR_PACKAGING = "YOUR_PACKAGING";
        public static final String FEDEX_PAK = "FEDEX_PAK";
        public static final String FEDEX_BOX = "FEDEX_BOX";
        public static final String FEDEX_ENVELOPE = "FEDEX_ENVELOPE";
        public static final String FEDEX_10KG_BOX = "FEDEX_10KG_BOX";
        public static final String FEDEX_25KG_BOX = "FEDEX_25KG_BOX";
        public static final String FEDEX_TUBE = "FEDEX_TUBE";
    }

    // ==================== PICKUP TYPES ====================
    
    /**
     * FedEx Pickup Types
     */
    public static class PickupTypes {
        public static final String USE_SCHEDULED_PICKUP = "USE_SCHEDULED_PICKUP";
        public static final String DROPOFF_AT_FEDEX_LOCATION = "DROPOFF_AT_FEDEX_LOCATION";
        public static final String CONTACT_FEDEX_TO_SCHEDULE = "CONTACT_FEDEX_TO_SCHEDULE";
    }

    // ==================== PAYMENT TYPES ====================
    
    /**
     * FedEx Payment Types
     */
    public static class PaymentTypes {
        public static final String SENDER = "SENDER";
        public static final String RECIPIENT = "RECIPIENT";
        public static final String THIRD_PARTY = "THIRD_PARTY";
    }

    // ==================== LABEL FORMATS ====================
    
    /**
     * Label Image Types
     */
    public static class LabelImageTypes {
        public static final String PDF = "PDF";
        public static final String PNG = "PNG";
        public static final String ZPLII = "ZPLII";
    }

    /**
     * Label Stock Types
     */
    public static class LabelStockTypes {
        public static final String PAPER_4X6 = "PAPER_4X6";
        public static final String PAPER_85X11_TOP_HALF_LABEL = "PAPER_85X11_TOP_HALF_LABEL";
        public static final String PAPER_85X11_BOTTOM_HALF_LABEL = "PAPER_85X11_BOTTOM_HALF_LABEL";
        public static final String STOCK_4X6 = "STOCK_4X6";
        public static final String STOCK_4X675 = "STOCK_4X675";
    }

    // ==================== WEIGHT UNITS ====================
    
    /**
     * Weight Units
     */
    public static class WeightUnits {
        public static final String LB = "LB";
        public static final String KG = "KG";
    }

    // ==================== HELPER METHODS ====================
    
    /**
     * Get default ship date (tomorrow)
     * 
     * @return Tomorrow's date in ISO format (YYYY-MM-DD)
     */
    public static String getDefaultShipDate() {
        return LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Get ship date for specific days in future
     * 
     * @param daysFromNow Number of days from today
     * @return Date in ISO format (YYYY-MM-DD)
     */
    public static String getShipDate(int daysFromNow) {
        return LocalDate.now().plusDays(daysFromNow).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Get current date in ISO format
     * 
     * @return Today's date in ISO format (YYYY-MM-DD)
     */
    public static String getTodayDate() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Check if a service type is overnight
     * 
     * @param serviceType Service type to check
     * @return true if overnight service
     */
    public static boolean isOvernightService(String serviceType) {
        return serviceType != null && (
                serviceType.equals(ServiceTypes.STANDARD_OVERNIGHT) ||
                serviceType.equals(ServiceTypes.PRIORITY_OVERNIGHT) ||
                serviceType.equals(ServiceTypes.FIRST_OVERNIGHT)
        );
    }

    /**
     * Check if a service type is ground
     * 
     * @param serviceType Service type to check
     * @return true if ground service
     */
    public static boolean isGroundService(String serviceType) {
        return serviceType != null && (
                serviceType.equals(ServiceTypes.FEDEX_GROUND) ||
                serviceType.equals(ServiceTypes.GROUND_HOME_DELIVERY)
        );
    }

    /**
     * Check if a service type is international
     * 
     * @param serviceType Service type to check
     * @return true if international service
     */
    public static boolean isInternationalService(String serviceType) {
        return serviceType != null && (
                serviceType.equals(ServiceTypes.FEDEX_INTERNATIONAL_PRIORITY) ||
                serviceType.equals(ServiceTypes.FEDEX_INTERNATIONAL_ECONOMY)
        );
    }

    /**
     * Get recommended packaging type for service type
     * 
     * @param serviceType Service type
     * @return Recommended packaging type
     */
    public static String getRecommendedPackagingType(String serviceType) {
        if (isOvernightService(serviceType)) {
            return PackagingTypes.FEDEX_PAK; // Overnight often uses paks
        } else if (isGroundService(serviceType)) {
            return PackagingTypes.YOUR_PACKAGING; // Ground often uses custom packaging
        }
        return PackagingTypes.YOUR_PACKAGING; // Default
    }

    /**
     * Validate if a value is a valid service type
     * 
     * @param serviceType Service type to validate
     * @return true if valid
     */
    public static boolean isValidServiceType(String serviceType) {
        return serviceType != null && (
                serviceType.equals(ServiceTypes.STANDARD_OVERNIGHT) ||
                serviceType.equals(ServiceTypes.PRIORITY_OVERNIGHT) ||
                serviceType.equals(ServiceTypes.FIRST_OVERNIGHT) ||
                serviceType.equals(ServiceTypes.FEDEX_2_DAY) ||
                serviceType.equals(ServiceTypes.FEDEX_2_DAY_AM) ||
                serviceType.equals(ServiceTypes.FEDEX_EXPRESS_SAVER) ||
                serviceType.equals(ServiceTypes.FEDEX_GROUND) ||
                serviceType.equals(ServiceTypes.GROUND_HOME_DELIVERY) ||
                serviceType.equals(ServiceTypes.FEDEX_INTERNATIONAL_PRIORITY) ||
                serviceType.equals(ServiceTypes.FEDEX_INTERNATIONAL_ECONOMY)
        );
    }

    // Private constructor to prevent instantiation
    private FedExDefaults() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
