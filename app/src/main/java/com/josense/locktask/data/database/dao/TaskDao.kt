package com.josense.locktask.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.josense.locktask.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TaskDao {
    @Query("SELECT * FROM task_table")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun updateTaskRaw(task: TaskEntity)

    suspend fun update(task: TaskEntity) {
        val updatedTask = task.copy(updateAt = Date())
        updateTaskRaw(updatedTask)
    }
}