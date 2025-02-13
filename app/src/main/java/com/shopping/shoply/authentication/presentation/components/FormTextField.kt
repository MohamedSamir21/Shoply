package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.shopping.shoply.R

@Composable
fun FormTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    trailingIcon: ImageVector,
    onTrailingIconClicked: () -> Unit,
    showSupportingText: Boolean = true,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = OutlinedTextFieldDefaults.shape
) {
    var hasFocus by remember {
        mutableStateOf(false)
    }
    val focusColor = if (hasFocus)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.tertiary

    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        label = {
            Text(text = label)
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "",
                tint = focusColor
            )
        },
        trailingIcon = {
            if (hasFocus) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = "",
                    modifier = Modifier.clickable { onTrailingIconClicked() },
                    tint = focusColor
                )
            }
        },
        supportingText = {
            if (showSupportingText) {
                Text(
                    text = if (supportingText.isNullOrEmpty())
                        "*${stringResource(R.string.required)}"
                    else supportingText,
                    color = if (supportingText != null)
                        MaterialTheme.colorScheme.error
                    else focusColor
                )
            }
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        modifier = modifier.onFocusChanged {
            hasFocus = it.isFocused
        },
        shape = shape
    )
}

@Preview
@Composable
private fun FormTextFieldPreview() {
    FormTextField(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        value = "value",
        onValueChange = {},
        label = "label",
        leadingIcon = Icons.Default.Info,
        trailingIcon = Icons.Default.Clear,
        onTrailingIconClicked = {}
    )
}

