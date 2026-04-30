# Product Requirements Document: Restaurant Gift Card Manager

## Project Overview
A single-venue Android application for employees to issue, scan, and manage digital gift cards. The system uses a centralized cloud database for real-time verification to prevent fraud and double-spending.

## 1. Core Constraints & Scope
*   **Zero-Cost Mandate:** The solution must utilize free-tier services only (e.g., Firebase Spark Plan). No paid subscriptions or paid API keys (like SendGrid or Twilio) should be required.
*   **Payment Out-of-Scope:** The app **does not** process actual monetary transactions. It is a ledger tool. It assumes the customer has already paid the restaurant via cash or existing POS before the employee interacts with this app.
*   **Email Delivery:** Use a cost-free method for sending QR codes (e.g., using a dedicated Gmail account via SMTP or Firebase Cloud Functions within free limits).

## 2. Issuance Module (Employee Access)
*   **Purpose:** Create and email a digital gift card record once external payment is confirmed.
*   **User Interface:**
    *   Input field for **Customer Email**.
    *   Pre-set value buttons: **20 €, 30 €, 50 €, 80 €, 100 €**.
    *   **Custom Amount** input field for non-standard values.
*   **System Logic:**
    *   **Tokenization:** Generate a unique, non-predictable Transaction Token.
    *   **Automation:** Set Expiry Date automatically to **90 days** from the current date.
    *   **QR Security:** The QR code must contain only the **Unique Token** (not the value) to prevent tampering.
*   **Database Output:**
    *   Create record: `Token`, `Email`, `InitialAmount`, `RemainingBalance`, `IssueDate`, `ExpiryDate`, and `Status (Active)`.

## 3. Validation & Redemption Module (Scanning)
*   **Purpose:** Scan codes to verify balance and deduct funds from a bill.
*   **User Interface:**
    *   Integrated camera scanner.
    *   High-contrast status screens: **Green (Valid)** or **Red (Invalid + Reason)**.
*   **System Logic:**
    1.  **Verification:** Lookup token in database. Reject if token is missing, expired, or balance is 0.
    2.  **Transaction:**
        *   Display current remaining balance clearly.
        *   Input field for **Amount to Deduct**.
        *   **Balance Guard:** If `DeductionAmount` > `RemainingBalance`, block the transaction and show an error.
    3.  **Update:** Record the deduction and timestamp in the database ledger.

## 4. Admin Management Dashboard
*   **Security:** Gatekept by a simple **4-digit PIN**.
*   **Features:**
    *   **Gift Card Registry:** A searchable list of all issued cards, sorted by date.
    *   **Detailed View:** Shows a full **Audit Log** for the specific card (Every scan and deduction).
    *   **The Danger Zone:**
        *   Manual override buttons for **Balance** and **Expiry Date**.
        *   **UI Requirement:** Must be styled with "DANGER" notices and require a secondary confirmation dialog before saving.
        *   **Accountability:** Log the Device ID and timestamp for every manual admin edit.

## 5. Technical Constraints
*   **Platform:** Android mobile.
*   **Connectivity:** Requires an active internet connection for all operations (to ensure real-time balance integrity).
*   **Units & Formatting:** 
    *   Use **Metric units** for all measurements (if applicable).
    *   Currency must always display the **€** symbol.
    *   Amounts must be stored and displayed with standard decimal formatting (e.g., 50.00 €).