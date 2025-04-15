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
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
    label: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClicked: () -> Unit = {},
    showSupportingText: Boolean = true,
    supportingText: String? = null,
    requestFocus: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) {
    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember {
        mutableStateOf(false)
    }
    val focusColor = if (hasFocus)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.tertiary

    if (requestFocus) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        label = {
            label?.let {
                Text(text = it)
            }
        },
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = "",
                    tint = focusColor
                )
            }
        } else null,
        trailingIcon = if (hasFocus) {
            if (trailingIcon != null) {
                {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = "",
                        modifier = Modifier.clickable { onTrailingIconClicked() },
                        tint = focusColor
                    )
                }
            } else null
        } else null,
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
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged {
                hasFocus = it.isFocused
            },
        shape = shape,
        colors = colors
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

