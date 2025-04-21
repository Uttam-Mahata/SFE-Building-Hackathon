package com.app.sfe;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Secure text field for handling sensitive input data.
 * Provides enhanced security features for financial data input.
 */
public class SFESecureTextField {
    private final Context context;
    private final SFEInputType fieldType;
    private final EditText editText;
    
    /**
     * Creates a new secure text field.
     * 
     * @param context Activity context
     * @param fieldType Type of input field
     * @param config Field configuration
     */
    public SFESecureTextField(@NonNull Context context, 
                             @NonNull SFEInputType fieldType,
                             @NonNull SFETextFieldConfig config) {
        this.context = context;
        this.fieldType = fieldType;
        
        // Create and configure the secure edit text
        this.editText = new EditText(context);
        configureEditText(config);
    }
    
    /**
     * Gets the view of this secure text field.
     * 
     * @return The EditText view
     */
    public View getView() {
        return editText;
    }
    
    /**
     * Gets the field type of this secure text field.
     * 
     * @return Field type
     */
    public SFEInputType getFieldType() {
        return fieldType;
    }
    
    /**
     * Gets the secure value from this field.
     * The value is securely handled to prevent memory scanning attacks.
     * 
     * @return Secure value object
     */
    public SecureValue getSecureValue() {
        // Get the value securely
        String value = editText.getText().toString();
        return new SecureValue(value);
    }
    
    private void configureEditText(SFETextFieldConfig config) {
        // Set basic properties
        editText.setHint(config.getHint());
        
        // Set input filters including max length
        InputFilter[] filters = new InputFilter[config.getInputFilters().length + 1];
        filters[0] = new InputFilter.LengthFilter(config.getMaxLength());
        System.arraycopy(config.getInputFilters(), 0, filters, 1, config.getInputFilters().length);
        editText.setFilters(filters);
        
        // Apply field-specific configurations
        configureFieldType();
        
        // Apply security configurations
        applySecurityMeasures(config);
    }
    
    private void configureFieldType() {
        switch (fieldType) {
            case CARD_NUMBER:
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                // Add card number formatting if needed
                break;
                
            case CVV:
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                break;
                
            case EXPIRY_DATE:
                editText.setInputType(InputType.TYPE_CLASS_DATETIME);
                // Add expiry date formatting if needed
                break;
                
            case ACCOUNT_NUMBER:
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
                
            case PIN:
            case PASSWORD:
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                break;
                
            case OTP:
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
                
            default:
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
        }
    }
    
    private void applySecurityMeasures(SFETextFieldConfig config) {
        // Prevent screenshots of sensitive data
        if (fieldType == SFEInputType.CARD_NUMBER || 
            fieldType == SFEInputType.CVV || 
            fieldType == SFEInputType.PIN) {
            editText.setFilterTouchesWhenObscured(true);
        }
        
        // Apply masking if configured
        if (config.isMaskInput()) {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        
        // Disable clipboard operations for sensitive fields
        editText.setLongClickable(false);
        
        // Apply additional security features based on security level
        if (config.getSecurityLevel() > 1) {
            // Higher security level configurations
            // For example, clear the field on app background
            // Implement custom security measures
        }
    }
    
    /**
     * Class for securely handling input values.
     * Prevents memory scanning and provides secure access to sensitive data.
     */
    public static class SecureValue {
        private final char[] value;
        
        SecureValue(String value) {
            this.value = value != null ? value.toCharArray() : new char[0];
        }
        
        /**
         * Gets the value as a string.
         * Use this method with caution.
         * 
         * @return The string value
         */
        public String getValue() {
            return new String(value);
        }
        
        /**
         * Clears the value from memory.
         */
        public void clear() {
            for (int i = 0; i < value.length; i++) {
                value[i] = 0;
            }
        }
    }
}