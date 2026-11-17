package com.josense.locktask.ui.testing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.josense.locktask.debug.DetailSaveFailure
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

/** Reproduces the host window and debug bindings without depending on the app module. */
class TaskTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (GlobalContext.getOrNull() == null) {
            startKoin { modules(module { single { DetailSaveFailure() } }) }
        }
    }
}
