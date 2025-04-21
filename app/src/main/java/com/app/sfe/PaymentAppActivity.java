package com.app.sfe;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.app.sfe.security.SecureFinancialEnvironment;
import com.app.sfe.security.SFETransactionResult;

import java.util.List;

public class PaymentAppActivity extends AppCompatActivity {

    // UI Components
    private CardView cardSecurityStatus;
    private ImageView ivSecurityIcon;
    private TextView tvSecurityStatus;
    private Button btnPay;
    private ProgressBar progressBar;
    private TextView tvSdkVersion;
    
    // SFE Components
    private SFESecureContainer secureContainer;
    private SFESecureTextField cardNumberField;
    private SFESecureTextField cvvField;
    private SFESecureTextField expiryDateField;
    private EditText etRecipientName;
    private EditText etAmount;
    private EditText etDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_app);

        // Initialize UI components
        cardSecurityStatus = findViewById(R.id.card_security_status);
        ivSecurityIcon = findViewById(R.id.iv_security_icon);
        tvSecurityStatus = findViewById(R.id.tv_security_status);
        btnPay = findViewById(R.id.btn_pay);
        progressBar = findViewById(R.id.progress_bar);
        tvSdkVersion = findViewById(R.id.tv_sdk_version);
        etRecipientName = findViewById(R.id.et_recipient_name);
        etAmount = findViewById(R.id.et_amount);
        etDescription = findViewById(R.id.et_description);

        // Set SDK version information
        tvSdkVersion.setText("SFE SDK v1.5.0");

        // Initialize secure container
        initializeSecureContainer();

        // Initialize listeners
        btnPay.setOnClickListener(v -> handlePaymentClick());
        
        // Perform device security check
        performSecurityCheck();
    }

    private void initializeSecureContainer() {
        // Create a secure container for handling sensitive UI components
        secureContainer = SFEManager.createSecureContainer(this);
        
        // Get the container view from layout
        FrameLayout containerLayout = findViewById(R.id.secure_container_layout);
        containerLayout.addView(secureContainer.getRootView());
        
        // Set up secure payment fields
        setupSecurePaymentFields();
    }
    
    private void setupSecurePaymentFields() {
        // Create card number field
        cardNumberField = secureContainer.createSecureTextField(
            SFEInputType.CARD_NUMBER, 
            new SFETextFieldConfig.Builder()
                .setHint("Card Number")
                .setMaxLength(16)
                .build()
        );
        
        // Create CVV field
        cvvField = secureContainer.createSecureTextField(
            SFEInputType.CVV, 
            new SFETextFieldConfig.Builder()
                .setHint("CVV")
                .setMaxLength(3)
                .setMaskInput(true)
                .build()
        );
        
        // Create expiry date field
        expiryDateField = secureContainer.createSecureTextField(
            SFEInputType.EXPIRY_DATE, 
            new SFETextFieldConfig.Builder()
                .setHint("MM/YY")
                .setMaxLength(5)
                .build()
        );
        
        // Add fields to the container
        ViewGroup cardNumberContainer = findViewById(R.id.card_number_container);
        ViewGroup cvvContainer = findViewById(R.id.cvv_container);
        ViewGroup expiryDateContainer = findViewById(R.id.expiry_date_container);
        
        cardNumberContainer.addView(cardNumberField.getView());
        cvvContainer.addView(cvvField.getView());
        expiryDateContainer.addView(expiryDateField.getView());
    }

    private void performSecurityCheck() {
        updateSecurityStatus("Checking device security...", false);
        
        SFEDeviceSecurity deviceSecurity = SFEManager.getDeviceSecurity();
        deviceSecurity.performSecurityCheck(result -> {
            if (result.isDeviceSecure()) {
                updateSecurityStatus("Device security verified", true);
            } else {
                List<SFESecurityRisk> risks = result.getSecurityRisks();
                showSecurityRisks(risks);
                
                // Allow payments only if there are no high risks
                btnPay.setEnabled(!result.hasHighRisks());
                
                if (result.hasHighRisks()) {
                    updateSecurityStatus("Security risk detected", false);
                } else {
                    updateSecurityStatus("Minor security warnings", false);
                }
            }
        });
    }
    
    private void showSecurityRisks(List<SFESecurityRisk> risks) {
        if (risks.isEmpty()) {
            return;
        }
        
        StringBuilder message = new StringBuilder("Security concerns detected:\n\n");
        
        for (SFESecurityRisk risk : risks) {
            message.append("- ").append(risk.getDescription())
                   .append(" (").append(risk.getLevel()).append(")\n");
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Security Warning")
            .setMessage(message.toString())
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void handlePaymentClick() {
        // Validate input fields
        if (!validateInputFields()) {
            return;
        }

        // Authenticate with biometric if available
        authenticateUser();
    }

    private boolean validateInputFields() {
        boolean isValid = true;

        if (etRecipientName.getText().toString().trim().isEmpty()) {
            etRecipientName.setError("Recipient name is required");
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
    
    private void authenticateUser() {
        SFEBiometric biometric = SFEManager.getBiometric();
        
        if (biometric.isAvailable()) {
            SFEBiometricPrompt.Builder promptBuilder = new SFEBiometricPrompt.Builder()
                .setTitle("Payment Authentication")
                .setSubtitle("Verify your identity")
                .setDescription("Authentication is required to complete this payment")
                .setNegativeButtonText("Cancel");
            
            biometric.authenticate(this, promptBuilder.build(), new SFEBiometricCallback() {
                @Override
                public void onSuccess() {
                    // User authenticated successfully
                    processPayment();
                }
                
                @Override
                public void onError(SFEBiometric.SFEBiometricError error) {
                    // Handle authentication error
                    Toast.makeText(PaymentAppActivity.this, 
                        "Authentication failed: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onCancel() {
                    // User canceled the authentication
                    Toast.makeText(PaymentAppActivity.this, 
                        "Authentication canceled", 
                        Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Biometric not available, proceed with payment
            processPayment();
        }
    }

    private void processPayment() {
        showLoading(true);
        updateSecurityStatus("Processing secure payment...", true);

        // Collect payment data securely
        SFESecurePayload securePayload = secureContainer.collectSecureData();
        
        // Add non-sensitive data
        securePayload.addField("recipientName", 
            new SFESecureTextField.SecureValue(etRecipientName.getText().toString()));
        securePayload.addField("amount", 
            new SFESecureTextField.SecureValue(etAmount.getText().toString()));
        securePayload.addField("description", 
            new SFESecureTextField.SecureValue(etDescription.getText().toString()));

        // Send the transaction securely
        SecureFinancialEnvironment.getSecureCommunication()
            .sendSecureTransaction(
                "/api/payments/process",
                securePayload,
                new SecureFinancialEnvironment.SFETransactionCallback() {
                    @Override
                    public void onSuccess(SFETransactionResult result) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            updateSecurityStatus("Payment completed securely", true);
                            showPaymentSuccessDialog(result.getTransactionId());
                            clearInputFields();
                        });
                    }
                    
                    @Override
                    public void onError(SFEException e) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            updateSecurityStatus("Secure Environment Active", true);
                            showPaymentFailureDialog(e.getMessage(), e.getErrorCode());
                        });
                    }
                }
            );
    }

    private void showPaymentSuccessDialog(String transactionId) {
        new AlertDialog.Builder(this)
            .setTitle("Payment Successful")
            .setMessage("Your payment of ₹" + etAmount.getText().toString() + 
                       " to " + etRecipientName.getText().toString() + 
                       " was successful.\n\nTransaction ID: " + transactionId)
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
        btnPay.setEnabled(!isLoading);
    }

    private void clearInputFields() {
        etRecipientName.setText("");
        etAmount.setText("");
        etDescription.setText("");
        
        // Clear secure fields (this would typically be handled by the SDK)
        // For demonstration purposes only
        View cardNumberView = cardNumberField.getView();
        if (cardNumberView instanceof EditText) {
            ((EditText) cardNumberView).setText("");
        }
        
        View cvvView = cvvField.getView();
        if (cvvView instanceof EditText) {
            ((EditText) cvvView).setText("");
        }
        
        View expiryDateView = expiryDateField.getView();
        if (expiryDateView instanceof EditText) {
            ((EditText) expiryDateView).setText("");
        }
    }
}