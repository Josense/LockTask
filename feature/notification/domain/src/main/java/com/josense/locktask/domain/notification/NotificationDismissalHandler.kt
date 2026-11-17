package com.josense.locktask.domain.notification

interface NotificationDismissalHandler {
    suspend fun onDismissed(taskIds: List<Int>)
}
