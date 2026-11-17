package com.josense.locktask.ui.page.task_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.josense.locktask.data.database.TaskDatabase
import com.josense.locktask.data.notification.TaskNotificationDismissalHandler
import com.josense.locktask.data.repository.TaskRepositoryImpl
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TaskDetailNotificationRaceTest {
    @Test
    fun dismissalBetweenReadingAndSavingTextRemainsDisabled() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, TaskDatabase::class.java).build()
        val repository = TaskRepositoryImpl(database.taskDao())
        val id = repository.insertTask(Task(title = "Original", content = "Description"))
        val readyToSave = CompletableDeferred<Unit>()
        val continueSave = CompletableDeferred<Unit>()
        val pausedRepository = object : TaskRepository by repository {
            override suspend fun updateTask(task: Task) {
                readyToSave.complete(Unit)
                continueSave.await()
                repository.updateTask(task)
            }

            override suspend fun updateTaskText(taskId: Int, title: String, content: String) {
                readyToSave.complete(Unit)
                continueSave.await()
                repository.updateTaskText(taskId, title, content)
            }
        }
        val model = TaskDetailViewModel(pausedRepository, SavedStateHandle(mapOf("taskId" to id)))
        try {
            withTimeout(5000) { model.uiState.first { it.task != null } }
            withContext(Dispatchers.Main) {
                model.onAction(TaskDetailAction.StartEditing(TaskDetailField.Title))
                model.onAction(TaskDetailAction.TitleChanged("Edited"))
                model.onAction(TaskDetailAction.DescriptionChanged("Edited description"))
                model.onAction(TaskDetailAction.FinishEditing)
            }
            withTimeout(5000) { readyToSave.await() }
            TaskNotificationDismissalHandler(repository).onDismissed(listOf(id))
            continueSave.complete(Unit)
            val state = withTimeout(5000) {
                model.uiState.first { it.isSaving.not() && it.edit == null && it.task?.title == "Edited" }
            }
            val saved = repository.getTaskById(id).first()!!
            assertEquals("Edited", saved.title)
            assertEquals("Edited description", saved.content)
            assertFalse(saved.needLockNotify)
            assertFalse(saved.isCompleted)
            assertFalse(saved.isDeleted)
            assertFalse(state.task!!.needLockNotify)
            assertFalse(state.saveFailed)
        } finally {
            withContext(Dispatchers.Main) { model.viewModelScope.cancel() }
            database.close()
        }
    }
}
