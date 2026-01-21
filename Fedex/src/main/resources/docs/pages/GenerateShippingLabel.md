# Generate Shipping Label

## Overview

Creates a FedEx shipping label with tracking numbers and downloadable label PDF. This catalog request generates a complete shipment with all necessary documentation for package delivery.

## Request Details

- **Area**: FedEx
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes - Automatic retry with exponential backoff (3 attempts, 1s initial delay, up to 30s max delay)

## Input Parameters

| Parameter Name | Type               | Required | Description                                                                     | Example                    |
|----------------|--------------------|----------|---------------------------------------------------------------------------------|----------------------------|
| FedExLabel     | Entity(FedExLabel) | No       | Complete shipment details including shipper, recipient, and package information | See entity structure below |

### FedExLabel Entity Structure

The FedExLabel entity contains all shipment details organized into logical sections:

#### Account Information

| Field          | Type | Required | Description                                 | Example       |
|----------------|------|----------|---------------------------------------------|---------------|
| Ticket Id      | Text | Yes      | Unique identifier for tracking this request | "ORDER-12345" |
| Account Number | Text | Yes      | Your FedEx account number                   | "123456789"   |

#### Shipment Details

| Field          | Type | Required | Description                                           | Example                |
|----------------|------|----------|-------------------------------------------------------|------------------------|
| Ship Date      | Text | No       | Ship date in YYYY-MM-DD format (defaults to tomorrow) | "2025-10-08"           |
| Service Type   | Text | Yes      | FedEx service type (see Service Types section)        | "FEDEX_GROUND"         |
| Packaging Type | Text | No       | Type of packaging (defaults to YOUR_PACKAGING)        | "YOUR_PACKAGING"       |
| Pickup Type    | Text | No       | Pickup method (defaults to USE_SCHEDULED_PICKUP)      | "USE_SCHEDULED_PICKUP" |

#### Shipper Information

| Field                 | Type | Required | Description                              | Example         |
|-----------------------|------|----------|------------------------------------------|-----------------|
| Shipper Person Name   | Text | Yes      | Contact person name at origin            | "John Smith"    |
| Shipper Company Name  | Text | No       | Company name at origin                   | "Acme Corp"     |
| Shipper Phone Number  | Text | Yes      | Contact phone number                     | "555-123-4567"  |
| Shipper Street Line 1 | Text | Yes      | Street address line 1                    | "123 Main St"   |
| Shipper Street Line 2 | Text | No       | Street address line 2 (suite, apt, etc.) | "Suite 100"     |
| Shipper City          | Text | Yes      | City name                                | "San Francisco" |
| Shipper State Code    | Text | Yes      | Two-letter state code                    | "CA"            |
| Shipper Postal Code   | Text | Yes      | ZIP or postal code                       | "94102"         |
| Shipper Country Code  | Text | No       | Two-letter country code (defaults to US) | "US"            |

#### Recipient Information

| Field                   | Type | Required | Description                              | Example              |
|-------------------------|------|----------|------------------------------------------|----------------------|
| Recipient Person Name   | Text | Yes      | Contact person name at destination       | "Jane Doe"           |
| Recipient Company Name  | Text | No       | Company name at destination              | "Tech Solutions Inc" |
| Recipient Phone Number  | Text | Yes      | Contact phone number                     | "555-987-6543"       |
| Recipient Street Line 1 | Text | Yes      | Street address line 1                    | "456 Oak Ave"        |
| Recipient Street Line 2 | Text | No       | Street address line 2 (suite, apt, etc.) | "Apt 5B"             |
| Recipient City          | Text | Yes      | City name                                | "New York"           |
| Recipient State Code    | Text | Yes      | Two-letter state code                    | "NY"                 |
| Recipient Postal Code   | Text | Yes      | ZIP or postal code                       | "10001"              |
| Recipient Country Code  | Text | No       | Two-letter country code (defaults to US) | "US"                 |

#### Package Information

