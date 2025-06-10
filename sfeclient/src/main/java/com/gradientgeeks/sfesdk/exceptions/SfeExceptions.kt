package com.gradientgeeks.sfesdk.exceptions

/**
 * Custom exception classes for the SFE SDK
 */

open class SfeException(message: String, cause: Throwable? = null) : Exception(message, cause)

class SfeSecurityException(message: String, cause: Throwable? = null) : SfeException(message, cause)

class SfeInitializationException(message: String, cause: Throwable? = null) : SfeException(message, cause)

class SfeAttestationException(message: String, cause: Throwable? = null) : SfeException(message, cause) 