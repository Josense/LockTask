package com.josense.locktask.ui.page.task_detail

import com.josense.locktask.domain.model.Task

data class TaskDetailState(
    val task: Task? = null,
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false,
    val isFinished: Boolean = false,
)
