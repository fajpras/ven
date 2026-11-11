package com.ven.app.util

import com.ven.app.R
import com.ven.app.core.localization.AppStrings
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

/**
 * Utilitas pemformat tanggal untuk aplikasi VEN.
 * Sesuai spesifikasi arsitektur DESIGN.md A3.2 & A8.6.1.
 */
object DateFormatter {

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
        return date.format(DateTimeFormatter.ofPattern("EEE, d MMMM yyyy", AppStrings.locale()))
    }

    /**
     * Memformat YearMonth menjadi format "September 2026".
     */
    fun formatMonthYear(yearMonth: YearMonth): String {
        return yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", AppStrings.locale()))
    }

    /**
     * Mengubah format tanggal ISO / SQL datetime menjadi format waktu relatif ala Instagram
     * (mis. "Just now", "2 hours ago", "1 day ago", "3 days ago").
     */
    fun formatRelativeTime(dateString: String?, nowMillis: Long = System.currentTimeMillis()): String {
        if (dateString.isNullOrBlank()) return AppStrings.get(R.string.time_just_now)

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
        if (diffMillis <= 0) return AppStrings.get(R.string.time_just_now)

        val seconds = diffMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> AppStrings.get(R.string.time_just_now)
            minutes < 60 -> if (minutes == 1L) {
                AppStrings.get(R.string.time_minute_ago)
            } else {
                AppStrings.get(R.string.time_minutes_ago, minutes)
            }
            hours < 24 -> if (hours == 1L) {
                AppStrings.get(R.string.time_hour_ago)
            } else {
                AppStrings.get(R.string.time_hours_ago, hours)
            }
            days < 7 -> if (days == 1L) {
                AppStrings.get(R.string.time_day_ago)
            } else {
                AppStrings.get(R.string.time_days_ago, days)
            }
            weeks < 4 -> if (weeks == 1L) {
                AppStrings.get(R.string.time_week_ago)
            } else {
                AppStrings.get(R.string.time_weeks_ago, weeks)
            }
            months < 12 -> if (months == 1L) {
                AppStrings.get(R.string.time_month_ago)
            } else {
                AppStrings.get(R.string.time_months_ago, months)
            }
            else -> if (years == 1L) {
                AppStrings.get(R.string.time_year_ago)
            } else {
                AppStrings.get(R.string.time_years_ago, years)
            }
        }
    }
}
