package com.shopping.shoply.authentication.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shopping.shoply.common.presentation.theme.ctaShadow

/** Onboarding progress dots — active dot is a 22dp orange pill. */
@Composable
fun Dots(
    count: Int,
    index: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { i ->
            val active = i == index
            val width by animateDpAsState(if (active) 22.dp else 7.dp, label = "dotWidth")
            val color by animateColorAsState(
                if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                label = "dotColor",
            )
            Box(
                Modifier
                    .height(7.dp)
                    .width(width)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}

/** Circular orange FAB used for the onboarding "next" affordance. */
@Composable
fun CircleFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 64.dp,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .ctaShadow(CircleShape)
            .size(size),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}
