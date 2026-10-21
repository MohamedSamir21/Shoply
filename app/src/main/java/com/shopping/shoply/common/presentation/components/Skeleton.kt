package com.shopping.shoply.common.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyExtra

/**
 * Pulses [content] gently (alpha 1 → 0.5 and back) while a screen loads. Put a screen's whole
 * skeleton inside one pulse so every bone moves together.
 */
@Composable
fun SkeletonPulse(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "skeletonPulse").animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    // Reading the animated value in the draw phase avoids recomposing every frame.
    Box(modifier = modifier.graphicsLayer { alpha = pulse }) { content() }
}

/** One grey placeholder shape; size it with [modifier]. */
@Composable
fun SkeletonBone(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    Box(modifier = modifier.background(MaterialTheme.shoplyExtra.surfaceHi, shape))
}

@PreviewLightDark
@Composable
fun SkeletonPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SkeletonPulse(modifier = Modifier.padding(ShoplyDimens.gutter)) {
                Column(verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space2)) {
                    SkeletonBone(shape = CircleShape, modifier = Modifier.size(48.dp))
                    SkeletonBone(modifier = Modifier.fillMaxWidth(0.8f).height(14.dp))
                    SkeletonBone(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp))
                }
            }
        }
    }
}
