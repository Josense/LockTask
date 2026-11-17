package com.josense.locktask.domain.repository

interface TaskReminderPreferences {
    suspend fun disableDismissedReminders(taskIds: List<Int>)
}
