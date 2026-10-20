package com.josense.locktask.ui.page.home

import com.josense.locktask.domain.model.Task

sealed interface HomeListState {
    data object Loading : HomeListState
    data object Empty : HomeListState
    data class Content(val tasks: List<Task>) : HomeListState
}


data class HomeState(
    val list: HomeListState = HomeListState.Loading,
    val pendingAnchorTaskId: Int? = null,
)
