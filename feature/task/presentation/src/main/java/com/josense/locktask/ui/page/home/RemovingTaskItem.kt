package com.josense.locktask.ui.page.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.first

internal object TaskRemovalTiming {
    const val HOLD_MILLIS = 100L
    const val SLIDE_MILLIS = 500
    const val COLLAPSE_MILLIS = 200
    const val EXIT_MILLIS = SLIDE_MILLIS + COLLAPSE_MILLIS
}

/** Completed rows exit left, deleted rows exit right; swiped rows only collapse. */
@Composable
internal fun RemovingTaskItem(
    visible: Boolean,
    alreadySwiped: Boolean = false,
    direction: TaskRemovalDirection = TaskRemovalDirection.Right,
    operationId: Long? = null,
    onFinished: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val transition = remember(operationId) { MutableTransitionState(true) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(visible, lifecycle, operationId) {
        if (visible) {
            transition.targetState = true
        } else {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                transition.targetState = false
                snapshotFlow { transition.isIdle && transition.currentState.not() }.first { it }
                currentOnFinished()
            }
        }
    }
    AnimatedVisibility(
        visibleState = transition,
        exit = shrinkVertically(
            tween(
                durationMillis = TaskRemovalTiming.COLLAPSE_MILLIS,
                delayMillis = TaskRemovalTiming.HOLD_MILLIS.toInt() +
                    if (alreadySwiped) 0 else TaskRemovalTiming.SLIDE_MILLIS,
            ),
        ),
    ) {
        Box(Modifier.clip(MaterialTheme.shapes.large)) {
            if (visible.not() && alreadySwiped.not()) {
                TaskRemovalBackground(
                    complete = direction == TaskRemovalDirection.Left,
                    modifier = Modifier.matchParentSize(),
                )
            }
            Box(
                Modifier.animateEnterExit(
                    exit = if (alreadySwiped) {
                        ExitTransition.None
                    } else {
                        slideOutHorizontally(
                            tween(TaskRemovalTiming.SLIDE_MILLIS, delayMillis = TaskRemovalTiming.HOLD_MILLIS.toInt()),
                        ) { width ->
                            if (direction == TaskRemovalDirection.Left) -width else width
                        }
                    },
                ),
            ) {
                content()
            }
        }
    }
}
