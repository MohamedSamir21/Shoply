package com.shopping.shoply.common.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/* ============================================================
   SHOPLY · SPACING, RADII, CONTROL SIZING
   8pt-ish rhythm with a 24dp screen gutter. Generously rounded.
   ============================================================ */
object ShoplyDimens {
    // —— Spacing scale ——
    val space1 = 4.dp
    val space2 = 8.dp
    val space3 = 12.dp
    val space4 = 16.dp
    val space5 = 20.dp
    val space6 = 24.dp
    val space8 = 32.dp
    val space10 = 40.dp
    val space12 = 48.dp

    val gutter = 24.dp          // left/right screen padding

    // —— Control sizing ——
    val controlHeight = 56.dp   // primary button / tappable row
    val fieldHeight = 58.dp     // text field
    val socialHeight = 54.dp    // social button
    val touchMin = 44.dp        // minimum hit target

    // —— Corner radii ——
    val radiusField = 14.dp     // inputs, social buttons
    val radiusButton = 16.dp    // primary buttons
    val radiusCard = 16.dp      // cards, OTP cells
    val radiusSheet = 24.dp     // bottom sheets
    val radiusPanel = 52.dp     // onboarding hero panel
    val radiusPill = 100.dp     // dots, chips, fully round
}

object ShoplyShapes {
    val field = RoundedCornerShape(ShoplyDimens.radiusField)
    val button = RoundedCornerShape(ShoplyDimens.radiusButton)
    val card = RoundedCornerShape(ShoplyDimens.radiusCard)
    val sheet = RoundedCornerShape(ShoplyDimens.radiusSheet)
    val pill = RoundedCornerShape(ShoplyDimens.radiusPill)
}

/**
 * Warm orange-glow elevation used under the primary CTA and the onboarding FAB.
 * Uses [MaterialTheme.colorScheme.primary] as the spot/ambient color (Android 9+).
 */
@Composable
fun Modifier.ctaShadow(
    shape: Shape = ShoplyShapes.button,
    elevation: androidx.compose.ui.unit.Dp = 12.dp,
): Modifier {
    val glow = MaterialTheme.colorScheme.primary
    return this.shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = glow,
        spotColor = glow,
    )
}
