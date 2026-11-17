package com.josense.locktask.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class LockTaskColorSchemeTest {
    @Test
    fun darkThemeUsesDarkSurfacesWithIncreasingElevation() {
        val colors = lockTaskColorScheme(darkTheme = true)
        assertTrue("Night background must be dark", colors.background.luminance() < 0.02f)
        val surfaces = listOf(
            colors.surfaceContainerLowest,
            colors.surface,
            colors.surfaceContainerLow,
            colors.surfaceContainer,
            colors.surfaceContainerHigh,
            colors.surfaceContainerHighest,
        )
        surfaces.zipWithNext().forEach { (lower, upper) ->
            assertTrue("Elevated dark surfaces must be lighter", lower.luminance() < upper.luminance())
        }
    }

    @Test
    fun lightThemeSeparatesInputsAndSheetsFromThePage() {
        val colors = lockTaskColorScheme(darkTheme = false)
        assertTrue(colors.surface.luminance() > colors.surfaceContainerLow.luminance())
        assertTrue(colors.surfaceContainerLow.luminance() > colors.surfaceContainerHigh.luminance())
    }

    @Test
    fun textRemainsReadableAcrossBothThemes() {
        listOf(false, true).forEach { dark ->
            val colors = lockTaskColorScheme(dark)
            listOf(
                colors.onSurface to colors.surface,
                colors.onSurfaceVariant to colors.surface,
                colors.onSurfaceVariant to colors.surfaceContainerHigh,
                colors.onSurfaceVariant to colors.surfaceContainerHighest,
                colors.onPrimary to colors.primary,
                colors.onPrimaryContainer to colors.primaryContainer,
                colors.onErrorContainer to colors.errorContainer,
                colors.inverseOnSurface to colors.inverseSurface,
                colors.inversePrimary to colors.inverseSurface,
            ).forEach { (foreground, background) ->
                assertTrue("Text contrast in dark=$dark must be >= 4.5", contrast(foreground, background) >= 4.5f)
            }
        }
    }

    private fun contrast(first: Color, second: Color): Float {
        val a = first.luminance()
        val b = second.luminance()
        return (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
    }
}
