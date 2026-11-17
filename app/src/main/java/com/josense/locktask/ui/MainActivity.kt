package com.josense.locktask.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.josense.locktask.domain.notification.NotificationController
import com.josense.locktask.ui.navigation.HomeNavGraph
import com.josense.locktask.ui.theme.LockTaskTheme
import org.koin.android.ext.android.getKoin
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notifications: NotificationController =
            getKoin().get { parametersOf(this@MainActivity) }
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            LockTaskTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeNavGraph(notifications)
                }
            }
        }
    }
}
