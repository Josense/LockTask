package com.josense.locktask.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary = AppLight,
    onPrimary = AppDark,
    primaryContainer = AppDark,
    onPrimaryContainer = AppLight,
    secondary = AppLight,
    onSecondary = AppDark,
    secondaryContainer = AppDark,
    onSecondaryContainer = AppLight,
    tertiary = AppLight,
    onTertiary = AppDark,
    background = AppDark,
    onBackground = AppLight,
    surface = AppDark,
    onSurface = AppLight,
    surfaceVariant = AppDark,
    onSurfaceVariant = AppLight,
    outline = AppLight,
    inverseSurface = AppLight,
    inverseOnSurface = AppDark,
    inversePrimary = AppDark
)

private val LightColorScheme = lightColorScheme(
    primary = AppDark,
    onPrimary = AppLight,
    primaryContainer = AppLight,
    onPrimaryContainer = AppDark,
    secondary = AppDark,
    onSecondary = AppLight,
    secondaryContainer = AppLight,
    onSecondaryContainer = AppDark,
    tertiary = AppDark,
    onTertiary = AppLight,
    background = AppLight,
    onBackground = AppDark,
    surface = AppLight,
    onSurface = AppDark,
    surfaceVariant = AppLight,
    onSurfaceVariant = AppDark,
    outline = AppDark,
    inverseSurface = AppDark,
    inverseOnSurface = AppLight,
    inversePrimary = AppLight
)

@Composable
fun LockTaskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    extendedTypography: ExtendedTypography = ExtendedTypography(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    CompositionLocalProvider(
        LocalExtendedTypography provides extendedTypography
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

val MaterialTheme.extendedTypography: ExtendedTypography
    @Composable get() = LocalExtendedTypography.current
