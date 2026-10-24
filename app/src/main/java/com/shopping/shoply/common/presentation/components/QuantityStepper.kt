package com.shopping.shoply.common.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyExtra

/**
 * The design's quantity stepper: − value +, 44 dp tall. − is disabled at [min], + at [max];
 * + is orange while enabled.
 */
@Composable
fun QuantityStepper(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 1,
    max: Int = 20,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .height(44.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape)
            .border(1.4.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepButton(
            icon = ShoplyAppIcons.Minus,
            label = stringResource(R.string.cd_quantity_decrease),
            enabled = value > min,
            enabledTint = MaterialTheme.colorScheme.onSurface,
            onClick = onDecrease,
        )
        Text(
            text = value.toString(),
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 26.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
        StepButton(
            icon = ShoplyAppIcons.Plus,
            label = stringResource(R.string.cd_quantity_increase),
            enabled = value < max,
            enabledTint = MaterialTheme.colorScheme.primary,
            onClick = onIncrease,
        )
    }
}

@Composable
private fun StepButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    enabledTint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(42.dp)
            .fillMaxHeight()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) enabledTint else MaterialTheme.shoplyExtra.inkFaint,
            modifier = Modifier.size(19.dp),
        )
    }
}

/** At the minimum (− disabled), in range, and at the maximum (+ disabled). */
@PreviewLightDark
@Composable
fun QuantityStepperPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(modifier = Modifier.padding(ShoplyDimens.gutter)) {
                QuantityStepper(value = 1, onDecrease = {}, onIncrease = {})
                QuantityStepper(value = 3, onDecrease = {}, onIncrease = {}, modifier = Modifier.padding(start = 12.dp))
                QuantityStepper(value = 20, onDecrease = {}, onIncrease = {}, modifier = Modifier.padding(start = 12.dp))
            }
        }
    }
}
