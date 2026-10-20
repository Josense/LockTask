package com.josense.locktask.ui.page.create

sealed interface CreateTaskAction {
    data class TitleChanged(val title: String) : CreateTaskAction
    data object ToggleLockNotify : CreateTaskAction
    data object Submit : CreateTaskAction
}
