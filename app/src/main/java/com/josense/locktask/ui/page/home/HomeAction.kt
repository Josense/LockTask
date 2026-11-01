package com.josense.locktask.ui.page.home

import com.josense.locktask.domain.model.Task

sealed interface HomeAction {
    data class CompleteTask(val taskId: Int) : HomeAction
    data class DeleteTask(val taskId: Int) : HomeAction
    data class AnchorTask(val taskId: Int) : HomeAction
    data class AnchorHandled(val taskId: Int) : HomeAction
    data class RemovalFinished(val taskId: Int, val operationId: Long) : HomeAction
    data class UndoRemoval(val task: Task) : HomeAction
    data class TaskClick(val task: Task) : HomeAction
}
