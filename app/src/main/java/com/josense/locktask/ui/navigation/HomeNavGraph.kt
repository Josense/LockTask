package com.josense.locktask.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.josense.locktask.domain.notification.NotificationController
import com.josense.locktask.ui.notification.BackgroundGuide
import com.josense.locktask.ui.notification.NavigationEffect
import com.josense.locktask.ui.page.main.MainPageRoot
import com.josense.locktask.ui.page.task_detail.TaskDetailRoot

@Composable
fun HomeNavGraph(notifications: NotificationController) {
    val navController = rememberNavController()
    notifications.NavigationEffect {
        navController.popBackStack<Home>(inclusive = false)
    }
    notifications.BackgroundGuide()
    NavHost(
        navController = navController,
        startDestination = Home,
        popEnterTransition = { AppNavTransitions.DefaultEnter },
        popExitTransition = { AppNavTransitions.DefaultExit },
        // 手势返回：使用相同效果，由导航系统处理进度与取消
        predictivePopEnterTransition = { AppNavTransitions.DefaultEnter },
        predictivePopExitTransition = { AppNavTransitions.DefaultExit },
    ) {
        composable<Home> {
            MainPageRoot(
                onTaskClick = { task ->
                    navController.navigate(TaskDetail(task.id))
                },
                notifications = notifications,
            )
        }
        composable<TaskDetail> {
            TaskDetailRoot(onBackClick = { navController.popBackStack() })
        }
    }
}
