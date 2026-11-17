package com.josense.locktask.notification.data.di

import android.content.Context
import com.josense.locktask.data.notification.AndroidTaskNotificationGateway
import com.josense.locktask.data.notification.NotificationPlatformConfig
import com.josense.locktask.domain.notification.TaskNotificationGateway
import kotlinx.coroutines.CoroutineScope
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val notificationScopeQualifier = named("notificationScope")

fun notificationDataModule(config: NotificationPlatformConfig, applicationScope: Qualifier) = module {
    single { config }
    single<CoroutineScope>(notificationScopeQualifier) { get(applicationScope) }
    single<TaskNotificationGateway> { AndroidTaskNotificationGateway(get<Context>(), get()) }
}
