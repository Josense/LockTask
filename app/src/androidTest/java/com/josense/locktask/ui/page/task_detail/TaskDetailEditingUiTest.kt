package com.josense.locktask.ui.page.task_detail

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.test.espresso.Espresso
import com.josense.locktask.ui.MainActivity
import com.josense.locktask.ui.theme.LockTaskTheme
import kotlinx.coroutines.cancel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TaskDetailEditingUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun backAfterCorrectingBlankTitleSavesWithoutAnExtraBack() {
        verifyBackRetry(saveFails = false)
    }

    @Test
    fun backAfterSaveFailureRetriesWithoutAnExtraBack() {
        verifyBackRetry(saveFails = true)
    }

    private fun verifyBackRetry(saveFails: Boolean) {
        val repository = TaskDetailViewModelTest.Repository().apply { fail = saveFails }
        val model = TaskDetailViewModel(repository, SavedStateHandle(mapOf("taskId" to 1)))
        var returned = false
        try {
            compose.runOnUiThread {
                compose.activity.setContent {
                    LockTaskTheme { TaskDetailRoot(model) { returned = true } }
                }
            }
            compose.onNodeWithText("Title").performClick()
            awaitKeyboard(visible = true)
            compose.onNodeWithTag("detail_edit_title").performTextReplacement(if (saveFails) "Draft" else "")
            Espresso.pressBack()
            awaitKeyboard(visible = false)
            compose.waitUntil(5000) {
                model.uiState.value.saveFailed || model.uiState.value.edit?.titleError == true
            }
            compose.runOnIdle {
                assertFalse(returned)
                assertTrue(model.uiState.value.edit != null)
                repository.fail = false
            }
            compose.onNodeWithTag("detail_edit_title").performClick()
            awaitKeyboard(visible = true)
            compose.onNodeWithTag("detail_edit_title").performTextReplacement("Corrected")
            Espresso.pressBack()
            compose.waitUntil(5000) { model.uiState.value.edit == null }
            compose.runOnIdle {
                assertFalse(returned)
                assertEquals("Corrected", repository.task.value.title)
            }
        } finally {
            model.viewModelScope.cancel()
        }
    }

    private fun awaitKeyboard(visible: Boolean) {
        compose.waitUntil(5000) {
            ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == visible
        }
        compose.waitForIdle()
    }

    @Test
    fun editorBottomMeetsKeyboardWithoutBottomBarGap() {
        val model = TaskDetailViewModel(
            TaskDetailViewModelTest.Repository(),
            SavedStateHandle(mapOf("taskId" to 1)),
        )
        try {
            compose.runOnUiThread {
                compose.activity.setContent {
                    LockTaskTheme { TaskDetailRoot(model, onBackClick = {}) }
                }
            }
            compose.onNodeWithText("Title").performClick()
            compose.waitUntil(5000) {
                ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            }
            compose.waitForIdle()
            val contentBottom = compose.onNodeWithTag("detail_content").fetchSemanticsNode().boundsInRoot.bottom
            compose.runOnIdle {
                val decor = compose.activity.window.decorView
                val keyboardHeight = ViewCompat.getRootWindowInsets(decor)!!
                    .getInsets(WindowInsetsCompat.Type.ime()).bottom
                assertEquals((decor.height - keyboardHeight).toFloat(), contentBottom, 2f)
            }
        } finally {
            model.viewModelScope.cancel()
        }
    }

    @Test
    fun editingBackSavesAndStaysOnDetailThenNextBackLeaves() {
        val repository = TaskDetailViewModelTest.Repository()
        val model = TaskDetailViewModel(repository, SavedStateHandle(mapOf("taskId" to 1)))
        var returned = false
        try {
            compose.runOnUiThread {
                compose.activity.setContent {
                    LockTaskTheme { TaskDetailRoot(model) { returned = true } }
                }
            }
            compose.onNodeWithText("Title").performClick()
            compose.onNodeWithTag("detail_edit_title").assertIsFocused().performTextReplacement("Changed")
            compose.onNodeWithTag("detail_edit_title").performImeAction()
            compose.onNodeWithTag("detail_edit_description").assertIsFocused().performTextReplacement("Line 1\nLine 2")
            Espresso.pressBack()
            compose.waitUntil(5000) { model.uiState.value.edit == null }
            compose.runOnIdle {
                assertFalse(returned)
                assertEquals("Changed", repository.task.value.title)
                assertEquals("Line 1\nLine 2", repository.task.value.content)
            }
            compose.onNodeWithText("Line 1\nLine 2").performClick()
            compose.onNodeWithTag("detail_edit_description").assertIsFocused()
            compose.onNodeWithText("保存").performClick()
            compose.waitUntil(5000) { model.uiState.value.edit == null }
            compose.onNodeWithContentDescription("返回").performClick()
            compose.runOnIdle { assertTrue(returned) }
        } finally {
            model.viewModelScope.cancel()
        }
    }
}
