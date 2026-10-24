package com.shopping.shoply.product.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.catalog.domain.model.ColorOption
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyExtra

/** Round colour swatches (42 dp); the picked one gets an orange ring. */
@Composable
fun ColorSwatches(
    colors: List<ColorOption>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        colors.forEach { option ->
            val isSelected = option.name == selected
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(option.name) })
                    .semantics { contentDescription = option.name },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(option.swatchColor(), CircleShape)
                        .border(1.dp, swatchEdge(), CircleShape),
                )
            }
        }
    }
}

/** Size chips (min 52 × 44 dp); they wrap when there are many. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SizeChips(
    sizes: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        sizes.forEach { size ->
            val isSelected = size == selected
            val scheme = MaterialTheme.colorScheme
            val shape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 52.dp)
                    .height(44.dp)
                    .clip(shape)
                    .background(if (isSelected) scheme.primaryContainer else scheme.surface)
                    .border(
                        width = if (isSelected) 2.dp else 1.4.dp,
                        color = if (isSelected) scheme.primary else scheme.outlineVariant,
                        shape = shape,
                    )
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(size) })
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = size,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface,
                )
            }
        }
    }
}

private fun ColorOption.swatchColor(): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)

// A hairline so light swatches show on light surfaces (and dark ones on dark).
@Composable
private fun swatchEdge(): Color =
    if (MaterialTheme.shoplyExtra.isDark) Color.White.copy(alpha = 0.13f) else Color.Black.copy(alpha = 0.08f)

@PreviewLightDark
@Composable
fun VariantPickersPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(ShoplyDimens.gutter),
                verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space4),
            ) {
                ColorSwatches(
                    colors = listOf(ColorOption("Sand", "#D8CAB6"), ColorOption("Charcoal", "#3A3735"), ColorOption("Sage", "#A2B09A")),
                    selected = "Sand",
                    onSelect = {},
                )
                SizeChips(sizes = listOf("XS", "S", "M", "L", "XL"), selected = "M", onSelect = {})
            }
        }
    }
}
