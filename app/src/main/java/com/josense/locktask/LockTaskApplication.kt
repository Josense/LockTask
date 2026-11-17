package com.josense.locktask

import android.app.Application
import androidx.annotation.Keep
import app.rive.runtime.kotlin.core.Rive
import com.josense.locktask.di.appModule
import com.josense.locktask.notification.TaskNotificationSync
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LockTaskApplication : Application() {
    private val notificationSync: TaskNotificationSync by inject()

    // Flyme calls this hook before drawing. Zero means the app supplies its own
    // night palette; otherwise Flyme recolors Compose backgrounds and vectors.
    // This method is absent from the standard Android SDK, so it has no override.
    @Keep
    @Suppress("unused")
    fun mzNightModeUseOf(): Int = 0

    override fun onCreate() {
        super.onCreate()
        Rive.init(this)
        startKoin {
            androidLogger()
            androidContext(this@LockTaskApplication)
            modules(appModule)
        }
        notificationSync.start()
    }
}
