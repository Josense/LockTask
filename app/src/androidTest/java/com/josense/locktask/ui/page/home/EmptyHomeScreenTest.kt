package com.josense.locktask.ui.page.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.josense.locktask.ui.theme.LockTaskTheme
import com.josense.locktask.ui.MainActivity
import org.junit.Rule
import org.junit.Test

class EmptyHomeScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun emptyStateLoadsNativeAnimationAndKeepsItsMessage() {
        compose.runOnUiThread {
            compose.activity.setContent {
                LockTaskTheme { EmptyHomeScreen() }
            }
        }
        compose.onNodeWithText("没有待完成的任务").assertIsDisplayed()
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodesWithTag("empty_task_animation", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("empty_task_animation", useUnmergedTree = true)
            .assertIsDisplayed()
    }
}
