package com.josense.locktask.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = AppWhite,
    primaryContainer = SageContainer,
    onPrimaryContainer = SageOnContainer,
    secondary = SagePrimary,
    onSecondary = AppWhite,
    secondaryContainer = SageContainer,
    onSecondaryContainer = SageOnContainer,
    tertiary = SagePrimary,
    onTertiary = AppWhite,
    tertiaryContainer = SageContainer,
    onTertiaryContainer = SageOnContainer,
    background = AppWhite,
    onBackground = TextPrimary,
    surface = AppWhite,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFE4EAE2),
    onSurfaceVariant = TextSecondary,
    surfaceDim = Color(0xFFD8DDD7),
    surfaceBright = AppWhite,
    surfaceContainerLowest = AppWhite,
    surfaceContainerLow = Color(0xFFF7F9F5),
    surfaceContainer = Color(0xFFF0F3ED),
    surfaceContainerHigh = Color(0xFFEAEFE8),
    surfaceContainerHighest = Color(0xFFE4EAE2),
    surfaceTint = Color.Transparent,
    outline = Outline,
    outlineVariant = OutlineVariant,
    inverseSurface = TextPrimary,
    inverseOnSurface = AppWhite,
    inversePrimary = Color(0xFFADCEB5),
    error = Color(0xFFBA1A1A),
    onError = AppWhite,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFADCEB5),
    onPrimary = Color(0xFF193827),
    primaryContainer = Color(0xFF304F3B),
    onPrimaryContainer = Color(0xFFD1E9D5),
    secondary = Color(0xFFADCEB5),
    onSecondary = Color(0xFF193827),
    secondaryContainer = Color(0xFF304F3B),
    onSecondaryContainer = Color(0xFFD1E9D5),
    tertiary = Color(0xFFADCEB5),
    onTertiary = Color(0xFF193827),
    tertiaryContainer = Color(0xFF304F3B),
    onTertiaryContainer = Color(0xFFD1E9D5),
    background = Color(0xFF101512),
    onBackground = Color(0xFFE1E5DF),
    surface = Color(0xFF101512),
    onSurface = Color(0xFFE1E5DF),
    surfaceVariant = Color(0xFF404941),
    onSurfaceVariant = Color(0xFFBFC9BF),
    surfaceDim = Color(0xFF101512),
    surfaceBright = Color(0xFF363C37),
    surfaceContainerLowest = Color(0xFF0B100D),
    surfaceContainerLow = Color(0xFF191F1A),
    surfaceContainer = Color(0xFF1D231E),
    surfaceContainerHigh = Color(0xFF272E28),
    surfaceContainerHighest = Color(0xFF323932),
    surfaceTint = Color.Transparent,
    outline = Color(0xFF89958B),
    outlineVariant = Color(0xFF404941),
    inverseSurface = Color(0xFFE1E5DF),
    inverseOnSurface = Color(0xFF2B322C),
    inversePrimary = SagePrimary,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

internal fun lockTaskColorScheme(darkTheme: Boolean): ColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

@Composable
fun LockTaskTheme(
    extendedTypography: ExtendedTypography = ExtendedTypography(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalExtendedTypography provides extendedTypography,
    ) {
        MaterialTheme(
            colorScheme = lockTaskColorScheme(darkTheme),
            typography = Typography,
            content = content,
        )
    }
}

val MaterialTheme.extendedTypography: ExtendedTypography
    @Composable get() = LocalExtendedTypography.current
