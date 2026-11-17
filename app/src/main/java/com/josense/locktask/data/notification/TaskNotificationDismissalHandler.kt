package com.josense.locktask.data.notification

import com.josense.locktask.domain.notification.NotificationDismissalHandler
import com.josense.locktask.domain.repository.TaskReminderPreferences

class TaskNotificationDismissalHandler(private val preferences: TaskReminderPreferences) :
    NotificationDismissalHandler {
    override suspend fun onDismissed(taskIds: List<Int>) = preferences.disableDismissedReminders(taskIds)
}
