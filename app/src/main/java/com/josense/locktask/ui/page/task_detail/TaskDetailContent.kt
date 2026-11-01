package com.josense.locktask.ui.page.task_detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.josense.locktask.R
import com.josense.locktask.domain.model.Task
import java.text.DateFormat

@Composable
internal fun TaskDetailContent(
    task: Task,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onEdit: (TaskDetailField) -> Unit = {},
) {
    SelectionContainer {
        TaskDetailLayout(
            task = task,
            modifier = modifier.clickable(
                enabled = task.isDeleted.not(),
                interactionSource = null,
                indication = null,
            ) { onEdit(TaskDetailField.Description) },
            scrollState = scrollState,
            title = {
                Text(
                    text = task.title,
                    modifier = Modifier.fillMaxWidth().clickable(enabled = task.isDeleted.not()) {
                        onEdit(TaskDetailField.Title)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            description = {
                Text(
                    modifier = Modifier.fillMaxWidth().clickable(enabled = task.isDeleted.not()) {
                        onEdit(TaskDetailField.Description)
                    },
                    text = task.content.ifBlank { stringResource(R.string.task_detail_no_description) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (task.content.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            },
        )
    }
}

/** Keep reading and editing on the same typography and spacing grid. */
@Composable
internal fun TaskDetailLayout(
    task: Task,
    modifier: Modifier,
    scrollState: ScrollState,
    title: @Composable () -> Unit,
    description: @Composable () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale)
    }
    Column(
        modifier = modifier.fillMaxSize().testTag("detail_content").verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = formatter.format(task.updateAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            title()
        }
        description()
    }
}
