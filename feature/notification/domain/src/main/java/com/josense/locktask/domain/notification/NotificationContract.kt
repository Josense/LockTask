package com.josense.locktask.domain.notification

/** Stable identifiers retained across app upgrades and module moves. */
object NotificationContract {
    const val CHANNEL_ID = "persistent_tasks"
    const val URI_SCHEME = "locktask"
    const val TASK_HOST = "task"
    const val NOTIFICATION_KEY = "notification"
    const val CREATE_HOST = "create"
    const val DISMISS_SCHEME = "locktask-dismiss"
    const val DISMISS_HOST = "notification"
}
