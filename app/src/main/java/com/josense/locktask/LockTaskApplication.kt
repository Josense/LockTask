package com.josense.locktask

import android.app.Application
import com.josense.locktask.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LockTaskApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@LockTaskApplication)
            modules(appModule)
        }
    }
}