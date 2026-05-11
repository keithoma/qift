<div align="center">

# Qift

**A modern Android gift card management system for restaurants**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.0-blue.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-BOM%2033.1.2-orange.svg)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![API](https://img.shields.io/badge/Min%20SDK-24-yellow.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/Target%20SDK-36-yellow.svg)](https://developer.android.com)

</div>

---

## 📋 Overview

Qift is a single-venue Android application designed for restaurant employees to issue, scan, and manage digital gift cards. The system uses a centralized cloud database (Firebase) for real-time verification to prevent fraud and double-spending.

### Key Features

- **🎫 Gift Card Issuance** - Create and email digital gift cards with unique QR codes
- **📱 QR Code Scanning** - Integrated camera scanner for quick validation
- **💰 Balance Management** - Real-time balance tracking and deduction
- **🔒 Admin Dashboard** - PIN-protected management interface with audit logs
- **📧 Email Delivery** - Automatic QR code delivery to customers
- **☁️ Cloud Sync** - Firebase-powered real-time database
- **🛡️ Security** - Token-based QR codes to prevent tampering

---

## 🚀 Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material3
- **Architecture**: MVVM with Navigation Compose
- **Backend**: Firebase (Firestore, Authentication)
- **Camera**: CameraX with ML Kit Barcode Scanning
- **Build System**: Gradle with Kotlin DSL
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14)

---

## 📸 Screenshots

| Issuance | Scanning | Admin Dashboard |
|----------|----------|-----------------|
| [Issuance] | [Scanning] | [Admin] |

*(Screenshots to be added)*

---

## 🛠️ Prerequisites

- Android Studio (latest stable recommended)
- JDK 11 (configured via `org.gradle.java.home`; update if you use a different JDK)
- Android SDK Platform 36 (API 36, extension level 1)
- A Firebase project with:
  - Firestore Database
  - Authentication (Email/Password)
  - Google Services configuration file (`google-services.json`) for `com.example.qift`

---

## 📦 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/keithoma/qift.git
   cd qift
   ```

2. **Configure Firebase**
   - Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
   - Enable Firestore Database and Authentication
   - Download `google-services.json` for Android and place it in `app/` (replace any existing file)
   - Use the package name `com.example.qift` (or update `applicationId` in `app/build.gradle.kts`)

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run on emulator or device**
   ```bash
   ./gradlew installDebug
   ```

---

## 🎯 Usage

### Issuing Gift Cards

1. Navigate to the **Issuance** screen
2. Enter the customer's email address
3. Select a pre-set value (20€, 30€, 50€, 80€, 100€) or enter a custom amount
4. Tap **Issue Card** to generate a unique QR code
5. The QR code will be automatically emailed to the customer
6. Gift cards expire automatically after 90 days

### Scanning & Redemption

1. Navigate to the **Scan** screen
2. Point the camera at the customer's QR code
3. The app will display:
   - **Green**: Valid card with remaining balance
   - **Red**: Invalid card (expired, not found, or zero balance)
4. Enter the amount to deduct from the bill
5. Confirm the transaction to update the balance

### Admin Dashboard

1. Access the **Admin** screen (default PIN: `0000`)
2. View all issued gift cards in the registry
3. Click on any card to view its full audit log
4. Use manual override buttons to adjust balance or expiry (requires confirmation)
5. All admin actions are logged with device ID and timestamp

---

## 🏗️ Architecture

```
app/
├── src/main/java/com/example/qift/
│   ├── MainActivity.kt              # Entry point
│   ├── navigation/                  # Navigation graph
│   ├── ui/                          # Compose screens
│   │   ├── issuance/               # Gift card issuance
│   │   ├── scan/                   # QR code scanning
│   │   └── admin/                  # Admin dashboard
│   ├── data/                       # Data layer
│   │   ├── model/                  # Data models
│   │   ├── repository/             # Repository pattern
│   │   └── service/                # Firebase services
│   └── util/                       # Utilities & helpers
```

---

## 🔐 Security Features

- **Token-based QR Codes**: QR codes contain only unique tokens, not values
- **Real-time Verification**: All operations require internet connection
- **Balance Guard**: Prevents overdraft by blocking invalid deductions
- **Audit Logging**: Complete transaction history for accountability
- **Admin Protection**: PIN-gated access with confirmation dialogs
- **Device Tracking**: Logs device ID for all manual edits
- **Firebase Authentication**: All database operations require authenticated users
- **App Check**: Uses Play Integrity API to verify app authenticity
- **Firestore Security Rules**: Server-side validation for all data operations

---

## 🔒 Security Setup

This project includes security measures to protect your Firebase database when the code is public. Follow these steps to configure security:

### 1. Firebase Console Configuration

#### Enable Authentication
1. Go to Firebase Console → Authentication
2. Click "Get Started"
3. Enable "Email/Password" sign-in provider
4. Create employee accounts for your staff

#### Configure Firestore Security Rules
1. Go to Firebase Console → Firestore Database → Rules
2. Copy the contents of `firestore.rules` from this repository
3. Paste and publish the rules
4. These rules ensure:
   - Only authenticated users can read/write data
   - Data validation for all fields
   - Audit logs are append-only (no updates/deletes)

#### Enable App Check
1. Go to Firebase Console → App Check
2. Register your Android app with package name `com.example.qift`
3. Enable Play Integrity provider
4. Copy the App Check token and add it to your Firebase project settings

#### Restrict API Key
1. Go to Google Cloud Console → APIs & Services → Credentials
2. Find your Firebase API key
3. Under "Application restrictions", set:
   - Package name: `com.example.qift`
   - SHA-1 certificate fingerprint (from your signing certificate)
4. Under "API restrictions", select only the Firebase APIs you use

### 2. Local Configuration

#### google-services.json
- The `google-services.json` file is **NOT** included in this repository
- Download it from Firebase Console after creating your project
- Place it in `app/` directory
- It's already in `.gitignore` to prevent accidental commits

#### SHA-1 Fingerprint
To get your SHA-1 fingerprint for API key restrictions:
```bash
# Debug key
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Release key (if you have one)
keytool -list -v -keystore path/to/your/keystore.jks -alias your-alias
```

### 3. Important Security Notes

- **Never commit** `google-services.json` or any API keys
- **Always use** Firebase Authentication for all database operations
- **Enable App Check** in production to prevent unauthorized app usage
- **Monitor** Firebase usage and set up alerts for suspicious activity
- **Rotate** API keys periodically if needed
- **Use** different Firebase projects for development and production

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Kei Thoma**

- GitHub: [@keithoma](https://github.com/keithoma)

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 Requirements

For detailed product requirements, see [REQUIREMENT.md](REQUIREMENT.md).

---

<div align="center">

Made with ❤️ for restaurants

</div>