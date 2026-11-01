package com.josense.locktask.ui.page.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import com.josense.locktask.ui.theme.LockTaskTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SwipeableTaskItemTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun successfulSwipeDoesNotReturnToItsOriginalPosition() {
        compose.setContent {
            LockTaskTheme {
                SwipeableTaskItem(true, {}, {}) {
                    Box(Modifier.fillMaxWidth().height(80.dp).testTag("task"))
                }
            }
        }
        compose.onNodeWithTag("task").performTouchInput { swipeLeft() }
        compose.waitForIdle()
        compose.onNodeWithTag("task").assertIsNotDisplayed()
    }

    @Test
    fun failedSwipeReturnsAndAllowsRetryInTheOtherDirection() {
        var completed = 0
        var deleted = 0
        val failed = mutableStateOf(0L)
        compose.setContent {
            LockTaskTheme {
                SwipeableTaskItem(true, { completed++ }, { deleted++ }, failureId = failed.value.takeIf { it > 0 }) {
                    Box(Modifier.fillMaxWidth().height(80.dp).testTag("task"))
                }
            }
        }
        compose.onNodeWithTag("task").performTouchInput { swipeLeft() }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(1, completed)
            assertEquals(0, deleted)
        }
        compose.runOnIdle { failed.value++ }
        compose.waitForIdle()
        compose.onNodeWithTag("task").performTouchInput { swipeRight() }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(1, completed)
            assertEquals(1, deleted)
        }
        compose.runOnIdle { failed.value++ }
        compose.waitForIdle()
        compose.onNodeWithTag("task").assertIsDisplayed()
        compose.onNodeWithTag("task").performTouchInput { swipeLeft() }
        compose.runOnIdle { assertEquals(2, completed) }
    }

    @Test
    fun pendingRowIgnoresSwipes() {
        var actions = 0
        compose.setContent {
            LockTaskTheme {
                SwipeableTaskItem(false, { actions++ }, { actions++ }) {
                    Box(Modifier.fillMaxWidth().height(80.dp).testTag("task"))
                }
            }
        }
        compose.onNodeWithTag("task").performTouchInput { swipeLeft() }
        compose.onNodeWithTag("task").performTouchInput { swipeRight() }
        compose.runOnIdle { assertEquals(0, actions) }
    }
}
