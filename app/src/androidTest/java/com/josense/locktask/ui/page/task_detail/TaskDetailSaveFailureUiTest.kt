package com.josense.locktask.ui.page.task_detail

import androidx.activity.compose.setContent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.josense.locktask.debug.DetailSaveFailure
import com.josense.locktask.debug.FailingDetailRepository
import com.josense.locktask.ui.MainActivity
import com.josense.locktask.ui.theme.LockTaskTheme
import kotlinx.coroutines.cancel
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext

class TaskDetailSaveFailureUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun debugMenuTriggersSnackbarAndToggleCanRetry() {
        val failure = GlobalContext.get().get<DetailSaveFailure>()
        val repository = TaskDetailViewModelTest.Repository()
        val model = TaskDetailViewModel(
            FailingDetailRepository(repository, failure),
            SavedStateHandle(mapOf("taskId" to 1)),
        )
        try {
            if (failure.armed.value) failure.toggle()
            compose.runOnUiThread {
                compose.activity.setContent {
                    LockTaskTheme {
                        TaskDetailRoot(model, onBackClick = {})
                    }
                }
            }
            compose.onNodeWithContentDescription("更多操作").performClick()
            compose.onNodeWithText("调试：模拟下次保存失败")
                .performClick()
            compose.onNodeWithText("常驻锁屏").performClick()
            compose.onNodeWithText("保存失败，请重试")
                .assertIsDisplayed()
            compose.onNodeWithText("常驻锁屏").assertIsOn()
            compose.waitUntil(10_000) {
                model.uiState.value.saveFailed.not()
            }
            compose.onNodeWithText("常驻锁屏").performClick()
            compose.onNodeWithText("常驻锁屏").assertIsOff()
            assertFalse(repository.task.value.needLockNotify)
        } finally {
            model.viewModelScope.cancel()
            if (failure.armed.value) failure.toggle()
        }
    }
}
