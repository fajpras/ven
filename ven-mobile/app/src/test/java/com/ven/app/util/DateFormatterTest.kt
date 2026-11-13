package com.ven.app.util

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class DateFormatterTest {

    @Test
    fun formatRequestMeetingDate_formatsFullDateWithDayOfWeek() {
        val date = LocalDate.of(2026, 9, 30)
        val formatted = DateFormatter.formatRequestMeetingDate(date)
        assertEquals("Wed, 30 September 2026", formatted)
    }

    @Test
    fun formatRequestMeetingDate_singleDigitDay_formattedWithoutLeadingZero() {
        val date = LocalDate.of(2026, 9, 5)
        val formatted = DateFormatter.formatRequestMeetingDate(date)
        assertEquals("Sat, 5 September 2026", formatted)
    }

    @Test
    fun formatRequestMeetingDate_januaryAndDecember_formattedProperly() {
        val newYear = LocalDate.of(2027, 1, 1)
        assertEquals("Fri, 1 January 2027", DateFormatter.formatRequestMeetingDate(newYear))

        val yearEnd = LocalDate.of(2026, 12, 31)
        assertEquals("Thu, 31 December 2026", DateFormatter.formatRequestMeetingDate(yearEnd))
    }

    @Test
    fun formatMonthYear_formatsMonthNameAndYear() {
        val sept2026 = YearMonth.of(2026, 9)
        assertEquals("September 2026", DateFormatter.formatMonthYear(sept2026))

        val oct2026 = YearMonth.of(2026, 10)
        assertEquals("October 2026", DateFormatter.formatMonthYear(oct2026))

        val jan2027 = YearMonth.of(2027, 1)
        assertEquals("January 2027", DateFormatter.formatMonthYear(jan2027))
    }

    @Test
    fun formatMonthYear_leapYearFebruary_formattedProperly() {
        val feb2028 = YearMonth.of(2028, 2)
        assertEquals("February 2028", DateFormatter.formatMonthYear(feb2028))
    }

    @Test
    fun formatRelativeTime_nullOrBlank_returnsJustNow() {
        assertEquals("Just now", DateFormatter.formatRelativeTime(null))
        assertEquals("Just now", DateFormatter.formatRelativeTime(""))
        assertEquals("Just now", DateFormatter.formatRelativeTime("   "))
    }

    @Test
    fun formatRelativeTime_relativeDifferences_calculatedAccurately() {
        val now = 1_700_000_000_000L

        // 30 seconds ago
        val date30SecAgo = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.ENGLISH).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date(now - 30_000L))
        assertEquals("Just now", DateFormatter.formatRelativeTime(date30SecAgo, now))

        // 5 minutes ago
        val date5MinAgo = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.ENGLISH).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date(now - 5 * 60 * 1000L))
        assertEquals("5 minutes ago", DateFormatter.formatRelativeTime(date5MinAgo, now))

        // 1 hour ago
        val date1HourAgo = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.ENGLISH).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date(now - 1 * 3600 * 1000L))
        assertEquals("1 hour ago", DateFormatter.formatRelativeTime(date1HourAgo, now))

        // 1 day ago
        val date1DayAgo = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.ENGLISH).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date(now - 24 * 3600 * 1000L))
        assertEquals("1 day ago", DateFormatter.formatRelativeTime(date1DayAgo, now))

        // 3 days ago
        val date3DaysAgo = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.ENGLISH).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date(now - 3 * 24 * 3600 * 1000L))
        assertEquals("3 days ago", DateFormatter.formatRelativeTime(date3DaysAgo, now))
    }
}
