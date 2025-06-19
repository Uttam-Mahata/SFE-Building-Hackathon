package com.sfe.sdk.backend.ops

import com.sfe.sdk.backend.StepUpType // Assuming StepUpType is in com.sfe.sdk.backend

interface AuthOperations {
    /**
     * Checks if step-up authentication is required for a given action or context.
     * @param userId The user ID.
     * @param action A descriptor of the action being attempted (e.g., "HIGH_VALUE_TRANSFER").
     * @return True if step-up authentication is required, false otherwise.
     */
    fun requireStepUpAuth(userId: String, action: String): Boolean {
        throw NotImplementedError("requireStepUpAuth is not yet implemented.")
    }

    /**
     * Generates a One-Time Password (OTP) for the given user.
     * @param userId The user ID for whom to generate the OTP.
     * @param type The type of OTP or challenge (e.g., SMS, EMAIL).
     * @return A reference ID for the generated OTP (not the OTP itself).
     */
    fun generateOTP(userId: String, type: StepUpType): String {
        throw NotImplementedError("generateOTP is not yet implemented.")
    }

    /**
     * Validates an OTP provided by the user.
     * @param userId The user ID.
     * @param otpReferenceId The reference ID of the OTP generation request.
     * @param otpValue The OTP value entered by the user.
     * @return True if the OTP is valid, false otherwise.
     */
    fun validateOTP(userId: String, otpReferenceId: String, otpValue: String): Boolean {
        throw NotImplementedError("validateOTP is not yet implemented.")
    }
}
