[<span style = "color : blue ; text-decoration: none">< Back<span>](/)

# Release Notes - FedEx Shipping Extension

---
## Version 1.0.4

- **Developer**: Rich Krista
- **Krista Service APIs (Java)**: 1.0.117
- **Global Catalog Version**: GC-2025.11.2

### New Features

#### Package Declared Value Support

- **Package Value Field**: Added optional "Package Value" field to FedEx Label entity for declaring package value for insurance purposes
- **Automatic Insurance**: When Package Value is provided, the extension automatically includes declared value in the FedEx API request
- **USD Currency**: All declared values are in USD currency (hardcoded for consistency)
- **Validation**: Package Value must be greater than 0 when provided
- Added `DeclaredValue` class to `FedExLabelRequest.RequestedPackageLineItem` with amount and currency fields
- Updated `FedExLabelMapper.createPackageLineItems()` to conditionally include declared value
- Added `createDeclaredValue()` helper method for building declared value objects

### Improvements

- **Documentation**: Updated GenerateShippingLabel.md with Package Value field details, validation rules, and usage example
- **Best Practices**: Added guidance on when to use Package Value for high-value shipments
- **Business Rules**: Documented that Package Value is optional and always uses USD currency

### Technical Details

- Package Value field added to FedExLabel entity (optional Number field)
- DeclaredValue inner class added to FedExLabelRequest model
- Mapper logic updated to include declaredValue only when Package Value is provided and valid
- Validation ensures Package Value > 0 if provided

---
## Version 1.0.3

- **Developer**: Ramgopal Patidar
- **Krista Service APIs (Java)**: 1.0.117
- **Global Catalog Version**: GC-2025.11.1

### Bug Fixes
[KE-2618](https://antbrains.atlassian.net/browse/KE-2618) : FEDEx Extension No Longer Mapping Cost Information
- Added mapping for the missing Cost field (previously blank) in the Create Label Catalog Request response. If the response value is 0 or blank, it will now be handled and populated as 0.
## Version 1.0.2 

- **Developer**: Ramgopal Patidar
- **Krista Service APIs (Java)**: 1.0.115
- **Global Catalog Version**: GC-2025.10.4

### New Features

#### Flexible Billing Support

- **Recipient Account Billing**: Allow customers to pay using their own FedEx account
- Added `recipientAccountNumber` field to FedEx Label entity
- Added `paymentType` field for UI guidance
- Added `Payor` and `ResponsibleParty` classes for payment structure
- Payment type automatically determined based on recipient account presence

### Improvements

- **Performance**: Static constant for API headers (eliminates HashMap creation on every request)
- **Security**: Account number masking in logs (shows first 4 and last 4 digits)
- **Date Format**: Changed delivery date format from DD-MM-YYYY to MM-DD-YYYY
- **Validation**: Package weight field now required
- **Constants**: Extracted payment type constants to prevent typos

### Bug Fixes

- Fixed package weight validation to align with entity definition
- Fixed date format parsing to handle multiple FedEx API date formats
- Fixed documentation links for Docsify compatibility

## Version 1.0.1
**Developer**: Ramgopal Patidar
**Krista Service APIs (Java)**: 1.0.115
**Global Catalog Version**: GC-2025.10.3

### New Features

#### Shipping Cost Information

- **Total Shipping Cost**: Extract total shipping cost from FedEx API response
- **Base Rate**: Get base rate before discounts and surcharges
- **Freight Discounts**: Track total freight discounts applied
- **Currency Code**: Identify currency for international shipments
- Extracts costs from `completedShipmentDetail.shipmentRating.shipmentRateDetails`
- Added proper class structure for `ShipmentRating` and `ShipmentRateDetail`
- Proper GSON deserialization for nested shipping cost objects

#### Base64 Encoded Label Support

- Added support for PNG labels returned as base64 encoded data
- Smart detection automatically identifies label format (URL vs base64)
- Seamless handling of both PDF (URL) and PNG (base64) label formats
- Automatic decoding and file creation from base64 data
- Backward compatible with existing URL-based label downloads
- Added `encodedLabel` field to `ShipmentDocument` and `PackageDocument` classes

### Improvements

- Enhanced label download mechanism to support multiple formats
- Improved error handling for label retrieval
- Better logging for debugging label format issues

---


## Version 1.0.0 - Initial Release

**Developer**: Ramgopal Patidar  
**Krista Service APIs (Java)**: 1.0.115   


### Core Features

#### Shipping Label Generation

- Create FedEx shipments with complete shipper and recipient details
- Generate shipping labels in PDF format via URL download
- Automatic label download and storage in Krista media
- Master tracking number assignment
- Package-level tracking numbers for multi-piece shipments
- Delivery date estimation

#### Shipment Details Support

- **Shipper Information**: Account number, name, address, contact details
- **Recipient Information**: Name, address, contact details
- **Package Details**: Weight, dimensions, quantity, packaging type
- **Service Types**: FEDEX_GROUND, PRIORITY_OVERNIGHT, STANDARD_OVERNIGHT, FEDEX_2_DAY, etc.
- **Packaging Types**: YOUR_PACKAGING, FEDEX_BOX, FEDEX_ENVELOPE, FEDEX_PAK, etc.
- **Special Services**: Signature options, Saturday delivery, hold at location

#### Authentication & Security

- OAuth 2.0 client credentials flow
- Automatic token caching and renewal with 60-second refresh buffer
- Thread-safe token management with ReentrantLock
- Secure credential management
- API key masking in logs (only first 4 characters shown)

#### Performance & Reliability

- HTTP connection pooling (100 max total, 20 per route)
- Request rate limiting (10 requests/second) with token bucket algorithm
- Retry logic with exponential backoff (3 attempts)
- Efficient resource management with try-with-resources
- Comprehensive error handling and logging

#### Catalog Requests

**Generate Shipping Label**
- Create FedEx shipment and generate label
- Support for domestic and international shipping
- Multiple service types and packaging options
- Package details (weight, dimensions, quantity)
- Special services (signature, Saturday delivery)
- Returns tracking numbers, label file, and delivery date

---

## Related Pages

- [Overview](/pages/overview.md) - Extension overview and features
- [Creating FedEx App](/pages/CreatingFedExApp.md) - Set up your FedEx developer account
- [Extension Configuration](/pages/ExtensionConfiguration.md) - Configure API credentials
- [Authentication](/pages/Authentication.md) - OAuth 2.0 authentication details
- [Generate Shipping Label](/pages/GenerateShippingLabel.md) - Create shipping labels
- [Error Handling Guide](/pages/ErrorHandlingGuide.md) - Troubleshoot common issues
