package com.josense.locktask.ui.page.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.josense.locktask.ui.theme.LockTaskTheme
import org.junit.Rule
import org.junit.Test

class TaskRemovalBackgroundTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun detailCompletionShowsCompleteBackgroundDuringExit() {
        verifyBackground(TaskRemovalDirection.Left, "完成")
    }

    @Test
    fun detailDeletionShowsDeleteBackgroundDuringExit() {
        verifyBackground(TaskRemovalDirection.Right, "删除")
    }

    private fun verifyBackground(direction: TaskRemovalDirection, label: String) {
        val visible = mutableStateOf(true)
        compose.setContent {
            LockTaskTheme {
                RemovingTaskItem(visible = visible.value, direction = direction) {
                    Box(Modifier.fillMaxWidth().height(80.dp).background(MaterialTheme.colorScheme.surface))
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText(label).assertDoesNotExist()
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { visible.value = false }
        compose.mainClock.advanceTimeBy(300)
        compose.onNodeWithText(label).assertIsDisplayed()
        compose.mainClock.advanceTimeBy(TaskRemovalTiming.EXIT_MILLIS.toLong() + 100)
        compose.onNodeWithText(label).assertDoesNotExist()
    }
}
