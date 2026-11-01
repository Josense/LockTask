package com.josense.locktask.ui.page.create

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import com.josense.locktask.ui.components.input.KeyboardInputSheet
import com.josense.locktask.ui.theme.LockTaskTheme
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class CreateTaskInputsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun openingFocusesTitleAndImeMovesToMultilineDescription() {
        showInputs()
        awaitTitleFocus()
        compose.onNodeWithTag("create_task_title").performTextInput("Title")
        compose.onNodeWithTag("create_task_title").performImeAction()
        compose.onNodeWithTag("create_task_description").assertIsFocused()
        compose.onNodeWithTag("create_task_description").performTextInput("Line one\nLine two")
        compose.onNodeWithTag("create_task_title").assertTextEquals("Title")
        compose.onNodeWithTag("create_task_description").assertTextEquals("Line one\nLine two")
        compose.onNodeWithTag("create_task_description").assertIsFocused()
    }

    @Test
    fun hardwareEnterMovesFocusAndReopeningFocusesTitleAgain() {
        val open = mutableStateOf(true)
        showInputs(open)
        awaitTitleFocus()
        compose.onNodeWithTag("create_task_title").performKeyInput { pressKey(Key.Enter) }
        compose.onNodeWithTag("create_task_description").assertIsFocused()
        compose.runOnIdle { open.value = false }
        compose.waitForIdle()
        compose.runOnIdle { open.value = true }
        awaitTitleFocus()
    }

    private fun showInputs(open: androidx.compose.runtime.MutableState<Boolean> = mutableStateOf(true)) {
        val state = mutableStateOf(CreateTaskState())
        compose.setContent {
            LockTaskTheme {
                if (open.value) {
                    KeyboardInputSheet(onDismissRequest = { open.value = false }) {
                        CreateTaskInputs(state.value, initialFocusModifier) { action ->
                            state.value = when (action) {
                                is CreateTaskAction.TitleChanged -> state.value.copy(title = action.title)
                                is CreateTaskAction.DescriptionChanged -> state.value.copy(
                                    description = action.description,
                                )
                                else -> state.value
                            }
                        }
                    }
                }
            }
        }
    }

    private fun awaitTitleFocus() {
        compose.waitUntil(5_000) {
            compose.onNodeWithTag("create_task_title").fetchSemanticsNode()
                .config[SemanticsProperties.Focused]
        }
        compose.onNodeWithTag("create_task_title").assertIsFocused()
    }
}
