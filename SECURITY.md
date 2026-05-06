# Security Guide for Qift

This document provides detailed security instructions for deploying Qift with a public repository.

## Overview

Qift uses Firebase as its backend. When the code is public, it's critical to properly configure Firebase security to prevent unauthorized access to your database.

## Firebase Security Configuration

### 1. Authentication Setup

**Location**: Firebase Console → Authentication

**Steps**:
1. Navigate to your Firebase project
2. Go to "Authentication" → "Sign-in method"
3. Enable "Email/Password" provider
4. Create user accounts for your restaurant staff
5. **Important**: Do not enable anonymous authentication or social providers unless needed

**Best Practices**:
- Use strong passwords for employee accounts
- Create separate accounts for different roles (e.g., cashier, manager)
- Regularly rotate employee passwords
- Disable accounts when employees leave

### 2. Firestore Security Rules

**Location**: Firebase Console → Firestore Database → Rules

The `firestore.rules` file in this repository contains production-ready security rules. Apply them as follows:

1. Open the Rules tab in Firestore
2. Copy the entire contents of `firestore.rules`
3. Paste into the rules editor
4. Click "Publish"

**What These Rules Do**:
- **Authentication Required**: All read/write operations require a signed-in user
- **Data Validation**: Ensures all required fields are present and correctly typed
- **Audit Log Protection**: Audit logs are append-only (cannot be modified or deleted)
- **User Data Isolation**: Users can only read their own user data

**Testing Rules**:
Use the Firebase Console's "Rules Playground" to test rules before publishing:
```javascript
// Test authenticated read
allow read: if request.auth != null

// Test write with valid data
allow write: if request.auth != null && request.resource.data.keys().hasAll(['token', 'email', ...])
```

### 3. App Check Configuration

**Location**: Firebase Console → App Check

**Steps**:
1. Navigate to "App Check"
2. Click "Get Started"
3. Select "Play Integrity" for Android
4. Register your app with package name: `com.example.qift`
5. Enable the provider

**What App Check Does**:
- Verifies that requests come from your authentic app
- Uses Google Play Integrity API to detect:
  - Unauthorized app installations
  - Modified/tampered apps
  - Emulators and rooted devices (configurable)

**Implementation**:
The app already includes App Check initialization in `MainActivity.kt`. No additional code changes needed.

### 4. API Key Restrictions

**Location**: Google Cloud Console → APIs & Services → Credentials

**Steps**:
1. Find your Firebase API key (starts with `AIza`)
2. Click the edit (pencil) icon
3. Under "Application restrictions":
   - Select "Android apps"
   - Add package name: `com.example.qift`
   - Add SHA-1 certificate fingerprint (see below)
4. Under "API restrictions":
   - Select "Restrict key"
   - Select only: Firebase Authentication, Firestore, App Check APIs

**Getting SHA-1 Fingerprint**:

```bash
# For debug builds
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android \
  -keypass android

# For release builds
keytool -list -v -keystore path/to/your/release.keystore \
  -alias your-alias
```

Copy the SHA-1 fingerprint (20 bytes separated by colons) and add it to the API key restrictions.

### 5. Database Indexes

**Location**: Firebase Console → Firestore Database → Indexes

Create composite indexes for common queries:
- `giftCards` collection indexed by `issueDate` (descending)
- `giftCards` collection indexed by `status` and `expiryDate`
- `auditLogs` collection indexed by `cardId` and `timestamp`

### 6. Monitoring and Alerts

**Location**: Firebase Console → Project Settings → Monitoring

**Recommended Alerts**:
- Unusual spike in read/write operations
- Failed authentication attempts
- App Check token failures
- Database size exceeding thresholds

## Local Security Practices

### 1. Never Commit Sensitive Files

The following files are in `.gitignore` and should never be committed:
- `google-services.json` (contains API keys)
- `local.properties` (contains local paths)
- Any keystore files (`.jks`, `.keystore`)

### 2. Use Separate Firebase Projects

**Development**: Use a separate Firebase project for development
- Lower security rules for easier testing
- Test data that can be deleted
- No real customer data

**Production**: Use a dedicated Firebase project for production
- Strict security rules
- Real customer data
- Monitoring and alerts enabled

### 3. Signing Configuration

**Debug Signing**: Use the default debug keystore for development
**Release Signing**: Create a dedicated release keystore

```bash
# Generate a release keystore
keytool -genkey -v -keystore release.keystore \
  -alias qift-release \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Store the keystore securely:
- Never commit it to version control
- Use a password manager for the keystore password
- Keep a backup in a secure location

### 4. ProGuard/R8

Enable code obfuscation in release builds to make reverse engineering harder:

In `app/build.gradle.kts`:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

## Security Checklist

Before making the repository public, verify:

- [ ] `google-services.json` is in `.gitignore`
- [ ] `google-services.json` is removed from git history
- [ ] Firestore security rules are applied
- [ ] Firebase Authentication is enabled
- [ ] App Check is configured
- [ ] API key has application restrictions (package name + SHA-1)
- [ ] API key has API restrictions (only Firebase APIs)
- [ ] Separate Firebase projects for dev/prod
- [ ] Release keystore is securely stored
- [ ] Code obfuscation is enabled for release builds
- [ ] Monitoring and alerts are configured

## Incident Response

If you suspect a security breach:

1. **Immediate Actions**:
   - Rotate all API keys in Google Cloud Console
   - Change all Firebase project passwords
   - Review Firestore security rules
   - Check Firebase Authentication logs

2. **Investigation**:
   - Review Firebase Console logs
   - Check for unusual database operations
   - Verify App Check token patterns
   - Audit user accounts

3. **Recovery**:
   - Restore database from backup if needed
   - Update all compromised credentials
   - Notify affected users if data was exposed
   - Document the incident for future prevention

## Additional Resources

- [Firebase Security Guide](https://firebase.google.com/docs/security)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/rules-structure)
- [App Check Documentation](https://firebase.google.com/docs/app-check)
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
