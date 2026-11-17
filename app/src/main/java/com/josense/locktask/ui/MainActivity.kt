package com.josense.locktask.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.josense.locktask.domain.notification.NotificationController
import com.josense.locktask.ui.navigation.HomeNavGraph
import com.josense.locktask.ui.page.settings.appearance.AppearanceViewModel
import com.josense.locktask.ui.theme.LockTaskTheme
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    private val appearance: AppearanceViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notifications: NotificationController =
            getKoin().get { parametersOf(this@MainActivity) }
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val state by appearance.state.collectAsStateWithLifecycle()
            val darkTheme = state.mode.isDark(isSystemInDarkTheme())
            LaunchedEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) {
                        darkTheme
                    },
                    navigationBarStyle = SystemBarStyle.auto(0xE6FFFFFF.toInt(), 0x801B1B1B.toInt()) { darkTheme },
                )
            }
            LockTaskTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeNavGraph(notifications)
                }
            }
        }
    }
}
