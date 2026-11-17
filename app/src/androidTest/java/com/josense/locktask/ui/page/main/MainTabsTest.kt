package com.josense.locktask.ui.page.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.josense.locktask.R
import com.josense.locktask.data.database.dao.TaskDao
import com.josense.locktask.data.model.toEntity
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import com.josense.locktask.notification.presentation.R as NotificationR
import com.josense.locktask.ui.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext

class MainTabsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun tabsRestoreSelectionAndSettingsGuideWorksWithoutReminders() {
        val repository = GlobalContext.get().get<TaskRepository>()
        val dao = GlobalContext.get().get<TaskDao>()
        val id = runBlocking { repository.insertTask(Task(title = "Tab test", needLockNotify = false)) }
        try {
            compose.onNodeWithTag("main_tab_Tasks").assertIsSelected()
            compose.onNodeWithTag("main_tab_Templates").performClick().assertIsSelected()
            compose.onNodeWithText(compose.activity.getString(R.string.templates_empty_title)).assertIsDisplayed()
            compose.onNodeWithTag("main_tab_Settings").performClick().assertIsSelected()
            compose.activityRule.scenario.recreate()
            compose.onNodeWithTag("main_tab_Settings").assertIsSelected()
            compose.onNodeWithText(
                compose.activity.getString(NotificationR.string.background_reminder_settings),
            ).performClick()
            compose.onNodeWithText(
                compose.activity.getString(NotificationR.string.background_reminder_guide),
            ).assertIsDisplayed()
            compose.onNodeWithText(
                compose.activity.getString(NotificationR.string.background_reminder_close),
            ).performClick()
            compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithTag("main_tab_Tasks").assertIsSelected()
        } finally {
            runBlocking {
                repository.getTaskById(id).first()?.let { dao.deletePhysically(it.toEntity()) }
            }
        }
    }
}
