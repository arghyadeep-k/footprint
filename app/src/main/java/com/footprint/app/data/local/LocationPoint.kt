package com.footprint.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_points")
data class LocationPoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val altitudeMeters: Double?,
    val speedMetersPerSecond: Float?,
    val bearingDegrees: Float?,
    val provider: String?,
    val recordedAtEpochMillis: Long,
    val batteryPercent: Int?,
    val trackingMode: String,
    val source: String
)
