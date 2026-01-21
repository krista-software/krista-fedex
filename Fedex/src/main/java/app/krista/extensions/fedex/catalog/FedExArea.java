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
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.fedex.entity.FedExLabel;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Catalog area for FedEx shipping operations.
 */
@Domain(id = "catEntryDomain_c0019254-1103-45dd-acef-1eca3463d3d3",
        name = "supply chain",
        ecosystemId = "catEntryEcosystem_753b460d-53fc-4530-b6b1-61de354f3379",
        ecosystemName = "Logistics",
        ecosystemVersion = "7655438f-552e-4d69-93fe-5c66f2bb0667")
@Service
public class FedExArea {

    private final FedExAreaHelper fedExAreaHelper;

    /**
     * Constructs FedExArea with helper for business logic.
     *
     * @param fedExAreaHelper helper class for FedEx operations
     */
    @Inject
    public FedExArea(FedExAreaHelper fedExAreaHelper) {
        this.fedExAreaHelper = fedExAreaHelper;
    }

    /**
     * Generates FedEx shipping label and returns tracking information with downloadable PDF.
     *
     * @param fedexLable FedEx label entity with shipment details
     * @return extension response with tracking numbers, label URLs, and PDF file
     * @throws IOException if shipment creation or label download fails
     */
    @CatalogRequest(
            id = "localDomainRequest_09b285b4-628a-4325-8e6f-21600864443b",
            name = "Generate Shipping Label",
            description = "Generate Shipping Label",
            area = "FedEx",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Text(name = "TransactionId", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = "MasterTrackingNumber", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = "TrackingNumbers", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = "LabelUrls", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = "DeliveryDate", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = "Success", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.File(name = "LabelFile", required = false, options = {})
    @Field(name = "TotalShippingCost", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field(name = "BaseRate", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field(name = "TotalDiscount", type = "Number", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = "CurrencyCode", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public ExtensionResponse generateShippingLable(
            @Field.Desc(name = "FedExLabel", type = "Entity(FedExLabel)", required = false) FedExLabel fedexLable) throws IOException {
        return fedExAreaHelper.generateShippingLabel(fedexLable);
    }
}

