[comment]: <> (Home page)

# Krista FedEx Extension #

### Home Page

The Krista FedEx extension connects to **FedEx** and performs shipping operations.

* [Overview](pages/overview.md)

* [Obtaining FedEx API Credentials](pages/CreatingFedExApp.md)

* [Configuring FedEx Extension in Krista](pages/ExtensionConfiguration.md)

* [Authentication](pages/Authentication.md)

* [Generate Shipping Label](pages/GenerateShippingLabel.md)

* [Error Handling Guide](pages/ErrorHandlingGuide.md)

* [Release Notes](pages/release_notes.md)

### Extension Information
- **Version**: 1.0.0
- **Area**: FedEx
- **Domain**: Supply Chain
- **Ecosystem**: Logistics

### FedEx API Resources
- [FedEx Developer Portal](https://developer.fedex.com/)
- [FedEx API Documentation](https://developer.fedex.com/api/en-us/catalog.html)
- [FedEx Service Types](https://developer.fedex.com/api/en-us/guides/api-reference.html#servicetypes)
- [FedEx Packaging Types](https://developer.fedex.com/api/en-us/guides/api-reference.html#packagingtypes)

### Environment URLs
- **Sandbox**: `https://apis-sandbox.fedex.com`
- **Production**: `https://apis.fedex.com`

### Rate Limits
- **Default**: 10 requests per second
- **Retry Attempts**: 3 attempts with exponential backoff
- **Initial Delay**: 1000ms
- **Max Delay**: 30000ms

## Common Use Cases

### E-Commerce Order Fulfillment
Automatically generate shipping labels when orders are placed, streamlining your fulfillment process.

### Return Label Generation
Create return labels for customers, making the return process seamless and professional.

### Multi-Package Shipments
Generate labels for shipments with multiple packages, all linked to a master tracking number.

### International Shipping
Create international shipping labels with proper customs documentation and service selection.

## Best Practices

### 1. Environment Selection
- Use **Sandbox** for development and testing
- Use **Production** only for real shipments
- Never mix sandbox and production credentials

### 2. Error Handling
- Always check the `Success` field in responses
- Implement proper error handling for failed label generation
- Log transaction IDs for troubleshooting

### 3. Address Validation
- Validate addresses before submitting to FedEx
- Use complete addresses with all required fields
- Include phone numbers for better delivery success

### 4. Service Type Selection
- Choose appropriate service types based on delivery requirements
- Consider cost vs. speed tradeoffs
- Verify service availability for destination

### 5. Testing
- Test thoroughly in sandbox environment before production
- Verify label downloads work correctly
- Test error scenarios and edge cases

## Troubleshooting

### Connection Issues
**Symptoms**: "Cannot connect to FedEx API", "Connection refused", "Timeout" errors

**Solutions**:
1. **Check Network Connectivity**: Ensure your internet connection is working
2. **Verify Base URL**:
   - Sandbox: `https://apis-sandbox.fedex.com`
   - Production: `https://apis.fedex.com`
3. **Firewall Settings**: If using corporate network, ensure firewall allows access to FedEx APIs
4. **Test Connection**: Use the "Test Connection" button in extension configuration
5. **FedEx API Status**: Check developer.fedex.com for any service outages

### Authentication Failures
**Symptoms**: "Invalid FedEx API credentials", "401 Unauthorized", "403 Forbidden" errors

**Solutions**:
1. **Verify Credentials**: Double-check API Key and Secret Key in FedEx Developer Portal
2. **Environment Matching**: Ensure credentials match your Base URL:
   - Sandbox credentials → Sandbox URL
   - Production credentials → Production URL
3. **Grant Type**: Must be set to `client_credentials`
4. **Account Status**: Check FedEx Developer Portal for account suspension or restrictions
5. **API Permissions**: Verify your account has Ship API access enabled
6. **Credential Regeneration**: If needed, regenerate API credentials in the portal

### Configuration Errors
**Symptoms**: "API Key is required", "Base URL is missing", configuration validation errors

**Solutions**:
1. **Complete All Fields**: Ensure API Key, Secret Key, Base URL, and Grant Type are all filled
2. **No Extra Spaces**: Remove any leading/trailing spaces from credential fields
3. **Correct Format**:
   - Grant Type must be exactly `client_credentials`
   - Base URL must include `https://` prefix
4. **Save Configuration**: Click "Save" after making changes
5. **Test After Changes**: Use "Test Connection" to verify configuration

### Label Generation Errors
**Symptoms**: "Validation failed", "Service type not available", "Address validation failed"

**Solutions**:
1. **Required Fields**: Ensure all mandatory fields are provided:
   - Account number (9 digits)
   - Service type (e.g., FEDEX_GROUND)
   - Package weight (greater than 0)
   - Complete shipper and recipient addresses
   - Phone numbers for both parties
2. **Address Validation**:
   - Use complete street addresses (avoid P.O. boxes for some services)
   - Verify state codes are 2 letters (CA, NY, TX, etc.)
   - Ensure ZIP codes match city/state
   - Include apartment/suite numbers in street address
3. **Service Availability**:
   - Try FEDEX_GROUND for most domestic shipments
   - Verify service is available for your origin/destination
   - Check account permissions for requested service type
4. **Weight and Dimensions**: Ensure package weight is accurate and within FedEx limits
5. **Account Validation**: Verify account number is correct and active

### API Response Errors
**Symptoms**: "Failed to parse FedEx response", "Transaction ID missing", parsing errors

**Solutions**:
1. **Retry Request**: Often resolves temporary API issues
2. **Check FedEx Status**: Verify FedEx API is operational
3. **Review Request Data**: Ensure all input data is valid and complete
4. **Contact Support**: If persistent, contact support with transaction details
5. **Check Logs**: Review error logs for specific technical details

## Getting Help

For additional support:
- **[Error Handling Guide](pages/ErrorHandlingGuide.md)** - Comprehensive troubleshooting for all error scenarios
- Review the detailed documentation pages linked above
- Check the [FedEx Developer Community](https://community.fedex.com/)
- Contact your Krista support team
- Use the "Test Connection" feature in extension configuration for quick diagnostics

---

**Ready to get started?** Begin with [Extension Configuration](pages/ExtensionConfiguration.md) to set up your FedEx integration.

