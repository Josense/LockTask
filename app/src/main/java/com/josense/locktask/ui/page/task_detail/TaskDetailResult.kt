package com.josense.locktask.ui.page.task_detail

import kotlinx.serialization.Serializable

const val TASK_DETAIL_RESULT_KEY = "task_detail_result"

data class TaskDetailResult(
    val taskId: Int,
    val completed: Boolean,
)