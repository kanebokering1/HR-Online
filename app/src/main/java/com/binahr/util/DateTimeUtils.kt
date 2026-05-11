package com.binahr.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Utilities for parsing ISO 8601 timestamps from the API
 * (e.g. "2026-05-11T08:30:00.000000Z") into display-friendly strings.
 *
 * All API timestamps are UTC. Times are converted to the device local timezone.
 */
object DateTimeUtils {

    private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /** "2026-05-11T08:30:00.000000Z" → "08:30" (local time) */
    fun toTime(iso: String?): String {
        if (iso.isNullOrBlank()) return "--:--"
        return try {
            val date = isoParser.parse(iso.take(19)) ?: return "--:--"
            SimpleDateFormat("HH:mm", Locale.getDefault())
                .apply { timeZone = TimeZone.getDefault() }
                .format(date)
        } catch (_: Exception) {
            "--:--"
        }
    }

    /** "2026-05-11T08:30:00.000000Z" → "11 Mei 2026" */
    fun toDate(iso: String?): String {
        if (iso.isNullOrBlank()) return "-"
        return try {
            val date = isoParser.parse(iso.take(19)) ?: return "-"
            SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
                .apply { timeZone = TimeZone.getDefault() }
                .format(date)
        } catch (_: Exception) {
            iso.take(10)
        }
    }

    /** "2026-05-11T08:30:00.000000Z" → "11 Mei 2026, 08:30" */
    fun toDateTime(iso: String?): String {
        if (iso.isNullOrBlank()) return "-"
        return try {
            val date = isoParser.parse(iso.take(19)) ?: return "-"
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
                .apply { timeZone = TimeZone.getDefault() }
                .format(date)
        } catch (_: Exception) {
            iso.take(16).replace("T", " ")
        }
    }

    /** "2026-05-11T08:30:00.000000Z" → "2026-05-11" (raw date for comparisons) */
    fun toRawDate(iso: String?): String = iso?.take(10) ?: ""
}
