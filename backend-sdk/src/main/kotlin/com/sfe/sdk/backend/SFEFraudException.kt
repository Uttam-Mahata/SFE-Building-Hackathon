package com.sfe.sdk.backend

class SFEFraudException(
    message: String,
    val fraudReportId: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)
