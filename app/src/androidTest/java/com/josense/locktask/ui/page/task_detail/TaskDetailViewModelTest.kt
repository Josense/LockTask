package com.josense.locktask.ui.page.task_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.josense.locktask.debug.DetailSaveFailure
import com.josense.locktask.debug.FailingDetailRepository
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskDetailViewModelTest {
    @Test
    fun toggleSavesIntentAndUpdateTime() = runBlocking {
        val repository = Repository()
        val model = TaskDetailViewModel(
            repository,
            SavedStateHandle(mapOf("taskId" to 1)),
        )
        try {
            withTimeout(5000) { model.uiState.first { it.task != null } }
            withContext(Dispatchers.Main) {
                model.onAction(TaskDetailAction.SetLockNotify(false))
            }
            withTimeout(5000) { model.uiState.first { it.isSaving.not() } }
            assertFalse(repository.task.value.needLockNotify)
            assertTrue(repository.task.value.updateAt.time > 0)
            assertEquals(Date(0), repository.task.value.createAt)
            assertFalse(model.uiState.value.isFinished)
            assertFalse(model.uiState.value.isSaving)
        } finally {
            model.viewModelScope.cancel()
        }
    }

    @Test
    fun failedToggleKeepsIntentAndPermitsRetry() = runBlocking {
        val repository = Repository().apply { fail = true }
        val model = TaskDetailViewModel(
            repository,
            SavedStateHandle(mapOf("taskId" to 1)),
        )
        try {
            withTimeout(5000) { model.uiState.first { it.task != null } }
            withContext(Dispatchers.Main) {
                model.onAction(TaskDetailAction.SetLockNotify(false))
            }
            withTimeout(5000) { model.uiState.first { it.isSaving.not() } }
            assertTrue(repository.task.value.needLockNotify)
            assertTrue(model.uiState.value.saveFailed)
            assertFalse(model.uiState.value.isSaving)
            repository.fail = false
            withContext(Dispatchers.Main) {
                model.onAction(TaskDetailAction.SetLockNotify(false))
            }
            withTimeout(5000) { model.uiState.first { it.isSaving.not() } }
            assertFalse(repository.task.value.needLockNotify)
            assertFalse(model.uiState.value.saveFailed)
        } finally {
            model.viewModelScope.cancel()
        }
    }

    @Test
    fun debugFailureIsOneShotAndErrorCanBeDismissed() = runBlocking {
        val actions = listOf(
            TaskDetailAction.SetLockNotify(false),
            TaskDetailAction.Complete,
            TaskDetailAction.Delete,
        )
        for (action in actions) {
            val repository = Repository()
            val original = repository.task.value
            val failure = DetailSaveFailure()
            val model = TaskDetailViewModel(
                FailingDetailRepository(repository, failure),
                SavedStateHandle(mapOf("taskId" to 1)),
            )
            try {
                withTimeout(5000) {
                    model.uiState.first { it.task != null }
                }
                failure.toggle()
                withContext(Dispatchers.Main) { model.onAction(action) }
                withTimeout(5000) {
                    model.uiState.first { it.isSaving.not() }
                }
                assertTrue(model.uiState.value.saveFailed)
                assertFalse(model.uiState.value.isFinished)
                assertFalse(failure.armed.value)
                assertEquals(original, repository.task.value)
                withContext(Dispatchers.Main) {
                    model.onAction(TaskDetailAction.DismissSaveError)
                }
                assertFalse(model.uiState.value.saveFailed)
                assertEquals(original, repository.task.value)
                withContext(Dispatchers.Main) { model.onAction(action) }
                withTimeout(5000) {
                    model.uiState.first { it.isSaving.not() }
                }
                assertFalse(model.uiState.value.saveFailed)
                assertNotEquals(original, repository.task.value)
                if (action == TaskDetailAction.Delete) {
                    assertTrue(repository.task.value.isDeleted)
                    assertTrue(model.uiState.value.isFinished)
                }
            } finally {
                model.viewModelScope.cancel()
            }
        }
    }

    internal class Repository : TaskRepository {
        val task = MutableStateFlow(
            Task(1, "Title", createAt = Date(0), updateAt = Date(0)),
        )
        var fail = false
        override fun getTaskById(taskId: Int) = task
        override fun getAllTasks() = task.map { listOf(it) }
        override fun getAllActiveTasks() = getAllTasks()
        override suspend fun insertTask(task: Task): Int = error("Unused")
        override suspend fun updateTask(task: Task) {
            if (fail) error("Save failed")
            this.task.value = task
        }
        override suspend fun completeTask(task: Task, isCompleted: Boolean) {
            updateTask(task.copy(isCompleted = isCompleted))
        }
        override suspend fun deleteTaskSoftly(task: Task) {
            updateTask(task.copy(isDeleted = true))
        }
        override suspend fun clearTrash() = Unit
    }
}