| Field          | Type   | Required | Description                                                      | Example |
|----------------|--------|----------|------------------------------------------------------------------|---------|
| Package Weight | Number | Yes      | Package weight (must be greater than 0)                          | 5.5     |
| Weight Unit    | Text   | No       | Weight unit: LB or KG (defaults to LB)                           | "LB"    |
| Package Value  | Number | No       | Declared value for insurance purposes (must be greater than 0 if provided, currency is USD) | 250.00  |

### Service Types

Common FedEx service types you can use:

**Express Services** (Overnight/Fast):
- `STANDARD_OVERNIGHT` - Next business day by 3 PM
- `PRIORITY_OVERNIGHT` - Next business day by 10:30 AM
- `FIRST_OVERNIGHT` - Next business day by 8:30 AM
- `FEDEX_2_DAY` - Second business day
- `FEDEX_2_DAY_AM` - Second business day by 10:30 AM
- `FEDEX_EXPRESS_SAVER` - Third business day

**Ground Services** (Economy):
- `FEDEX_GROUND` - 1-5 business days (most economical)
- `GROUND_HOME_DELIVERY` - Residential delivery

**International Services**:
- `FEDEX_INTERNATIONAL_PRIORITY` - 1-3 business days
- `FEDEX_INTERNATIONAL_ECONOMY` - 2-5 business days

> **💡 Tip**: Use `FEDEX_GROUND` for most domestic shipments to balance cost and speed.

### Packaging Types

- `YOUR_PACKAGING` - Your own box or packaging (default, most flexible)
- `FEDEX_PAK` - FedEx Pak envelope
- `FEDEX_BOX` - FedEx Box
- `FEDEX_ENVELOPE` - FedEx Envelope
- `FEDEX_TUBE` - FedEx Tube

### Pickup Types

- `USE_SCHEDULED_PICKUP` - Use existing scheduled pickup (default, most common)
- `DROPOFF_AT_FEDEX_LOCATION` - Customer drops off at FedEx location
- `CONTACT_FEDEX_TO_SCHEDULE` - Request one-time pickup

## Output Parameters

| Parameter Name       | Type       | Description                                        |
|----------------------|------------|----------------------------------------------------|
| TransactionId        | Text       | FedEx transaction ID for this shipment             |
| MasterTrackingNumber | Text       | Primary tracking number for the shipment           |
| TrackingNumbers      | List<Text> | All tracking numbers (for multi-package shipments) |
| LabelUrls            | List<Text> | URLs to download label PDFs                        |
| DeliveryDate         | Text       | Estimated delivery date                            |
| Success              | Boolean    | Whether label generation succeeded                 |
| LabelFile            | File       | Downloaded label PDF file (automatically attached) |
| TotalShippingCost    | Number     | Total shipping cost (net charge amount)            |
| BaseRate             | Number     | Base shipping rate before discounts                |
| TotalDiscount        | Number     | Total discount amount applied                      |
| CurrencyCode         | Text       | Currency code for shipping costs (typically "USD") |

### Output Details

**TransactionId**: Unique identifier assigned by FedEx for this transaction. Use this for troubleshooting with FedEx support.

**MasterTrackingNumber**: The primary tracking number for the shipment. This is the main number to provide to customers.

**TrackingNumbers**: Array of all tracking numbers. For single-package shipments, this contains one number. For multi-package shipments, contains all package tracking numbers.

**LabelUrls**: Array of URLs where label PDFs can be downloaded. The extension automatically downloads the first label.

**DeliveryDate**: Estimated delivery date in YYYY-MM-DD format based on service type and destination.

**Success**: Boolean indicating if the label was generated successfully. Always check this field.

**LabelFile**: The downloaded label PDF file, ready to print. Automatically downloaded and attached to the response.

**TotalShippingCost**: The total shipping cost charged for this shipment (net charge amount after discounts). This is the actual amount that will be billed to your FedEx account.

**BaseRate**: The base shipping rate before any discounts are applied. This represents the standard published rate for the selected service.

**TotalDiscount**: The total discount amount applied to the shipment. This is the difference between BaseRate and TotalShippingCost.

**CurrencyCode**: The currency code for all cost amounts. Typically "USD" for US domestic shipments. For international shipments, this may vary based on your account settings.

## Validation Rules

