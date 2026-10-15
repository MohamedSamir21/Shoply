package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.ctaShadow

enum class ShoplyButtonVariant { Filled, Soft, Tonal, Outline, Ghost }

/**
 * Primary action button from the Shoply Remix design system.
 * 56dp tall, 16dp radius, 16/600 label. The [ShoplyButtonVariant.Filled]
 * variant carries the warm orange-glow CTA shadow.
 */
@Composable
fun ShoplyButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ShoplyButtonVariant = ShoplyButtonVariant.Filled,
    enable: Boolean = true,
    shape: Shape = ShoplyShapes.button,
    leadingIcon: ImageVector? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val container: Color
    val content: Color
    var border: BorderStroke? = null

    when (variant) {
        ShoplyButtonVariant.Filled -> {
            container = scheme.primary; content = scheme.onPrimary
        }
        ShoplyButtonVariant.Soft -> {
            container = scheme.primaryContainer; content = scheme.onPrimaryContainer
        }
        ShoplyButtonVariant.Tonal -> {
            container = scheme.surfaceVariant; content = scheme.onSurface
        }
        ShoplyButtonVariant.Outline -> {
            container = Color.Transparent; content = scheme.onSurface
            border = BorderStroke(1.5.dp, scheme.outline)
        }
        ShoplyButtonVariant.Ghost -> {
            container = Color.Transparent; content = scheme.primary
        }
    }

    val shadow = if (variant == ShoplyButtonVariant.Filled && enable) {
        Modifier.ctaShadow(shape)
    } else Modifier

    val isGhost = variant == ShoplyButtonVariant.Ghost
    val rowModifier = if (isGhost) {
        Modifier.padding(horizontal = ShoplyDimens.space1, vertical = ShoplyDimens.space2)
    } else {
        Modifier
            .fillMaxWidth()
            .height(ShoplyDimens.controlHeight)
            .padding(horizontal = ShoplyDimens.space5)
    }

    Surface(
        onClick = onClick,
        enabled = enable,
        shape = shape,
        color = container,
        contentColor = content,
        border = border,
        modifier = modifier.then(shadow),
    ) {
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let {
                Icon(imageVector = it, contentDescription = null)
            }
            Text(
                text = label,
                style = if (isGhost) MaterialTheme.typography.labelLarge
                else MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ShoplyButtonPreview() {
    ShoplyTheme {
        ShoplyButton(label = "Sign In", onClick = {})
    }
}
