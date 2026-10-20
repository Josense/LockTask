package com.josense.locktask.ui.page.home

import com.josense.locktask.domain.model.Task

sealed interface HomeAction {
    data class TaskClick(val task: Task) : HomeAction
}
