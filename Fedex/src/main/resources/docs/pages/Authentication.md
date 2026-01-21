# Authentication

## Overview

The FedEx Extension uses OAuth 2.0 Client Credentials grant type for authentication with FedEx APIs. This authentication method provides secure, server-to-server communication without requiring user interaction. The extension automatically manages token acquisition, renewal, and expiration.

## Authentication Mode

### Client Credentials Authentication

The FedEx Extension uses **Client Credentials** authentication exclusively:

- **Grant Type**: `client_credentials`
- **Authentication Flow**: Server-to-server
- **User Interaction**: None required
- **Token Type**: Access token only
- **Refresh Mechanism**: Automatic token renewal

#### When to Use Client Credentials

Client Credentials authentication is ideal for:
- ✅ Automated workflows and batch processing
- ✅ Server-to-server API calls
- ✅ Background jobs and scheduled tasks
- ✅ Applications acting on their own behalf (not on behalf of users)

## OAuth 2.0 Scopes and Permissions

### Required Scopes

FedEx API uses a simplified scope model. The Client Credentials grant provides access to all APIs associated with your FedEx account:

| Scope   | Permission Level | Description                                       |
|---------|------------------|---------------------------------------------------|
| Default | Full API Access  | Access to all FedEx APIs enabled for your account |

> **📝 Note**: Unlike some OAuth 2.0 implementations, FedEx doesn't require explicit scope parameters. Access is controlled by your account's API permissions in the FedEx Developer Portal.

### API Permissions

Your FedEx account determines which APIs you can access:

- **Ship API**: Create shipping labels and shipments
- **Track API**: Track package status and delivery
- **Rate API**: Get shipping rate quotes
- **Address Validation API**: Validate shipping addresses
- **Pickup API**: Schedule package pickups

> **⚠️ Important**: Ensure your FedEx account has the necessary API permissions enabled in the Developer Portal.

## OAuth 2.0 Grant Type

### Client Credentials Grant

The Client Credentials grant is used for initial authentication and token renewal.

#### Grant Type Details

- **Grant Type**: `client_credentials`
- **Used For**: Obtaining access tokens
- **Authentication**: API Key + Secret Key
- **Token Response**: Access token with expiration time

#### Request Example

```http
POST /oauth/token HTTP/1.1
Host: apis-sandbox.fedex.com
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id=l7xx1234567890abcdef1234567890ab
&client_secret=1234567890abcdef1234567890abcdef
```

#### Response Example

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer",
  "expires_in": 3600,
  "scope": "default"
}
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| access_token | String | JWT access token for API calls |
| token_type | String | Always "bearer" |
| expires_in | Integer | Token lifetime in seconds (3600 = 1 hour) |
| scope | String | Granted scope (typically "default") |

## Authentication Flow

### Initial Authentication

The extension follows this flow for initial authentication:

```
1. Extension Starts
   ↓
2. Check for Valid Token
   ↓
3. No Valid Token → Request New Token
   ↓
4. POST /oauth/token
   - grant_type: client_credentials
   - client_id: [API Key]
   - client_secret: [Secret Key]
   ↓
5. Receive Access Token
   ↓
6. Store Token in Memory
   ↓
7. Use Token for API Calls
```

### API Call with Authentication

Every API call includes the access token:

```http
POST /ship/v1/shipments HTTP/1.1
Host: apis-sandbox.fedex.com
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "requestedShipment": { ... }
}
```

## Token Management

### Access Token Lifecycle

#### Token Expiration

- **Lifetime**: 1 hour (3600 seconds)
- **Format**: JWT (JSON Web Token)
- **Storage**: In-memory (not persisted)
- **Renewal**: Automatic before expiration

#### Token Refresh Flow

The extension automatically refreshes tokens:

```
1. Before Each API Call
   ↓
2. Check Token Expiration
   ↓
3. If Expired or About to Expire
   ↓
4. Request New Token
   - POST /oauth/token
   - grant_type: client_credentials
   ↓
5. Update Stored Token
   ↓
6. Proceed with API Call
```

