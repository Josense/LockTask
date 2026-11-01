package com.josense.locktask.ui.page.task_detail

sealed class TaskDetailAction {
    object DismissSaveError : TaskDetailAction()
    object Delete : TaskDetailAction()
    object Complete : TaskDetailAction()
    data class SetLockNotify(val enabled: Boolean) : TaskDetailAction()
}