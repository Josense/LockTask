package com.josense.locktask.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry

object AppNavTransitions {
    private const val RETURN_DURATION_MILLIS = 220
    private const val RETURN_TARGET_SCALE = 0.96f

    val DefaultEnter: EnterTransition = EnterTransition.None

    val DefaultExit: ExitTransition =
        scaleOut(
            targetScale = RETURN_TARGET_SCALE,
            animationSpec = tween(
                RETURN_DURATION_MILLIS,
                easing = FastOutSlowInEasing,
            ),
        ) + fadeOut(
            animationSpec = tween(
                RETURN_DURATION_MILLIS,
                easing = FastOutSlowInEasing,
            ),
        )
}