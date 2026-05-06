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

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK with API level 36
- A Firebase project with:
  - Firestore Database
  - Authentication (Email/Password)
  - Google Services configuration file (`google-services.json`)

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
   - Download `google-services.json` and place it in `app/`
   - Add your app's package name: `com.example.qift`

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