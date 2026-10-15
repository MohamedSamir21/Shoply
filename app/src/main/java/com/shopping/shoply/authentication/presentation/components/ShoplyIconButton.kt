package com.shopping.shoply.authentication.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

/** Icon-only action button — thin wrapper over M3 [IconButton] that requires a labelled icon. */
@Composable
fun ShoplyIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
        colors = colors,
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

@PreviewLightDark
@Composable
fun ShoplyIconButtonPreview() {
    ShoplyTheme {
        ShoplyIconButton(
            icon = Icons.Default.ThumbUp,
            contentDescription = "Like"
        ) { }
    }
}