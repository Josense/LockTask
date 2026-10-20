package com.josense.locktask

import android.app.Application
import app.rive.runtime.kotlin.core.Rive
import com.josense.locktask.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LockTaskApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Rive.init(this)
        startKoin {
            androidLogger()
            androidContext(this@LockTaskApplication)
            modules(appModule)
        }
    }
}
