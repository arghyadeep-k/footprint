package com.footprint.app.data.importing

import com.footprint.app.location.TrackingMode

data class CsvImportSummary(
    val rowsRead: Int,
    val pointsImported: Int,
    val rowsSkipped: Int,
    val errors: List<String>
)

data class ParsedCsvLocationPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val recordedAtEpochMillis: Long,
    val trackingMode: String
)

data class CsvParseResult(
    val rowsRead: Int,
    val parsedPoints: List<ParsedCsvLocationPoint>,
    val rowsSkipped: Int,
    val errors: List<String>
)

object LocationCsvImportParser {
    private val requiredHeaders = setOf("latitude", "longitude", "recorded_timestamp", "tracking_mode")

    fun parse(csvContent: String): CsvParseResult {
        val lines = csvContent
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        if (lines.isEmpty()) {
            return CsvParseResult(
                rowsRead = 0,
                parsedPoints = emptyList(),
                rowsSkipped = 0,
                errors = listOf("CSV file is empty")
            )
        }

        val headerColumns = parseCsvLine(lines.first()).map { it.trim().lowercase() }
        val indexByHeader = headerColumns.withIndex().associate { it.value to it.index }

        val missingHeaders = requiredHeaders.filterNot { it in indexByHeader }
        if (missingHeaders.isNotEmpty()) {
            return CsvParseResult(
                rowsRead = 0,
                parsedPoints = emptyList(),
                rowsSkipped = lines.size - 1,
                errors = listOf("Missing required columns: ${missingHeaders.joinToString()}")
            )
        }

        val parsedPoints = mutableListOf<ParsedCsvLocationPoint>()
        val errors = mutableListOf<String>()
        var rowsRead = 0
        var rowsSkipped = 0

        lines.drop(1).forEachIndexed { index, line ->
            rowsRead += 1
            val rowNumber = index + 2
            val columns = parseCsvLine(line)

            fun column(header: String): String? {
                val idx = indexByHeader[header] ?: return null
                if (idx >= columns.size) return null
                return columns[idx].trim().takeIf { it.isNotEmpty() }
            }

            val latitude = column("latitude")?.toDoubleOrNull()
            if (latitude == null || latitude !in -90.0..90.0) {
                rowsSkipped += 1
                errors += "Row $rowNumber: invalid latitude"
                return@forEachIndexed
            }

            val longitude = column("longitude")?.toDoubleOrNull()
            if (longitude == null || longitude !in -180.0..180.0) {
                rowsSkipped += 1
                errors += "Row $rowNumber: invalid longitude"
                return@forEachIndexed
            }

            val timestamp = column("recorded_timestamp")?.toLongOrNull()
            if (timestamp == null || timestamp <= 0L) {
                rowsSkipped += 1
                errors += "Row $rowNumber: invalid recorded_timestamp"
                return@forEachIndexed
            }

            val trackingMode = column("tracking_mode")?.uppercase()
            if (trackingMode == null || TrackingMode.entries.none { it.name == trackingMode }) {
                rowsSkipped += 1
                errors += "Row $rowNumber: invalid tracking_mode"
                return@forEachIndexed
            }

            val accuracy = column("accuracy")?.toFloatOrNull()

            parsedPoints += ParsedCsvLocationPoint(
                latitude = latitude,
                longitude = longitude,
                accuracyMeters = accuracy,
                recordedAtEpochMillis = timestamp,
                trackingMode = trackingMode
            )
        }

        return CsvParseResult(
            rowsRead = rowsRead,
            parsedPoints = parsedPoints,
            rowsSkipped = rowsSkipped,
            errors = errors
        )
    }

    private fun parseCsvLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i += 1
                }
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    out += current.toString()
                    current.clear()
                }
                else -> current.append(ch)
            }
            i += 1
        }
        out += current.toString()
        return out
    }
}
