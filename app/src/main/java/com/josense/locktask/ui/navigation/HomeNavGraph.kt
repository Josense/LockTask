package com.josense.locktask.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.josense.locktask.ui.page.home.HomePageRoot
import com.josense.locktask.ui.page.task_detail.TaskDetailRoot

@Composable
fun HomeNavGraph(
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Home,
        popEnterTransition = { AppNavTransitions.DefaultEnter },
        popExitTransition = { AppNavTransitions.DefaultExit },
        // 手势返回：使用相同效果，由导航系统处理进度与取消
        predictivePopEnterTransition = { AppNavTransitions.DefaultEnter },
        predictivePopExitTransition = { AppNavTransitions.DefaultExit }
    ) {
        composable<Home> {
            HomePageRoot(onTaskClick = { task ->
                navController.navigate(TaskDetail(task.id))
            })
        }
        composable<TaskDetail> {
            TaskDetailRoot(onBackClick = { navController.popBackStack() })
        }
    }
}
