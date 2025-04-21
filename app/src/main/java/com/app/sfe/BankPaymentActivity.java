package com.app.sfe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.sfe.security.SFEConfiguration;
import com.app.sfe.security.SFEInitResult;
import com.app.sfe.security.SFEProviderType;
import com.app.sfe.security.SFESecurityLevel;
import com.app.sfe.security.SFETransactionResult;
import com.app.sfe.security.SecureFinancialEnvironment;

import java.util.Arrays;
import java.util.List;

public class BankPaymentActivity extends AppCompatActivity {
    
    private Button btnInitialize;
    private Button btnProcessPayment;
    private ProgressBar progressBar;
    private TextView tvStatus;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_payment);
        
        // Initialize UI components
        btnInitialize = findViewById(R.id.btn_initialize);
        btnProcessPayment = findViewById(R.id.btn_process_payment);
        progressBar = findViewById(R.id.progress_bar);
        tvStatus = findViewById(R.id.tv_status);
        
        btnProcessPayment.setEnabled(false);
        
        btnInitialize.setOnClickListener(v -> initializeSecureEnvironment());
        btnProcessPayment.setOnClickListener(v -> processPayment());
    }
    
    private void initializeSecureEnvironment() {
        showLoading(true);
        tvStatus.setText("Initializing secure environment...");
        
        // Define allowed API endpoints
        List<String> allowedAPIs = Arrays.asList(
            "https://bank-api.example.com/payments/process",
            "https://bank-api.example.com/accounts/verify"
        );
        
        // Initialize SFE with bank-specific configuration
        SFEConfiguration config = new SFEConfiguration.Builder()
            .setAppId("bank-app-registered-id")
            .setProviderType(SFEProviderType.BANK)
            .setCustomSecurityLevel(SFESecurityLevel.HIGH)
            .build();
            
        SecureFinancialEnvironment.initialize(this, config, this::handleInitResult);
    }
    
    private void handleInitResult(SFEInitResult result) {
        runOnUiThread(() -> {
            showLoading(false);
            
            if (result.isSuccess()) {
                tvStatus.setText("Secure environment initialized successfully");
                btnProcessPayment.setEnabled(true);
            } else {
                tvStatus.setText("Initialization failed: " + result.getErrorReason());
                showSecurityAlert(result.getErrorReason());
            }
        });
    }
    
    private void processPayment() {
        showLoading(true);
        tvStatus.setText("Processing secure payment...");
        
        // Get payment data from bank UI
        BankPaymentData paymentData = collectPaymentDataFromUI();
        
        // Create a secure payload from the payment data
        SFESecurePayload securePayload = new SFESecurePayload();
        securePayload.addField("sourceAccount", 
            new SFESecureTextField.SecureValue(paymentData.getSourceAccount()));
        securePayload.addField("destinationAccount", 
            new SFESecureTextField.SecureValue(paymentData.getDestinationAccount()));
        securePayload.addField("amount", 
            new SFESecureTextField.SecureValue(String.valueOf(paymentData.getAmount())));
        securePayload.addField("currency", 
            new SFESecureTextField.SecureValue(paymentData.getCurrency()));
        securePayload.addField("description", 
            new SFESecureTextField.SecureValue(paymentData.getDescription()));
        
        // Process through SFE secure channel
        SecureFinancialEnvironment.getSecureCommunication()
            .sendSecureTransaction(
                "https://bank-api.example.com/payments/process",
                securePayload,
                new SecureFinancialEnvironment.SFETransactionCallback() {
                    @Override
                    public void onSuccess(SFETransactionResult result) {
                        handleTransactionSuccess(result);
                    }
                    
                    @Override
                    public void onError(SFEException e) {
                        handleTransactionError(e);
                    }
                }
            );
    }
    
    private void handleTransactionSuccess(SFETransactionResult result) {
        runOnUiThread(() -> {
            showLoading(false);
            tvStatus.setText("Transaction completed successfully");
            Toast.makeText(this, "Payment processed successfully", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void handleTransactionError(SFEException e) {
        runOnUiThread(() -> {
            showLoading(false);
            tvStatus.setText("Transaction failed: " + e.getMessage());
            showTransactionFailureDialog(e.getMessage(), e.getErrorCode());
        });
    }
    
    private BankPaymentData collectPaymentDataFromUI() {
        // In a real app, this would collect data from UI fields
        return new BankPaymentData(
            "ACC123456789",
            "RECV987654321",
            1000.0,
            "INR",
            "Payment for services"
        );
    }
    
    private void showSecurityAlert(String message) {
        new AlertDialog.Builder(this)
            .setTitle("Security Warning")
            .setMessage("Security initialization failed: " + message)
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
    
    private void showTransactionFailureDialog(String message, int errorCode) {
        new AlertDialog.Builder(this)
            .setTitle("Transaction Failed")
            .setMessage("Error " + errorCode + ": " + message)
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnInitialize.setEnabled(!isLoading);
        
        // Check if SDK is initialized before enabling payment button
        if (!isLoading) {
            try {
                SecureFinancialEnvironment.getSecureCommunication();
                btnProcessPayment.setEnabled(true);
            } catch (IllegalStateException e) {
                btnProcessPayment.setEnabled(false);
            }
        } else {
            btnProcessPayment.setEnabled(false);
        }
    }
    
    /**
     * Sample class to represent bank payment data
     */
    static class BankPaymentData {
        private final String sourceAccount;
        private final String destinationAccount;
        private final double amount;
        private final String currency;
        private final String description;
        
        BankPaymentData(String sourceAccount, String destinationAccount, 
                         double amount, String currency, String description) {
            this.sourceAccount = sourceAccount;
            this.destinationAccount = destinationAccount;
            this.amount = amount;
            this.currency = currency;
            this.description = description;
        }
        
        // Getters
        public String getSourceAccount() { return sourceAccount; }
        public String getDestinationAccount() { return destinationAccount; }
        public double getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public String getDescription() { return description; }
    }
}