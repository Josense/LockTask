package com.josense.locktask.ui.page.home

import com.josense.locktask.domain.model.Task

sealed interface HomeState {
    data object Loading : HomeState
    data object Empty : HomeState
    data class Content(val tasks: List<Task>) : HomeState
}
