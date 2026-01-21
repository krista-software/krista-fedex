# Creating FedEx App

## Overview

To use the FedEx Extension, you need to create a project in the FedEx Developer Portal and obtain API credentials (API Key and Secret Key). This guide walks you through the complete process of setting up your FedEx developer account and generating the necessary credentials.

## Prerequisites

Before you begin, ensure you have:

- ✅ Active FedEx account (personal or business)
- ✅ Valid email address for developer account
- ✅ FedEx account number (for production access)
- ✅ Web browser with internet access

> **📝 Note**: You can create a developer account even if you don't have a FedEx shipping account, but you'll be limited to sandbox (testing) environment.

## Step-by-Step Instructions

### Step 1: Access FedEx Developer Portal

1. Open your web browser
2. Navigate to [https://developer.fedex.com/](https://developer.fedex.com/)
3. You'll see the FedEx Developer Portal homepage

![FedEx Developer Portal Homepage]

> **💡 Tip**: Bookmark this page for easy access in the future.

### Step 2: Create Developer Account

If you don't already have a developer account:

1. Click **"Sign Up"** or **"Register"** button in the top-right corner
2. Fill in the registration form:
   - **Email Address**: Your business or personal email
   - **Password**: Strong password (min 8 characters)
   - **First Name**: Your first name
   - **Last Name**: Your last name
   - **Company Name**: Your company or organization name
   - **Country**: Your country
3. Accept the Terms of Service
4. Click **"Create Account"** or **"Register"**
5. Check your email for verification link
6. Click the verification link to activate your account

### Step 3: Sign In to Developer Portal

1. Return to [https://developer.fedex.com/](https://developer.fedex.com/)
2. Click **"Sign In"** button
3. Enter your email and password
4. Click **"Sign In"**

### Step 4: Create a New Project

Once signed in:

1. Navigate to **"My Projects"** or **"Projects"** section
2. Click **"Create Project"** or **"New Project"** button
3. Fill in the project details:
   - **Project Name**: Descriptive name (e.g., "Krista FedEx Integration")
   - **Project Description**: Brief description of your integration
   - **Organization**: Select your organization (if applicable)
4. Click **"Create"** or **"Save"**


> **📝 Note**: You can create multiple projects for different environments or use cases.

### Step 5: Select APIs

After creating the project, select which FedEx APIs you need:

1. In your project, click **"Add API"** or **"Select APIs"**
2. Choose the APIs you need:
   - ✅ **Ship API** - Required for generating shipping labels
   - ⬜ **Track API** - Optional for tracking shipments
   - ⬜ **Rate API** - Optional for getting rate quotes
   - ⬜ **Address Validation API** - Optional for validating addresses
   - ⬜ **Pickup API** - Optional for scheduling pickups
3. Click **"Add Selected APIs"** or **"Save"**

> **💡 Tip**: Start with Ship API only. You can add more APIs later as needed.

### Step 6: Generate API Credentials

Now generate your API credentials:

1. In your project, navigate to **"Credentials"** or **"API Keys"** section
2. You'll see two environments:
   - **Test (Sandbox)**: For development and testing
   - **Production**: For live shipments

#### Generate Sandbox Credentials

1. Under **"Test"** or **"Sandbox"** section:
2. Click **"Create Credentials"** or **"Generate Key"**
3. Your credentials will be displayed:
   - **API Key** (also called Client ID)
   - **Secret Key** (also called Client Secret)
4. **IMPORTANT**: Copy both values immediately and store them securely
5. Click **"I have saved my credentials"** or similar confirmation

> **⚠️ Warning**: The Secret Key is shown only once. If you lose it, you'll need to regenerate credentials.

#### Generate Production Credentials (Optional)

For production access:

1. Under **"Production"** section:
2. You may need to:
   - Link your FedEx account number
   - Complete additional verification
   - Accept production terms
3. Click **"Create Credentials"** or **"Generate Key"**
4. Copy and securely store your production credentials


> **📝 Note**: Production credentials may require FedEx account verification and approval.

### Step 7: Note Your Credentials

Record your credentials in a secure location:

**Sandbox Environment**:
```
Base URL: https://apis-sandbox.fedex.com
API Key: l7xx1234567890abcdef1234567890ab
Secret Key: 1234567890abcdef1234567890abcdef
Grant Type: client_credentials
```

**Production Environment** (when ready):
```
Base URL: https://apis.fedex.com
API Key: l7xxPROD1234567890abcdef1234567890ab
Secret Key: PROD1234567890abcdef1234567890abcdef
Grant Type: client_credentials
```

> **🔒 Security**: Store credentials in a password manager or secure vault. Never commit them to version control.

### Step 8: Configure Extension in Krista

Now use your credentials to configure the FedEx Extension:

1. Navigate to Extensions in Krista
2. Find the FedEx Extension
3. Click **"Configure"** or **"Settings"**
4. Enter your credentials:
   - **Base URL**: `https://apis-sandbox.fedex.com` (for testing)
   - **API Key**: Your API Key from Step 6
   - **Secret Key**: Your Secret Key from Step 6
   - **Grant Type**: `client_credentials`
5. Click **"Save"**
6. Click **"Test Connection"** to verify


## Verification

### Verify Setup is Correct

After configuration, verify everything is working:

1. **Test Connection**: Click "Test Connection" in Krista extension settings
   - ✅ Expected: "Connection successful" message
   - ❌ If failed: Review credentials and try again

2. **Check Developer Portal**: Log in to FedEx Developer Portal
   - Verify project is active
   - Verify APIs are enabled
   - Check for any error messages

3. **Test API Call**: Try generating a test shipping label
   - Use sandbox environment
   - Provide valid test data
   - Verify label is generated successfully

### Common Verification Issues

#### Issue: "Invalid API Key"

**Cause**: API Key copied incorrectly or from wrong environment

**Resolution**:
1. Return to FedEx Developer Portal
2. Verify you're looking at correct environment (sandbox vs. production)
3. Copy API Key again (ensure no extra spaces)
4. Update in Krista configuration

#### Issue: "Authentication Failed"

**Cause**: Secret Key incorrect or credentials expired

**Resolution**:
1. Verify Secret Key is correct
2. If lost, regenerate credentials in FedEx portal
3. Update both API Key and Secret Key in Krista

#### Issue: "API Not Enabled"

**Cause**: Required API not added to project

**Resolution**:
1. Go to FedEx Developer Portal
2. Open your project
3. Add Ship API to project
4. Wait a few minutes for changes to propagate

## Next Steps

After successfully creating your FedEx app and obtaining credentials:

1. **Configure Extension**: Complete [Extension Configuration](ExtensionConfiguration)
2. **Understand Authentication**: Review [Authentication](Authentication) flow
3. **Generate First Label**: Try [Generate Shipping Label](GenerateShippingLabel)
4. **Test Thoroughly**: Validate all use cases in sandbox
5. **Go to Production**: When ready, use production credentials

## Managing Your FedEx App

### Viewing Credentials

To view your credentials later:

1. Sign in to [FedEx Developer Portal](https://developer.fedex.com/)
2. Navigate to **"My Projects"**
3. Select your project
4. Go to **"Credentials"** section
5. View API Key (Secret Key not shown after initial generation)

### Regenerating Credentials

If you need to regenerate credentials:

1. In your project's Credentials section
2. Click **"Regenerate"** or **"Revoke and Create New"**
3. Confirm the action
4. Copy new credentials immediately
5. Update credentials in Krista configuration

> **⚠️ Warning**: Regenerating credentials invalidates old credentials immediately. Update all systems using the old credentials.

### Rotating Credentials

For security, rotate credentials periodically:

1. Generate new credentials in FedEx portal
2. Update Krista configuration with new credentials
3. Test connection with new credentials
4. Revoke old credentials in FedEx portal

**Recommended Rotation Schedule**:
- **Development**: Every 6 months
- **Production**: Every 3 months or per security policy

### Monitoring API Usage

Monitor your API usage in FedEx Developer Portal:

1. Navigate to your project
2. Go to **"Analytics"** or **"Usage"** section
3. Review:
   - API call volume
   - Success/failure rates
   - Response times
   - Error types

## Troubleshooting

### Issue: Can't Create Developer Account

**Cause**: Email already registered or validation issues

**Resolution**:
1. Try password reset if account exists
2. Use different email address
3. Contact FedEx Developer Support

### Issue: Can't Create Project

**Cause**: Account not fully activated or permissions issue

**Resolution**:
1. Verify email is confirmed
2. Complete account profile
3. Wait 24 hours after registration
4. Contact FedEx Developer Support

### Issue: Production Credentials Not Available

**Cause**: FedEx account not linked or not verified

**Resolution**:
1. Link your FedEx shipping account number
2. Complete business verification
3. Wait for FedEx approval (may take 1-2 business days)
4. Contact FedEx support if delayed

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Configure the extension with your credentials
- [Authentication](/pages/Authentication.md) - Understand OAuth 2.0 authentication
- [FedEx Developer Portal](https://developer.fedex.com/) - Official FedEx developer resources
- [FedEx Developer Support](https://developer.fedex.com/support) - Get help from FedEx

---

## Related Pages

- [Overview](/pages/overview.md) - Extension overview and features
- [Extension Configuration](/pages/ExtensionConfiguration.md) - Configure API credentials
- [Authentication](/pages/Authentication.md) - OAuth 2.0 authentication details
- [Generate Shipping Label](/pages/GenerateShippingLabel.md) - Create shipping labels
- [Error Handling Guide](/pages/ErrorHandlingGuide.md) - Troubleshoot common issues
- [Release Notes](/pages/release_notes.md) - Version history and updates

---

**Need Help?** If you encounter issues creating your FedEx app, contact [FedEx Developer Support](https://developer.fedex.com/support) or your Krista support team.

