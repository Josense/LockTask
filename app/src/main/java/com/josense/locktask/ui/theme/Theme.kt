package com.josense.locktask.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

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
    surfaceVariant = AppWhite,
    onSurfaceVariant = TextSecondary,
    surfaceDim = AppWhite,
    surfaceBright = AppWhite,
    surfaceContainerLowest = AppWhite,
    surfaceContainerLow = AppWhite,
    surfaceContainer = AppWhite,
    surfaceContainerHigh = AppWhite,
    surfaceContainerHighest = AppWhite,
    surfaceTint = androidx.compose.ui.graphics.Color.Transparent,
    outline = Outline,
    outlineVariant = OutlineVariant,
    inverseSurface = TextPrimary,
    inverseOnSurface = AppWhite,
    inversePrimary = SageContainer,
)

@Composable
fun LockTaskTheme(
    extendedTypography: ExtendedTypography = ExtendedTypography(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalExtendedTypography provides extendedTypography
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}

val MaterialTheme.extendedTypography: ExtendedTypography
    @Composable get() = LocalExtendedTypography.current
