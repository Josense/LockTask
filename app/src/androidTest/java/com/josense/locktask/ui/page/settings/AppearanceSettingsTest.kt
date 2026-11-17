package com.josense.locktask.ui.page.settings

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.core.view.WindowInsetsControllerCompat
import androidx.test.filters.SdkSuppress
import com.josense.locktask.data.settings.LocalAppearanceRepository
import com.josense.locktask.domain.settings.AppearanceMode
import com.josense.locktask.domain.settings.AppearanceRepository
import com.josense.locktask.ui.MainActivity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext

@SdkSuppress(minSdkVersion = 26)
class AppearanceSettingsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private lateinit var repository: AppearanceRepository
    private lateinit var original: AppearanceMode

    @Before
    fun setup() {
        repository = GlobalContext.get().get()
        original = repository.mode.value
        compose.onNodeWithTag("main_tab_Settings").performClick()
    }

    @After
    fun restore() {
        compose.runOnUiThread { repository.setMode(original) }
    }

    @Test
    fun selectingModesChangesPaletteAndSystemBarsAndPersistsAcrossRecreation() {
        select("浅色")
        assertTheme(dark = false)
        select("深色")
        assertTheme(dark = true)
        val reloaded =
            LocalAppearanceRepository(compose.activity.getSharedPreferences("appearance", Context.MODE_PRIVATE))
        assertEquals(AppearanceMode.Dark, reloaded.mode.value)
        compose.activityRule.scenario.recreate()
        assertTheme(dark = true)
        compose.onNodeWithText("外观").performClick()
        compose.onNode(hasText("深色") and hasAnyAncestor(isDialog())).assertIsSelected()
        compose.onNodeWithText("取消").performClick()
        assertEquals(AppearanceMode.Dark, repository.mode.value)
    }

    @Test
    fun systemSelectionUsesDeviceTheme() {
        select("深色")
        select("系统默认设置")
        val systemDark = compose.activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        assertTheme(dark = systemDark)
    }

    private fun select(label: String) {
        compose.onNodeWithText("外观").performClick()
        compose.onNode(hasText(label) and hasAnyAncestor(isDialog())).performClick()
        compose.waitForIdle()
    }

    private fun assertTheme(dark: Boolean) {
        val luminance = compose.onRoot().captureToImage().toPixelMap()[2, 2].luminance()
        assertEquals("Page palette", dark, luminance < 0.5f)
        compose.runOnUiThread {
            val window = compose.activity.window
            assertEquals(
                "Status bar icons",
                dark.not(),
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars,
            )
        }
    }
}
