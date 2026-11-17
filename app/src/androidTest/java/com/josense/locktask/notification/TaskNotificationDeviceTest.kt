package com.josense.locktask.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.josense.locktask.R
import com.josense.locktask.data.database.dao.TaskDao
import com.josense.locktask.data.model.toEntity
import com.josense.locktask.data.notification.AndroidTaskNotificationGateway
import com.josense.locktask.data.notification.NotificationPlatformConfig
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.notification.TaskNotification
import com.josense.locktask.domain.repository.TaskRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.core.context.GlobalContext

class TaskNotificationDeviceTest {
    @Test
    fun notificationsAreOngoingIndependentAndHaveNoActions() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            instrumentation.uiAutomation
                .executeShellCommand(
                    "pm grant ${context.packageName} " +
                        Manifest.permission.POST_NOTIFICATIONS,
                )
                .use {
                    android.os.ParcelFileDescriptor.AutoCloseInputStream(it)
                        .readBytes()
                }
        }
        val gateway =
            AndroidTaskNotificationGateway(
                context,
                NotificationPlatformConfig(com.josense.locktask.R.mipmap.ic_launcher_monochrome),
            )
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        try {
            gateway.publish(
                TaskNotification(
                    "device-test:a",
                    listOf(900001),
                    "Device A",
                    "Body",
                ),
            )
            gateway.publish(
                TaskNotification(
                    "device-test:b",
                    listOf(900002),
                    "Device B",
                    "Body",
                ),
            )
            awaitCondition {
                manager.activeNotifications.count {
                    it.tag?.startsWith("locktask:device-test:") == true
                } == 2
            }
            val posted =
                manager.activeNotifications.filter {
                    it.tag?.startsWith("locktask:device-test:") == true
                }
            assertEquals(2, posted.size)
            posted.forEach {
                assertEquals(
                    R.mipmap.ic_launcher_monochrome,
                    it.notification.smallIcon.resId,
                )
                assertTrue(
                    it.notification.flags and Notification.FLAG_ONGOING_EVENT !=
                        0,
                )
                assertEquals(
                    0,
                    it.notification.flags and Notification.FLAG_AUTO_CANCEL,
                )
                assertTrue(it.notification.actions.isNullOrEmpty())
            }
            assertNotEquals(posted[0].notification.deleteIntent, posted[1].notification.deleteIntent)
            assertNotEquals(
                posted[0].notification.contentIntent,
                posted[1].notification.contentIntent,
            )
            gateway.publish(
                TaskNotification(
                    "device-test:a",
                    listOf(900001),
                    "Updated",
                    "Body",
                ),
            )
            awaitCondition {
                manager.activeNotifications
                    .find { it.tag == "locktask:device-test:a" }
                    ?.notification
                    ?.extras
                    ?.getString(Notification.EXTRA_TITLE) == "Updated"
            }
            val updated =
                manager.activeNotifications.single {
                    it.tag == "locktask:device-test:a"
                }
            assertEquals(
                "Updated",
                updated.notification.extras.getString(Notification.EXTRA_TITLE),
            )
            gateway.cancel("device-test:a")
            awaitCondition {
                gateway.publishedKeys().contains("device-test:a").not()
            }
            assertFalse(gateway.publishedKeys().contains("device-test:a"))
            assertTrue(gateway.publishedKeys().contains("device-test:b"))
        } finally {
            gateway.cancel("device-test:a")
            gateway.cancel("device-test:b")
        }
    }

    @Test
    fun dismissedNotificationDisablesOnlyItsTaskAndDoesNotReturnOnReconcile() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            instrumentation.uiAutomation.executeShellCommand(
                "pm grant ${context.packageName} ${Manifest.permission.POST_NOTIFICATIONS}",
            ).use { android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes() }
        }
        val koin = GlobalContext.get()
        val repository = koin.get<TaskRepository>()
        val dao = koin.get<TaskDao>()
        val sync = koin.get<TaskNotificationSync>()
        val manager = context.getSystemService(NotificationManager::class.java)
        val gateway =
            AndroidTaskNotificationGateway(
                context,
                NotificationPlatformConfig(com.josense.locktask.R.mipmap.ic_launcher_monochrome),
            )
        val firstId = repository.insertTask(Task(title = "Dismiss test", content = "Keep description"))
        val secondId = repository.insertTask(Task(title = "Keep notification"))
        try {
            sync.reconcile()
            awaitCondition {
                gateway.publishedKeys().containsAll(listOf("task:$firstId", "task:$secondId"))
            }
            val posted = manager.activeNotifications.single { it.tag == "locktask:task:$firstId" }
            // App-initiated cancellation must not be interpreted as a user dismissal.
            gateway.cancel("task:$firstId")
            assertTrue(repository.getTaskById(firstId).first()!!.needLockNotify)
            // Deliver the same PendingIntent Android sends when the user clears the notification.
            posted.notification.deleteIntent.send()
            awaitCondition { repository.getTaskById(firstId).first()?.needLockNotify == false }
            val dismissed = repository.getTaskById(firstId).first()!!
            assertEquals("Dismiss test", dismissed.title)
            assertEquals("Keep description", dismissed.content)
            assertFalse(dismissed.isCompleted)
            assertFalse(dismissed.isDeleted)
            assertTrue(repository.getTaskById(secondId).first()!!.needLockNotify)
            assertTrue(repository.getAllActiveTasks().first().any { it.id == firstId })
            sync.reconcile()
            awaitCondition { "task:$firstId" !in gateway.publishedKeys() }
            assertTrue("task:$secondId" in gateway.publishedKeys())
            // Duplicate deliveries must be harmless and not keep changing the task timestamp.
            val updatedAt = dismissed.updateAt
            posted.notification.deleteIntent.send()
            delay(200)
            assertEquals(updatedAt, repository.getTaskById(firstId).first()!!.updateAt)
            repository.updateTask(dismissed.copy(needLockNotify = true))
            awaitCondition { "task:$firstId" in gateway.publishedKeys() }
        } finally {
            listOf(firstId, secondId).forEach { id ->
                repository.getTaskById(id).first()?.let { dao.deletePhysically(it.toEntity()) }
                gateway.cancel("task:$id")
            }
        }
    }

    private suspend fun awaitCondition(condition: suspend () -> Boolean) {
        withTimeout(5000) { while (condition().not()) delay(50) }
    }
}
