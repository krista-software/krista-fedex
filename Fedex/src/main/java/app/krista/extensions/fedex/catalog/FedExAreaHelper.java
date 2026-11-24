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

package app.krista.extensions.fedex.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extensions.fedex.entity.FedExLabel;
import app.krista.extensions.fedex.krista.KristaMediaClient;
import app.krista.extensions.fedex.model.FedExLabelResponse;
import app.krista.extensions.fedex.model.FedExResponseHelper;
import app.krista.extensions.fedex.service.FedExService;
import app.krista.extensions.fedex.util.LabelDownloader;
import app.krista.model.base.File;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for FedExArea catalog operations with business logic.
 */
@Service
public class FedExAreaHelper {

    private static final Logger log = LoggerFactory.getLogger(FedExAreaHelper.class);
    private static final String LABEL_FILE_PREFIX = "FedEx_Label_";
    private static final String LABEL_FILE_EXTENSION = ".pdf";
    private static final String TEMP_FILE_PREFIX = "fedex_label_";

    private final FedExService fedExService;
    private final KristaMediaClient kristaMediaClient;

    /**
     * Constructs FedExAreaHelper with required services.
     *
     * @param fedExService FedEx service for API operations
     * @param kristaMediaClient Krista media client for file handling
     */
    @Inject
    public FedExAreaHelper(FedExService fedExService, KristaMediaClient kristaMediaClient) {
        this.fedExService = fedExService;
        this.kristaMediaClient = kristaMediaClient;
    }

    /**
     * Generates FedEx shipping label and returns complete response with tracking information and PDF.
     *
     * @param fedexLabel FedEx label entity with shipment details
     * @return extension response with tracking numbers, label URLs, costs, and PDF file
     * @throws IOException if shipment creation or label download fails
     */
    public ExtensionResponse generateShippingLabel(FedExLabel fedexLabel) throws IOException {
        FedExLabelResponse response = fedExService.createShipment(fedexLabel);

        String masterTracking = FedExResponseHelper.extractMasterTrackingNumber(response);
        List<String> trackingNumbers = FedExResponseHelper.extractTrackingNumbers(response);
        List<String> labelUrls = FedExResponseHelper.extractLabelUrls(response);
        String deliveryDate = FedExResponseHelper.extractDeliveryDate(response);

        Double totalShippingCost = FedExResponseHelper.extractTotalShippingCost(response);
        Double baseRate = FedExResponseHelper.extractBaseRate(response);
        Double totalDiscount = FedExResponseHelper.extractTotalDiscount(response);
        String currencyCode = FedExResponseHelper.extractCurrencyCode(response);

        File labelFile = downloadOrExtractLabelFile(response, labelUrls, masterTracking);

        Map<String, Object> resultMap = buildResponseMap(response, masterTracking, trackingNumbers,
                labelUrls, deliveryDate, totalShippingCost, baseRate, totalDiscount, currencyCode, labelFile);

        return new ExtensionResponseBuilder().success(resultMap).build();
    }

    /**
     * Downloads or extracts label file from FedEx response and converts to Krista file.
     * Automatically detects whether label is provided as URL or base64 encoded data.
     *
     * @param response FedEx API response
     * @param labelUrls list of label URLs
     * @param masterTracking master tracking number for file naming
     * @return Krista file object or null if extraction fails
     */
    private File downloadOrExtractLabelFile(FedExLabelResponse response, List<String> labelUrls, String masterTracking) {
        // Check what FedEx actually returned in the response
        String encodedLabel = extractEncodedLabel(response);
        boolean hasEncodedLabel = encodedLabel != null && !encodedLabel.isEmpty();
        boolean hasLabelUrl = labelUrls != null && !labelUrls.isEmpty();

        // Process based on what's available in the response
        if (hasEncodedLabel) {
            log.info("Label provided as base64 encoded data");
            return createFileFromBase64(encodedLabel, masterTracking);
        } else if (hasLabelUrl) {
            log.info("Label provided as URL");
            return downloadLabelFromUrl(labelUrls.get(0), masterTracking);
        } else {
            log.warn("No label data available - neither URL nor encoded label found in response");
            return null;
        }
    }

