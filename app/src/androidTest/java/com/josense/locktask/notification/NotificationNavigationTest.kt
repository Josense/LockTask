package com.josense.locktask.notification

import android.Manifest
import android.app.NotificationManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import com.josense.locktask.ui.MainActivity
import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext

class NotificationNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun notificationReturnsFromDetailAndScrollsToItsTask() {
        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.TIRAMISU
        ) {
            InstrumentationRegistry.getInstrumentation()
                .uiAutomation
                .executeShellCommand(
                    "pm grant ${compose.activity.packageName} " +
                        Manifest.permission.POST_NOTIFICATIONS,
                )
                .use {
                    android.os.ParcelFileDescriptor.AutoCloseInputStream(it)
                        .readBytes()
                }
        }
        val repository = GlobalContext.get().get<TaskRepository>()
        val manager =
            compose.activity.getSystemService(NotificationManager::class.java)
        val ids = mutableListOf<Int>()
        val prefix = "notification-test-${System.nanoTime()}"
        try {
            runBlocking {
                repeat(30) { index ->
                    ids +=
                        repository.insertTask(
                            Task(
                                title = "$prefix-$index",
                                createAt = Date(index.toLong()),
                            ),
                        )
                }
            }
            compose.waitUntil(10_000) {
                manager.activeNotifications.any {
                    it.tag == "locktask:task:${ids.last()}"
                }
            }
            // Open the newest row's detail first, then click the oldest task's
            // notification.
            val firstNotification =
                manager.activeNotifications.single {
                    it.tag == "locktask:task:${ids.last()}"
                }
            compose.runOnUiThread {
                firstNotification.notification.contentIntent.send()
            }
            compose.waitForIdle()
            compose
                .onNodeWithText("$prefix-29")
                .assertIsDisplayed()
                .performClick()
            compose.waitForIdle()
            val targetNotification =
                manager.activeNotifications.single {
                    it.tag == "locktask:task:${ids.first()}"
                }
            compose.runOnUiThread {
                targetNotification.notification.contentIntent.send()
            }
            compose.waitForIdle()
            compose.onNodeWithText("$prefix-0").assertIsDisplayed()
            // A second click has an independent request and must still work.
            compose.runOnUiThread {
                firstNotification.notification.contentIntent.send()
            }
            compose.waitForIdle()
            compose.onNodeWithText("$prefix-29").assertIsDisplayed()
            // Recreation must register fresh callbacks without replaying a
            // consumed target.
            compose.activityRule.scenario.recreate()
            compose.waitForIdle()
            compose.runOnUiThread {
                targetNotification.notification.contentIntent.send()
            }
            compose.waitForIdle()
            compose.onNodeWithText("$prefix-0").assertIsDisplayed()
        } finally {
            runBlocking {
                ids.forEach { id ->
                    repository.getTaskById(id).first()?.let {
                        repository.deleteTaskSoftly(it)
                    }
                }
            }
        }
    }
}
