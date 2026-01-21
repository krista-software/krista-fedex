# Extension Configuration

## Overview

The FedEx Extension requires configuration of API credentials and environment settings to connect to FedEx's shipping services. This page guides you through the configuration process, including obtaining credentials, setting up the extension, and verifying your connection.

## Configuration Parameters

The following parameters are required to configure the FedEx Extension:

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| Base URL | Text | Yes | FedEx API endpoint URL (sandbox or production) | `https://apis-sandbox.fedex.com` |
| API Key | Text | Yes | FedEx API Key (also called Client ID) | `l7xx1234567890abcdef1234567890ab` |
| Secret Key | Text (Secured) | Yes | FedEx Secret Key (also called Client Secret) | `1234567890abcdef1234567890abcdef` |
| Grant Type | Text | Yes | OAuth 2.0 grant type for authentication | `client_credentials` |

## Step-by-Step Setup

### Step 1: Obtain FedEx API Credentials

Before configuring the extension, you need to obtain API credentials from FedEx:

1. Visit the [FedEx Developer Portal](https://developer.fedex.com/)
2. Sign in with your FedEx account or create a new developer account
3. Create a new project/application
4. Generate API credentials (API Key and Secret Key)
5. Note your credentials securely

> **📝 Note**: See [Creating FedEx App](CreatingFedExApp) for detailed instructions on obtaining credentials.

### Step 2: Choose Your Environment

FedEx provides two environments for API access:

#### Sandbox Environment (Testing)
- **Base URL**: `https://apis-sandbox.fedex.com`
- **Purpose**: Development and testing
- **Behavior**: Returns virtual responses, no real shipments created
- **Cost**: Free
- **Use When**: Developing, testing, or validating integration

#### Production Environment (Live)
- **Base URL**: `https://apis.fedex.com`
- **Purpose**: Live shipment creation
- **Behavior**: Creates real shipments and charges your account
- **Cost**: Standard FedEx shipping rates apply
- **Use When**: Ready for production use with real shipments

> **⚠️ Warning**: Production environment creates real shipments and incurs actual shipping costs. Always test thoroughly in sandbox first.

### Step 3: Configure Extension in Krista

1. Navigate to the Extensions section in Krista
2. Locate the FedEx Extension
3. Click "Configure" or "Settings"
4. Enter the configuration parameters:

   **For Sandbox (Testing)**:
   ```
   Base URL: https://apis-sandbox.fedex.com
   API Key: [Your Sandbox API Key]
   Secret Key: [Your Sandbox Secret Key]
   Grant Type: client_credentials
   ```

   **For Production (Live)**:
   ```
   Base URL: https://apis.fedex.com
   API Key: [Your Production API Key]
   Secret Key: [Your Production Secret Key]
   Grant Type: client_credentials
   ```

5. Click "Save" to store the configuration

### Step 4: Test the Connection

After configuration, verify the connection:

1. Click "Test Connection" button in the extension settings
2. Wait for the connection test to complete
3. Verify you see a success message

**Expected Success Message**:
```
✅ FedEx connection test successful
```

**If Connection Fails**:
- Verify API Key and Secret Key are correct
- Check Base URL matches your environment
- Ensure credentials haven't expired
- Verify your FedEx account has API access enabled

## Grant Type Configuration

### Client Credentials Grant

The FedEx Extension uses the **Client Credentials** OAuth 2.0 grant type for authentication:

- **Grant Type**: `client_credentials`
- **Purpose**: Server-to-server authentication
- **Token Type**: Access token only (no refresh token)
- **Token Lifetime**: 1 hour
- **Renewal**: Automatic token refresh when expired

> **📝 Note**: The extension automatically manages token lifecycle. You don't need to manually refresh tokens.

### Why Client Credentials?

Client Credentials grant is used because:
1. **Server-to-Server**: No user interaction required
2. **Automated**: Suitable for automated workflows
3. **Secure**: Credentials stored securely in Krista
4. **Simple**: No redirect URIs or authorization codes needed

## Security Considerations

### Credential Protection

🔒 **Best Practices**:

1. **Never Share Credentials**: Keep API Key and Secret Key confidential
2. **Use Secured Fields**: Secret Key is stored in encrypted format
3. **Rotate Regularly**: Periodically rotate credentials for security
4. **Separate Environments**: Use different credentials for sandbox and production
5. **Limit Access**: Restrict who can view/modify extension configuration

### Environment Isolation

- **Development**: Use sandbox credentials only
- **Testing**: Use sandbox credentials for all testing
- **Production**: Use production credentials only in production environment
- **Never Mix**: Don't use production credentials in development/testing

### Monitoring

- **Log Access**: Monitor who accesses extension configuration
- **Track Usage**: Review API usage in FedEx Developer Portal
- **Alert on Failures**: Set up alerts for authentication failures
- **Audit Changes**: Track configuration changes

## Troubleshooting

### Issue: "Invalid API Key" Error

**Cause**: API Key is incorrect or doesn't match the environment

**Resolution**:
1. Verify API Key is copied correctly (no extra spaces)
2. Ensure you're using sandbox key with sandbox URL
3. Check credentials in FedEx Developer Portal
4. Regenerate credentials if necessary

### Issue: "Authentication Failed" Error

**Cause**: Secret Key is incorrect or credentials are expired

**Resolution**:
1. Verify Secret Key is copied correctly
2. Check if credentials have expired in FedEx portal
3. Ensure grant type is set to `client_credentials`
4. Regenerate credentials if needed

### Issue: "Connection Timeout" Error

**Cause**: Network connectivity issues or incorrect Base URL

**Resolution**:
1. Verify Base URL is correct for your environment
2. Check network connectivity to FedEx APIs
3. Ensure firewall allows outbound HTTPS connections
4. Try again after a few minutes

### Issue: "Sandbox Environment Warning"

**Cause**: Using sandbox environment (expected behavior)

**Resolution**:
- This is informational, not an error
- Sandbox returns virtual responses for testing
- Switch to production URL when ready for live shipments

## Configuration Validation

The extension validates configuration on save:

✅ **Validated Items**:
- Base URL format is valid
- API Key is not empty
- Secret Key is not empty
- Grant Type is `client_credentials`

❌ **Common Validation Errors**:
- Empty or missing required fields
- Invalid URL format
- Incorrect grant type value

## Next Steps

After successful configuration:

1. **Review Authentication**: Learn about [OAuth 2.0 authentication flow](/pages/Authentication.md)
2. **Generate First Label**: Try [generating a shipping label](/pages/GenerateShippingLabel.md)
3. **Explore Features**: Review available catalog requests
4. **Test Thoroughly**: Validate all use cases in sandbox before production

## See Also

- [Authentication](/pages/Authentication.md) - OAuth 2.0 authentication details
- [Creating FedEx App](/pages/CreatingFedExApp.md) - How to obtain API credentials
- [Generate Shipping Label](/pages/GenerateShippingLabel.md) - Create your first label

---

## Related Pages

- [Overview](/pages/overview.md) - Extension overview and features
- [Creating FedEx App](/pages/CreatingFedExApp.md) - Set up your FedEx developer account
- [Authentication](/pages/Authentication.md) - OAuth 2.0 authentication details
- [Generate Shipping Label](/pages/GenerateShippingLabel.md) - Create shipping labels
- [Error Handling Guide](/pages/ErrorHandlingGuide.md) - Troubleshoot common issues
- [Release Notes](/pages/release_notes.md) - Version history and updates

---

**Need Help?** If you encounter issues during configuration, contact your Krista support team or review the [FedEx Developer Documentation](https://developer.fedex.com/).

