package com.josense.locktask.domain.settings

import kotlinx.coroutines.flow.StateFlow

interface AppearanceRepository {
    val mode: StateFlow<AppearanceMode>

    fun setMode(mode: AppearanceMode)
}
