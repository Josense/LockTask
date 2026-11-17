package com.josense.locktask.task.data.di

import android.content.Context
import androidx.room.Room
import com.josense.locktask.data.database.TaskDatabase
import com.josense.locktask.data.repository.TaskRepositoryImpl
import com.josense.locktask.domain.repository.TaskReminderPreferences
import com.josense.locktask.domain.repository.TaskRepository
import org.koin.dsl.module

val taskDataModule = module {
    single { Room.databaseBuilder(get<Context>(), TaskDatabase::class.java, "task_database").build() }
    single { get<TaskDatabase>().taskDao() }
    single { TaskRepositoryImpl(get()) }
    single<TaskRepository> { get<TaskRepositoryImpl>() }
    single<TaskReminderPreferences> { get<TaskRepositoryImpl>() }
}
