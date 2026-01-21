[<span style = "color : blue ; text-decoration: none">< Back<span>](/)
# Overview

## What is FedEx?

FedEx is a global leader in shipping and logistics services, providing express delivery, ground shipping, freight services, and supply chain management solutions worldwide. The FedEx Ship API enables businesses to integrate shipping capabilities directly into their applications.

**Key Features:**
• Express and ground shipping services
• Real-time rate calculation and comparison
• Shipping label generation (PDF and PNG formats)
• Package tracking and delivery notifications
• International shipping with customs documentation
• Multiple service types (overnight, 2-day, ground, etc.)
• Address validation and verification

## What Does This Extension Do?

The FedEx Shipping Extension provides seamless integration with the FedEx Ship API, enabling automated shipping label generation, rate calculation, and shipment tracking within the Krista platform.

**What is Actually Implemented:**

## Shipping Label Generation
**Create Shipping Labels**
• Generate FedEx shipping labels with complete shipment details
• Support for both domestic and international shipments
• Automatic label PDF/PNG generation and storage
• Master tracking number assignment
• Package-level tracking numbers for multi-piece shipments
• Delivery date estimation

**Label Format Support**
• PDF labels via URL download
• PNG labels via base64 encoded data
• Smart detection of label format (automatic handling)
• Temporary file management with cleanup
• Integration with Krista media storage

## Shipping Cost Calculation
**Real-Time Rate Information**
• Total shipping cost extraction from API response
• Base rate calculation before discounts
• Freight discount tracking
• Currency code identification
• Cost breakdown by service type

## Shipment Details
**Comprehensive Shipment Information**
• Shipper details (name, address, contact, account number)
• Recipient details (name, address, contact)
• Package details (weight, dimensions, quantity)
• Service type selection (FEDEX_GROUND, PRIORITY_OVERNIGHT, etc.)
• Packaging type (YOUR_PACKAGING, FEDEX_BOX, FEDEX_ENVELOPE, etc.)
• Special services (signature options, Saturday delivery, etc.)

## Authentication & Security
**OAuth 2.0 Integration**
• Client credentials flow for API authentication
• Automatic token caching with expiration management
• 60-second refresh buffer for token renewal
• Thread-safe token management
• Secure credential storage

## Performance & Reliability
**Robust Error Handling**
• Exponential backoff retry logic (3 attempts)
• Rate limiting (10 requests/second)
• Connection pooling (100 max connections)
• Comprehensive error logging
• Graceful degradation

**Catalog Requests**
• Generate Shipping Label - Create FedEx shipment and generate label

**Data Access Approach**
This extension provides secure, production-ready integration with FedEx Ship API. It handles authentication, rate limiting, retry logic, and error handling automatically, allowing seamless shipping label generation and cost calculation within Krista workflows.

---

## Related Pages

- [Creating FedEx App](/pages/CreatingFedExApp.md) - Set up your FedEx developer account
- [Extension Configuration](/pages/ExtensionConfiguration.md) - Configure API credentials
- [Authentication](/pages/Authentication.md) - OAuth 2.0 authentication details
- [Generate Shipping Label](/pages/GenerateShippingLabel.md) - Create shipping labels
- [Error Handling Guide](/pages/ErrorHandlingGuide.md) - Troubleshoot common issues
- [Release Notes](/pages/release_notes.md) - Version history and updates
