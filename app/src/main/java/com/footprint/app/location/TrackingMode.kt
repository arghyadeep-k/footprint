package com.footprint.app.location

import com.google.android.gms.location.Priority

enum class TrackingMode(
    val storageValue: String,
    val notificationLabel: String,
    val intervalMillis: Long,
    val minUpdateIntervalMillis: Long,
    val minDistanceMeters: Float,
    val priority: Int
) {
    LOW_POWER(
        storageValue = "low_power",
        notificationLabel = "Battery saver tracking",
        intervalMillis = 15 * 60 * 1000L,
        minUpdateIntervalMillis = 5 * 60 * 1000L,
        minDistanceMeters = 120f,
        priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
    ),
    BALANCED(
        storageValue = "balanced",
        notificationLabel = "Balanced tracking",
        intervalMillis = 5 * 60 * 1000L,
        minUpdateIntervalMillis = 60 * 1000L,
        minDistanceMeters = 50f,
        priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
    ),
    ACTIVE(
        storageValue = "active",
        notificationLabel = "Active trip tracking",
        intervalMillis = 60 * 1000L,
        minUpdateIntervalMillis = 30 * 1000L,
        minDistanceMeters = 15f,
        priority = Priority.PRIORITY_HIGH_ACCURACY
    );

    companion object {
        fun fromStorageValue(value: String?): TrackingMode {
            return entries.firstOrNull { it.storageValue == value } ?: BALANCED
        }
    }
}
