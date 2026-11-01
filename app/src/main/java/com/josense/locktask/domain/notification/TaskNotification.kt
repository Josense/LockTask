package com.josense.locktask.domain.notification

/**
 * Identity is independent of the represented tasks to support future
 * aggregation.
 */
data class TaskNotification(
    val key: String,
    val taskIds: List<Int>,
    val title: String,
    val content: String,
) {
    init {
        require(taskIds.isNotEmpty())
    }
}
