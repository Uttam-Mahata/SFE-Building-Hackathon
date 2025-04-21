package com.app.sfe;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

/**
 * Implementation of biometric authentication for the SFE SDK.
 * This class uses the AndroidX Biometric library to provide secure biometric authentication.
 */
public class SFEBiometric {
    private static final String TAG = "SFEBiometric";
    
    private final Context context;
    private final Executor mainExecutor;
    private final Handler mainHandler;
    
    /**
     * Creates a new SFEBiometric instance.
     * 
     * @param context Application context
     */
    public SFEBiometric(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.mainExecutor = ContextCompat.getMainExecutor(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Checks if biometric authentication is available on the device.
     * 
     * @return true if biometric authentication is available, false otherwise
     */
    public boolean isAvailable() {
        BiometricManager biometricManager = BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;
    }
    
    /**
     * Authenticates the user using biometric authentication.
     * 
     * @param activity Activity to show the biometric prompt
     * @param promptInfo Biometric prompt configuration
     * @param callback Callback for authentication result
     */
    public void authenticate(@NonNull FragmentActivity activity, 
                            @NonNull SFEBiometricPrompt promptInfo,
                            @NonNull SFEBiometricCallback callback) {
        if (!isAvailable()) {
            mainHandler.post(() -> callback.onError(new SFEBiometricError(
                "Biometric authentication not available", 
                SFEBiometricError.ERROR_HARDWARE_UNAVAILABLE)));
            return;
        }
        
        try {
            BiometricPrompt.PromptInfo promptBuilder = createPromptInfo(promptInfo);
            BiometricPrompt biometricPrompt = createBiometricPrompt(activity, callback);
            
            biometricPrompt.authenticate(promptBuilder);
        } catch (Exception e) {
            Log.e(TAG, "Error during biometric authentication: " + e.getMessage(), e);
            mainHandler.post(() -> callback.onError(new SFEBiometricError(
                "Authentication error: " + e.getMessage(), 
                SFEBiometricError.ERROR_INTERNAL)));
        }
    }
    
    private BiometricPrompt.PromptInfo createPromptInfo(SFEBiometricPrompt promptInfo) {
        return new BiometricPrompt.PromptInfo.Builder()
            .setTitle(promptInfo.getTitle())
            .setSubtitle(promptInfo.getSubtitle() != null ? promptInfo.getSubtitle() : "")
            .setDescription(promptInfo.getDescription() != null ? promptInfo.getDescription() : "")
            .setNegativeButtonText(promptInfo.getNegativeButtonText())
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build();
    }
    
    private BiometricPrompt createBiometricPrompt(FragmentActivity activity, SFEBiometricCallback callback) {
        return new BiometricPrompt(activity, mainExecutor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                mainHandler.post(callback::onSuccess);
            }
            
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                
                // If user cancels, call the cancel callback
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    mainHandler.post(callback::onCancel);
                } else {
                    mainHandler.post(() -> callback.onError(new SFEBiometricError(
                        errString.toString(), errorCode)));
                }
            }
            
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // This is called when the authentication fails (e.g., wrong fingerprint)
                // We don't call the error callback here because the user can try again
            }
        });
    }
    
    /**
     * Class representing a biometric authentication error.
     */
    public static class SFEBiometricError {
        public static final int ERROR_INTERNAL = -1;
        public static final int ERROR_HARDWARE_UNAVAILABLE = 1;
        
        private final String message;
        private final int code;
        
        public SFEBiometricError(String message, int code) {
            this.message = message;
            this.code = code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public int getCode() {
            return code;
        }
    }
}