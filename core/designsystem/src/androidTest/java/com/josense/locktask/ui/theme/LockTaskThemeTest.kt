package com.josense.locktask.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LockTaskThemeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun darkPaletteSurvivesPlatformRendering() {
        compose.setContent {
            LockTaskTheme(darkTheme = true) {
                Box(
                    Modifier.size(96.dp).background(MaterialTheme.colorScheme.background).testTag("palette"),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
        }
        val pixels = compose.onNodeWithTag("palette").captureToImage().toPixelMap()
        assertEquals(Color(0xFF101512), pixels[pixels.width - 1, pixels.height - 1])
        assertEquals(Color(0xFFD1E9D5), pixels[pixels.width / 4, pixels.height / 4])
    }

    @Test
    fun themeTracksSystemNightModeChanges() {
        val nightMode = mutableStateOf(false)
        var background = Color.Unspecified
        compose.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    if (nightMode.value) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
            }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                LockTaskTheme {
                    val currentBackground = MaterialTheme.colorScheme.background
                    SideEffect { background = currentBackground }
                }
            }
        }
        compose.runOnIdle {
            assertEquals(Color.White, background)
            nightMode.value = true
        }
        compose.runOnIdle {
            assertEquals(Color(0xFF101512), background)
            nightMode.value = false
        }
        compose.runOnIdle { assertEquals(Color.White, background) }
    }
}
