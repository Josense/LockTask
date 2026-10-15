package com.josense.locktask.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Home


@Serializable
data class TaskDetail(val taskId: Int)