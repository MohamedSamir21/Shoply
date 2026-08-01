package com.shopping.shoply.common.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.shopping.shoply.common.presentation.theme.shoplyExtra

enum class HeroKind { Store, Delivery, Payment }

/**
 * Onboarding illustration — a clean iconographic scene (store / delivery /
 * payment) inside a 232dp circular white panel with a soft warm shadow.
 * Recreated from the handoff's `HeroArt` (ui_kits/auth/shoply-screens-1.jsx).
 */
@Composable
fun HeroArt(
    kind: HeroKind,
    modifier: Modifier = Modifier,
    panelSize: androidx.compose.ui.unit.Dp = 232.dp,
    artSize: androidx.compose.ui.unit.Dp = 180.dp,
) {
    val dark = isSystemInDarkTheme()
    val panel = if (dark) Color(0xFF33303B) else MaterialTheme.colorScheme.surface
    val border = if (dark) Color(0x14FFFFFF) else MaterialTheme.colorScheme.outlineVariant

    val orange = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val surfaceAlt = MaterialTheme.colorScheme.surfaceVariant
    val lineStrong = MaterialTheme.colorScheme.outline
    val green = MaterialTheme.shoplyExtra.green
    val grey = if (dark) Color(0xFF8A8F99) else Color(0xFF9AA0AB)

    Box(
        modifier = modifier
            .size(panelSize)
            .shadow(
                elevation = if (dark) 0.dp else 18.dp,
                shape = CircleShape,
                clip = false,
            )
            .background(panel, CircleShape)
            .border(1.dp, border, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(artSize)) {
            val s = size.width / 200f
            scale(scaleX = s, scaleY = s, pivot = Offset.Zero) {
                when (kind) {
                    HeroKind.Store -> drawStore(orange, surface, surfaceAlt, lineStrong)
                    HeroKind.Delivery -> drawDelivery(orange, surface, grey)
                    HeroKind.Payment -> drawPayment(orange, green, dark)
                }
            }
        }
    }
}

private fun path(d: String) = PathParser().parsePathString(d).toPath()

private fun DrawScope.drawStore(orange: Color, surface: Color, surfaceAlt: Color, lineStrong: Color) {
    // counter
    drawRoundRect(surface, Offset(42f, 78f), Size(116f, 86f), CornerRadius(8f))
    drawRoundRect(lineStrong, Offset(42f, 78f), Size(116f, 86f), CornerRadius(8f), style = Stroke(2.5f))
    // scalloped awning — arc flags are spelled out ("0 0 1" not the packed
    // "01") and each scallop gets an explicit `a`, because Compose's PathParser
    // mis-reads packed arc flags and would otherwise mangle the roof.
    drawPath(
        path(
            "M38 56 h124 l8 26 " +
                "a14 14 0 0 1 -28 0 " +
                "a14 14 0 0 1 -28 0 " +
                "a14 14 0 0 1 -28 0 " +
                "a14 14 0 0 1 -28 0 " +
                "a14 14 0 0 1 -28 0 " +
                "l8 -26 z"
        ),
        orange.copy(alpha = 0.9f),
    )
    // door
    drawRoundRect(surfaceAlt, Offset(64f, 108f), Size(34f, 56f), CornerRadius(5f))
    drawRoundRect(lineStrong, Offset(64f, 108f), Size(34f, 56f), CornerRadius(5f), style = Stroke(2f))
    // window + handle
    drawRoundRect(orange.copy(alpha = 0.25f), Offset(116f, 108f), Size(30f, 26f), CornerRadius(5f))
    drawCircle(orange, radius = 3f, center = Offset(90f, 138f))
}

private fun DrawScope.drawDelivery(orange: Color, surface: Color, grey: Color) {
    drawCircle(grey, radius = 20f, center = Offset(60f, 150f), style = Stroke(6f))
    drawCircle(grey, radius = 20f, center = Offset(150f, 150f), style = Stroke(6f))
    drawPath(path("M60 150l16-44h40l18 44"), orange, style = Stroke(6f, join = StrokeJoin.Round))
    drawRoundRect(orange.copy(alpha = 0.9f), Offset(96f, 56f), Size(44f, 44f), CornerRadius(6f))
    drawPath(path("M96 70h44M118 56v44"), surface, style = Stroke(3f))
    drawPath(path("M150 150l8-30h18"), grey, style = Stroke(6f, cap = StrokeCap.Round))
}

private fun DrawScope.drawPayment(orange: Color, green: Color, dark: Boolean) {
    drawRoundRect(orange.copy(alpha = 0.92f), Offset(40f, 66f), Size(120f, 78f), CornerRadius(12f))
    drawRect(Color.Black.copy(alpha = if (dark) 0.25f else 0.15f), Offset(40f, 86f), Size(120f, 16f))
    drawRoundRect(Color.White.copy(alpha = 0.8f), Offset(56f, 118f), Size(44f, 9f), CornerRadius(4.5f))
    drawRoundRect(Color.White.copy(alpha = 0.5f), Offset(116f, 116f), Size(28f, 14f), CornerRadius(7f))
    drawCircle(green, radius = 16f, center = Offset(150f, 64f))
    drawPath(
        path("M144 64l4.5 4.5L157 60"),
        Color.White,
        style = Stroke(3f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}
