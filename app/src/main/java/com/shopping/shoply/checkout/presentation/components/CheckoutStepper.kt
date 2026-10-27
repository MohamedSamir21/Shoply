package com.shopping.shoply.checkout.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyExtra

private val STEP_LABELS = listOf(
    R.string.checkout_step_address,
    R.string.checkout_step_delivery,
    R.string.checkout_step_payment,
    R.string.checkout_step_review,
)

/**
 * The 4 numbered steps joined by lines. Done steps show ✓ in orange and can be tapped
 * ([onStepClick]); the current one is outlined; later ones are faint.
 */
@Composable
fun CheckoutStepper(step: Int, onStepClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 4.dp, bottom = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        STEP_LABELS.forEachIndexed { index, label ->
            val done = index < step
            val current = index == step
            Column(
                modifier = Modifier
                    .width(56.dp)
                    .clickable(enabled = done) { onStepClick(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = when {
                                done -> scheme.primary
                                current -> scheme.primaryContainer
                                else -> scheme.surface.copy(alpha = 0f)
                            },
                            shape = CircleShape,
                        )
                        .then(
                            when {
                                current -> Modifier.border(2.dp, scheme.primary, CircleShape)
                                !done -> Modifier.border(2.dp, scheme.outline, CircleShape)
                                else -> Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (done) {
                        Icon(ShoplyAppIcons.Check, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(15.dp))
                    } else {
                        Text(
                            text = "${index + 1}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (current) scheme.primary else MaterialTheme.shoplyExtra.inkFaint,
                        )
                    }
                }
                Text(
                    text = stringResource(label),
                    fontSize = 11.5.sp,
                    fontWeight = if (current) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (index <= step) scheme.onSurface else scheme.onSurfaceVariant,
                )
            }
            if (index < STEP_LABELS.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 13.dp)
                        .height(2.dp)
                        .background(if (done) scheme.primary else scheme.outlineVariant, RoundedCornerShape(1.dp)),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun CheckoutStepperPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                CheckoutStepper(step = 0, onStepClick = {})
                CheckoutStepper(step = 2, onStepClick = {})
            }
        }
    }
}
