package com.shopping.shoply.authentication.presentation.splash

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shopping.shoply.common.presentation.components.ShoplyLogo
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import kotlinx.coroutines.delay

// Kept short because the OS cold-start splash (installSplashScreen) already
// runs ~1.2s before this Compose splash is shown.
private const val SPLASH_DURATION_MS = 1400L

@Composable
fun SplashScreen(
    navController: NavController
) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        navController.navigate(NavRoutes.Onboarding.route) {
            popUpTo(NavRoutes.Splash.route) { inclusive = true }
        }
    }

    SplashContent(
        onTap = {
            navController.navigate(NavRoutes.Onboarding.route) {
                popUpTo(NavRoutes.Splash.route) { inclusive = true }
            }
        }
    )
}

@Composable
fun SplashContent(
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Tap-to-skip; no ripple across the whole splash.
            .clickable(interactionSource = null, indication = null, onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        ShoplyLogo(size = 108.dp)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp),
        ) {
            BouncingDots()
        }
    }
}

@Composable
private fun BouncingDots() {
    val transition = rememberInfiniteTransition(label = "splashDots")
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        repeat(3) { i ->
            val alpha by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000, delayMillis = i * 150),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$i",
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SplashPreview() {
    ShoplyTheme {
        SplashContent()
    }
}
