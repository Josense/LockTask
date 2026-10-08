package com.josense.locktask.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.josense.locktask.data.database.dao.TaskDao
import com.josense.locktask.data.model.TaskEntity


@Database(entities = [TaskEntity::class], version = 1)
@TypeConverters(TimeConverters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}