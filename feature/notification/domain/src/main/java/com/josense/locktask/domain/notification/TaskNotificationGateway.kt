package com.josense.locktask.domain.notification

interface TaskNotificationGateway {
    suspend fun access(): NotificationAccess

    suspend fun setRetentionEnabled(enabled: Boolean)

    suspend fun publishedKeys(): Set<String>

    suspend fun publish(notification: TaskNotification)

    suspend fun cancel(key: String)
}
