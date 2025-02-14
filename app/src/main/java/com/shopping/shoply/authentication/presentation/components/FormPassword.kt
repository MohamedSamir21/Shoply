package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.shopping.shoply.R

@Composable
fun FormPassword(
    modifier: Modifier = Modifier,
    password: String,
    showPassword: Boolean,
    onPasswordChanged: (String) -> Unit,
    showSupportingText: Boolean = true,
    errorMessage: String?,
    onVisibilityChanged: (Boolean) -> Unit,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) {
    val visibilityIcon = ImageVector.vectorResource(
        id = if (showPassword) {
            R.drawable.ic_visibility_on
        } else {
            R.drawable.ic_visibility_off
        }
    )

    FormTextField(
        value = password,
        onValueChange = { onPasswordChanged(it) },
        onTrailingIconClicked = { onVisibilityChanged(showPassword.not()) },
        label = stringResource(R.string.password),
        leadingIcon = Icons.Outlined.Lock,
        trailingIcon = visibilityIcon,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        showSupportingText = showSupportingText,
        supportingText = errorMessage,
        modifier = modifier,
        shape = shape,
        colors = colors
    )
}

@Preview
@Composable
private fun FormPasswordPreview() {
    FormPassword(
        password = "password",
        showPassword = false,
        errorMessage = null,
        onPasswordChanged = {},
        onVisibilityChanged = {}
    )
}