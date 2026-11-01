package com.josense.locktask.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.josense.locktask.data.database.dao.TaskDao
import com.josense.locktask.data.model.toEntity
import com.josense.locktask.data.notification.AndroidTaskNotificationGateway
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import com.josense.locktask.ui.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext

class TaskReminderServiceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun serviceLivesUntilLastReminderIsDisabledAndDoesNotReplaceTaskNotifications() = runBlocking {
        val context = compose.activity
        val repository = GlobalContext.get().get<TaskRepository>()
        val dao = GlobalContext.get().get<TaskDao>()
        val sync = GlobalContext.get().get<TaskNotificationSync>()
        assumeTrue(repository.getAllActiveTasks().first().none { it.needLockNotify })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                "pm grant ${context.packageName} ${Manifest.permission.POST_NOTIFICATIONS}",
            ).use { android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes() }
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        val gateway = AndroidTaskNotificationGateway(context)
        fun serviceNotifications() = manager.activeNotifications.filter {
            it.notification.flags and Notification.FLAG_FOREGROUND_SERVICE != 0
        }
        val ids = mutableListOf<Int>()
        try {
            ids += repository.insertTask(Task(title = "First service test"))
            ids += repository.insertTask(Task(title = "Second service test"))
            sync.reconcile()
            awaitCondition { serviceNotifications().size == 1 && gateway.publishedKeys().size == 2 }
            assertEquals(setOf("task:${ids[0]}", "task:${ids[1]}"), gateway.publishedKeys())
            val service = serviceNotifications().single()
            assertEquals(Notification.CATEGORY_SERVICE, service.notification.category)
            assertTrue(service.notification.deleteIntent == null)
            val first = manager.activeNotifications.single { it.tag == "locktask:task:${ids[0]}" }
            first.notification.deleteIntent.send()
            awaitCondition { gateway.publishedKeys() == setOf("task:${ids[1]}") }
            assertEquals(1, serviceNotifications().size)
            assertFalse(repository.getTaskById(ids[0]).first()!!.needLockNotify)
            repository.completeTask(repository.getTaskById(ids[1]).first()!!)
            awaitCondition { gateway.publishedKeys().isEmpty() && serviceNotifications().isEmpty() }
            ids += repository.insertTask(Task(title = "Restart service test"))
            awaitCondition { serviceNotifications().size == 1 && gateway.publishedKeys().size == 1 }
        } finally {
            ids.forEach { id ->
                repository.getTaskById(id).first()?.let { dao.deletePhysically(it.toEntity()) }
            }
            sync.reconcile()
            awaitCondition { serviceNotifications().isEmpty() && gateway.publishedKeys().isEmpty() }
        }
    }

    private suspend fun awaitCondition(condition: suspend () -> Boolean) {
        withTimeout(5000) { while (condition().not()) delay(50) }
    }
}
