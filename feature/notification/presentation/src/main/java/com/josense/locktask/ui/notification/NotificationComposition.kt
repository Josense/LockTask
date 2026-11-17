package com.josense.locktask.ui.notification

import androidx.compose.runtime.Composable
import com.josense.locktask.domain.notification.NotificationAccess
import com.josense.locktask.domain.notification.NotificationAction
import com.josense.locktask.domain.notification.NotificationController

@Composable
fun NotificationController.BackgroundGuide() {
    if (state.backgroundGuideVisible) {
        BackgroundReminderGuide(
            onOpenSettings = { onAction(NotificationAction.OpenBackgroundSettings) },
            onClose = { onAction(NotificationAction.HideBackgroundGuide) },
        )
    }
}

fun NotificationController.taskCreated(taskId: Int) {
    onAction(NotificationAction.Created(taskId))
}

@Composable
fun NotificationController.AccessNotice(hasReminders: Boolean) {
    if (hasReminders && state.access != NotificationAccess.Available) {
        NotificationAccessCard(state.access) { onAction(NotificationAction.Enable) }
    } else if (hasReminders) {
        BackgroundReminderNotice(
            dismissed = state.backgroundNoticeDismissed,
            onShowGuide = { onAction(NotificationAction.ShowBackgroundGuide) },
            onDismiss = { onAction(NotificationAction.DismissBackgroundNotice) },
        )
    }
}
