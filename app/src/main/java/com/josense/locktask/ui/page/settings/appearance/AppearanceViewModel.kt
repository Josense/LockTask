package com.josense.locktask.ui.page.settings.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josense.locktask.domain.settings.AppearanceMode
import com.josense.locktask.domain.settings.AppearanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class AppearanceState(val mode: AppearanceMode = AppearanceMode.System, val showPicker: Boolean = false)

internal sealed interface AppearanceAction {
    data object ShowPicker : AppearanceAction
    data object DismissPicker : AppearanceAction
    data class SelectMode(val mode: AppearanceMode) : AppearanceAction
}

internal class AppearanceViewModel(private val repository: AppearanceRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(AppearanceState(mode = repository.mode.value))
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.mode.collect { mode -> mutableState.update { it.copy(mode = mode) } }
        }
    }

    fun onAction(action: AppearanceAction) {
        when (action) {
            AppearanceAction.ShowPicker -> mutableState.update { it.copy(showPicker = true) }
            AppearanceAction.DismissPicker -> mutableState.update { it.copy(showPicker = false) }
            is AppearanceAction.SelectMode -> {
                repository.setMode(action.mode)
                mutableState.update { it.copy(mode = action.mode, showPicker = false) }
            }
        }
    }
}
