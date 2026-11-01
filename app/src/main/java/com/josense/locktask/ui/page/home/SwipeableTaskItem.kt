package com.josense.locktask.ui.page.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.josense.locktask.R
import kotlin.math.abs
import kotlinx.coroutines.launch

@Composable
internal fun SwipeableTaskItem(
    enabled: Boolean,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    failureId: Long? = null,
    onSwiped: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val triggerDistance = with(LocalDensity.current) { 64.dp.toPx() }
    val state = rememberSwipeToDismissBoxState(
        positionalThreshold = { width -> minOf(width * 0.2f, triggerDistance) },
    )
    val scope = rememberCoroutineScope()
    var gestureDirection by remember { mutableStateOf<SwipeToDismissBoxValue?>(null) }
    var gestureCancelled by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    LaunchedEffect(failureId) {
        if (failureId != null) {
            state.reset()
            submitted = false
        }
    }
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val completeDirection = if (isLtr) SwipeToDismissBoxValue.EndToStart else SwipeToDismissBoxValue.StartToEnd
    val completeLabel = stringResource(R.string.complete)
    val deleteLabel = stringResource(R.string.delete)
    SwipeToDismissBox(
        state = state,
        gesturesEnabled = enabled,
        modifier = Modifier.clip(MaterialTheme.shapes.large).pointerInput(enabled, isLtr) {
            if (enabled) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    gestureDirection = null
                    gestureCancelled = false
                    var peak = 0f
                    var sign = 0f
                    var vertical = false
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        val displacement = change.position - down.position
                        if (sign == 0f && vertical.not()) {
                            if (abs(displacement.y) > viewConfiguration.touchSlop &&
                                abs(displacement.y) > abs(displacement.x)
                            ) {
                                vertical = true
                            } else if (abs(displacement.x) > viewConfiguration.touchSlop) {
                                sign = if (displacement.x > 0f) 1f else -1f
                                peak = abs(displacement.x)
                                gestureDirection = if ((sign > 0f) == isLtr) {
                                    SwipeToDismissBoxValue.StartToEnd
                                } else {
                                    SwipeToDismissBoxValue.EndToStart
                                }
                            }
                        }
                        if (sign != 0f && gestureCancelled.not()) {
                            val progress = displacement.x * sign
                            peak = maxOf(peak, progress)
                            // Ignore finger jitter, but an intentional reversal cancels this entire gesture.
                            if (peak - progress > viewConfiguration.touchSlop) {
                                gestureCancelled = true
                                scope.launch { state.reset() }
                            }
                        }
                        if (gestureCancelled) event.changes.forEach { it.consume() }
                        if (event.changes.none { it.pressed } && sign != 0f) {
                            val direction = gestureDirection
                            val shouldDismiss = gestureCancelled.not() &&
                                abs(state.requireOffset()) >= minOf(size.width * 0.2f, triggerDistance)
                            // Decide after the draggable handles release, so a slow swipe uses the
                            // same distance threshold as a fast one rather than nearest-anchor snapping.
                            awaitPointerEvent(PointerEventPass.Final)
                            scope.launch {
                                if (shouldDismiss && direction != null) state.dismiss(direction) else state.reset()
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
        }.semantics {
            customActions = if (enabled) {
                listOf(
                    CustomAccessibilityAction(completeLabel) {
                        onComplete()
                        true
                    },
                    CustomAccessibilityAction(deleteLabel) {
                        onDelete()
                        true
                    },
                )
            } else {
                emptyList()
            }
        },
        onDismiss = { direction ->
            if (gestureCancelled || direction != gestureDirection) {
                scope.launch { state.reset() }
            } else if (enabled && submitted.not() && direction != SwipeToDismissBoxValue.Settled) {
                submitted = true
                onSwiped()
                if (direction == completeDirection) onComplete() else onDelete()
            }
        },
        backgroundContent = {
            val direction = state.dismissDirection
            if (direction != SwipeToDismissBoxValue.Settled) {
                TaskRemovalBackground(
                    complete = direction == completeDirection,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        },
    ) {
        Box(Modifier.background(MaterialTheme.colorScheme.surface)) { content() }
    }
}

/** Shared by direct swipes and removals initiated on the detail page. */
@Composable
internal fun TaskRemovalBackground(complete: Boolean, modifier: Modifier = Modifier) {
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val background = if (complete) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val foreground = if (complete) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    Box(
        modifier = modifier.background(background).padding(horizontal = 24.dp),
        contentAlignment = if (complete == isLtr) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (complete) Icons.Outlined.Done else Icons.Outlined.Delete, null, tint = foreground)
            Text(stringResource(if (complete) R.string.complete else R.string.delete), color = foreground)
        }
    }
}
