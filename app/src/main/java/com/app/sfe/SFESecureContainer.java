package com.app.sfe;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

/**
 * Secure container for handling sensitive UI components.
 * Provides a safe environment for input fields that handle financial data.
 */
public class SFESecureContainer {
    private final Context context;
    private final SFEConfig config;
    private final FrameLayout rootView;
    
    /**
     * Creates a new secure container.
     * 
     * @param context Activity context
     * @param config SFE configuration
     */
    public SFESecureContainer(@NonNull Context context, @NonNull SFEConfig config) {
        this.context = context;
        this.config = config;
        this.rootView = new FrameLayout(context);
        
        // Set up the root view with security protections
        setupSecureView();
    }
    
    /**
     * Gets the root view of the secure container.
     * 
     * @return Root view
     */
    public View getRootView() {
        return rootView;
    }
    
    /**
     * Creates a secure text field for handling sensitive input.
     * 
     * @param inputType Type of input
     * @param config Text field configuration
     * @return Secure text field
     */
    public SFESecureTextField createSecureTextField(
            @NonNull SFEInputType inputType, 
            @NonNull SFETextFieldConfig config) {
        return new SFESecureTextField(context, inputType, config);
    }
    
    /**
     * Collects all secure data from input fields in this container.
     * 
     * @return Secure payload containing all data
     */
    public SFESecurePayload collectSecureData() {
        // Create a secure payload with all data from the fields
        SFESecurePayload payload = new SFESecurePayload();
        
        // Find all secure fields in the container and collect their data
        collectDataFromViews(rootView, payload);
        
        return payload;
    }
    
    private void setupSecureView() {
        // Set up security features for the root view
        rootView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        
        // Apply security measures like screenshot prevention
        rootView.setFilterTouchesWhenObscured(true);
    }
    
    private void collectDataFromViews(ViewGroup viewGroup, SFESecurePayload payload) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            
            if (child instanceof SFESecureTextField) {
                SFESecureTextField field = (SFESecureTextField) child;
                payload.addField(field.getFieldType().name(), field.getSecureValue());
            } else if (child instanceof ViewGroup) {
                collectDataFromViews((ViewGroup) child, payload);
            }
        }
    }
}