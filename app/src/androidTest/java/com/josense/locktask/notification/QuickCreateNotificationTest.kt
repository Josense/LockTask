package com.josense.locktask.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.josense.locktask.R
import com.josense.locktask.data.database.dao.TaskDao
import com.josense.locktask.data.model.toEntity
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.notification.NotificationContract
import com.josense.locktask.domain.repository.TaskRepository
import com.josense.locktask.notification.data.R as NotificationDataR
import com.josense.locktask.task.presentation.R as TaskR
import com.josense.locktask.ui.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext

class QuickCreateNotificationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun serviceNotificationOpensCreationFromSettingsAndPreservesAnOpenDraft() {
        val context = compose.activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                "pm grant ${context.packageName} ${Manifest.permission.POST_NOTIFICATIONS}",
            ).use { android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes() }
        }
        val repository = GlobalContext.get().get<TaskRepository>()
        val dao = GlobalContext.get().get<TaskDao>()
        val manager = context.getSystemService(NotificationManager::class.java)
        val id = runBlocking { repository.insertTask(Task(title = "Quick create test")) }
        try {
            GlobalContext.get().get<TaskNotificationSync>().reconcile()
            compose.waitUntil(5000) {
                manager.activeNotifications.any { it.notification.flags and Notification.FLAG_FOREGROUND_SERVICE != 0 }
            }
            val notification = manager.activeNotifications.single {
                it.notification.flags and Notification.FLAG_FOREGROUND_SERVICE != 0
            }.notification
            assertEquals(
                context.getString(NotificationDataR.string.reminder_service_title),
                notification.extras.getString(Notification.EXTRA_TITLE),
            )
            compose.onNodeWithTag("main_tab_Settings").performClick()
            compose.runOnUiThread { notification.contentIntent.send() }
            awaitTitleFocus()
            compose.onNodeWithTag("create_task_title").performTextInput("Keep my draft")
            compose.runOnUiThread { notification.contentIntent.send() }
            compose.onNodeWithTag("create_task_title").assertTextEquals("Keep my draft")
            closeSheet()
            compose.onNodeWithTag("main_tab_Tasks").assertIsSelected()
            compose.activityRule.scenario.recreate()
            compose.onNodeWithTag("create_task_title").assertDoesNotExist()
            compose.runOnUiThread { notification.contentIntent.send() }
            awaitTitleFocus()
            closeSheet()
        } finally {
            runBlocking { repository.getTaskById(id).first()?.let { dao.deletePhysically(it.toEntity()) } }
        }
    }

    @Test
    fun initialActivityIntentOpensCreation() {
        val context = compose.activity
        val repository = GlobalContext.get().get<TaskRepository>()
        val dao = GlobalContext.get().get<TaskDao>()
        val id = runBlocking { repository.insertTask(Task(title = "Launch test", needLockNotify = false)) }
        val intent = Intent(context, MainActivity::class.java).apply {
            data = Uri.Builder()
                .scheme(NotificationContract.URI_SCHEME)
                .authority(NotificationContract.CREATE_HOST)
                .build()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            compose.activityRule.scenario.close()
            context.applicationContext.startActivity(intent)
            awaitTitleFocus()
            closeSheet(context.getString(TaskR.string.dismiss_input_sheet))
            compose.onNodeWithTag("main_tab_Tasks").assertIsSelected()
        } finally {
            runBlocking { repository.getTaskById(id).first()?.let { dao.deletePhysically(it.toEntity()) } }
        }
    }

    private fun closeSheet(label: String = compose.activity.getString(TaskR.string.dismiss_input_sheet)) {
        compose.onNodeWithContentDescription(label).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.onNodeWithTag("create_task_title").assertDoesNotExist()
    }

    private fun awaitTitleFocus() {
        compose.waitUntil(5000) {
            compose.onAllNodesWithTag("create_task_title").fetchSemanticsNodes().any {
                it.config[SemanticsProperties.Focused]
            }
        }
        compose.onNodeWithTag("create_task_title").assertIsFocused()
    }
}
