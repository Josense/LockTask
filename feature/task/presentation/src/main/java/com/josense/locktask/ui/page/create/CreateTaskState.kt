package com.josense.locktask.ui.page.create

data class CreateTaskState(
    val title: String = "",
    val description: String = "",
    val needLockNotify: Boolean = true,
    val submission: Submission = Submission.Idle,
)

sealed interface Submission {
    data object Idle : Submission
    data object Saving : Submission
    data object Failed : Submission
    data class Succeeded(val taskId: Int) : Submission
}
