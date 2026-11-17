package com.josense.locktask.di

import com.josense.locktask.R
import com.josense.locktask.data.notification.NotificationPlatformConfig
import com.josense.locktask.data.notification.TaskNotificationDismissalHandler
import com.josense.locktask.domain.notification.NotificationDismissalHandler
import com.josense.locktask.domain.notification.NotificationReconciler
import com.josense.locktask.domain.notification.NotificationReminderLookup
import com.josense.locktask.domain.repository.TaskRepository
import com.josense.locktask.notification.TaskNotificationSync
import com.josense.locktask.notification.data.di.notificationDataModule
import com.josense.locktask.notification.presentation.di.notificationPresentationModule
import com.josense.locktask.task.data.di.taskDataModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    includes(
        taskDataModule,
        taskPresentationModule,
        notificationDataModule(NotificationPlatformConfig(R.mipmap.ic_launcher_monochrome), applicationScopeQualifier),
        notificationPresentationModule,
    )
    single<CoroutineScope>(applicationScopeQualifier) {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }
    singleOf(::TaskNotificationDismissalHandler) bind NotificationDismissalHandler::class
    single { TaskNotificationSync(get(), get(), get(applicationScopeQualifier)) }
    single<NotificationReconciler> { NotificationReconciler(get<TaskNotificationSync>()::reconcile) }
    single<NotificationReminderLookup> {
        val repository = get<TaskRepository>()
        NotificationReminderLookup { taskId -> repository.getTaskById(taskId).first()?.needLockNotify == true }
    }

}
