package com.josense.locktask.notification.presentation.di

import androidx.activity.ComponentActivity
import com.josense.locktask.domain.notification.NotificationController
import com.josense.locktask.ui.notification.NotificationDelegate
import org.koin.dsl.module

val notificationPresentationModule = module {
    factory<NotificationController> { (activity: ComponentActivity) ->
        NotificationDelegate(activity, get(), get(), get())
    }
}
