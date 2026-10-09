package com.josense.locktask.di

import androidx.room.Room
import com.josense.locktask.data.database.TaskDatabase
import com.josense.locktask.data.repository.TaskRepositoryImpl
import com.josense.locktask.domain.repository.TaskRepository
import com.josense.locktask.ui.page.create.CreateTaskViewModel
import com.josense.locktask.ui.page.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {

    // 任务数据库
    single {
        Room.databaseBuilder(
            androidContext(),
            TaskDatabase::class.java,
            "task_database"
        ).build()
    }

    // Dao 对象注入
    single {
        get<TaskDatabase>().taskDao()
    }

    singleOf(::TaskRepositoryImpl) bind TaskRepository::class

    viewModelOf(::CreateTaskViewModel)

    viewModelOf(::HomeViewModel)
}
