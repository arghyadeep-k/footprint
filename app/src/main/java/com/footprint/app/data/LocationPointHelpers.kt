package com.footprint.app.data

/**
 * Pure helper functions used by repository logic, designed to be unit-testable.
 */
object LocationPointHelpers {
    fun clampBatteryPercent(value: Int?): Int? {
        return value?.coerceIn(0, 100)
    }

    fun normalizeOptionalText(value: String?): String? {
        val normalized = value?.trim().orEmpty()
        return normalized.ifBlank { null }
    }

    fun normalizeRequiredText(value: String): String {
        val normalized = value.trim()
        require(normalized.isNotEmpty()) { "Required text field must not be blank." }
        return normalized
    }

    fun normalizeTimeWindow(startEpochMillis: Long, endEpochMillis: Long): Pair<Long, Long> {
        return if (startEpochMillis <= endEpochMillis) {
            startEpochMillis to endEpochMillis
        } else {
            endEpochMillis to startEpochMillis
        }
    }
}
