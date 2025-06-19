package com.sfe.sdk.backend

data class EncryptedBalance(
    val encryptedData: String, // Represents the encrypted balance
    val encryptionKeyId: String, // Identifier for the key used
    private var auditLogId: String? = null // Mutable private property
) {
    fun withAuditLog(logId: String): EncryptedBalance {
        this.auditLogId = logId
        return this
    }

    fun getAuditLogId(): String? = auditLogId
}
