package com.shopping.shoply.common.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.toggleable
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

/**
 * The design's pill chip (40 dp): optional [icon], the label, an orange [count] badge and a
 * [trailingIcon]. Selected = orange tint and border.
 */
@Composable
fun ShoplyChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    icon: ImageVector? = null,
    count: Int = 0,
    trailingIcon: ImageVector? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(ShoplyShapes.pill)
            .background(if (selected) scheme.primaryContainer else scheme.surface)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) scheme.primary else scheme.outlineVariant,
                shape = ShoplyShapes.pill,
            )
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val content = if (selected) scheme.onPrimaryContainer else scheme.onSurface
        if (icon != null) Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(17.dp))
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = content)
        if (count > 0) {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 20.dp)
                    .height(20.dp)
                    .background(scheme.primary, CircleShape)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = count.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = scheme.onPrimary)
            }
        }
        if (trailingIcon != null) Icon(trailingIcon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
    }
}

@PreviewLightDark
@Composable
fun ShoplyChipPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(modifier = Modifier.padding(ShoplyDimens.space4), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShoplyChip(label = "Filters", onClick = {}, icon = ShoplyAppIcons.Filter, count = 2)
                ShoplyChip(label = "Sort", onClick = {}, trailingIcon = ShoplyAppIcons.ChevronDown)
                ShoplyChip(label = "4.5 & up", onClick = {}, selected = true)
            }
        }
    }
}
