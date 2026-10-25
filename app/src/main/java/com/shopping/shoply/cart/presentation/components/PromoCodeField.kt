package com.shopping.shoply.cart.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.cart.presentation.AppliedPromo
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.darkRing
import com.shopping.shoply.common.presentation.theme.shoplyExtra

/**
 * "Promo Code" and either the input (🏷, text, Apply) or, once [applied], a green "SHOPLY10 applied ·
 * 10% off" bar with Remove. [invalid] gives the input a red border and an error line under it;
 * [applying] disables Apply.
 */
@Composable
fun PromoCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    onApply: () -> Unit,
    onRemove: () -> Unit,
    applied: AppliedPromo?,
    invalid: Boolean,
    applying: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.cart_promo_title),
            style = MaterialTheme.typography.titleSmall,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        if (applied != null) {
            AppliedBar(promo = applied, onRemove = onRemove)
        } else {
            CodeInput(value, onValueChange, onApply, invalid, applying)
            if (invalid) {
                Text(
                    text = stringResource(R.string.cart_promo_invalid),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.shoplyExtra.red,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun CodeInput(
    value: String,
    onValueChange: (String) -> Unit,
    onApply: () -> Unit,
    invalid: Boolean,
    applying: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val red = MaterialTheme.shoplyExtra.red
    val focusManager = LocalFocusManager.current
    val label = stringResource(R.string.cd_promo_code)
    val apply = {
        focusManager.clearFocus()
        onApply()
    }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 14.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
        ),
        cursorBrush = SolidColor(scheme.primary),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { apply() }),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = label },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .height(54.dp)
                    .clip(ShoplyShapes.field)
                    .background(scheme.surfaceVariant)
                    .border(
                        width = if (invalid) 2.dp else 1.4.dp,
                        color = if (invalid) red else scheme.outlineVariant,
                        shape = ShoplyShapes.field,
                    )
                    .padding(start = 14.dp, end = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = ShoplyAppIcons.Tag,
                    contentDescription = null,
                    tint = if (invalid) red else scheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp),
                )
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.cart_promo_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    innerTextField()
                }
                SmallButton(
                    label = stringResource(R.string.cart_promo_apply),
                    onClick = apply,
                    enabled = !applying && value.isNotBlank(),
                    container = scheme.primaryContainer,
                    content = scheme.onPrimaryContainer,
                )
            }
        },
    )
}

@Composable
private fun AppliedBar(promo: AppliedPromo, onRemove: () -> Unit) {
    val extra = MaterialTheme.shoplyExtra
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(ShoplyShapes.field)
            .background(extra.greenSoft)
            .darkRing(ShoplyShapes.field)
            .padding(start = 14.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ShoplyAppIcons.CheckCircle,
            contentDescription = null,
            tint = extra.onGreenSoft,
            modifier = Modifier.size(21.dp),
        )
        val applied = stringResource(R.string.cart_promo_applied, promo.percentOff)
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(promo.code) }
                append(" ")
                append(applied)
            },
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = extra.onGreenSoft,
            modifier = Modifier.weight(1f),
        )
        SmallButton(
            label = stringResource(R.string.cart_remove),
            onClick = onRemove,
            enabled = true,
            container = Color.Transparent,
            content = extra.onGreenSoft,
        )
    }
}

/** The design's small button: 40 dp, 12 dp corners, bold 13.5 sp label. */
@Composable
private fun SmallButton(label: String, onClick: () -> Unit, enabled: Boolean, container: Color, content: Color) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = container,
        contentColor = if (enabled) content else MaterialTheme.shoplyExtra.inkFaint,
    ) {
        Box(modifier = Modifier.height(40.dp).padding(horizontal = ShoplyDimens.space4), contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PromoCodeFieldPreview(value: String, applied: AppliedPromo?, invalid: Boolean) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PromoCodeField(
                value = value,
                onValueChange = {},
                onApply = {},
                onRemove = {},
                applied = applied,
                invalid = invalid,
                applying = false,
                modifier = Modifier.padding(ShoplyDimens.gutter),
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PromoCodeFieldEmptyPreview() = PromoCodeFieldPreview(value = "", applied = null, invalid = false)

@PreviewLightDark
@Composable
fun PromoCodeFieldInvalidPreview() = PromoCodeFieldPreview(value = "SAVE50", applied = null, invalid = true)

@PreviewLightDark
@Composable
fun PromoCodeFieldAppliedPreview() =
    PromoCodeFieldPreview(value = "", applied = AppliedPromo("SHOPLY10", 10), invalid = false)
