# Restaurant Testing Checklist

This checklist guides you through testing Qift in your restaurant environment before full deployment.

## Pre-Deployment Setup

### Firebase Configuration
- [ ] Firebase Authentication is enabled (Email/Password)
- [ ] Firestore security rules are applied from `firestore.rules`
- [ ] App Check is enabled with Play Integrity provider
- [ ] API key has application restrictions (package name + SHA-1)
- [ ] Employee accounts are created in Firebase Authentication
- [ ] Separate Firebase project is used (not development)

### Device Setup
- [ ] Test device has Android 7.0 (API 24) or higher
- [ ] Device has active internet connection
- [ ] Google Play Services is installed and updated
- [ ] Camera permissions are granted
- [ ] Device storage has sufficient space

### App Installation
- [ ] Debug APK is installed on test device
- [ ] App launches without crashes
- [ ] Firebase initialization completes successfully
- [ ] App Check token is generated (check Firebase Console logs)

---

## Module 1: Gift Card Issuance

### Basic Functionality
- [ ] Navigate to Issuance screen
- [ ] Email input field accepts valid email addresses
- [ ] Email validation rejects invalid formats
- [ ] Pre-set value buttons (20€, 30€, 50€, 80€, 100€) work correctly
- [ ] Custom amount input accepts decimal values
- [ ] Custom amount validation rejects negative numbers
- [ ] Custom amount validation rejects unreasonably high values

### Gift Card Creation
- [ ] "Issue Card" button creates a new gift card
- [ ] Unique token is generated (check Firestore)
- [ ] Expiry date is set to 90 days from issue date
- [ ] Initial amount equals remaining balance
- [ ] Status is set to "active"
- [ ] QR code is generated and displayed
- [ ] QR code contains only the token (not the value)

### Email Delivery
- [ ] QR code is emailed to customer
- [ ] Email contains the gift card value
- [ ] Email contains the expiry date
- [ ] Email contains the QR code image
- [ ] Email is sent from configured email address

### Error Handling
- [ ] Empty email shows error message
- [ ] Invalid email shows error message
- [ ] Zero amount shows error message
- [ ] Network error shows appropriate message
- [ ] Firebase error shows user-friendly message

---

## Module 2: QR Code Scanning & Redemption

### Camera Integration
- [ ] Navigate to Scan screen
- [ ] Camera permission is requested
- [ ] Camera preview displays correctly
- [ ] Camera focuses automatically
- [ ] Flash toggle works (if available)

### QR Code Detection
- [ ] App detects valid gift card QR codes
- [ ] App rejects invalid/unknown QR codes
- [ ] App rejects expired QR codes
- [ ] Scanning is fast and responsive
- [ ] Scan sound/vibration works (if enabled)

### Validation Display
- [ ] Valid card shows green screen
- [ ] Valid card displays remaining balance
- [ ] Valid card displays expiry date
- [ ] Invalid card shows red screen
- [ ] Invalid card shows reason (expired/not found/zero balance)
- [ ] Zero balance card shows appropriate message

### Balance Deduction
- [ ] Amount input field accepts decimal values
- [ ] Amount validation rejects negative numbers
- [ ] Amount validation rejects values exceeding balance
- [ ] Deduction updates remaining balance in real-time
- [ ] Deduction is recorded in audit log
- [ ] Transaction shows confirmation message
- [ ] Transaction timestamp is recorded

### Error Handling
- [ ] Network error shows appropriate message
- [ ] Firebase error shows user-friendly message
- [ ] Invalid token shows error message
- [ ] Expired card shows error message
- [ ] Zero balance shows error message

---

## Module 3: Admin Dashboard

### PIN Authentication
- [ ] Admin screen requires PIN entry
- [ ] Default PIN (0000) works
- [ ] Incorrect PIN shows error
- [ ] PIN input is masked (••••)
- [ ] PIN can be changed (if implemented)

### Gift Card Registry
- [ ] Registry displays all issued cards
- [ ] Cards are sorted by issue date (newest first)
- [ ] Search/filter functionality works
- [ ] Each card shows: token, email, balance, status, expiry
- [ ] Loading state shows during data fetch
- [ ] Empty state shows when no cards exist

### Card Details & Audit Log
- [ ] Clicking a card shows detailed view
- [ ] Audit log displays all transactions
- [ ] Each log entry shows: action, timestamp, device ID, user ID
- [ ] Audit log is sorted chronologically
- [ ] Audit log cannot be modified

