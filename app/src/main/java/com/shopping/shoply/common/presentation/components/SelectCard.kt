package com.shopping.shoply.common.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.darkRing

/**
 * A radio card (the design's `SelectCard`): a leading [icon] in a soft square (or a custom [lead]),
 * [title] with an optional [badge], [subtitle], an optional [trailing] slot, and a radio dot.
 * Selected: orange 2 dp border and a filled dot; [expanded] content then shows under the row.
 */
@Composable
fun SelectCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    lead: (@Composable () -> Unit)? = null,
    subtitle: String? = null,
    badge: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    expanded: (@Composable () -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ShoplyShapes.card)
            .background(scheme.surface)
            .border(
                width = if (selected) 2.dp else 1.4.dp,
                color = if (selected) scheme.primary else scheme.outlineVariant,
                shape = ShoplyShapes.card,
            )
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            when {
                lead != null -> lead()
                icon != null -> Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(scheme.primaryContainer, RoundedCornerShape(12.dp))
                        .darkRing(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(21.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                    badge?.invoke()
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 19.sp,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            trailing?.invoke()
            RadioDot(selected)
        }
        if (expanded != null) {
            AnimatedVisibility(visible = selected) {
                Column(modifier = Modifier.padding(top = 14.dp)) { expanded() }
            }
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(22.dp)
            .border(2.dp, if (selected) scheme.primary else scheme.outline, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Box(Modifier.size(11.dp).background(scheme.primary, CircleShape))
    }
}

/** A small rounded label next to a title, e.g. "Default". */
@Composable
fun SelectCardBadge(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, ShoplyShapes.pill)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@PreviewLightDark
@Composable
fun SelectCardPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(ShoplyDimens.gutter),
                verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
            ) {
                SelectCard(
                    title = "Home",
                    subtitle = "24 Elm Street, Apt 5B\nBrooklyn, NY 11201",
                    icon = ShoplyAppIcons.Home,
                    badge = { SelectCardBadge("Default") },
                    selected = true,
                    onClick = {},
                )
                SelectCard(
                    title = "Express",
                    subtitle = "Within 45 minutes",
                    icon = ShoplyAppIcons.Bolt,
                    trailing = { Text("+$4.99", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                    selected = false,
                    onClick = {},
                )
            }
        }
    }
}
