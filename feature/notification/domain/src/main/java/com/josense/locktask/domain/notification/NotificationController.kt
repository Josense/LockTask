package com.josense.locktask.domain.notification

/** Activity-scoped notification interaction exposed without Android or UI types. */
interface NotificationController {
    val state: NotificationUiState
    fun onAction(action: NotificationAction)
}

sealed interface NotificationTarget {
    val requestId: Long
    data class Task(override val requestId: Long, val taskId: Int) : NotificationTarget
    data class Create(override val requestId: Long) : NotificationTarget
}

data class NotificationUiState(
    val target: NotificationTarget? = null,
    val access: NotificationAccess = NotificationAccess.Available,
    val backgroundGuideVisible: Boolean = false,
    val backgroundNoticeDismissed: Boolean = false,
)

sealed interface NotificationAction {
    data object Refresh : NotificationAction
    data object Enable : NotificationAction
    data object ShowBackgroundGuide : NotificationAction
    data object HideBackgroundGuide : NotificationAction
    data object DismissBackgroundNotice : NotificationAction
    data object OpenNotificationSettings : NotificationAction
    data object OpenBackgroundSettings : NotificationAction
    data class Created(val taskId: Int) : NotificationAction
    data class Consume(val requestId: Long) : NotificationAction
}

fun interface NotificationReminderLookup {
    suspend fun needsReminder(taskId: Int): Boolean
}

fun interface NotificationReconciler {
    fun reconcile()
}
