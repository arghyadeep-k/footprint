package com.footprint.app.timeline

data class VisitSegment(
    val latitude: Double,
    val longitude: Double,
    val arrivalEpochMillis: Long,
    val departureEpochMillis: Long,
    val durationMillis: Long,
    val pointCount: Int
)