The following validations are performed before submitting to FedEx API:

| Validation                        | Error Message                                           | Resolution                                      |
|-----------------------------------|---------------------------------------------------------|-------------------------------------------------|
| Account number is null or empty   | "FedEx Account Number is missing. Please enter your 9-digit FedEx account number (found on your FedEx invoice or account dashboard). Example: '123456789'" | Enter your 9-digit FedEx account number. For sandbox testing, use '740561073' |
| Service type is null or empty     | "Shipping Service Type is missing. Please select a FedEx service such as 'FEDEX_GROUND' (most economical), 'STANDARD_OVERNIGHT' (next day), or 'FEDEX_2_DAY'. Check the documentation for all available service types." | Select appropriate service type (e.g., FEDEX_GROUND for economy, STANDARD_OVERNIGHT for fast delivery) |
| Package weight is null or ≤ 0     | "Package Weight is missing or invalid. Please enter the actual weight of your package in pounds (e.g., 5.5 for 5.5 lbs). Weight must be greater than 0. Accurate weight is important for correct shipping costs and delivery." | Enter actual package weight in pounds, must be greater than 0 |
| Package value provided but ≤ 0    | "Package value must be greater than 0 when provided" | If declaring a package value for insurance, enter an amount greater than 0. Leave blank if no declared value needed |
| Shipper person name is empty      | "Shipper Contact Name is missing. Please enter the full name of the person sending the package (e.g., 'John Smith'). This helps FedEx contact the sender if needed." | Enter full name of person sending the package |
| Shipper street address is empty   | "Shipper Street Address is missing. Please enter the complete street address where the package will be picked up (e.g., '123 Main Street'). Include building numbers and street names." | Enter complete pickup street address with building number |
| Shipper city is empty             | "Shipper City is missing. Please enter the city name for the pickup location (e.g., 'San Francisco')." | Enter city name for pickup location |
| Shipper state code is empty       | "Shipper State is missing. Please enter the 2-letter state code for the pickup location (e.g., 'CA' for California, 'NY' for New York)." | Enter 2-letter state code (CA, NY, TX, etc.) |
| Shipper postal code is empty      | "Shipper ZIP Code is missing. Please enter the 5-digit ZIP code for the pickup location (e.g., '94102'). This ensures accurate pickup and pricing." | Enter 5-digit ZIP code for pickup location |
| Shipper phone number is empty     | "Shipper Phone Number is missing. Please enter a valid phone number (e.g., '555-123-4567'). FedEx uses this to contact the sender about pickup or delivery issues." | Enter valid phone number for pickup contact |
| Recipient person name is empty    | "Recipient Contact Name is missing. Please enter the full name of the person receiving the package (e.g., 'Jane Doe'). This helps FedEx deliver to the correct person." | Enter full name of person receiving the package |
| Recipient street address is empty | "Recipient Street Address is missing. Please enter the complete delivery address (e.g., '456 Oak Avenue'). Include building numbers, street names, and any apartment or suite numbers." | Enter complete delivery address with building/apartment numbers |
| Recipient city is empty           | "Recipient City is missing. Please enter the city name for the delivery location (e.g., 'New York')." | Enter city name for delivery location |
| Recipient state code is empty     | "Recipient State is missing. Please enter the 2-letter state code for the delivery location (e.g., 'NY' for New York, 'TX' for Texas)." | Enter 2-letter state code for delivery location |
| Recipient postal code is empty    | "Recipient ZIP Code is missing. Please enter the 5-digit ZIP code for the delivery location (e.g., '10001'). This ensures accurate delivery and pricing." | Enter 5-digit ZIP code for delivery location |
| Recipient phone number is empty   | "Recipient Phone Number is missing. Please enter a valid phone number (e.g., '555-987-6543'). FedEx uses this to contact the recipient about delivery attempts or issues." | Enter valid phone number for delivery contact |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters in your shipment request

**Common Scenarios**:
- Missing required fields (account number, addresses, weight, service type)
- Invalid data formats or values
- Empty or whitespace-only values in required fields

