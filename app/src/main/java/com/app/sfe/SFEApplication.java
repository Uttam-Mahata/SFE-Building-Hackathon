package com.app.sfe;

import android.app.Application;
import android.util.Log;

import com.app.sfe.security.SFEProviderType;
import com.app.sfe.security.SFESecurityLevel;

/**
 * Application class that initializes the SFE SDK.
 */
public class SFEApplication extends Application {
    private static final String TAG = "SFEApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        initializeSFESDK();
    }

    private void initializeSFESDK() {
        try {
            // Configure the SFE SDK
            SFEConfig config = new SFEConfig.Builder()
                .setApplicationId("sfe-registered-app-id")
                .setProviderType(SFEProviderType.FINTECH)
                .setEnvironment(SFEEnvironment.SANDBOX)
                .setSecurityLevel(SFESecurityLevel.HIGH)
                .setApiEndpoint("https://api.sfe-india.com")
                .build();
            
            // Initialize SDK
            SFEManager.initialize(this, config, new SFEInitCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "SFE SDK initialized successfully");
                }
                
                @Override
                public void onError(SFEException e) {
                    Log.e(TAG, "SFE SDK initialization failed: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing SFE SDK: " + e.getMessage());
        }
    }
}