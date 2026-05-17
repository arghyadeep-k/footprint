package com.footprint.app.data.importing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationCsvImportParserTest {

    @Test
    fun parse_validRows_returnsParsedPoints() {
        val csv = """
            latitude,longitude,accuracy,recorded_timestamp,tracking_mode
            41.1,-87.6,12.5,1700000000000,BALANCED
            41.2,-87.7,,1700000600000,LOW_POWER
        """.trimIndent()

        val result = LocationCsvImportParser.parse(csv)

        assertEquals(2, result.rowsRead)
        assertEquals(2, result.parsedPoints.size)
        assertEquals(0, result.rowsSkipped)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun parse_missingRequiredHeader_returnsError() {
        val csv = """
            latitude,longitude,accuracy,tracking_mode
            41.1,-87.6,12.5,BALANCED
        """.trimIndent()

        val result = LocationCsvImportParser.parse(csv)

        assertEquals(0, result.parsedPoints.size)
        assertTrue(result.errors.first().contains("Missing required columns"))
    }

    @Test
    fun parse_invalidRows_skipsAndCollectsErrors() {
        val csv = """
            latitude,longitude,accuracy,recorded_timestamp,tracking_mode
            200,-87.6,12.5,1700000000000,BALANCED
            41.1,-500,12.5,1700000000000,BALANCED
            41.1,-87.6,12.5,not_a_timestamp,BALANCED
            41.1,-87.6,12.5,1700000000000,UNKNOWN
        """.trimIndent()

        val result = LocationCsvImportParser.parse(csv)

        assertEquals(4, result.rowsRead)
        assertEquals(0, result.parsedPoints.size)
        assertEquals(4, result.rowsSkipped)
        assertEquals(4, result.errors.size)
    }
}
