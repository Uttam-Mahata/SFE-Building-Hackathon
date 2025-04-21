package com.app.sfe;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Secure payload for handling sensitive data collected from secure input fields.
 * Provides encryption and secure handling of the data.
 */
public class SFESecurePayload {
    private final Map<String, SFESecureTextField.SecureValue> fields;
    
    /**
     * Creates a new empty secure payload.
     */
    public SFESecurePayload() {
        this.fields = new HashMap<>();
    }
    
    /**
     * Adds a field to the payload.
     * 
     * @param fieldName Name of the field
     * @param value Secure value of the field
     */
    public void addField(@NonNull String fieldName, @NonNull SFESecureTextField.SecureValue value) {
        fields.put(fieldName, value);
    }
    
    /**
     * Gets a field value from the payload.
     * 
     * @param fieldName Name of the field
     * @return Secure value of the field, or null if not found
     */
    public SFESecureTextField.SecureValue getField(String fieldName) {
        return fields.get(fieldName);
    }
    
    /**
     * Gets all field names in the payload.
     * 
     * @return Array of field names
     */
    public String[] getFieldNames() {
        return fields.keySet().toArray(new String[0]);
    }
    
    /**
     * Converts the payload to a secure JSON string for transmission.
     * The string is encrypted using the SDK's encryption keys.
     * 
     * @return Encrypted JSON string
     */
    public String toSecureJson() {
        // In a real implementation, this would use cryptographic libraries
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, SFESecureTextField.SecureValue> entry : fields.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            
            json.append("\"").append(entry.getKey()).append("\":\"")
                .append(entry.getValue().getValue()).append("\"");
        }
        
        json.append("}");
        
        // For demonstration, just return the JSON string
        // In a real implementation, this would be encrypted
        return json.toString();
    }
    
    /**
     * Clears all sensitive data from memory.
     * Should be called when the payload is no longer needed.
     */
    public void clear() {
        for (SFESecureTextField.SecureValue value : fields.values()) {
            value.clear();
        }
        fields.clear();
    }
}