package com.app.sfe;

/**
 * Enumeration of different input types for secure fields.
 * Each type has specific validation and security requirements.
 */
public enum SFEInputType {
    CARD_NUMBER,      // Credit/Debit card number
    CVV,              // Card verification value
    EXPIRY_DATE,      // Card expiry date
    ACCOUNT_NUMBER,   // Bank account number
    IFSC_CODE,        // Indian Financial System Code
    UPI_ID,           // Unified Payment Interface ID
    PIN,              // Personal Identification Number
    OTP,              // One-Time Password
    PASSWORD          // Password field
}