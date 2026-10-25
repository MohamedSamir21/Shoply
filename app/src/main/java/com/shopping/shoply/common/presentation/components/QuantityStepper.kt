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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyExtra

/**
 * The design's quantity stepper: − value +, 44 dp tall ([compact]: 34 dp, for list rows). − is
 * disabled at [min], + at [max]; + is orange while enabled. With [onRemove], − becomes a red trash
 * button at [min] instead of disabling.
 */
@Composable
fun QuantityStepper(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 1,
    max: Int = 20,
    compact: Boolean = false,
    onRemove: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    val size = if (compact) StepperSize.Compact else StepperSize.Regular
    Row(
        modifier = modifier
            .height(size.height)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape)
            .border(1.4.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onRemove != null && value <= min) {
            StepButton(
                icon = ShoplyAppIcons.Trash,
                label = stringResource(R.string.cd_cart_remove_item),
                enabled = true,
                enabledTint = MaterialTheme.shoplyExtra.red,
                size = size,
                onClick = onRemove,
            )
        } else {
            StepButton(
                icon = ShoplyAppIcons.Minus,
                label = stringResource(R.string.cd_quantity_decrease),
                enabled = value > min,
                enabledTint = MaterialTheme.colorScheme.onSurface,
                size = size,
                onClick = onDecrease,
            )
        }
        Text(
            text = value.toString(),
            fontSize = size.fontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = size.valueWidth)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
        StepButton(
            icon = ShoplyAppIcons.Plus,
            label = stringResource(R.string.cd_quantity_increase),
            enabled = value < max,
            enabledTint = MaterialTheme.colorScheme.primary,
            size = size,
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
    size: StepperSize,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(size.buttonWidth)
            .fillMaxHeight()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) enabledTint else MaterialTheme.shoplyExtra.inkFaint,
            modifier = Modifier.size(size.iconSize),
        )
    }
}

private enum class StepperSize(
    val height: Dp,
    val buttonWidth: Dp,
    val iconSize: Dp,
    val valueWidth: Dp,
    val fontSize: TextUnit,
) {
    Regular(height = 44.dp, buttonWidth = 42.dp, iconSize = 19.dp, valueWidth = 26.dp, fontSize = 15.5.sp),
    Compact(height = 34.dp, buttonWidth = 32.dp, iconSize = 17.dp, valueWidth = 20.dp, fontSize = 14.sp),
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

/** Compact, as in a cart row: trash at 1 (with onRemove), and in range. */
@PreviewLightDark
@Composable
fun QuantityStepperCompactPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(modifier = Modifier.padding(ShoplyDimens.gutter)) {
                QuantityStepper(value = 1, onDecrease = {}, onIncrease = {}, compact = true, onRemove = {})
                QuantityStepper(
                    value = 2,
                    onDecrease = {},
                    onIncrease = {},
                    compact = true,
                    onRemove = {},
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}
