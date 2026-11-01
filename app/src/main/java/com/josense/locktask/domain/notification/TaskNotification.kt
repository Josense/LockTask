package com.josense.locktask.domain.notification

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
