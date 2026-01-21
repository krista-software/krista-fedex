# FedEx Extension - Quick Start Guide

## 🚀 Get Started in 5 Minutes

This quick start guide will help you generate your first FedEx shipping label in just a few minutes.

## Prerequisites

- [ ] FedEx account
- [ ] Access to FedEx Developer Portal
- [ ] Krista platform access

## Step 1: Get FedEx API Credentials (5 minutes)

1. Go to [FedEx Developer Portal](https://developer.fedex.com/)
2. Sign in or create account
3. Create a new project
4. Select "Ship API"
5. Generate **Sandbox** credentials
6. Copy your **API Key** and **Secret Key**

> **💡 Tip**: Start with Sandbox for testing. It's free and doesn't create real shipments.

## Step 2: Configure Extension (2 minutes)

1. Open Krista Extensions
2. Find "FedEx Extension"
3. Click "Configure"
4. Enter:
   ```
   Base URL: https://apis-sandbox.fedex.com
   API Key: [Your API Key]
   Secret Key: [Your Secret Key]
   Grant Type: client_credentials
   ```
5. Click "Save"
6. Click "Test Connection" → Should see ✅ Success

## Step 3: Generate Your First Label (3 minutes)

Use the **Generate Shipping Label** catalog request with this minimal data:

### Required Fields

```
Ticket Id: "TEST-001"
Account Number: "123456789"  (your FedEx account number)
Service Type: "FEDEX_GROUND"
Package Weight: 5.0

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

### Expected Result

```json
{
  "Success": true,
  "MasterTrackingNumber": "794947717776",
  "LabelFile": [PDF File],
  "DeliveryDate": "2025-10-13"
}
```

## Step 4: Print and Ship

1. Download the `LabelFile` PDF
2. Print the label
3. Attach to your package
4. Ship via FedEx

## 🎉 Congratulations!

You've successfully generated your first FedEx shipping label!

## Next Steps

### Learn More

- **[Full Documentation](README.md)** - Complete guide
- **[Configuration Details](pages/ExtensionConfiguration.md)** - Advanced setup
- **[All Parameters](pages/GenerateShippingLabel.md)** - Complete field reference

### Go to Production

When ready for real shipments:

1. Generate **Production** credentials in FedEx portal
2. Update extension configuration:
   ```
   Base URL: https://apis.fedex.com
   API Key: [Production API Key]
   Secret Key: [Production Secret Key]
   ```
3. Test with a real shipment
4. Start shipping!

> **⚠️ Warning**: Production creates real shipments and charges your account.

## Common Service Types

| Service Type | Delivery Time | Use Case |
|--------------|---------------|----------|
| `FEDEX_GROUND` | 1-5 business days | Most economical |
| `STANDARD_OVERNIGHT` | Next day by 3 PM | Fast delivery |
| `FEDEX_2_DAY` | 2 business days | Balanced cost/speed |
| `FEDEX_EXPRESS_SAVER` | 3 business days | Economy express |

## Troubleshooting

### "Account number is required"
→ Make sure Account Number field is filled

### "Authentication Failed"
→ Check API Key and Secret Key are correct

### "Service type not available"
→ Try `FEDEX_GROUND` instead

### "Address validation failed"
→ Verify all address fields are complete

## Need Help?

- 📖 [Full Documentation](README.md)
- 🔧 [Troubleshooting Guide](pages/GenerateShippingLabel.md#troubleshooting)
- 💬 Contact Krista Support

---

**Ready to dive deeper?** Check out the [complete documentation](README.md) for advanced features and best practices.

