package com.josense.locktask.domain.time

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskTimeRuleTest {
    private val shanghai = TimeZone.getTimeZone("Asia/Shanghai")

    @Test
    fun `same calendar day displays today`() {
        check("2026-10-20 00:00", "2026-10-20 23:59", TaskTimeDisplay.Today)
    }

    @Test
    fun `one minute across midnight displays yesterday`() {
        check("2026-10-19 23:59", "2026-10-20 00:00", TaskTimeDisplay.Yesterday)
    }

    @Test
    fun `yesterday takes priority over different year`() {
        check("2025-12-31 12:00", "2026-01-01 00:01", TaskTimeDisplay.Yesterday)
    }

    @Test
    fun `older dates distinguish this year from previous years`() {
        check("2026-01-01 10:00", "2026-10-20 10:00", TaskTimeDisplay.ThisYear)
        check("2025-10-20 10:00", "2026-10-20 10:00", TaskTimeDisplay.OtherYear)
    }

    @Test
    fun `calendar rules handle leap day`() {
        check("2024-02-29 10:00", "2024-03-01 10:00", TaskTimeDisplay.Yesterday)
    }

    @Test
    fun `yesterday uses calendar days across daylight saving changes`() {
        check(
            "2026-03-08 00:01",
            "2026-03-09 23:59",
            TaskTimeDisplay.Yesterday,
            TimeZone.getTimeZone("America/New_York"),
        )
        check(
            "2026-11-01 00:01",
            "2026-11-02 23:59",
            TaskTimeDisplay.Yesterday,
            TimeZone.getTimeZone("America/New_York"),
        )
    }

    @Test
    fun `same instant is classified using supplied timezone`() {
        val timestamp = parse("2026-10-19 23:30", TimeZone.getTimeZone("UTC"))
        val now = parse("2026-10-20 01:00", TimeZone.getTimeZone("UTC"))
        assertEquals(TaskTimeDisplay.Today, taskTimeDisplay(timestamp, now, shanghai))
        assertEquals(TaskTimeDisplay.Yesterday, taskTimeDisplay(timestamp, now, TimeZone.getTimeZone("UTC")))
    }

    private fun check(timestamp: String, now: String, expected: TaskTimeDisplay, zone: TimeZone = shanghai) {
        assertEquals(expected, taskTimeDisplay(parse(timestamp, zone), parse(now, zone), zone))
    }

    private fun parse(text: String, zone: TimeZone): Long =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).apply { timeZone = zone }.parse(text)!!.time
}
