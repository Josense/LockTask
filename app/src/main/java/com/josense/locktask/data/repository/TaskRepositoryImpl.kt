package com.josense.locktask.data.repository

import com.josense.locktask.data.database.dao.TaskDao
import com.josense.locktask.data.model.toDomain
import com.josense.locktask.data.model.toEntity
import com.josense.locktask.domain.model.Task
import com.josense.locktask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(private val taskDao: TaskDao) : TaskRepository {
    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllActiveTasks(): Flow<List<Task>> {
        return taskDao.getActiveTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTaskById(taskId: Int): Flow<Task?> {
        return taskDao.getTaskById(taskId).map { it?.toDomain() }
    }

    override suspend fun insertTask(task: Task) = taskDao.insert(task.toEntity())

    override suspend fun updateTask(task: Task) = taskDao.update(task.toEntity())

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