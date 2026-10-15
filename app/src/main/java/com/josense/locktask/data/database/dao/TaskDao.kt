package com.josense.locktask.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.josense.locktask.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TaskDao {

    /** 插入与更新 */
    @Insert
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun updateTaskRaw(task: TaskEntity)

    suspend fun update(task: TaskEntity) {
        val updatedTask = task.copy(updateAt = Date())
        updateTaskRaw(updatedTask)
    }

    /** 删除 */

    @Delete
    suspend fun deletePhysically(task: TaskEntity)

    suspend fun deleteSoftly(task: TaskEntity) {
        val deletedTask = task.copy(isDeleted = true, updateAt = Date())
        updateTaskRaw(deletedTask)
    }

    @Query("DELETE FROM task_table WHERE isDeleted = 1")
    abstract suspend fun clearTrash()

    @Query("SELECT * FROM task_table")
    fun getAllTasks(): Flow<List<TaskEntity>>

    /** 查询 */
    @Query("SELECT * FROM task_table WHERE id = :taskId LIMIT 1")
    abstract fun getTaskById(taskId: Int): Flow<TaskEntity?>

    @Query("SELECT * FROM task_table WHERE isCompleted = 0 AND isDeleted = 0 ORDER BY createAt DESC")
    abstract fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task_table WHERE isCompleted = 1 AND isDeleted = 0 ORDER BY updateAt DESC")
    abstract fun getCompletedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task_table WHERE isDeleted = 1 ORDER BY updateAt DESC")
    abstract fun getDeletedTasks(): Flow<List<TaskEntity>>
}