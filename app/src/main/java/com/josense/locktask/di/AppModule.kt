package com.josense.locktask.di

import androidx.activity.ComponentActivity
import androidx.room.Room
import com.josense.locktask.data.database.TaskDatabase
import com.josense.locktask.data.notification.AndroidTaskNotificationGateway
import com.josense.locktask.data.repository.TaskRepositoryImpl
import com.josense.locktask.domain.notification.TaskNotificationGateway
import com.josense.locktask.domain.repository.TaskRepository
import com.josense.locktask.notification.TaskNotificationSync
import com.josense.locktask.ui.notification.NotificationDelegate
import com.josense.locktask.ui.page.create.CreateTaskViewModel
import com.josense.locktask.ui.page.home.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

private val applicationScope = named("applicationScope")

val appModule = module {
    single<CoroutineScope>(applicationScope) {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    factory { (activity: ComponentActivity) ->
        NotificationDelegate(
            activity,
            get(),
            get(),
            get<TaskNotificationSync>()::reconcile,
        )
    }

    single<TaskNotificationGateway> {
        AndroidTaskNotificationGateway(androidContext())
    }
    single { TaskNotificationSync(get(), get(), get(applicationScope)) }

    // 任务数据库
    single {
        Room.databaseBuilder(
            androidContext(),
            TaskDatabase::class.java,
            "task_database",
        ).build()
    }

    // Dao 对象注入
    single {
        get<TaskDatabase>().taskDao()
    }

    singleOf(::TaskRepositoryImpl) bind TaskRepository::class

    viewModelOf(::CreateTaskViewModel)

    viewModelOf(::HomeViewModel)
    taskDetailModule()
}
