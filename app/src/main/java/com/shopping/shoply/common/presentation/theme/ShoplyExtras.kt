package com.shopping.shoply.common.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Design-system colors that don't map cleanly onto a Material 3 [androidx.compose.material3.ColorScheme]
 * role. Accessed through [MaterialTheme.shoplyExtra].
 *
 * @property isDark the design swaps card shadows for a thin ring in dark mode (see [shoplyCard]).
 */
@Immutable
data class ShoplyExtraColors(
    val orangePress: Color,
    val inkFaint: Color,
    val green: Color,
    val greenSoft: Color,
    val onGreenSoft: Color,
    val red: Color,
    val redSoft: Color,
    val facebook: Color,
    val surfaceHi: Color,
    val isDark: Boolean,
)

val LightShoplyExtraColors = ShoplyExtraColors(
    orangePress = orangePressLight,
    inkFaint = inkFaintLight,
    green = greenLight,
    greenSoft = greenSoftLight,
    onGreenSoft = onGreenSoftLight,
    red = redLight,
    redSoft = redSoftLight,
    facebook = facebookBlue,
    surfaceHi = surfaceHiLight,
    isDark = false,
)

val DarkShoplyExtraColors = ShoplyExtraColors(
    orangePress = orangePressDark,
    inkFaint = inkFaintDark,
    green = greenDark,
    greenSoft = greenSoftDark,
    onGreenSoft = onGreenSoftDark,
    red = redDark,
    redSoft = redSoftDark,
    facebook = facebookBlue,
    surfaceHi = surfaceHiDark,
    isDark = true,
)

val LocalShoplyExtraColors = staticCompositionLocalOf { LightShoplyExtraColors }

val MaterialTheme.shoplyExtra: ShoplyExtraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalShoplyExtraColors.current
