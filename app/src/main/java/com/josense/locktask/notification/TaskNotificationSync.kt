package com.josense.locktask.notification

import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.notification.NotificationAccess
import com.josense.locktask.domain.notification.TaskNotification
import com.josense.locktask.domain.notification.TaskNotificationGateway
import com.josense.locktask.domain.repository.TaskRepository
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level
import java.util.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TaskNotificationSync(
    private val repository: TaskRepository,
    private val gateway: TaskNotificationGateway,
    private val scope: CoroutineScope,
) {
    private val requests = Channel<Unit>(Channel.CONFLATED)
    private var job: Job? = null
    private var applied = emptyMap<String, TaskNotification>()
    private var initialized = false
    private val forceReconcile = AtomicBoolean(true)

    fun start() {
        if (job != null) return
        job =
            scope.launch {
                launch {
                    repository.getAllActiveTasks().collect {
                        requests.send(Unit)
                    }
                }
                for (request in requests) {
                    try {
                        synchronize(repository.getAllActiveTasks().first())
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        handleFailure(error)
                    }
                }
            }
    }

    fun reconcile() {
        forceReconcile.set(true)
        requests.trySend(Unit)
    }

    private suspend fun synchronize(tasks: List<Task>) {
        val full = forceReconcile.getAndSet(false) || initialized.not()
        val desired =
            tasks
                .filter {
                    it.needLockNotify &&
                        it.isCompleted.not() &&
                        it.isDeleted.not()
                }
                .associate { task ->
                    val key = "task:${task.id}"
                    key to
                        TaskNotification(
                            key,
                            listOf(task.id),
                            task.title,
                            task.content,
                        )
                }
        val published = if (full) gateway.publishedKeys() else applied.keys
        val next = applied.toMutableMap()
        for (key in (published + applied.keys) - desired.keys) {
            attempt {
                gateway.cancel(key)
                next.remove(key)
            }
        }
        val hasAccess = gateway.access() == NotificationAccess.Available
        // Background start restrictions must not prevent ordinary task notifications from publishing.
        attempt { gateway.setRetentionEnabled(hasAccess && desired.isNotEmpty()) }
        if (hasAccess) {
            for ((key, spec) in desired) {
                if (applied[key] != spec || (full && key !in published)) {
                    attempt {
                        gateway.publish(spec)
                        next[key] = spec
                    }
                }
            }
        }
        applied = next
        initialized = true
    }

    private suspend fun attempt(block: suspend () -> Unit) {
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            handleFailure(error)
        }
    }

    private fun handleFailure(error: Exception) {
        forceReconcile.set(true)
        logger.log(
            Level.WARNING,
            "Notification synchronization failed",
            error,
        )
    }

    private companion object {
        val logger: Logger =
            Logger.getLogger(TaskNotificationSync::class.java.name)
    }
}