    /**
     * Extracts base64 encoded label from FedEx response.
     *
     * @param response FedEx API response
     * @return base64 encoded label string or null if not found
     */
    private String extractEncodedLabel(FedExLabelResponse response) {
        if (response == null || response.getOutput() == null ||
            response.getOutput().getTransactionShipments() == null) {
            return null;
        }

        return response.getOutput().getTransactionShipments().stream()
                .flatMap(shipment -> {
                    // Check shipment documents
                    if (shipment.getShipmentDocuments() != null) {
                        return shipment.getShipmentDocuments().stream()
                                .map(FedExLabelResponse.ShipmentDocument::getEncodedLabel)
                                .filter(label -> label != null && !label.isEmpty());
                    }
                    // Check package documents
                    if (shipment.getPieceResponses() != null) {
                        return shipment.getPieceResponses().stream()
                                .flatMap(piece -> piece.getPackageDocuments() != null ?
                                        piece.getPackageDocuments().stream() : java.util.stream.Stream.empty())
                                .map(FedExLabelResponse.PackageDocument::getEncodedLabel)
                                .filter(label -> label != null && !label.isEmpty());
                    }
                    return java.util.stream.Stream.empty();
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Creates Krista file from base64 encoded label data.
     *
     * @param encodedLabel base64 encoded label string
     * @param masterTracking master tracking number for file naming
     * @return Krista file object or null if creation fails
     */
    private File createFileFromBase64(String encodedLabel, String masterTracking) {
        try {
            log.info("Processing base64 encoded label");
            byte[] labelData = java.util.Base64.getDecoder().decode(encodedLabel);

            String fileName = LABEL_FILE_PREFIX + masterTracking + LABEL_FILE_EXTENSION;
            java.io.File tempFile = java.io.File.createTempFile(TEMP_FILE_PREFIX, LABEL_FILE_EXTENSION);

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(labelData);
            }

            File kristaFile = kristaMediaClient.toKristaFile(tempFile, fileName);
            tempFile.delete();

            log.info("Base64 label uploaded to Krista: {} ({} bytes)", fileName, labelData.length);
            return kristaFile;
        } catch (Exception e) {
            log.error("Failed to process base64 encoded label: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Downloads label PDF from URL and converts to Krista file.
     *
     * @param labelUrl label URL
     * @param masterTracking master tracking number for file naming
     * @return Krista file object or null if download fails
     */
    private File downloadLabelFromUrl(String labelUrl, String masterTracking) {
        try {
            log.info("Downloading label from URL: {}", labelUrl);
            byte[] labelData = LabelDownloader.downloadLabel(labelUrl);

            String fileName = LABEL_FILE_PREFIX + masterTracking + LABEL_FILE_EXTENSION;
            java.io.File tempFile = java.io.File.createTempFile(TEMP_FILE_PREFIX, LABEL_FILE_EXTENSION);

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(labelData);
            }

            File kristaFile = kristaMediaClient.toKristaFile(tempFile, fileName);
            tempFile.delete();

            log.info("Label downloaded and uploaded to Krista: {} ({} bytes)", fileName, labelData.length);
            return kristaFile;
        } catch (Exception e) {
            log.error("Failed to download label from URL: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Builds response map with all shipment information.
     *
     * @param response FedEx API response
     * @param masterTracking master tracking number
     * @param trackingNumbers list of tracking numbers
     * @param labelUrls list of label URLs
     * @param deliveryDate estimated delivery date
     * @param totalShippingCost total shipping cost
     * @param baseRate base shipping rate
     * @param totalDiscount total discount applied
     * @param currencyCode currency code
     * @param labelFile label PDF file
     * @return response map
     */
    private Map<String, Object> buildResponseMap(FedExLabelResponse response, String masterTracking,
                                                  List<String> trackingNumbers, List<String> labelUrls,
                                                  String deliveryDate, Double totalShippingCost,
                                                  Double baseRate, Double totalDiscount,
                                                  String currencyCode, File labelFile) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("TransactionId", response.getTransactionId());
        resultMap.put("MasterTrackingNumber", masterTracking);
        resultMap.put("TrackingNumbers", trackingNumbers);
        resultMap.put("LabelUrls", labelUrls);
        resultMap.put("DeliveryDate", deliveryDate);
        resultMap.put("Success", true);

        addShippingCostToResponse(resultMap, totalShippingCost, baseRate, totalDiscount, currencyCode);
        addLabelFileToResponse(resultMap, labelFile);

        return resultMap;
    }

    /**
     * Adds shipping cost information to response map.
     * Always adds fields to ensure consistent catalog response structure.
     *
     * @param resultMap response map
     * @param totalShippingCost total shipping cost (0.0 if not available)
     * @param baseRate base shipping rate (0.0 if not available)
     * @param totalDiscount total discount applied (0.0 if not available)
     * @param currencyCode currency code (defaults to "USD")
     */
    private void addShippingCostToResponse(Map<String, Object> resultMap, Double totalShippingCost,
                                           Double baseRate, Double totalDiscount, String currencyCode) {
        // Always add fields to response map so they appear in catalog response
        resultMap.put("TotalShippingCost", totalShippingCost);
        resultMap.put("BaseRate", baseRate);
        resultMap.put("TotalDiscount", totalDiscount);
        resultMap.put("CurrencyCode", currencyCode);

        if (totalShippingCost > 0) {
            log.info("Shipping cost: {} {} (Base: {}, Discount: {})",
                    totalShippingCost, currencyCode, baseRate, totalDiscount);
        } else {
            log.info("Shipping cost: 0.0 {} (FedEx API did not return pricing information)",
                    currencyCode);
        }
    }

    /**
     * Adds label file to response map if available.
     *
     * @param resultMap response map
     * @param labelFile label PDF file
     */
    private void addLabelFileToResponse(Map<String, Object> resultMap, File labelFile) {
        if (labelFile != null) {
            resultMap.put("LabelFile", labelFile);
            log.info("Label file added to response");
        } else {
            log.warn("No label file available in response");
        }
    }
}

