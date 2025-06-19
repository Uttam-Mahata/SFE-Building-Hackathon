package com.sfe.paymentbackend.service

import org.springframework.stereotype.Service

@Service
class NotificationService {

    fun sendPaymentInitiated(userId: String, transactionId: String) {
        // In a real application, this would send an email, SMS, or push notification.
        println("NotificationService (mock): Payment initiated for user $userId, transaction ID: $transactionId. Notification sent.")
    }

    fun sendPaymentSuccess(userId: String, transactionId: String) {
        // In a real application, this would send an email, SMS, or push notification.
        println("NotificationService (mock): Payment successful for user $userId, transaction ID: $transactionId. Notification sent.")
    }

    fun sendPaymentFailure(userId: String, transactionId: String?, reason: String?) {
        // In a real application, this would send an email, SMS, or push notification.
        val txId = transactionId ?: "N/A"
        println("NotificationService (mock): Payment failed for user $userId, transaction ID: $txId. Reason: $reason. Notification sent.")
    }

    fun sendOtp(userId: String, otp: String, medium: String = "SMS") {
        // In a real application, this would send an OTP via the specified medium.
        println("NotificationService (mock): OTP $otp sent to user $userId via $medium.")
    }

    fun sendTestMessage(message: String) {
         println("NotificationService (mock): Test message: $message")
    }
}
