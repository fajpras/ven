package com.ven.app.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

/**
 * Utilitas pemformat tanggal untuk aplikasi VEN.
 * Sesuai spesifikasi arsitektur DESIGN.md §3.2 & §8.6.1.
 */
object DateFormatter {

    private val requestMeetingPattern: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE, d MMMM yyyy", Locale.ENGLISH)

    private val monthYearPattern: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

    private val utcDatePatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
    )

    private val localDatePatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
    )

    /**
     * Memformat tanggal menjadi format "Sat, 26 September 2026".
     */
    fun formatRequestMeetingDate(date: LocalDate): String {
        return date.format(requestMeetingPattern)
    }

    /**
     * Memformat YearMonth menjadi format "September 2026".
     */
    fun formatMonthYear(yearMonth: YearMonth): String {
        return yearMonth.format(monthYearPattern)
    }

    /**
     * Mengubah format tanggal ISO / SQL datetime menjadi format waktu relatif ala Instagram
     * (mis. "Just now", "2 hours ago", "1 day ago", "3 days ago").
     */
    fun formatRelativeTime(dateString: String?, nowMillis: Long = System.currentTimeMillis()): String {
        if (dateString.isNullOrBlank()) return "Just now"

        var parsedMillis: Long? = null

        // Coba parsing pola UTC bila ada akhiran 'Z'
        if (dateString.endsWith("Z", ignoreCase = true)) {
            for (pattern in utcDatePatterns) {
                try {
                    val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val date = sdf.parse(dateString)
                    if (date != null) {
                        parsedMillis = date.time
                        break
                    }
                } catch (_: Exception) {
                }
            }
        }

        // Coba parsing pola lokal / tanpa timezone
        if (parsedMillis == null) {
            val allPatterns = if (dateString.endsWith("Z", ignoreCase = true)) localDatePatterns else (localDatePatterns + utcDatePatterns)
            for (pattern in allPatterns) {
                try {
                    val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
                    val date = sdf.parse(dateString)
                    if (date != null) {
                        parsedMillis = date.time
                        break
                    }
                } catch (_: Exception) {
                }
            }
        }

        if (parsedMillis == null) {
            return dateString
        }

        val diffMillis = nowMillis - parsedMillis
        if (diffMillis <= 0) return "Just now"

        val seconds = diffMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> if (minutes == 1L) "1 minute ago" else "$minutes minutes ago"
            hours < 24 -> if (hours == 1L) "1 hour ago" else "$hours hours ago"
            days < 7 -> if (days == 1L) "1 day ago" else "$days days ago"
            weeks < 4 -> if (weeks == 1L) "1 week ago" else "$weeks weeks ago"
            months < 12 -> if (months == 1L) "1 month ago" else "$months months ago"
            else -> if (years == 1L) "1 year ago" else "$years years ago"
        }
    }
}
