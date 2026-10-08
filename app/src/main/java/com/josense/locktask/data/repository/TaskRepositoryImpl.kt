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

    override suspend fun insertTask(task: Task) = taskDao.insert(task.toEntity())

    override suspend fun updateTask(task: Task) = taskDao.update(task.toEntity())
}