package com.josense.locktask.ui.page.task_detail

sealed class TaskDetailAction {
    object Complete : TaskDetailAction()
    object Delete : TaskDetailAction()
}