package com.josense.locktask.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.josense.locktask.ui.page.home.HomePageRoot
import com.josense.locktask.ui.page.task_detail.TASK_DETAIL_RESULT_KEY
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
        composable<TaskDetail> { backStackEntry ->
            TaskDetailRoot(
                onBackClick = { result ->
                    if (result != null) {
//                        navController.previousBackStackEntry?.savedStateHandle?.set(
//                            TASK_DETAIL_RESULT_KEY,
//                            result
//                        )
                    }
                    navController.popBackStack()
                }
            )
        }
    }
}