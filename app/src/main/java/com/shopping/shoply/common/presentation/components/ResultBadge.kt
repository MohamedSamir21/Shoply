package com.shopping.shoply.common.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.shopping.shoply.common.presentation.theme.shoplyExtra

/**
 * Success / error result badge — concentric soft disc + ring + solid badge with
 * a check (✓) or cross (✕) and confetti accents. Recreated from the handoff's
 * `ResultBadge` (ui_kits/auth/shoply-screens-2.jsx).
 */
@Composable
fun ResultBadge(
    ok: Boolean,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 200.dp,
) {
    val extra = MaterialTheme.shoplyExtra
    val orange = MaterialTheme.colorScheme.primary
    val main = if (ok) extra.green else extra.red
    val soft = if (ok) extra.greenSoft else extra.redSoft

    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 200f
        scale(scaleX = s, scaleY = s, pivot = Offset.Zero) {
            val center = Offset(100f, 100f)
            drawCircle(soft, radius = 84f, center = center)
            drawCircle(main.copy(alpha = 0.45f), radius = 62f, center = center, style = Stroke(3f))
            drawCircle(main, radius = 46f, center = center)

            // glyph, authored on a 24-grid, scaled to ~46px and centered
            val f = 46f / 24f
            translate(left = 100f - 12f * f, top = 100f - 12f * f) {
                scale(scaleX = f, scaleY = f, pivot = Offset.Zero) {
                    if (ok) {
                        drawPath(
                            path("M5 12.5l4.5 4.5L19 7"),
                            Color.White,
                            style = Stroke(2.4f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                    } else {
                        drawPath(
                            path("M8 8l8 8M16 8l-8 8"),
                            Color.White,
                            style = Stroke(2.6f, cap = StrokeCap.Round),
                        )
                    }
                }
            }

            // confetti accents
            val confetti = listOf(
                Offset(28.5f, 34.5f) to main,
                Offset(174.5f, 50.5f) to orange,
                Offset(44.5f, 154.5f) to orange,
                Offset(168.5f, 154.5f) to main,
            )
            confetti.forEach { (pos, col) ->
                drawCircle(col.copy(alpha = 0.7f), radius = 4.5f, center = pos)
            }
        }
    }
}

private fun path(d: String) = PathParser().parsePathString(d).toPath()
