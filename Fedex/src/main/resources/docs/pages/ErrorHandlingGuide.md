# FedEx Extension Error Handling Guide

## Overview

This comprehensive guide covers all error scenarios you may encounter when using the FedEx Extension, along with their causes and step-by-step resolution instructions. Each error includes the exact message you'll see and specific actions to resolve the issue.

## Error Categories

- [Configuration Errors](#configuration-errors)
- [Authentication Errors](#authentication-errors)
- [Validation Errors](#validation-errors)
- [API Response Errors](#api-response-errors)
- [Network and Connection Errors](#network-and-connection-errors)
- [System Errors](#system-errors)

---

## Configuration Errors

### Error: "FedEx API Key is required"
**Cause**: API Key field is empty or not configured
**Resolution**:
1. Go to FedEx Developer Portal (developer.fedex.com)
2. Log in to your account
3. Navigate to your application
4. Copy the API Key
5. Paste it into the extension configuration
6. Click "Save" and then "Test Connection"

### Error: "FedEx Secret Key is required"
**Cause**: Secret Key field is empty or not configured
**Resolution**:
1. Go to FedEx Developer Portal (developer.fedex.com)
2. Log in to your account
3. Navigate to your application
4. Copy the Secret Key (paired with your API Key)
5. Paste it into the extension configuration
6. Click "Save" and then "Test Connection"

### Error: "FedEx API Base URL is required"
**Cause**: Base URL field is empty or not configured
**Resolution**:
1. For testing/development: Enter `https://apis-sandbox.fedex.com`
2. For production: Enter `https://apis.fedex.com`
3. Ensure the URL matches your API credentials environment
4. Click "Save" and then "Test Connection"

### Error: "Grant Type must be 'client_credentials'"
**Cause**: Grant Type is not set correctly
**Resolution**:
1. Set Grant Type to exactly `client_credentials`
2. This is the only supported authentication method for FedEx APIs
3. Click "Save" and then "Test Connection"

---

## Authentication Errors

### Error: "FedEx API authentication failed"
**Cause**: Invalid API credentials or environment mismatch
**Resolution**:
1. **Verify Credentials**:
   - Check API Key and Secret Key in FedEx Developer Portal
   - Ensure no extra spaces or characters
2. **Environment Matching**:
   - Sandbox credentials must use sandbox Base URL
   - Production credentials must use production Base URL
3. **Account Status**:
   - Check if account is active in FedEx Developer Portal
   - Verify API access hasn't been suspended
4. **Test Connection**: Use the "Test Connection" button to verify

### Error: "401 Unauthorized" or "Invalid client credentials"
**Cause**: API credentials are incorrect or expired
**Resolution**:
1. Log in to FedEx Developer Portal
2. Verify your application status is active
3. Check if credentials need to be regenerated
4. If needed, generate new API Key and Secret Key
5. Update extension configuration with new credentials
6. Test the connection

### Error: "403 Forbidden" or "Access denied"
**Cause**: Account doesn't have permission to use Ship API
**Resolution**:
1. Contact FedEx support to enable Ship API access
2. Verify your FedEx account has shipping permissions
3. Check if additional account setup is required
4. Ensure you're using the correct account credentials

---

## Validation Errors

### Error: "FedEx Account Number is missing"
**Cause**: Account number field is empty
**Resolution**:
1. **For Production**: Enter your 9-digit FedEx account number (found on invoices)
2. **For Sandbox Testing**: Use test account number `740561073`
3. Ensure no spaces or special characters
4. Verify the account number is active and valid

### Error: "Shipping Service Type is missing"
**Cause**: Service type not specified
**Resolution**:
1. Choose an appropriate service type:
   - `FEDEX_GROUND` - Most economical (1-5 business days)
   - `STANDARD_OVERNIGHT` - Next business day by 3 PM
   - `FEDEX_2_DAY` - Second business day
2. Verify service is available for your route
3. Check account permissions for the service type

### Error: "Package Weight is missing or invalid"
**Cause**: Weight not provided or invalid value
**Resolution**:
1. Enter actual package weight in pounds (e.g., 5.5)
2. Weight must be greater than 0
3. Use minimum weight of 0.1 lbs for very light items
4. Ensure accurate weight to avoid additional charges

### Address Validation Errors

#### Error: "Shipper Contact Name is missing"
**Resolution**: Enter the full name of the person sending the package

#### Error: "Shipper Street Address is missing"
**Resolution**: Enter complete street address including building number and street name

#### Error: "Shipper City is missing"
**Resolution**: Enter the city name for pickup location

#### Error: "Shipper State is missing"
**Resolution**: Enter 2-letter state code (e.g., 'CA', 'NY', 'TX')

#### Error: "Shipper ZIP Code is missing"
**Resolution**: Enter 5-digit ZIP code for pickup location

#### Error: "Shipper Phone Number is missing"
**Resolution**: Enter valid phone number (e.g., '555-123-4567')

*Similar validation applies to all Recipient fields*

---

## API Response Errors

### Error: "Unable to process FedEx API response"
**Cause**: FedEx API returned unexpected or incomplete response
**Resolution**:
1. Wait a few moments and try again
2. Check FedEx API status at developer.fedex.com
3. Verify all input data is complete and valid
4. If persistent, contact support with transaction details

### Error: "FedEx did not provide a tracking number"
**Cause**: Shipment creation failed or was incomplete
**Resolution**:
1. Verify all required shipment information is provided
2. Check that service type is available for your route
3. Ensure account has permissions for the requested service
4. Try again or contact FedEx support

### Error: "FedEx response is missing shipment information"
**Cause**: Shipment creation failed on FedEx side
**Resolution**:
1. Check shipment details are complete and valid
2. Verify account permissions and status
3. Try creating the shipment again
4. Contact support if issue persists

---

## Network and Connection Errors

### Error: "Cannot connect to FedEx API"
**Cause**: Network connectivity issues or incorrect Base URL
**Resolution**:
1. **Check Internet Connection**: Verify you can access other websites
2. **Verify Base URL**: Ensure correct URL for your environment
3. **Firewall Settings**: If using corporate network, check firewall rules
4. **DNS Issues**: Try accessing the FedEx URL directly in a browser
5. **Proxy Settings**: Configure proxy if required by your network

### Error: "Connection timeout" or "Request timeout"
**Cause**: Network latency or FedEx API performance issues
**Resolution**:
1. Wait a few moments and try again (automatic retry enabled)
2. Check network connection stability
3. Verify FedEx API status
4. If persistent, contact your network administrator

---

## System Errors

### Error: "Shipment creation was interrupted"
**Cause**: Request was interrupted during rate limiting
**Resolution**:
1. Try the request again (usually resolves automatically)
2. If making many requests, add small delays between them
3. The extension automatically manages rate limits

### Error: "Retry failed" or "All attempts failed"
**Cause**: Multiple retry attempts failed
**Resolution**:
1. Check network connectivity
2. Verify FedEx API status
3. Review all input data for accuracy
4. Wait a few minutes before trying again
5. Contact support if issue persists

---

## Getting Additional Help

### When to Contact Support
Contact support when you encounter:
- Persistent errors after following resolution steps
- Errors not covered in this guide
- Issues that affect multiple users
- Suspected system or API problems

### Information to Provide
When contacting support, include:
- Exact error message
- Transaction ID (if available)
- Steps you've already tried
- Your environment (sandbox vs production)
- Timestamp when error occurred

### Resources
- [FedEx Developer Portal](https://developer.fedex.com/)
- [FedEx Developer Community](https://community.fedex.com/)
- [Extension Configuration Guide](/pages/ExtensionConfiguration.md)
- [Generate Shipping Label Documentation](/pages/GenerateShippingLabel.md)

---

## Related Pages

- [Overview](/pages/overview.md) - Extension overview and features
- [Creating FedEx App](/pages/CreatingFedExApp.md) - Set up your FedEx developer account
- [Extension Configuration](/pages/ExtensionConfiguration.md) - Configure API credentials
- [Authentication](/pages/Authentication.md) - OAuth 2.0 authentication details
- [Generate Shipping Label](/pages/GenerateShippingLabel.md) - Create shipping labels
- [Release Notes](/pages/release_notes.md) - Version history and updates

---

**Need immediate help?** Use the "Test Connection" feature in your extension configuration to quickly diagnose common issues.
