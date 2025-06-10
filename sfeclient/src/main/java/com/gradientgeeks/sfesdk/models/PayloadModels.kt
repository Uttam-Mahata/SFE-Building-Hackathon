package com.gradientgeeks.sfesdk.models

/**
 * Data models for the secure payload structure
 */

data class SecurePayload(
    val appVersion: String,
    val sdkVersion: String,
    val timestamp: String,
    val deviceInfo: DeviceInfo,
    val bindingInfo: BindingInfo,
    val attestationToken: String?
)

data class DeviceInfo(
    val osVersion: String,
    val deviceModel: String,
    val isRooted: Boolean,
    val isDebuggerAttached: Boolean,
    val isAppTampered: Boolean
)

data class BindingInfo(
    val simPresent: Boolean,
    val networkOperator: String?,
    val deviceBindingToken: String
) 