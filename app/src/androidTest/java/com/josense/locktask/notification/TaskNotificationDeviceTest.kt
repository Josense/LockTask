package com.josense.locktask.notification

import com.josense.locktask.R
import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.josense.locktask.data.notification.AndroidTaskNotificationGateway
import com.josense.locktask.domain.notification.TaskNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

class TaskNotificationDeviceTest {
    @Test
    fun notificationsAreOngoingIndependentAndHaveNoActions() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            instrumentation.uiAutomation
                .executeShellCommand(
                    "pm grant ${context.packageName} " +
                        Manifest.permission.POST_NOTIFICATIONS
                )
                .use {
                    android.os.ParcelFileDescriptor.AutoCloseInputStream(it)
                        .readBytes()
                }
        }
        val gateway = AndroidTaskNotificationGateway(context)
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
                )
            )
            gateway.publish(
                TaskNotification(
                    "device-test:b",
                    listOf(900002),
                    "Device B",
                    "Body",
                )
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
                        0
                )
                assertEquals(
                    0,
                    it.notification.flags and Notification.FLAG_AUTO_CANCEL,
                )
                assertTrue(it.notification.actions.isNullOrEmpty())
            }
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
                )
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

    private suspend fun awaitCondition(condition: suspend () -> Boolean) {
        withTimeout(5000) { while (condition().not()) delay(50) }
    }
}
