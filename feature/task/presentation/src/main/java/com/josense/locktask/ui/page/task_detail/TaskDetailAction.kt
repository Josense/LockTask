package com.josense.locktask.ui.page.task_detail

sealed class TaskDetailAction {
    data class StartEditing(val field: TaskDetailField) : TaskDetailAction()
    data class TitleChanged(val title: String) : TaskDetailAction()
    data class DescriptionChanged(val description: String) : TaskDetailAction()
    object FinishEditing : TaskDetailAction()
    object DismissSaveError : TaskDetailAction()
    object Delete : TaskDetailAction()
    object Complete : TaskDetailAction()
    data class SetLockNotify(val enabled: Boolean) : TaskDetailAction()
}
