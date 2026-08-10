package com.shopping.shoply.authentication.presentation.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.PasswordStrength
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

private const val TOTAL_SEGMENTS = 4

// Test tags: the segments are text-free boxes, so the semantics tree has nothing else
// to find them by. Inert metadata at runtime.
internal const val STRENGTH_SEGMENT_FILLED_TAG = "strength_segment_filled"
internal const val STRENGTH_SEGMENT_EMPTY_TAG = "strength_segment_empty"

/**
 * Four-segment password strength bar with a tier label beneath it. Segments fill left-to-right
 * as the estimate improves; [PasswordStrength.NONE] renders nothing, so the meter fades in only
 * once the user starts typing.
 */
@Composable
fun PasswordStrengthMeter(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    // NONE has no bar to show; AnimatedVisibility fades the meter in/out as the user types.
    AnimatedVisibility(visible = strength != PasswordStrength.NONE) {
        val filledSegments = strength.filledSegments()
        val barColor = strength.color()

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(TOTAL_SEGMENTS) { index ->
                    val filled = index < filledSegments
                    val segmentColor =
                        if (filled) barColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = .15f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(segmentColor, RoundedCornerShape(2.dp))
                            .testTag(
                                if (filled) STRENGTH_SEGMENT_FILLED_TAG
                                else STRENGTH_SEGMENT_EMPTY_TAG
                            )
                    )
                }
            }
            strength.labelRes()?.let { labelRes ->
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = barColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun PasswordStrength.filledSegments(): Int = when (this) {
    PasswordStrength.NONE -> 0
    PasswordStrength.WEAK -> 1
    PasswordStrength.FAIR -> 2
    PasswordStrength.GOOD -> 3
    PasswordStrength.STRONG -> 4
}

@StringRes
private fun PasswordStrength.labelRes(): Int? = when (this) {
    PasswordStrength.NONE -> null
    PasswordStrength.WEAK -> R.string.password_strength_weak
    PasswordStrength.FAIR -> R.string.password_strength_fair
    PasswordStrength.GOOD -> R.string.password_strength_good
    PasswordStrength.STRONG -> R.string.password_strength_strong
}

// Tier colors live in the composable layer: color is a presentation concern, so the domain
// enum stays color-agnostic. WEAK borrows the theme's error color; the rest are explicit.
@Composable
private fun PasswordStrength.color(): Color = when (this) {
    PasswordStrength.STRONG -> Color(0xFF2E7D32) // green
    PasswordStrength.GOOD -> Color(0xFF9E9D24)   // lime
    PasswordStrength.FAIR -> Color(0xFFF9A825)   // amber
    else -> MaterialTheme.colorScheme.error      // NONE / WEAK
}

@PreviewLightDark
@Composable
private fun PasswordStrengthMeterPreview() {
    ShoplyTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PasswordStrengthMeter(strength = PasswordStrength.WEAK)
            PasswordStrengthMeter(strength = PasswordStrength.FAIR)
            PasswordStrengthMeter(strength = PasswordStrength.GOOD)
            PasswordStrengthMeter(strength = PasswordStrength.STRONG)
        }
    }
}