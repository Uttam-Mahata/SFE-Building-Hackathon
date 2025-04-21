package com.app.sfe;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Class for checking device security status.
 * Provides methods to verify if the device meets security requirements.
 */
public class SFEDeviceSecurity {
    private final Context context;
    private final Executor backgroundExecutor = Executors.newCachedThreadPool();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    /**
     * Creates a new device security checker.
     * 
     * @param context Application context
     */
    public SFEDeviceSecurity(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Performs a security check on the device.
     * 
     * @param callback Callback for security check result
     */
    public void performSecurityCheck(@NonNull SFESecurityCheckCallback callback) {
        backgroundExecutor.execute(() -> {
            // Perform security checks
            SFESecurityCheckResult result = checkDeviceSecurity();
            
            mainHandler.post(() -> callback.onResult(result));
        });
    }
    
    private SFESecurityCheckResult checkDeviceSecurity() {
        List<SFESecurityRisk> risks = new ArrayList<>();
        
        // Check if device is rooted
        if (isDeviceRooted()) {
            risks.add(new SFESecurityRisk(
                SFESecurityRisk.RiskType.DEVICE_ROOTED,
                "Device appears to be rooted",
                SFESecurityRisk.RiskLevel.HIGH
            ));
        }
        
        // Check for developer options
        if (isDeveloperOptionsEnabled()) {
            risks.add(new SFESecurityRisk(
                SFESecurityRisk.RiskType.DEVELOPER_OPTIONS_ENABLED,
                "Developer options are enabled",
                SFESecurityRisk.RiskLevel.MEDIUM
            ));
        }
        
        // Check for device lock
        if (!isDeviceLockEnabled()) {
            risks.add(new SFESecurityRisk(
                SFESecurityRisk.RiskType.NO_DEVICE_LOCK,
                "Device lock is not enabled",
                SFESecurityRisk.RiskLevel.MEDIUM
            ));
        }
        
        // Check for installed debugging tools
        if (hasDebuggingApps()) {
            risks.add(new SFESecurityRisk(
                SFESecurityRisk.RiskType.DEBUGGING_TOOLS_INSTALLED,
                "Debugging tools detected",
                SFESecurityRisk.RiskLevel.MEDIUM
            ));
        }
        
        return new SFESecurityCheckResult(risks.isEmpty(), risks);
    }
    
    private boolean isDeviceRooted() {
        // Check for common root management apps
        String[] rootApps = { 
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu",
            "com.topjohnwu.magisk"
        };
        
        for (String app : rootApps) {
            try {
                context.getPackageManager().getPackageInfo(app, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                // Package not found, continue checking
            }
        }
        
        // Check for su binary
        try {
            Process process = Runtime.getRuntime().exec("which su");
            return process.waitFor() == 0;
        } catch (Exception e) {
            // Not rooted
        }
        
        return false;
    }
    
    private boolean isDeveloperOptionsEnabled() {
        return Settings.Global.getInt(context.getContentResolver(), 
                                     Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
    }
    
    private boolean isDeviceLockEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.app.KeyguardManager keyguardManager = 
                (android.app.KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            return keyguardManager != null && keyguardManager.isDeviceSecure();
        } else {
            android.app.KeyguardManager keyguardManager = 
                (android.app.KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            return keyguardManager != null && keyguardManager.isKeyguardSecure();
        }
    }
    
    private boolean hasDebuggingApps() {
        // Check for common debugging tools
        String[] debuggingApps = {
            "com.android.tools.fd.debug",
            "com.facebook.flipper",
            "com.android.debug.app"
        };
        
        for (String app : debuggingApps) {
            try {
                context.getPackageManager().getPackageInfo(app, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                // Package not found, continue checking
            }
        }
        
        return false;
    }
}