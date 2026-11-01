package com.josense.locktask.ui.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.josense.locktask.domain.notification.NotificationAccess
import com.josense.locktask.ui.page.home.HomeAction
import com.josense.locktask.ui.page.home.HomeListState
import com.josense.locktask.ui.page.home.HomeState

@Composable
fun NotificationDelegate.NavigationEffect(returnHome: () -> Unit) {
    LaunchedEffect(state.target?.requestId) {
        if (state.target != null) returnHome()
    }
}

@Composable
fun NotificationDelegate.HomeEffect(home: HomeState, dispatch: (HomeAction) -> Unit, onCreateRequested: () -> Unit) {
    val target = state.target
    LaunchedEffect(target, home.list) {
        when (target) {
            is NotificationDelegate.Target.Create -> onCreateRequested()
            is NotificationDelegate.Target.Task -> {
                if (home.list == HomeListState.Loading) return@LaunchedEffect
                val tasks = (home.list as? HomeListState.Content)?.tasks.orEmpty()
                if (tasks.any { it.id == target.taskId && it.isCompleted.not() && it.isDeleted.not() }) {
                    dispatch(HomeAction.AnchorTask(target.taskId))
                }
            }
            null -> return@LaunchedEffect
        }
        onAction(NotificationDelegate.Action.Consume(target.requestId))
    }
}

@Composable
fun NotificationDelegate.AccessNotice(home: HomeState) {
    val tasks = (home.list as? HomeListState.Content)?.tasks.orEmpty()
    val hasReminders = tasks.any {
        it.needLockNotify && it.isCompleted.not() && it.isDeleted.not()
    }
    if (hasReminders && state.access != NotificationAccess.Available) {
        NotificationAccessCard(
            access = state.access,
            onEnable = { onAction(NotificationDelegate.Action.Enable) },
        )
    } else if (hasReminders) {
        BackgroundReminderNotice(
            dismissed = state.backgroundNoticeDismissed,
            onShowGuide = { onAction(NotificationDelegate.Action.ShowBackgroundGuide) },
            onDismiss = { onAction(NotificationDelegate.Action.DismissBackgroundNotice) },
        )
    }
}

@Composable
fun NotificationDelegate.BackgroundGuide() {
    if (state.backgroundGuideVisible) {
        BackgroundReminderGuide(
            onOpenSettings = { onAction(NotificationDelegate.Action.OpenBackgroundSettings) },
            onClose = { onAction(NotificationDelegate.Action.HideBackgroundGuide) },
        )
    }
}

fun NotificationDelegate.taskCreated(taskId: Int) {
    onAction(NotificationDelegate.Action.Created(taskId))
}
