package com.app.sfe;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.app.sfe.security.SFEConfiguration;
import com.app.sfe.security.SecureFinancialEnvironment;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Main manager class for the SFE SDK.
 * Provides access to various SDK features and manages initialization.
 */
public class SFEManager {
    private static final String TAG = "SFEManager";
    
    private static SFEManager instance;
    private final Context applicationContext;
    private final SFEConfig config;
    private final Executor backgroundExecutor = Executors.newCachedThreadPool();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    private SFESecureContainer secureContainer;
    private SFEBiometric biometric;
    private SFEDeviceSecurity deviceSecurity;
    private boolean isInitialized = false;
    
    private SFEManager(Context context, SFEConfig config) {
        this.applicationContext = context.getApplicationContext();
        this.config = config;
    }
    
    /**
     * Initializes the SFE SDK with the provided configuration.
     * 
     * @param context Application context
     * @param config SDK configuration
     * @param callback Callback to receive initialization result
     */
    public static void initialize(@NonNull Context context, 
                                 @NonNull SFEConfig config,
                                 @NonNull SFEInitCallback callback) {
        // If already initialized, return success immediately
        if (instance != null && instance.isInitialized) {
            callback.onSuccess();
            return;
        }
        
        instance = new SFEManager(context, config);
        instance.performInitialization(callback);
    }
    
    /**
     * Gets the SFE SDK instance.
     * 
     * @return SFEManager instance
     * @throws IllegalStateException if SDK is not initialized
     */
    public static SFEManager getInstance() {
        if (instance == null || !instance.isInitialized) {
            throw new IllegalStateException("SFE SDK not initialized. Call initialize() first.");
        }
        return instance;
    }
    
    /**
     * Creates a new secure container for handling sensitive UI components.
     * 
     * @param context Activity context
     * @return Secure container instance
     */
    public static SFESecureContainer createSecureContainer(@NonNull Context context) {
        checkInitialized();
        return new SFESecureContainer(context, instance.config);
    }
    
    /**
     * Gets the biometric authentication module.
     * 
     * @return Biometric module instance
     */
    public static SFEBiometric getBiometric() {
        checkInitialized();
        if (instance.biometric == null) {
            instance.biometric = new SFEBiometric(instance.applicationContext);
        }
        return instance.biometric;
    }
    
    /**
     * Gets the device security module.
     * 
     * @return Device security module instance
     */
    public static SFEDeviceSecurity getDeviceSecurity() {
        checkInitialized();
        if (instance.deviceSecurity == null) {
            instance.deviceSecurity = new SFEDeviceSecurity(instance.applicationContext);
        }
        return instance.deviceSecurity;
    }
    
    /**
     * Gets the secure communication module.
     * 
     * @return SecureFinancialEnvironment instance for communication
     */
    public static SecureFinancialEnvironment.SecureCommunicationManager getSecureCommunication() {
        checkInitialized();
        return SecureFinancialEnvironment.getSecureCommunication();
    }
    
    private static void checkInitialized() {
        if (instance == null || !instance.isInitialized) {
            throw new IllegalStateException("SFE SDK not initialized. Call initialize() first.");
        }
    }
    
    private void performInitialization(SFEInitCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                Log.d(TAG, "Starting SFE SDK initialization");
                
                // Convert SDK config to SFE configuration
                SFEConfiguration sfeConfig = new SFEConfiguration.Builder()
                    .setAppId(config.getApplicationId())
                    .setProviderType(config.getProviderType())
                    .setCustomSecurityLevel(config.getSecurityLevel())
                    .setAnalyticsEnabled(config.isAnalyticsEnabled())
                    .setTransactionTimeout(config.getTransactionTimeout())
                    .build();
                
                // Initialize the core SFE environment
                SecureFinancialEnvironment.initialize(
                    applicationContext, 
                    sfeConfig, 
                    result -> {
                        if (result.isSuccess()) {
                            // Initialize other SDK components
                            try {
                                initializeComponents();
                                isInitialized = true;
                                
                                mainHandler.post(() -> callback.onSuccess());
                            } catch (Exception e) {
                                String errorMsg = "Failed to initialize SFE components: " + e.getMessage();
                                Log.e(TAG, errorMsg, e);
                                SFEException exception = new SFEException(errorMsg, e, 1002);
                                
                                mainHandler.post(() -> callback.onError(exception));
                            }
                        } else {
                            String errorMsg = "SFE core initialization failed: " + 
                                             result.getErrorReason();
                            Log.e(TAG, errorMsg);
                            SFEException exception = new SFEException(
                                errorMsg, result.getErrorCode());
                            
                            mainHandler.post(() -> callback.onError(exception));
                        }
                    }
                );
            } catch (Exception e) {
                String errorMsg = "SFE initialization failed with exception: " + e.getMessage();
                Log.e(TAG, errorMsg, e);
                SFEException exception = new SFEException(errorMsg, e, 1000);
                
                mainHandler.post(() -> callback.onError(exception));
            }
        });
    }
    
    private void initializeComponents() {
        // Initialize components that need to be created during SDK initialization
        Log.d(TAG, "Initializing SFE SDK components");
        
        // Create the device security module
        deviceSecurity = new SFEDeviceSecurity(applicationContext);
        
        // Other component initializations can be added here
    }
}