package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyIcons
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

/**
 * [FormTextField] preset for password entry — lock leading icon, a trailing eye that toggles
 * masking, and a password keyboard that advances to the next field.
 *
 * [onVisibilityChanged] receives the **current** value of [showPassword], not its negation:
 * inverting it is the caller's job. Both call sites hold the flag in a ViewModel, which flips it
 * on receipt.
 */
@Composable
fun FormPassword(
    modifier: Modifier = Modifier,
    password: String,
    showPassword: Boolean,
    onPasswordChanged: (String) -> Unit,
    showSupportingText: Boolean = true,
    label: String = stringResource(R.string.password),
    errorMessage: String?,
    onVisibilityChanged: (Boolean) -> Unit,
    shape: Shape = ShoplyShapes.field,
    colors: TextFieldColors = shoplyTextFieldColors()
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
        onTrailingIconClicked = { onVisibilityChanged(showPassword) },
        label = label,
        leadingIcon = ShoplyIcons.Lock,
        trailingIcon = visibilityIcon,
        trailingIconContentDescription = if (showPassword) stringResource(R.string.visibility_on) else stringResource(
            R.string.visibility_off
        ),
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

@PreviewLightDark
@Composable
private fun FormPasswordPreview() {
    ShoplyTheme {
        FormPassword(
            password = "password",
            showPassword = false,
            errorMessage = null,
            onPasswordChanged = {},
            onVisibilityChanged = {}
        )
    }
}
