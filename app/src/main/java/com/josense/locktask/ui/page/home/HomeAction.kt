package com.josense.locktask.ui.page.home

import com.josense.locktask.domain.model.Task

sealed interface HomeAction {
    data class AnchorTask(val taskId: Int) : HomeAction
    data class AnchorHandled(val taskId: Int) : HomeAction
    data class RemovalFinished(val taskIds: Set<Int>) : HomeAction
    data class UndoRemoval(val task: Task) : HomeAction
    data class TaskClick(val task: Task) : HomeAction
}
