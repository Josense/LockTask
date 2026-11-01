package com.josense.locktask.ui.page.task_detail

import com.josense.locktask.domain.model.Task

data class TaskDetailState(
    val task: Task? = null,
    val edit: TaskDetailEdit? = null,
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false,
    val isFinished: Boolean = false,
)

enum class TaskDetailField { Title, Description }

data class TaskDetailEdit(
    val field: TaskDetailField,
    val title: String,
    val description: String,
    val titleError: Boolean = false,
)
