package com.josense.locktask.ui.page.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable

internal object TaskRemovalTiming {
    const val HOLD_MILLIS = 100L
    const val SLIDE_MILLIS = 500
    const val COLLAPSE_MILLIS = 200
    const val EXIT_MILLIS = SLIDE_MILLIS + COLLAPSE_MILLIS
    // Allow the final animation frame to render before removing the retained row.
    private const val FINAL_FRAME_BUFFER_MILLIS = 50
    const val REMOVAL_ACK_DELAY_MILLIS = EXIT_MILLIS + FINAL_FRAME_BUFFER_MILLIS
}

/** Use the same exit for every row removed from the active list. */
@Composable
internal fun RemovingTaskItem(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        exit = slideOutHorizontally(tween(TaskRemovalTiming.SLIDE_MILLIS)) { width -> width } +
            shrinkVertically(tween(
                durationMillis = TaskRemovalTiming.COLLAPSE_MILLIS,
                delayMillis = TaskRemovalTiming.SLIDE_MILLIS,
            )),
    ) {
        content()
    }
}
