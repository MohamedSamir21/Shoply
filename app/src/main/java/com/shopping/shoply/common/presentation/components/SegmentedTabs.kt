package com.shopping.shoply.common.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyExtra

/** One option of [SegmentedTabs]; [count] shows as a small badge when not null. */
data class SegmentedTab(val label: String, val count: Int? = null)

/**
 * The design's `Segmented` control: equal-width options in a grey track; the selected one is a
 * raised surface, and its count badge turns orange.
 */
@Composable
fun SegmentedTabs(
    tabs: List<SegmentedTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(scheme.surfaceVariant)
            .padding(4.dp),
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .then(if (selected) Modifier.shadow(2.dp, RoundedCornerShape(11.dp)) else Modifier)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (selected) scheme.surface else scheme.surfaceVariant)
                    .selectable(selected = selected, onClick = { onSelect(index) }, role = Role.Tab),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tab.label,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
                )
                if (tab.count != null) {
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 20.dp)
                            .height(20.dp)
                            .background(if (selected) scheme.primary else MaterialTheme.shoplyExtra.surfaceHi, RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${tab.count}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun SegmentedTabsPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SegmentedTabs(
                tabs = listOf(SegmentedTab("Active", 2), SegmentedTab("Past", 3)),
                selectedIndex = 0,
                onSelect = {},
                modifier = Modifier.padding(ShoplyDimens.gutter),
            )
        }
    }
}
