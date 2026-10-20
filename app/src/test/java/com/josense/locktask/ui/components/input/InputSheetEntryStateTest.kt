package com.josense.locktask.ui.components.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputSheetEntryStateTest {
    @Test
    fun panelWaitsForKeyboardInsteadOfAppearingAtScreenBottom() {
        val entry = InputSheetEntryState(initiallyVisible = false)
        entry.onInsetsChanged(bottom = 60, target = 900, navigationBottom = 60)
        assertFalse(entry.presentation(60, 900, 60).visible)
        assertEquals(1f, entry.presentation(60, 900, 60).hiddenFraction, 0f)
    }

    @Test
    fun renderUsesCurrentInsetsEvenBeforeObserverRuns() {
        val entry = InputSheetEntryState(initiallyVisible = false)
        val presentation = entry.presentation(bottom = 480, target = 900, navigationBottom = 60)
        assertTrue(presentation.visible)
        assertEquals(0.5f, presentation.hiddenFraction, 0.001f)
    }

    @Test
    fun keyboardClosingAndReopeningDoesNotReplayFirstEntry() {
        val entry = InputSheetEntryState(initiallyVisible = false)
        entry.onInsetsChanged(480, 900, 60)
        entry.onInsetsChanged(900, 900, 60)
        entry.onInsetsChanged(60, 0, 60)
        assertTrue(entry.presentation(60, 0, 60).visible)
        entry.onInsetsChanged(480, 1100, 60)
        assertEquals(0f, entry.presentation(480, 1100, 60).hiddenFraction, 0f)
    }

    @Test
    fun cancellingKeyboardEntryKeepsPanelAvailable() {
        val entry = InputSheetEntryState(initiallyVisible = false)
        entry.onInsetsChanged(480, 900, 60)
        entry.onInsetsChanged(300, 0, 60)
        entry.onInsetsChanged(60, 0, 60)
        assertTrue(entry.presentation(60, 0, 60).visible)
        assertEquals(0f, entry.presentation(480, 900, 60).hiddenFraction, 0f)
    }

    @Test
    fun unavailableKeyboardFallbackRemainsVisibleIfKeyboardArrivesLater() {
        val entry = InputSheetEntryState(initiallyVisible = false)
        entry.revealWithoutKeyboard()
        assertTrue(entry.presentation(60, 0, 60).visible)
        assertEquals(0f, entry.presentation(480, 900, 60).hiddenFraction, 0f)
    }

    @Test
    fun hardwareKeyboardDoesNotRequireImeInsets() {
        val entry = InputSheetEntryState(initiallyVisible = true)
        assertTrue(entry.presentation(0, 0, 0).visible)
        assertEquals(0f, entry.presentation(0, 0, 0).hiddenFraction, 0f)
    }

    @Test
    fun newSessionWaitsForKeyboardAgain() {
        val previous = InputSheetEntryState(initiallyVisible = false)
        previous.onInsetsChanged(900, 900, 60)
        val next = InputSheetEntryState(initiallyVisible = false)
        assertFalse(next.presentation(60, 0, 60).visible)
    }
}
