package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.shopping.shoply.authentication.presentation.Description
import com.shopping.shoply.common.presentation.theme.ShoplyIcons
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

/**
 * Back chevron. Carries its own accessibility label so every call site is announced
 * correctly by default; pass [contentDescription] when a screen needs a more specific one.
 */
@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    iconColor: Color = MaterialTheme.colorScheme.onSurface,
    contentDescription: String = Description.BACK_PRESS.asString(),
    onBack: () -> Unit
) {
    IconButton(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp)),
        onClick = onBack,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = iconColor
        )
    ) {
        Icon(
            imageVector = ShoplyIcons.Back,
            contentDescription = contentDescription
        )
    }
}

@PreviewLightDark
@Composable
private fun BackButtonPreview() {
    ShoplyTheme {
        BackButton(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surface
            )
        ) {}
    }
}