**Error Messages You'll See**:
- "Shipment information validation failed. Please review and correct the following issues: [specific validation errors]"
- "FedEx Account Number is missing. Please enter your 9-digit FedEx account number..."
- "Package Weight is missing or invalid. Please enter the actual weight of your package..."
- "Shipper Contact Name is missing. Please enter the full name of the person sending..."

**Resolution Steps**:
1. **Read the Complete Error Message**: Each validation error provides specific guidance on what's missing or incorrect
2. **Check All Required Fields**: Ensure account number, service type, package weight, and complete addresses are provided
3. **Verify Data Formats**:
   - Account numbers should be 9 digits
   - State codes should be 2 letters (CA, NY, TX)
   - ZIP codes should be 5 digits
   - Phone numbers should include area code
4. **Remove Extra Spaces**: Check for leading/trailing spaces in all fields
5. **Use Examples**: Follow the format examples provided in error messages

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic failures from FedEx API - your request is valid but FedEx cannot fulfill it

**Common Scenarios**:
- Service type not available for your specific route
- Account doesn't have permission for requested service
- Address cannot be serviced by FedEx
- Package exceeds weight, dimension, or value limits for the service

**Error Messages You'll See**:
- FedEx API errors about service availability
- Account authorization failures
- Address validation rejections
- Package limit violations

**Resolution Steps**:
1. **Try Alternative Service Types**:
   - If STANDARD_OVERNIGHT fails, try FEDEX_GROUND
   - Check service availability for your origin/destination combination
2. **Verify Account Permissions**:
   - Contact FedEx to ensure your account can use the requested service
   - Check if additional account setup is required
3. **Address Validation**:
   - Ensure addresses are complete and accurate
   - Verify ZIP codes match city/state
   - Try alternative address formats if available
   - Avoid P.O. boxes for certain services
4. **Package Requirements**:
   - Check FedEx weight limits for your service type
   - Verify package dimensions are within limits
   - Consider splitting large shipments into multiple packages

### System Errors (SYSTEM_ERROR)

**Cause**: Technical failures in the system, network, or FedEx API

**Common Scenarios**:
- Network connectivity problems
- FedEx API temporarily unavailable
- Rate limiting or too many requests
- Response parsing failures due to unexpected API changes

**Error Messages You'll See**:
- "Shipment creation failed due to an unexpected error. Please verify your FedEx configuration..."
- "Unable to process FedEx API response. The response format was unexpected..."
- "Shipment creation was interrupted while managing request rate limits..."

**Resolution Steps**:
1. **Wait and Retry**: Most system errors are temporary - try again in a few minutes
2. **Check Network**: Verify your internet connection is stable
3. **Verify FedEx API Status**: Check developer.fedex.com for service announcements
4. **Review Configuration**: Ensure extension configuration is correct
5. **Automatic Retry**: The extension automatically retries failed requests up to 3 times
6. **Contact Support**: If errors persist, contact support with error details and timestamps

### Authorization Errors

**Cause**: Authentication or permission issues with your FedEx API credentials

**Common Scenarios**:
- Invalid or expired API credentials
- Wrong environment (sandbox credentials with production URL)
- Account doesn't have Ship API access enabled
- API key has been revoked or suspended

**Error Messages You'll See**:
- "FedEx API authentication failed. Your API Key or Secret Key is incorrect..."
- "FedEx API access denied. Your account may not have permission to use the Ship API..."
- "Invalid FedEx API credentials. Please check your API Key and Secret Key..."

**Error Messages**:
- "401 Unauthorized"
- "403 Forbidden"
- "Invalid client credentials"

**Resolution**:
1. Verify API Key and Secret Key in configuration
2. Check credentials haven't expired
3. Ensure account has Ship API enabled
4. Test connection in extension settings

## Usage Examples

### Example 1: Basic Domestic Ground Shipment

**Scenario**: Ship a 5 lb package from California to New York using FedEx Ground

