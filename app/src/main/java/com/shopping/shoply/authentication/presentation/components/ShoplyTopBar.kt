package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Auth app-bar — optional back chevron on the left, centered 19/700 title.
 * Mirrors the handoff `TopBar` (ui_kits/auth/shoply-ui.jsx).
 */
@Composable
fun ShoplyTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
        if (onBack != null) {
            BackButton(
                onBack = onBack,
                iconColor = contentColor,
                modifier = Modifier.align(Alignment.CenterStart),
            )
        }
    }
}