### Token Expiration Scenarios

#### Scenario 1: Normal Expiration

**Cause**: Token reaches 1-hour lifetime

**Handling**:
1. Extension detects expiration before API call
2. Automatically requests new token
3. Updates stored token
4. Proceeds with original API call

**User Impact**: None (transparent renewal)

#### Scenario 2: Invalid Token

**Cause**: Token becomes invalid (rare)

**Handling**:
1. API call returns 401 Unauthorized
2. Extension requests new token
3. Retries original API call

**User Impact**: Minimal (automatic retry)

#### Scenario 3: Authentication Failure

**Cause**: Invalid credentials or account issues

**Handling**:
1. Token request fails
2. Extension logs error
3. Returns error to user

**User Impact**: API calls fail until credentials are corrected

### Token Revocation

Tokens can be revoked in these scenarios:

1. **Credential Rotation**: When you regenerate API credentials in FedEx portal
2. **Account Suspension**: If FedEx account is suspended
3. **Security Event**: If suspicious activity is detected
4. **Manual Revocation**: If you manually revoke access in FedEx portal

**Recovery**: Extension automatically requests new token on next API call

## Security Best Practices

### Credential Security

🔒 **Protect Your Credentials**:

1. **Never Expose**: Don't log or display API Key or Secret Key
2. **Secure Storage**: Credentials stored encrypted in Krista
3. **Rotate Regularly**: Change credentials periodically
4. **Limit Access**: Restrict who can view extension configuration
5. **Monitor Usage**: Review API usage in FedEx Developer Portal

### Token Security

🔒 **Token Best Practices**:

1. **Short-Lived**: Tokens expire after 1 hour (reduces risk)
2. **In-Memory Only**: Tokens not persisted to disk
3. **HTTPS Only**: All API calls use HTTPS encryption
4. **No Sharing**: Tokens never shared between instances
5. **Automatic Renewal**: Fresh tokens reduce exposure window

### Network Security

🔒 **Communication Security**:

1. **TLS 1.2+**: All connections use modern TLS encryption
2. **Certificate Validation**: Server certificates validated
3. **No Proxies**: Direct connection to FedEx APIs (unless configured)
4. **Firewall Rules**: Allow outbound HTTPS to FedEx domains

## Troubleshooting Authentication

### Issue: "FedEx API authentication failed" Error

**Cause**: Invalid API credentials or environment mismatch

**Error Messages You'll See**:
- "FedEx API authentication failed. Your API Key or Secret Key is incorrect. Please verify your credentials in the FedEx Developer Portal and update them in the extension configuration. Make sure you're using the correct credentials for your environment (sandbox vs production)."
- `401 Unauthorized`
- `Invalid client credentials`

**Step-by-Step Resolution**:
1. **Verify Credentials in FedEx Developer Portal**:
   - Log in to developer.fedex.com
   - Navigate to your application
   - Confirm API Key and Secret Key are active
   - Check if credentials have been revoked or expired

2. **Check Environment Matching**:
   - Sandbox credentials must use Base URL: `https://apis-sandbox.fedex.com`
   - Production credentials must use Base URL: `https://apis.fedex.com`
   - Never mix sandbox credentials with production URL or vice versa

3. **Verify Configuration**:
   - Remove any extra spaces from API Key and Secret Key fields
   - Ensure Grant Type is exactly `client_credentials`
   - Save configuration after making changes

4. **Test Connection**: Use the "Test Connection" button to verify authentication

5. **Regenerate if Needed**: If issues persist, generate new credentials in the FedEx Developer Portal

### Issue: "FedEx API access denied" Error

**Cause**: Account doesn't have permission to use Ship API

**Error Messages You'll See**:
- "FedEx API access denied. Your account may not have permission to use the Ship API, or your credentials may be for a different environment. Please contact FedEx support to verify your API access permissions."
- `403 Forbidden`
- `Access denied`

