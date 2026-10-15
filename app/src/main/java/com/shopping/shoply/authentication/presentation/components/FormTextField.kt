package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyShapes

/**
 * Outlined text field from the Shoply Remix design system: warm filled
 * container that turns to plain surface on focus, 14dp radius, leading icon,
 * floating label, always-visible trailing affordance and a red error helper.
 */
@Composable
fun FormTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    leadingIcon: ImageVector? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    trailingIconContentDescription: String = "",
    onTrailingIconClicked: () -> Unit = {},
    enabled: Boolean = true,
    showSupportingText: Boolean = true,
    supportingText: String? = null,
    requestFocus: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = ShoplyShapes.field,
    colors: TextFieldColors = shoplyTextFieldColors()
) {
    val focusRequester = remember { FocusRequester() }
    val isError = !supportingText.isNullOrEmpty()
    val requiredHint = "*${stringResource(R.string.required)}"

    if (requestFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }

    // The input box keeps a single minimum control height and the helper is drawn
    // below it (instead of M3's built-in supporting slot), so every field box is the
    // same height whether or not a helper/error line is shown. The phone field reuses
    // this exact control and embeds the country picker as its leading content.
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label?.let { { Text(text = it) } },
            // A custom leading composable (e.g. the embedded country picker) takes
            // precedence over the plain leading icon.
            leadingIcon = leadingContent ?: leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null) }
            },
            trailingIcon = trailingIcon?.let {
                {
                    // IconButton (not a clickable Icon) for the 48dp touch target and button role.
                    IconButton(onClick = onTrailingIconClicked) {
                        Icon(imageVector = it, contentDescription = trailingIconContentDescription)
                    }
                }
            },
            isError = isError,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ShoplyDimens.fieldHeight)
                .focusRequester(focusRequester),
            shape = shape,
            colors = colors,
        )
        if (showSupportingText) {
            Text(
                text = supportingText?.takeIf { it.isNotEmpty() } ?: requiredHint,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = ShoplyDimens.space4,
                    top = ShoplyDimens.space1,
                ),
            )
        }
    }
}

/** Shared field palette: warm filled container, orange focus, red error. */
@Composable
fun shoplyTextFieldColors(): TextFieldColors {
    val scheme = MaterialTheme.colorScheme
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = scheme.surface,
        unfocusedContainerColor = scheme.surfaceVariant,
        disabledContainerColor = scheme.surfaceVariant,
        errorContainerColor = scheme.surfaceVariant,
        focusedBorderColor = scheme.primary,
        unfocusedBorderColor = scheme.outlineVariant,
        errorBorderColor = scheme.error,
        focusedLeadingIconColor = scheme.primary,
        unfocusedLeadingIconColor = scheme.onSurfaceVariant,
        errorLeadingIconColor = scheme.error,
        focusedTrailingIconColor = scheme.primary,
        unfocusedTrailingIconColor = scheme.onSurfaceVariant,
        errorTrailingIconColor = scheme.error,
        focusedLabelColor = scheme.primary,
        unfocusedLabelColor = scheme.onSurfaceVariant,
        errorLabelColor = scheme.error,
        focusedTextColor = scheme.onSurface,
        unfocusedTextColor = scheme.onSurface,
        cursorColor = scheme.primary,
    )
}
