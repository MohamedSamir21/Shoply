package com.shopping.shoply.common.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

@Composable
fun LoadingLogoDialog(
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        LoadingLogo()
    }
}

/**
 * "Orbit Ring" loading indicator: the solid Shoply smile-bag sits static in its
 * soft orange tile while a short orange arc spins around it — a classic spinner.
 * Recreated from the handoff "Shoply Loading Logo (standalone)" Orbit Ring study.
 *
 * @param logoSize the diameter of the soft tile; the bag and ring scale from it.
 */
@Composable
fun LoadingLogo(
    modifier: Modifier = Modifier,
    logoPosition: Alignment = Alignment.Center,
    logoSize: Dp = 100.dp,
) {
    val transition = rememberInfiniteTransition(label = "orbit")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
        ),
        label = "angle",
    )
    val ringColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier,
        contentAlignment = logoPosition,
    ) {
        Box(
            modifier = Modifier
                .size(logoSize)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            // Spinning arc — circle r=52 on a 120 viewBox, ~70/326 of the
            // circumference visible (≈77°), rotating once every 1.4s.
            Canvas(modifier = Modifier.size(logoSize)) {
                val s = size.minDimension / 120f
                rotate(degrees = angle) {
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 77f,
                        useCenter = false,
                        topLeft = Offset(8f * s, 8f * s),
                        size = Size(104f * s, 104f * s),
                        style = Stroke(width = 4f * s, cap = StrokeCap.Round),
                    )
                }
            }
            // Static bag mark centred in the tile (wrap is ~1.85× the bag).
            ShoplyMark(size = logoSize * 0.54f)
        }
    }
}

@Preview
@Composable
private fun LoadingLogoPreview() {
    ShoplyTheme {
        LoadingLogo()
    }
}
