package com.footprint.app.data.export

import com.footprint.app.data.local.LocationPoint

object TimelineExportFormatter {
    fun toCsv(points: List<LocationPoint>): String {
        val builder = StringBuilder()
        builder.append("latitude,longitude,accuracy,recorded_timestamp,tracking_mode\n")
        points.forEach { point ->
            val accuracyValue = point.accuracyMeters?.toString().orEmpty()
            builder.append(point.latitude)
                .append(',')
                .append(point.longitude)
                .append(',')
                .append(accuracyValue)
                .append(',')
                .append(point.recordedAtEpochMillis)
                .append(',')
                .append(escapeCsv(point.trackingMode))
                .append('\n')
        }
        return builder.toString()
    }

    fun toGeoJson(points: List<LocationPoint>): String {
        val features = points.joinToString(separator = ",") { point ->
            val accuracyPart = point.accuracyMeters?.let { ",\"accuracy\":$it" } ?: ""
            """
            {"type":"Feature","geometry":{"type":"Point","coordinates":[${point.longitude},${point.latitude}]},"properties":{"recorded_timestamp":${point.recordedAtEpochMillis},"tracking_mode":"${escapeJson(point.trackingMode)}"$accuracyPart}}
            """.trimIndent()
        }

        return """
            {"type":"FeatureCollection","features":[${features}]}
        """.trimIndent()
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n')
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }
}
