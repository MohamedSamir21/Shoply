package com.shopping.shoply.common.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// Warm brown at 8%, the design's card shadow colour (rgba(120, 80, 40, 0.08)).
private val cardShadowColor = Color(0x14785028)
private val darkRingColor = Color.White.copy(alpha = 0.08f)

/**
 * A card surface: [MaterialTheme.colorScheme.surface] fill, clipped to [shape]. Light mode adds a
 * soft warm shadow; dark mode adds a thin ring instead (shadows don't read on dark backgrounds).
 */
@Composable
fun Modifier.shoplyCard(shape: Shape = ShoplyShapes.card): Modifier {
    val isDark = MaterialTheme.shoplyExtra.isDark
    return this
        .then(
            if (isDark) Modifier
            else Modifier.shadow(6.dp, shape, clip = false, ambientColor = cardShadowColor, spotColor = cardShadowColor)
        )
        .clip(shape)
        .background(MaterialTheme.colorScheme.surface)
        .then(if (isDark) Modifier.border(1.dp, darkRingColor, shape) else Modifier)
}

/** The dark-mode ring the design puts around soft icon circles. Nothing in light mode. */
@Composable
fun Modifier.darkRing(shape: Shape): Modifier =
    if (MaterialTheme.shoplyExtra.isDark) border(1.dp, darkRingColor, shape) else this