**Input**:
```
Ticket Id: "ORDER-12345"
Account Number: "123456789"
Service Type: "FEDEX_GROUND"
Package Weight: 5.0
Weight Unit: "LB"

Shipper Person Name: "John Smith"
Shipper Phone Number: "555-123-4567"
Shipper Street Line 1: "123 Main St"
Shipper City: "San Francisco"
Shipper State Code: "CA"
Shipper Postal Code: "94102"

Recipient Person Name: "Jane Doe"
Recipient Phone Number: "555-987-6543"
Recipient Street Line 1: "456 Oak Ave"
Recipient City: "New York"
Recipient State Code: "NY"
Recipient Postal Code: "10001"
```

**Output**:
```json
{
  "TransactionId": "624deea6-b709-470c-8c39-4b5511281492",
  "MasterTrackingNumber": "794947717776",
  "TrackingNumbers": ["794947717776"],
  "LabelUrls": ["https://apis-sandbox.fedex.com/ship/v1/shipments/794947717776/label"],
  "DeliveryDate": "2025-10-13",
  "Success": true,
  "LabelFile": [File: FedEx_Label_794947717776.pdf],
  "TotalShippingCost": 12.45,
  "BaseRate": 15.00,
  "TotalDiscount": 2.55,
  "CurrencyCode": "USD"
}
```

**Result**: Label generated successfully with shipping cost of $12.45 USD, ready to print and attach to package.

### Example 2: Overnight Express Shipment

**Scenario**: Ship urgent 2 lb document package overnight

**Input**:
```
Ticket Id: "URGENT-789"
Account Number: "123456789"
Service Type: "STANDARD_OVERNIGHT"
Packaging Type: "FEDEX_PAK"
Package Weight: 2.0
Weight Unit: "LB"

[Same shipper and recipient details as Example 1]
```

**Output**:
```json
{
  "TransactionId": "724deea6-c809-570d-9d49-5c6622392503",
  "MasterTrackingNumber": "795058828887",
  "TrackingNumbers": ["795058828887"],
  "LabelUrls": ["https://apis-sandbox.fedex.com/ship/v1/shipments/795058828887/label"],
  "DeliveryDate": "2025-10-08",
  "Success": true,
  "LabelFile": [File: FedEx_Label_795058828887.pdf],
  "TotalShippingCost": 45.75,
  "BaseRate": 52.00,
  "TotalDiscount": 6.25,
  "CurrencyCode": "USD"
}
```

**Result**: Overnight label generated with next-day delivery date. Shipping cost: $45.75 USD.

### Example 3: High-Value Package with Declared Value

**Scenario**: Ship valuable electronics with declared value for insurance

**Input**:
```
Ticket Id: "ORDER-56789"
Account Number: "123456789"
Service Type: "FEDEX_GROUND"
Package Weight: 10.0
Weight Unit: "LB"
Package Value: 250.00

Shipper Person Name: "John Smith"
Shipper Phone Number: "555-123-4567"
Shipper Street Line 1: "123 Main St"
Shipper City: "San Francisco"
Shipper State Code: "CA"
Shipper Postal Code: "94102"

Recipient Person Name: "Jane Doe"
Recipient Phone Number: "555-987-6543"
Recipient Street Line 1: "456 Oak Ave"
Recipient City: "New York"
Recipient State Code: "NY"
Recipient Postal Code: "10001"
```

**Output**:
```json
{
  "TransactionId": "824deea6-d909-680e-0e59-6d7733493614",
  "MasterTrackingNumber": "796169939998",
  "TrackingNumbers": ["796169939998"],
  "LabelUrls": ["https://apis-sandbox.fedex.com/ship/v1/shipments/796169939998/label"],
  "DeliveryDate": "2025-10-13",
  "Success": true,
  "LabelFile": [File: FedEx_Label_796169939998.pdf],
  "TotalShippingCost": 18.50,
  "BaseRate": 22.00,
  "TotalDiscount": 3.50,
  "CurrencyCode": "USD"
}
```

**Result**: Label generated with $250.00 USD declared value for insurance coverage. Package is insured up to the declared value amount.

### Example 4: Retry Scenario

**Scenario**: Temporary network issue, automatic retry succeeds

**Input**: [Same as Example 1]

**Process**:
```
Attempt 1: Network timeout → Retry in 1000ms
Attempt 2: Success
```

