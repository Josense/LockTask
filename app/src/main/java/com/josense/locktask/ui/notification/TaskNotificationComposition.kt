package com.josense.locktask.ui.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.josense.locktask.domain.notification.NotificationAction
import com.josense.locktask.domain.notification.NotificationController
import com.josense.locktask.domain.notification.NotificationTarget
import com.josense.locktask.ui.page.home.HomeAction
import com.josense.locktask.ui.page.home.HomeListState
import com.josense.locktask.ui.page.home.HomeState

@Composable
fun NotificationController.NavigationEffect(returnHome: () -> Unit) {
    LaunchedEffect(state.target?.requestId) {
        if (state.target != null) returnHome()
    }
}

@Composable
fun NotificationController.HomeEffect(home: HomeState, dispatch: (HomeAction) -> Unit, onCreateRequested: () -> Unit) {
    val target = state.target
    LaunchedEffect(target, home.list) {
        when (target) {
            is NotificationTarget.Create -> onCreateRequested()
            is NotificationTarget.Task -> {
                if (home.list == HomeListState.Loading) return@LaunchedEffect
                val tasks = (home.list as? HomeListState.Content)?.tasks.orEmpty()
                if (tasks.any { it.id == target.taskId && it.isCompleted.not() && it.isDeleted.not() }) {
                    dispatch(HomeAction.AnchorTask(target.taskId))
                }
            }
            null -> return@LaunchedEffect
        }
        onAction(NotificationAction.Consume(target.requestId))
    }
}

@Composable
fun NotificationController.AccessNotice(home: HomeState) {
    val tasks = (home.list as? HomeListState.Content)?.tasks.orEmpty()
    val hasReminders = tasks.any {
        it.needLockNotify && it.isCompleted.not() && it.isDeleted.not()
    }
    AccessNotice(hasReminders)
}
