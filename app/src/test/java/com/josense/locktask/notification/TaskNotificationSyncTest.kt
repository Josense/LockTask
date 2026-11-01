package com.josense.locktask.notification

import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.notification.NotificationAccess
import com.josense.locktask.domain.notification.TaskNotification
import com.josense.locktask.domain.notification.TaskNotificationGateway
import com.josense.locktask.domain.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskNotificationSyncTest {
    @Test
    fun `publish update and remove follow task intent`() = runTest {
        val repo = Repository()
        val gateway = Gateway()
        TaskNotificationSync(repo, gateway, backgroundScope).start()
        repo.tasks.value =
            listOf(
                Task(1, "First"),
                Task(2, "Disabled", needLockNotify = false),
            )
        runCurrent()
        assertEquals(setOf("task:1"), gateway.notifications.keys)
        repo.tasks.value = listOf(Task(1, "Edited"))
        runCurrent()
        assertEquals("Edited", gateway.notifications["task:1"]?.title)
        repo.tasks.value = listOf(Task(1, "Edited", isCompleted = true))
        runCurrent()
        assertTrue(gateway.notifications.isEmpty())
    }

    @Test
    fun `unrelated edit respects dismissal until foreground reconciliation`() = runTest {
        val repo = Repository()
        val gateway = Gateway()
        val sync = TaskNotificationSync(repo, gateway, backgroundScope)
        repo.tasks.value = listOf(Task(1, "A"), Task(2, "B"))
        sync.start()
        runCurrent()
        gateway.notifications.remove("task:1")
        repo.tasks.value = listOf(Task(1, "A"), Task(2, "Edited B"))
        runCurrent()
        assertFalse(gateway.notifications.containsKey("task:1"))
        sync.reconcile()
        runCurrent()
        assertTrue(gateway.notifications.containsKey("task:1"))
    }

    @Test
    fun `authorization restores tasks and removes stale notifications`() = runTest {
        val repo = Repository()
        val gateway = Gateway()
        gateway.status = NotificationAccess.PermissionRequired
        gateway.notifications["stale"] =
            TaskNotification("stale", listOf(99), "Stale", "")
        repo.tasks.value = listOf(Task(1, "Saved"))
        val sync = TaskNotificationSync(repo, gateway, backgroundScope)
        sync.start()
        runCurrent()
        assertTrue(gateway.notifications.isEmpty())
        assertEquals(1, repo.tasks.value.size)
        gateway.status = NotificationAccess.Available
        sync.reconcile()
        runCurrent()
        assertEquals(setOf("task:1"), gateway.notifications.keys)
    }

    @Test
    fun `one failed publication does not block others and retries later`() = runTest {
        val repo = Repository()
        val gateway = Gateway()
        gateway.failKey = "task:1"
        repo.tasks.value = listOf(Task(1, "A"), Task(2, "B"))
        val sync = TaskNotificationSync(repo, gateway, backgroundScope)
        sync.start()
        runCurrent()
        assertEquals(setOf("task:2"), gateway.notifications.keys)
        gateway.failKey = null
        sync.reconcile()
        runCurrent()
        assertEquals(setOf("task:1", "task:2"), gateway.notifications.keys)
    }

    @Test
    fun `reconciliation waits for data before removing notifications`() = runTest {
        val flow =
            kotlinx.coroutines.flow.MutableSharedFlow<List<Task>>(
                replay = 1,
            )
        val repo =
            object : TaskRepository by Repository() {
                override fun getAllActiveTasks() = flow
            }
        val gateway = Gateway()
        gateway.notifications["task:1"] =
            TaskNotification("task:1", listOf(1), "A", "")
        val sync = TaskNotificationSync(repo, gateway, backgroundScope)
        sync.start()
        sync.reconcile()
        runCurrent()
        assertTrue(gateway.notifications.containsKey("task:1"))
        flow.emit(emptyList())
        runCurrent()
        assertTrue(gateway.notifications.isEmpty())
    }

    private class Gateway : TaskNotificationGateway {
        var status = NotificationAccess.Available
        var failKey: String? = null
        val notifications = mutableMapOf<String, TaskNotification>()

        override suspend fun access() = status

        override suspend fun publishedKeys() = notifications.keys.toSet()

        override suspend fun publish(notification: TaskNotification) {
            if (notification.key == failKey) error("Test publication failure")
            notifications[notification.key] = notification
        }

        override suspend fun cancel(key: String) {
            notifications.remove(key)
        }
    }

    private class Repository : TaskRepository {
        val tasks = MutableStateFlow<List<Task>>(emptyList())

        override fun getAllTasks() = tasks

        override fun getAllActiveTasks() = tasks

        override fun getTaskById(taskId: Int) = tasks.map { all -> all.find { it.id == taskId } }

        override suspend fun insertTask(task: Task): Int = error("Not used")

        override suspend fun updateTask(task: Task) = Unit

        override suspend fun updateTaskText(taskId: Int, title: String, content: String): Unit = error("Unused")

        override suspend fun completeTask(task: Task, isCompleted: Boolean) = Unit

        override suspend fun deleteTaskSoftly(task: Task) = Unit

        override suspend fun clearTrash() = Unit
    }
}