**Output**: [Same as Example 1]

**Result**: Request succeeded on second attempt without user intervention.

## Business Rules

1. **Ship Date Defaults**: If not provided, ship date defaults to tomorrow's date
2. **Country Code Defaults**: If not provided, country codes default to "US"
3. **Weight Unit Defaults**: If not provided, weight unit defaults to "LB" (pounds)
4. **Packaging Defaults**: If not provided, packaging type defaults to "YOUR_PACKAGING"
5. **Pickup Defaults**: If not provided, pickup type defaults to "USE_SCHEDULED_PICKUP"
6. **Label Format**: Labels are always generated in PDF format for compatibility
7. **Label Size**: Labels use 8.5x11 inch paper, top half layout by default
8. **Payment**: Shipping charges are billed to sender (shipper) account
9. **Single Package**: Currently supports single package per shipment
10. **Automatic Download**: First label PDF is automatically downloaded and attached
11. **Shipping Costs**: TotalShippingCost represents the actual amount billed to your account (BaseRate minus TotalDiscount)
12. **Cost Currency**: All shipping costs are returned in the currency associated with your FedEx account (typically USD for US accounts)
13. **Declared Value**: Package Value is optional; when provided, it declares the package value for insurance purposes in USD currency

## Limitations

1. **Single Package Only**: Currently supports one package per shipment (multi-package support planned for future)
2. **Domestic Focus**: Optimized for US domestic shipments (international requires additional fields)
3. **Rate Limiting**: Maximum 10 requests per second (enforced by extension)
4. **Label Format**: PDF only (PNG and thermal printer formats not currently supported)
5. **Service Availability**: Not all service types available for all routes
6. **Weight Limits**: Subject to FedEx weight limits per service type
7. **Sandbox Limitations**: Sandbox returns virtual responses, no real shipments created
8. **Shipping Cost Availability**: Shipping costs may not be available in all sandbox responses; production environment provides accurate costs

## Best Practices

### 1. Validate Addresses Before Submission
Always validate addresses are complete and accurate before generating labels. Incomplete addresses cause shipment delays.

### 2. Choose Appropriate Service Type
Select service type based on delivery urgency and cost requirements. Use FEDEX_GROUND for most shipments to minimize costs.

### 3. Include Complete Contact Information
Always provide phone numbers for both shipper and recipient. This helps FedEx contact parties if delivery issues arise.

### 4. Test in Sandbox First
Always test your integration in sandbox environment before using production credentials. Sandbox is free and returns realistic responses.

### 5. Store Tracking Numbers
Save the MasterTrackingNumber and TransactionId for future reference, customer service, and shipment tracking.

### 6. Handle Errors Gracefully
Implement proper error handling to catch validation errors, API failures, and network issues. Display clear error messages to users.

### 7. Print Labels Immediately
Print labels as soon as they're generated to avoid delays in shipping process.

### 8. Verify Package Weight
Ensure package weight is accurate. Incorrect weights may result in additional charges or delivery delays.

### 9. Review Shipping Costs
Always review the TotalShippingCost in the response to ensure it matches your expectations. Compare BaseRate and TotalDiscount to understand pricing.

### 10. Track Shipping Expenses
Store shipping cost information (TotalShippingCost, BaseRate, TotalDiscount) for accounting, budgeting, and cost analysis purposes.

### 11. Declare Package Value for High-Value Items
For packages containing valuable items (electronics, jewelry, etc.), always provide the Package Value field to ensure proper insurance coverage. This protects against loss or damage during transit.

## Common Use Cases

### 1. E-Commerce Order Fulfillment
```
Scenario: Customer places order on website
Action: Automatically generate shipping label when order is confirmed
Result: Label ready to print, tracking number sent to customer
```

### 2. Return Label Generation
```
Scenario: Customer requests product return
Action: Generate return label with customer as shipper, warehouse as recipient
Result: Return label emailed to customer for easy returns
```

### 3. Batch Label Generation
```
Scenario: Process 100 orders at end of day
Action: Loop through orders, generate label for each
Result: All labels ready for next-day pickup
```

