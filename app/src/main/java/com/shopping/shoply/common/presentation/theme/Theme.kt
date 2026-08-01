package com.shopping.shoply.common.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightScheme = lightColorScheme(
    primary = orangeLight,
    onPrimary = onOrangeLight,
    primaryContainer = orangeSoftLight,
    onPrimaryContainer = onOrangeSoftLight,
    inversePrimary = orangeDark,

    // Secondary reused as muted-ink / resting-tile so legacy refs stay sane.
    secondary = inkMuteLight,
    onSecondary = onOrangeLight,
    secondaryContainer = surfaceAltLight,
    onSecondaryContainer = inkLight,

    // Tertiary = success (calm olive-green).
    tertiary = greenLight,
    onTertiary = onOrangeLight,
    tertiaryContainer = greenSoftLight,
    onTertiaryContainer = onOrangeSoftLight,

    background = bgLight,
    onBackground = inkLight,
    surface = surfaceLight,
    onSurface = inkLight,
    surfaceVariant = surfaceAltLight,
    onSurfaceVariant = inkMuteLight,
    surfaceTint = orangeLight,

    surfaceDim = surfaceHiLight,
    surfaceBright = surfaceLight,
    surfaceContainerLowest = surfaceLight,
    surfaceContainerLow = surfaceAltLight,
    surfaceContainer = surfaceAltLight,
    surfaceContainerHigh = surfaceHiLight,
    surfaceContainerHighest = surfaceHiLight,

    outline = lineStrongLight,
    outlineVariant = lineLight,

    error = redLight,
    onError = onOrangeLight,
    errorContainer = redSoftLight,
    onErrorContainer = onOrangeSoftLight,

    inverseSurface = inkLight,
    inverseOnSurface = bgLight,
    scrim = inkLight,
)

private val DarkScheme = darkColorScheme(
    primary = orangeDark,
    onPrimary = onOrangeDark,
    primaryContainer = orangeSoftDark,
    onPrimaryContainer = onOrangeSoftDark,
    inversePrimary = orangeLight,

    secondary = inkMuteDark,
    onSecondary = onOrangeDark,
    secondaryContainer = surfaceAltDark,
    onSecondaryContainer = inkDark,

    tertiary = greenDark,
    onTertiary = onOrangeDark,
    tertiaryContainer = greenSoftDark,
    onTertiaryContainer = onOrangeSoftDark,

    background = bgDark,
    onBackground = inkDark,
    surface = surfaceDark,
    onSurface = inkDark,
    surfaceVariant = surfaceAltDark,
    onSurfaceVariant = inkMuteDark,
    surfaceTint = orangeDark,

    surfaceDim = bgDark,
    surfaceBright = surfaceHiDark,
    surfaceContainerLowest = bgDark,
    surfaceContainerLow = surfaceDark,
    surfaceContainer = surfaceAltDark,
    surfaceContainerHigh = surfaceHiDark,
    surfaceContainerHighest = surfaceHiDark,

    outline = lineStrongDark,
    outlineVariant = lineDark,

    error = redDark,
    onError = onOrangeDark,
    errorContainer = redSoftDark,
    onErrorContainer = inkDark,

    inverseSurface = inkDark,
    inverseOnSurface = bgDark,
    scrim = androidx.compose.ui.graphics.Color.Black,
)

@Composable
fun ShoplyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val extraColors = if (darkTheme) DarkShoplyExtraColors else LightShoplyExtraColors

    CompositionLocalProvider(LocalShoplyExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
