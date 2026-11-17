package com.josense.locktask.notification

import android.content.res.Configuration
import android.view.ContextThemeWrapper
import android.widget.FrameLayout
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.test.platform.app.InstrumentationRegistry
import com.josense.locktask.notification.data.R
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationTextContrastTest {
    @Test
    fun customNotificationTextHasContrastInBothSystemModes() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            for (night in listOf(false, true)) {
                val base = instrumentation.targetContext
                val configuration = Configuration(base.resources.configuration).apply {
                    uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                        if (night) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
                }
                // RemoteViews can inherit a light app theme even while System UI is dark.
                val context = ContextThemeWrapper(
                    base.createConfigurationContext(configuration),
                    android.R.style.Theme_Material_Light_NoActionBar,
                )
                val view = RemoteViews(base.packageName, R.layout.notification_task)
                    .apply(context, FrameLayout(context))
                val background = if (night) 0xFF252A29.toInt() else 0xFFF5F5F5.toInt()
                for (id in listOf(R.id.notification_title, R.id.notification_body)) {
                    val text = view.findViewById<TextView>(id)
                    val contrast = ColorUtils.calculateContrast(
                        ColorUtils.compositeColors(text.currentTextColor, background),
                        background,
                    )
                    assertTrue("night=$night view=$id contrast=$contrast", contrast >= 4.5)
                }
            }
        }
    }
}
