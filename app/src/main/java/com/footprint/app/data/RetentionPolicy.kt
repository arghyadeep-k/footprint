package com.footprint.app.data

enum class RetentionPolicy(val storageValue: String) {
    KEEP_FOREVER("keep_forever"),
    DELETE_OLDER_THAN_30_DAYS("delete_30d"),
    DELETE_OLDER_THAN_90_DAYS("delete_90d"),
    DELETE_OLDER_THAN_1_YEAR("delete_1y");

    companion object {
        fun fromStorageValue(value: String?): RetentionPolicy {
            return entries.firstOrNull { it.storageValue == value } ?: KEEP_FOREVER
        }
    }
}

object RetentionPolicyHelper {
    fun cutoffEpochMillis(
        policy: RetentionPolicy,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): Long? {
        val retentionMillis = when (policy) {
            RetentionPolicy.KEEP_FOREVER -> return null
            RetentionPolicy.DELETE_OLDER_THAN_30_DAYS -> 30L * 24 * 60 * 60 * 1000
            RetentionPolicy.DELETE_OLDER_THAN_90_DAYS -> 90L * 24 * 60 * 60 * 1000
            RetentionPolicy.DELETE_OLDER_THAN_1_YEAR -> 365L * 24 * 60 * 60 * 1000
        }
        return nowEpochMillis - retentionMillis
    }
}
