package com.josense.locktask.ui.page.home

import androidx.compose.foundation.background
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.josense.locktask.R

@Composable
internal fun SwipeableTaskItem(
    enabled: Boolean,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    failureId: Long? = null,
    onSwiped: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState(positionalThreshold = { it * 0.4f })
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
        modifier = Modifier.clip(MaterialTheme.shapes.large).semantics {
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
            if (enabled && submitted.not() && direction != SwipeToDismissBoxValue.Settled) {
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
