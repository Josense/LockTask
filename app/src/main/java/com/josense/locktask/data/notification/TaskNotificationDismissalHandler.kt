package com.josense.locktask.data.notification

import com.josense.locktask.data.database.dao.TaskDao
import com.josense.locktask.domain.notification.NotificationDismissalHandler
import java.util.Date

class TaskNotificationDismissalHandler(private val taskDao: TaskDao) : NotificationDismissalHandler {
    override suspend fun onDismissed(taskIds: List<Int>) {
        val ids = taskIds.filter { it > 0 }.distinct()
        if (ids.isNotEmpty()) taskDao.disableDismissedTaskNotifications(ids, Date())
    }
}
