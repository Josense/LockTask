package com.josense.locktask.ui.page.home

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.josense.locktask.domain.model.Task
import com.josense.locktask.ui.theme.LockTaskTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeTaskRowStateTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun savedSwipeStaysOffscreenUntilIndependentAnimationFinishes() {
        val operation = mutableStateOf<TaskOperation?>(null)
        val finished = mutableListOf<HomeAction.RemovalFinished>()
        compose.setContent {
            LockTaskTheme {
                HomeTaskRow(Task(1, "Swipe task"), operation.value, false, 0) { action ->
                    when (action) {
                        is HomeAction.CompleteTask ->
                            operation.value =
                                TaskOperation.Saving(1, TaskRemovalKind.Complete)
                        is HomeAction.RemovalFinished -> finished.add(action)
                        else -> Unit
                    }
                }
            }
        }
        compose.onNodeWithText("Swipe task").performTouchInput { swipeLeft() }
        compose.waitForIdle()
        compose.onNodeWithText("Swipe task").assertIsNotDisplayed()
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { operation.value = TaskOperation.Removing(1, TaskRemovalKind.Complete) }
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Swipe task").assertIsNotDisplayed()
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(listOf(HomeAction.RemovalFinished(1, 1)), finished) }
    }

    @Test
    fun startingSecondRemovalDoesNotRestartFirstAnimation() {
        val first = mutableStateOf<TaskOperation?>(null)
        val second = mutableStateOf<TaskOperation?>(null)
        val finished = mutableListOf<Int>()
        compose.setContent {
            LockTaskTheme {
                Column {
                    HomeTaskRow(Task(1, "First"), first.value, false, 0) {
                        if (it is HomeAction.RemovalFinished) finished.add(it.taskId)
                    }
                    HomeTaskRow(Task(2, "Second"), second.value, false, 0) {
                        if (it is HomeAction.RemovalFinished) finished.add(it.taskId)
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { first.value = TaskOperation.Removing(1, TaskRemovalKind.Complete) }
        compose.mainClock.advanceTimeBy(450)
        compose.runOnIdle { second.value = TaskOperation.Removing(2, TaskRemovalKind.Delete) }
        compose.mainClock.advanceTimeBy(450)
        compose.runOnIdle { assertEquals(listOf(1), finished) }
        compose.mainClock.advanceTimeBy(500)
        compose.runOnIdle { assertEquals(listOf(1, 2), finished) }
        assertTrue(first.value is TaskOperation.Removing)
    }
}
