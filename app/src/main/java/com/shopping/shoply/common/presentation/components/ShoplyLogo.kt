package com.shopping.shoply.common.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.shopping.shoply.common.presentation.theme.ctaShadow
import com.shopping.shoply.common.presentation.theme.plusJakartaSans

private val BagTop = Color(0xFFE88A56)
private val BagMid = Color(0xFFDE7A48)
private val BagBottom = Color(0xFFC96A3B)

/**
 * Shoply brand lockup — the solid "smile bag" mark (the orange shape itself is
 * the shopping bag, with eyes and a big smile) seated in a rounded surface card
 * with a thin line border and a soft warm shadow, optionally with the "Shoply"
 * wordmark below. Recreated from the "Shoply Smile Bag Solid" `.ctx` tiles.
 *
 * @param size the edge length of the bag mark; the surrounding card scales from it.
 */
@Composable
fun ShoplyLogo(
    modifier: Modifier = Modifier,
    size: Dp = 78.dp,
    showWord: Boolean = true,
    showCard: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((size.value * 0.18f).dp),
    ) {
        if (showCard) {
            val cardRadius = (size.value * 0.34f).dp
            val cardPadding = (size.value * 0.24f).dp
            Surface(
                modifier = Modifier.ctaShadow(
                    shape = RoundedCornerShape(cardRadius),
                    elevation = (size.value * 0.10f).dp,
                ),
                shape = RoundedCornerShape(cardRadius),
                // surfaceBright = white (light) / a lighter grey (dark), so the tile
                // clearly sits above the warm page rather than blending into it.
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Box(modifier = Modifier.padding(cardPadding)) {
                    ShoplyMark(size = size)
                }
            }
        } else {
            ShoplyMark(size = size)
        }
        if (showWord) {
            Text(
                text = "Shoply",
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = plusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (size.value * 0.34f).sp,
                letterSpacing = (-0.02).em,
            )
        }
    }
}

@Composable
internal fun ShoplyMark(size: Dp) {
    val paths = remember {
        BagPaths(
            handle = PathParser()
                .parsePathString("M33 44 V36 a17 17 0 0 1 34 0 V44")
                .toPath(),
            body = PathParser()
                .parsePathString(
                    "M20 40 H80 a6 6 0 0 1 5.96 6.6 l-4 42 A7 7 0 0 1 75 95 " +
                        "H25 a7 7 0 0 1 -6.96 -6.4 l-4 -42 A6 6 0 0 1 20 40 Z"
                )
                .toPath(),
            smile = PathParser()
                .parsePathString("M31 69 C35 84 65 84 69 69")
                .toPath(),
        )
    }

    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.width / 100f
        // Gradient is drawn inside the scaled block, so its coordinates are in the
        // 100-unit design space.
        val gradient = Brush.verticalGradient(
            0f to BagTop,
            0.5f to BagMid,
            1f to BagBottom,
            startY = 0f,
            endY = 100f,
        )
        scale(scaleX = s, scaleY = s, pivot = Offset.Zero) {
            // handle arch behind the body
            drawPath(paths.handle, gradient, style = Stroke(width = 9.5f, cap = StrokeCap.Round))
            // bag body
            drawPath(paths.body, gradient)
            // eyes
            drawOval(Color.White, topLeft = Offset(40f - 3.7f, 60f - 4.8f), size = Size(7.4f, 9.6f))
            drawOval(Color.White, topLeft = Offset(60f - 3.7f, 60f - 4.8f), size = Size(7.4f, 9.6f))
            // smile
            drawPath(
                paths.smile,
                Color.White,
                style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

private class BagPaths(
    val handle: Path,
    val body: Path,
    val smile: Path,
)
