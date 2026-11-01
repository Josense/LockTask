package com.josense.locktask.data.repository

import com.josense.locktask.data.database.dao.TaskDao
import com.josense.locktask.data.model.toDomain
import com.josense.locktask.data.model.toEntity
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import com.josense.locktask.domain.repository.TaskUnavailableException
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(private val taskDao: TaskDao) : TaskRepository {
    private companion object {
        const val EXPECTED_UPDATED_TASK_COUNT = 1
    }

    override fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks().map { entities ->
        entities.map { it.toDomain() }
    }

    override fun getAllActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks().map { entities ->
        entities.map { it.toDomain() }
    }

    override fun getTaskById(taskId: Int): Flow<Task?> = taskDao.getTaskById(taskId).map { it?.toDomain() }

    override suspend fun insertTask(task: Task) = taskDao.insert(task.toEntity()).toInt()

    override suspend fun updateTask(task: Task) = taskDao.update(task.toEntity())

    override suspend fun updateTaskText(taskId: Int, title: String, content: String) {
        val updatedTaskCount = taskDao.updateTaskText(taskId, title, content, Date())
        if (updatedTaskCount != EXPECTED_UPDATED_TASK_COUNT) throw TaskUnavailableException()
    }

    override suspend fun completeTask(task: Task, isCompleted: Boolean) {
        val updatedTask = task.copy(isCompleted = isCompleted)
        taskDao.update(updatedTask.toEntity())
    }

    override suspend fun deleteTaskSoftly(task: Task) {
        val deletedTask = task.copy(isDeleted = true)
        taskDao.update(deletedTask.toEntity())
    }
    override suspend fun clearTrash() {
        taskDao.clearTrash()
    }
}
