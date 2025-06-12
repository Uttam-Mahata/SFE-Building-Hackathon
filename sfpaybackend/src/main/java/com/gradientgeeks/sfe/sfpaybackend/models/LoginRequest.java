package com.gradientgeeks.sfe.sfpaybackend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    @Email(message = "Username must be a valid email address")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private String sfePayload; // Optional SFE payload for enhanced security
} 