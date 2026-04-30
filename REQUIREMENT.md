# Product Requirements Document: Restaurant Gift Card Manager
## Project Overview
A single-venue Android application for employees to issue, scan, and manage digital gift cards. The system relies on a central database to prevent fraud and ensure real-time balance updates.

## 1. Issuance Module (Employee Access)
**Purpose:** Create and email new gift cards after a successful payment.

**User Interface:**
- Input field for Customer Email.
- Pre-set value buttons: 20 €, 30 €, 50 €, 80 €, 100 €.
- Custom Amount input field for non-standard values.

**System Logic:**
- Generate a unique, non-predictable Transaction Token.
- Set Expiry Date automatically to 90 days from the current date.
- The QR code must contain only the Unique Token (not the value) to prevent tampering.

**Output:**
- Save record to database: Token, Email, InitialAmount, RemainingBalance, IssueDate, ExpiryDate, and Status (Active).
- Trigger an automated email to the customer with the QR code.

## 2. Validation & Redemption Module (Scanning)
**Purpose:** Scan physical/digital QR codes to deduct funds from a bill.

**User Interface:**
- Integrated camera scanner.
- High-contrast status screens: Green (Valid) or Red (Invalid + Reason).

**System Logic:**
- Lookup: Fetch token data from the database.
- Validation Check: Reject if:
  - Token does not exist.
  - Current Date > Expiry Date.
  - Remaining Balance = 0.

**Transaction:**
- Display current remaining balance.
- Input field for Amount to Deduct.
- Strict Logic: If DeductionAmount > RemainingBalance, block transaction.
- Update: Record the deduction and timestamp in the database.

## 3. Admin Management Dashboard
**Security:** This section is locked behind a 4-digit PIN.

**Features:**
- Gift Card Registry: A searchable list showing all cards, sorted by date issued.
- Detailed View: Upon selection, show a full Audit Log (e.g., "01/05/2026: 25 € deducted").
- The Danger Zone:
  - An Edit Button allowing manual override of Balance and Expiry Date.
  - UI Requirement: Plastered with "DANGER" notices and requires a secondary confirmation dialog before saving changes.
  - Accountability: Every manual edit or issuance must be logged with a timestamp and the Device ID to monitor internal use.

## 4. Technical Constraints
- Platform: Android (Optimized for mobile camera use).
- Data Integrity: The app must require an internet connection to verify balances (no local-only validation to prevent double-spending).
- Currency Format: All financial displays must use the € symbol and metric-standard decimal formatting.