### 4. Express Shipping Upgrade
```
Scenario: Customer pays for overnight shipping
Action: Generate label with STANDARD_OVERNIGHT service type
Result: Package delivered next business day
```

## Related Catalog Requests

Currently, Generate Shipping Label is the only catalog request in the FedEx Extension. Future releases may include:
- Track Shipment
- Cancel Shipment
- Get Rate Quote
- Validate Address
- Schedule Pickup

## Technical Implementation

### Helper Class
- **Class**: FedExLabelMapper
- **Package**: app.krista.extensions.fedex.mapper
- **Validation**: Validates all required fields before API call
- **Service**: FedExService.createShipment()

### Service Flow
1. **Validation**: FedExLabelMapper.validateLabel() checks all required fields
2. **Mapping**: FedExLabelMapper.toFedExLabelRequest() converts entity to API payload
3. **Rate Limiting**: RateLimiter ensures max 10 requests/second
4. **Authentication**: FedExTokenManager provides valid access token
5. **API Call**: FedExHttpClient.post() sends request to FedEx Ship API
6. **Retry Logic**: RetryHelper retries up to 3 times with exponential backoff
7. **Response Parsing**: FedExResponseParser validates and parses JSON response
8. **Label Download**: LabelDownloader fetches PDF from label URL
9. **File Upload**: KristaMediaClient converts PDF to Krista file format

### Retry Configuration
- **Max Attempts**: 3
- **Initial Delay**: 1000ms (1 second)
- **Max Delay**: 30000ms (30 seconds)
- **Backoff Multiplier**: 2.0 (exponential)
- **Retryable Errors**: Network timeouts, 5xx server errors, rate limit errors

### Rate Limiting
- **Requests Per Second**: 10
- **Implementation**: Token bucket algorithm
- **Behavior**: Requests wait if rate limit reached

## Troubleshooting

### Issue: "Account number is required" Error

**Cause**: Account number field is empty or null

**Solution**:
1. Verify Account Number field is populated
2. Check for extra spaces or invalid characters
3. Ensure account number is your FedEx account number

### Issue: "Service type not available" Error

**Cause**: Selected service type not available for origin/destination

**Solution**:
1. Verify service type is spelled correctly
2. Check service is available for your route
3. Try FEDEX_GROUND as fallback option
4. Contact FedEx to verify account service permissions

### Issue: "Address validation failed" Error

**Cause**: Address is incomplete or invalid

**Solution**:
1. Verify all required address fields are provided
2. Check state codes are two-letter abbreviations
3. Ensure postal codes are valid for the state
4. Verify street addresses are complete

### Issue: Label PDF not downloading

**Cause**: Network issue or invalid label URL

**Solution**:
1. Check network connectivity
2. Verify label URL in response is valid
3. Try downloading manually from label URL
4. Check firewall allows downloads from FedEx domains

### Issue: "Rate limit exceeded" Error

**Cause**: Too many requests in short time period

**Solution**:
1. Wait a few seconds and retry
2. Implement delays between batch requests
3. Reduce request frequency
4. Extension automatically handles rate limiting

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Configure FedEx API credentials
- [Authentication](/pages/Authentication.md) - OAuth 2.0 authentication details
- [Creating FedEx App](/pages/CreatingFedExApp.md) - Obtain API credentials
- [FedEx Ship API Documentation](https://developer.fedex.com/api/en-us/catalog/ship/v1/docs.html) - Official FedEx API docs

---

## Related Pages

- [Overview](/pages/overview.md) - Extension overview and features
- [Creating FedEx App](/pages/CreatingFedExApp.md) - Set up your FedEx developer account
- [Extension Configuration](/pages/ExtensionConfiguration.md) - Configure API credentials
- [Authentication](/pages/Authentication.md) - OAuth 2.0 authentication details
- [Error Handling Guide](/pages/ErrorHandlingGuide.md) - Troubleshoot common issues
- [Release Notes](/pages/release_notes.md) - Version history and updates

---

**Ready to generate labels?** Ensure you've completed [Extension Configuration](/pages/ExtensionConfiguration.md) and have valid FedEx credentials.

