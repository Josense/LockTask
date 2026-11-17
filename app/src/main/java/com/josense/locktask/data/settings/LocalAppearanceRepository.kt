package com.josense.locktask.data.settings

import android.content.SharedPreferences
import com.josense.locktask.domain.settings.AppearanceMode
import com.josense.locktask.domain.settings.AppearanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class LocalAppearanceRepository(private val preferences: SharedPreferences) : AppearanceRepository {
    private val storedMode = preferences.getString(MODE_KEY, null)
    private val mutableMode = MutableStateFlow(
        AppearanceMode.entries.firstOrNull { it.name == storedMode } ?: AppearanceMode.System,
    )
    override val mode = mutableMode.asStateFlow()

    override fun setMode(mode: AppearanceMode) {
        preferences.edit().putString(MODE_KEY, mode.name).apply()
        mutableMode.value = mode
    }

    private companion object {
        const val MODE_KEY = "appearance_mode"
    }
}
