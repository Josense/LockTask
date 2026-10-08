package com.josense.locktask.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.josense.locktask.domain.model.Task
import java.util.Date

@Entity(tableName = "task_table")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val needLockNotify: Boolean = true,
    val isCompleted: Boolean = false,
    val isDeleted: Boolean = false,
    val createAt: Date = Date(),
    val updateAt: Date = Date(),
)

fun TaskEntity.toDomain() = Task(id, title, content, needLockNotify, isCompleted, isDeleted, createAt, updateAt)

fun Task.toEntity(): TaskEntity = TaskEntity(id, title, content, needLockNotify, isCompleted, isDeleted, createAt, updateAt)