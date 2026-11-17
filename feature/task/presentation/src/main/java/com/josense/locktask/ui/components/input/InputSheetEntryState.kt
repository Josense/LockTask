package com.josense.locktask.ui.components.input

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 首次入场状态一旦完成便保持不变，收起或重新打开软键盘时不再重播面板入场动画。 */
internal class InputSheetEntryState(initiallyVisible: Boolean) {
    var hasStarted by mutableStateOf(initiallyVisible)
        private set
    private var hasFinished by mutableStateOf(initiallyVisible)

    fun onInsetsChanged(bottom: Int, target: Int, navigationBottom: Int) {
        if (bottom > navigationBottom) hasStarted = true
        if (hasStarted && (target <= navigationBottom || bottom >= target)) {
            hasFinished = true
        }
    }

    fun revealWithoutKeyboard() {
        hasStarted = true
        hasFinished = true
    }

    fun presentation(bottom: Int, target: Int, navigationBottom: Int): InputSheetPresentation {
        val entering = !hasFinished && target > bottom && target > navigationBottom
        val progress = if (entering) {
            ((bottom - navigationBottom).toFloat() / (target - navigationBottom)).coerceIn(0f, 1f)
        } else {
            1f
        }
        return InputSheetPresentation(
            visible = hasStarted || bottom > navigationBottom,
            hiddenFraction = 1f - progress,
        )
    }
}

internal data class InputSheetPresentation(val visible: Boolean, val hiddenFraction: Float)
