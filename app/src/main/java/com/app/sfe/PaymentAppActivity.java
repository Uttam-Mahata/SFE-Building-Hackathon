package com.app.sfe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.app.sfe.security.SFEConfiguration;
import com.app.sfe.security.SFEInitResult;
import com.app.sfe.security.SFEProviderType;
import com.app.sfe.security.SFESecurityLevel;
import com.app.sfe.security.SFETransactionResult;
import com.app.sfe.security.SecureFinancialEnvironment;

import java.util.Arrays;
import java.util.List;

public class PaymentAppActivity extends AppCompatActivity {

    // UI Components
    private CardView cardSecurityStatus;
    private ImageView ivSecurityIcon;
    private TextView tvSecurityStatus;
    private EditText etRecipientName;
    private EditText etRecipientNumber;
    private EditText etAmount;
    private EditText etDescription;
    private Button btnPay;
    private ProgressBar progressBar;
    private TextView tvSdkVersion;

    // SFE SDK Initialization Status
    private boolean sfeInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_app);

        // Initialize UI components
        cardSecurityStatus = findViewById(R.id.card_security_status);
        ivSecurityIcon = findViewById(R.id.iv_security_icon);
        tvSecurityStatus = findViewById(R.id.tv_security_status);
        etRecipientName = findViewById(R.id.et_recipient_name);
        etRecipientNumber = findViewById(R.id.et_recipient_number);
        etAmount = findViewById(R.id.et_amount);
        etDescription = findViewById(R.id.et_description);
        btnPay = findViewById(R.id.btn_pay);
        progressBar = findViewById(R.id.progress_bar);
        tvSdkVersion = findViewById(R.id.tv_sdk_version);

        // Set SDK version information
        tvSdkVersion.setText("SFE SDK v1.0.5");

        // Initialize listeners
        btnPay.setOnClickListener(v -> handlePaymentClick());

        // Initialize SFE SDK
        initializeSfeSDK();
    }

    private void initializeSfeSDK() {
        showLoading(true);
        updateSecurityStatus("Initializing security...", false);

        // Define allowed API endpoints for this fintech app
        List<String> allowedAPIs = Arrays.asList(
            "https://api.fintechapp.com/payments/process",
            "https://api.fintechapp.com/accounts/verify",
            "https://api.fintechapp.com/transactions/history"
        );

        // Initialize SFE with fintech-specific configuration
        SFEConfiguration config = new SFEConfiguration.Builder()
            .setAppId("fintech-payment-app-id")
            .setProviderType(SFEProviderType.FINTECH)
            .setCustomSecurityLevel(SFESecurityLevel.HIGH)
            .setAllowedAPIs(allowedAPIs)
            .setAnalyticsEnabled(true)
            .setTransactionTimeout(15000) // 15 seconds
            .build();

        // Initialize the SFE SDK
        SecureFinancialEnvironment.initialize(this, config, this::onSfeInitialized);
    }

    private void onSfeInitialized(SFEInitResult result) {
        runOnUiThread(() -> {
            showLoading(false);

            if (result.isSuccess()) {
                sfeInitialized = true;
                updateSecurityStatus("Secure Environment Active", true);
                btnPay.setEnabled(true);
            } else {
                sfeInitialized = false;
                updateSecurityStatus("Security Initialization Failed", false);
                showSecurityAlert(result.getErrorReason(), result.getErrorCode());
            }
        });
    }

    private void handlePaymentClick() {
        // Validate input fields
        if (!validateInputFields()) {
            return;
        }

        // Check if SFE is initialized
        if (!sfeInitialized) {
            Toast.makeText(this, "Secure environment not initialized. Please restart the app.", 
                          Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify secure environment before proceeding
        if (!SecureFinancialEnvironment.verifySecureEnvironment()) {
            showSecurityAlert("Secure environment verification failed. " +
                             "Your device may be compromised.", 3001);
            return;
        }

        // Proceed with payment
        processPayment();
    }

    private boolean validateInputFields() {
        boolean isValid = true;

        if (etRecipientName.getText().toString().trim().isEmpty()) {
            etRecipientName.setError("Recipient name is required");
            isValid = false;
        }

        if (etRecipientNumber.getText().toString().trim().isEmpty()) {
            etRecipientNumber.setError("Account number is required");
            isValid = false;
        }

        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            etAmount.setError("Amount is required");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    etAmount.setError("Amount must be greater than zero");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etAmount.setError("Invalid amount format");
                isValid = false;
            }
        }

        return isValid;
    }

    private void processPayment() {
        showLoading(true);
        updateSecurityStatus("Processing secure payment...", true);

        // Collect payment data from UI
        PaymentData paymentData = new PaymentData(
            etRecipientName.getText().toString(),
            etRecipientNumber.getText().toString(),
            Double.parseDouble(etAmount.getText().toString()),
            etDescription.getText().toString()
        );

        // Use the SFE SDK to process the payment securely
        SecureFinancialEnvironment.getSecureCommunication()
            .sendTransaction(
                "https://api.fintechapp.com/payments/process",
                paymentData,
                15000, // 15 seconds timeout
                this::onPaymentProcessed
            );
    }

    private void onPaymentProcessed(SFETransactionResult<?> result) {
        runOnUiThread(() -> {
            showLoading(false);

            if (result.isSuccess()) {
                updateSecurityStatus("Secure Environment Active", true);
                showPaymentSuccessDialog();
                clearInputFields();
            } else {
                updateSecurityStatus("Secure Environment Active", true);
                showPaymentFailureDialog(result.getErrorMessage(), result.getErrorCode());
            }
        });
    }

    private void showPaymentSuccessDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Payment Successful")
            .setMessage("Your payment of ₹" + etAmount.getText().toString() + 
                       " to " + etRecipientName.getText().toString() + " was successful.")
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_info)
            .show();
    }

    private void showPaymentFailureDialog(String message, int errorCode) {
        new AlertDialog.Builder(this)
            .setTitle("Payment Failed")
            .setMessage("Error " + errorCode + ": " + message)
            .setPositiveButton("Try Again", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void showSecurityAlert(String message, int errorCode) {
        new AlertDialog.Builder(this)
            .setTitle("Security Warning")
            .setMessage("Error " + errorCode + ": " + message)
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void updateSecurityStatus(String status, boolean isSecure) {
        tvSecurityStatus.setText(status);
        if (isSecure) {
            cardSecurityStatus.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light, null));
            ivSecurityIcon.setImageResource(android.R.drawable.ic_lock_lock);
        } else {
            cardSecurityStatus.setCardBackgroundColor(getResources().getColor(android.R.color.holo_orange_light, null));
            ivSecurityIcon.setImageResource(android.R.drawable.ic_lock_idle_lock);
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnPay.setEnabled(!isLoading && sfeInitialized);
    }

    private void clearInputFields() {
        etRecipientName.setText("");
        etRecipientNumber.setText("");
        etAmount.setText("");
        etDescription.setText("");
    }

    /**
     * Data class for payment information
     */
    static class PaymentData {
        private final String recipientName;
        private final String recipientAccount;
        private final double amount;
        private final String description;

        PaymentData(String recipientName, String recipientAccount, 
                   double amount, String description) {
            this.recipientName = recipientName;
            this.recipientAccount = recipientAccount;
            this.amount = amount;
            this.description = description;
        }

        // Getters
        public String getRecipientName() { return recipientName; }
        public String getRecipientAccount() { return recipientAccount; }
        public double getAmount() { return amount; }
        public String getDescription() { return description; }
    }
}