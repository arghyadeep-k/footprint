package com.footprint.app.data.importing

object CsvImportSummaryFormatter {
    fun format(summary: CsvImportSummary): String {
        val base = "Import complete. Rows read: ${summary.rowsRead}, imported: ${summary.pointsImported}, skipped: ${summary.rowsSkipped}."
        if (summary.errors.isEmpty()) return base
        val preview = summary.errors.take(3).joinToString(" | ")
        val suffix = if (summary.errors.size > 3) " (+${summary.errors.size - 3} more errors)" else ""
        return "$base Errors: $preview$suffix"
    }
}
