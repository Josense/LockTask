package com.josense.locktask.ui.page.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.josense.locktask.R
import com.josense.locktask.domain.time.TaskTimeDisplay
import com.josense.locktask.domain.time.taskTimeDisplay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@Composable
internal fun TaskCreatedTime(createdAt: Date, nowMillis: Long) {
    val locale = LocalConfiguration.current.locales[0]
    val display = taskTimeDisplay(createdAt.time, nowMillis)
    val pattern = when (display) {
        TaskTimeDisplay.Today, TaskTimeDisplay.Yesterday -> "HH:mm"
        TaskTimeDisplay.ThisYear -> "MM-dd HH:mm"
        TaskTimeDisplay.OtherYear -> "yyyy-MM-dd HH:mm"
    }
    val formatted = SimpleDateFormat(pattern, locale).format(createdAt)
    val text = when (display) {
        TaskTimeDisplay.Yesterday -> stringResource(R.string.task_created_yesterday, formatted)
        else -> formatted
    }
    Text(
        text = text,
        textAlign = TextAlign.Start,
        maxLines = 1,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** One clock for the visible list; refresh on return and at minute boundaries. */
@Composable
internal fun currentTaskTimeMillis(): Long {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val now by produceState(System.currentTimeMillis(), lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val minuteMillis = TimeUnit.MINUTES.toMillis(1)
            while (true) {
                value = System.currentTimeMillis()
                delay((minuteMillis - value % minuteMillis).milliseconds)
            }
        }
    }
    return now
}