**Step-by-Step Resolution**:
1. **Contact FedEx Support**: Request Ship API access for your account
2. **Verify Account Status**: Ensure your FedEx account is active and in good standing
3. **Check API Permissions**: Confirm Ship API is enabled in your FedEx Developer Portal application
4. **Account Setup**: Complete any additional account setup required for API access
5. **Wait for Activation**: API access may take time to activate after approval

### Issue: "Grant Type must be 'client_credentials'" Error

**Cause**: Grant type is not set correctly

**Error Messages You'll See**:
- "Grant Type must be 'client_credentials' for FedEx API authentication. Please update the Grant Type in your extension configuration. This is the only supported authentication method for FedEx APIs."
- `Unsupported grant type`
- `Invalid grant`

**Step-by-Step Resolution**:
1. **Set Correct Grant Type**: Enter exactly `client_credentials` (case-sensitive)
2. **Remove Extra Characters**: Ensure no spaces or special characters
3. **Save Configuration**: Click "Save" after making the change
4. **Test Connection**: Verify the change works with "Test Connection"

### Issue: "FedEx API Base URL is missing" Error

**Cause**: Base URL is not configured or incorrect

**Error Messages You'll See**:
- "FedEx API Base URL is required. Please enter the correct URL for your environment: Use 'https://apis-sandbox.fedex.com' for testing/development or 'https://apis.fedex.com' for production. Make sure the URL matches your API credentials."
- "Base URL is not configured or invalid"

**Step-by-Step Resolution**:
1. **Choose Correct URL**:
   - For testing/development: `https://apis-sandbox.fedex.com`
   - For production: `https://apis.fedex.com`
2. **Match Credentials**: Ensure URL matches your API credential environment
3. **Include HTTPS**: Always use `https://` prefix
4. **No Trailing Slash**: Don't add `/` at the end of the URL
5. **Save and Test**: Save configuration and use "Test Connection"
3. Restart extension if necessary

### Issue: "Insufficient Permissions" Error

**Cause**: FedEx account doesn't have required API access

**Error Message**: `403 Forbidden` or `Insufficient permissions`

**Resolution**:
1. Log in to FedEx Developer Portal
2. Check your project's API permissions
3. Enable required APIs (Ship, Track, etc.)
4. Wait a few minutes for permissions to propagate
5. Retry the operation

## Authentication Monitoring

### Logging

The extension logs authentication events:

- ✅ **Successful Authentication**: Token acquired successfully
- 🔄 **Token Renewal**: Token refreshed automatically
- ❌ **Authentication Failure**: Credential or permission errors
- ⚠️ **Token Expiration**: Token expired (before renewal)

### Metrics

Monitor these authentication metrics:

- **Token Acquisition Time**: Time to obtain new token
- **Token Renewal Frequency**: How often tokens are renewed
- **Authentication Failures**: Failed authentication attempts
- **API Call Success Rate**: Percentage of successful API calls

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Configure API credentials
- [Creating FedEx App](/pages/CreatingFedExApp.md) - Obtain API credentials
- [Generate Shipping Label](/pages/GenerateShippingLabel.md) - Use authenticated APIs

---

## Related Pages

- [Overview](/pages/overview.md) - Extension overview and features
- [Creating FedEx App](/pages/CreatingFedExApp.md) - Set up your FedEx developer account
- [Extension Configuration](/pages/ExtensionConfiguration.md) - Configure API credentials
- [Generate Shipping Label](/pages/GenerateShippingLabel.md) - Create shipping labels
- [Error Handling Guide](/pages/ErrorHandlingGuide.md) - Troubleshoot common issues
- [Release Notes](/pages/release_notes.md) - Version history and updates

---

**Need Help?** If you encounter authentication issues, verify your credentials in the [FedEx Developer Portal](https://developer.fedex.com/) or contact your Krista support team.