### Manual Overrides
- [ ] Balance adjustment button exists
- [ ] Expiry date adjustment button exists
- [ ] Both buttons show "DANGER" styling
- [ ] Both buttons require confirmation dialog
- [ ] Balance update validates new value
- [ ] Expiry date update validates new date
- [ ] Manual edits are logged with device ID
- [ ] Manual edits are logged with timestamp
- [ ] Manual edits are logged with user ID

### Error Handling
- [ ] Network error shows appropriate message
- [ ] Firebase error shows user-friendly message
- [ ] Invalid balance shows error message
- [ ] Invalid date shows error message

---

## Integration Testing

### End-to-End Flow
1. **Full Gift Card Lifecycle**
   - [ ] Issue a gift card for 50€
   - [ ] Verify card appears in admin registry
   - [ ] Scan the QR code
   - [ ] Deduct 20€
   - [ ] Verify balance is now 30€
   - [ ] Deduct remaining 30€
   - [ ] Verify balance is now 0€
   - [ ] Verify audit log shows all transactions

2. **Expiry Handling**
   - [ ] Issue a gift card
   - [ ] Manually set expiry to past date (via admin)
   - [ ] Attempt to scan expired card
   - [ ] Verify it shows as expired

3. **Multiple Scans**
   - [ ] Issue a gift card
   - [ ] Scan and deduct partial amount
   - [ ] Scan again and deduct more
   - [ ] Verify balance decreases correctly
   - [ ] Verify audit log shows all scans

4. **Admin Override**
   - [ ] Issue a gift card
   - [ ] Use admin to increase balance
   - [ ] Verify audit log shows manual edit
   - [ ] Scan and verify new balance

---

## Performance Testing

### Load Testing
- [ ] Issue 10+ gift cards in quick succession
- [ ] Scan 10+ gift cards in quick succession
- [ ] Admin registry loads 50+ cards smoothly
- [ ] Audit log loads 100+ entries smoothly

### Network Conditions
- [ ] App works on strong WiFi
- [ ] App works on weak WiFi
- [ ] App works on mobile data
- [ ] App shows appropriate error when offline
- [ ] App recovers when connection restored

---

## Security Testing

### Authentication
- [ ] Unauthenticated user cannot access Firestore (check Firebase Console)
- [ ] Invalid credentials show error message
- [ ] Session timeout works (if implemented)

### Data Integrity
- [ ] QR code cannot be tampered with
- [ ] Balance cannot go negative
- [ ] Audit logs cannot be deleted
- [ ] Audit logs cannot be modified

### App Check
- [ ] App Check token is present in requests (check Firebase Console)
- [ ] Requests without valid token are rejected

---

## User Experience Testing

### Usability
- [ ] Interface is intuitive for staff
- [ ] Buttons are large enough for touch
- [ ] Text is readable in restaurant lighting
- [ ] Color contrast is sufficient
- [ ] Screens load quickly

### Error Messages
- [ ] Error messages are clear and actionable
- [ ] Error messages use restaurant-friendly language
- [ ] Success messages provide confirmation

### Accessibility
- [ ] App works with screen reader (if needed)
- [ ] Font sizes are appropriate
- [ ] Color coding is not the only indicator (use text/icons)

---

## Production Readiness

### Final Checks
- [ ] All test cases pass
- [ ] No crashes during testing
- [ ] No data loss during testing
- [ ] Staff training completed
- [ ] Backup procedures documented
- [ ] Rollback plan prepared

### Deployment
- [ ] Release APK is built
- [ ] APK is signed with release keystore
- [ ] APK is installed on production devices
- [ ] Firebase project is set to production mode
- [ ] Monitoring and alerts are enabled
- [ ] Staff have login credentials

---

## Post-Deployment Monitoring

### First Week
- [ ] Monitor Firebase usage for unusual activity
- [ ] Check for any error reports
- [ ] Gather staff feedback
- [ ] Verify all gift cards are working correctly
- [ ] Check audit logs for anomalies

### Ongoing
- [ ] Regular security audits
- [ ] Monitor database size
- [ ] Review authentication logs
- [ ] Update employee accounts as needed
- [ ] Rotate API keys periodically

---

## Issue Reporting

If you encounter issues during testing:

1. **Document the issue**:
   - Steps to reproduce
   - Expected behavior
   - Actual behavior
   - Device information
   - Firebase Console logs

2. **Check common causes**:
   - Internet connection
   - Firebase configuration
   - App Check status
   - Authentication status

3. **Escalate if needed**:
   - Check SECURITY.md for security-related issues
   - Review Firebase Console for backend issues
   - Check device logs for app crashes
