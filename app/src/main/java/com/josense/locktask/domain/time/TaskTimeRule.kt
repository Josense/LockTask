package com.josense.locktask.domain.time

import java.util.Calendar
import java.util.TimeZone

/** Calendar-day rules in the user's timezone, independent of Android and display language. */
enum class TaskTimeDisplay {
    Today,
    Yesterday,
    ThisYear,
    OtherYear,
}

fun taskTimeDisplay(
    timestampMillis: Long,
    nowMillis: Long,
    timeZone: TimeZone = TimeZone.getDefault(),
): TaskTimeDisplay {
    val timestamp = Calendar.getInstance(timeZone).apply { timeInMillis = timestampMillis }
    val today = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }
    val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    return when {
        timestamp.isSameDay(today) -> TaskTimeDisplay.Today
        timestamp.isSameDay(yesterday) -> TaskTimeDisplay.Yesterday
        timestamp.get(Calendar.ERA) == today.get(Calendar.ERA) &&
            timestamp.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> TaskTimeDisplay.ThisYear
        else -> TaskTimeDisplay.OtherYear
    }
}

private fun Calendar.isSameDay(other: Calendar): Boolean = get(Calendar.ERA) == other.get(Calendar.ERA) &&
    get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
    get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
