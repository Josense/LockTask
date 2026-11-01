package com.josense.locktask.ui.notification

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.josense.locktask.R
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
fun NotificationDelegate.HomeEffect(
    home: HomeState,
    dispatch: (HomeAction) -> Unit,
) {
    val target = state.target
    LaunchedEffect(target, home.list) {
        if (target == null || home.list == HomeListState.Loading)
            return@LaunchedEffect
        val tasks = (home.list as? HomeListState.Content)?.tasks.orEmpty()
        if (
            tasks.any {
                it.id == target.taskId &&
                    it.isCompleted.not() &&
                    it.isDeleted.not()
            }
        ) {
            dispatch(HomeAction.AnchorTask(target.taskId))
        }
        onAction(NotificationDelegate.Action.Consume(target.requestId))
    }
}

@Composable
fun NotificationDelegate.AccessNotice(home: HomeState) {
    val tasks = (home.list as? HomeListState.Content)?.tasks.orEmpty()
    if (
        state.access != NotificationAccess.Available &&
            tasks.any {
                it.needLockNotify && it.isCompleted.not() && it.isDeleted.not()
            }
    ) {
        TextButton(onClick = { onAction(NotificationDelegate.Action.Enable) }) {
            Text(stringResource(R.string.notification_access_action))
        }
    }
}

fun NotificationDelegate.taskCreated(taskId: Int) {
    onAction(NotificationDelegate.Action.Created(taskId))
}
