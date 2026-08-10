package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyIcons
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

/**
 * [FormTextField] preset for email entry — at-sign leading icon, clear affordance on the
 * trailing side, and an email keyboard that advances to the next field.
 */
@Composable
fun FormEmail(
    modifier: Modifier = Modifier,
    email: String,
    onEmailChanged: (String) -> Unit,
    showSupportingText: Boolean = true,
    errorMessage: String?,
    onClear: () -> Unit,
    colors: TextFieldColors = shoplyTextFieldColors(),
    shape: Shape = ShoplyShapes.field
) {
    FormTextField(
        modifier = modifier,
        value = email,
        onValueChange = { onEmailChanged(it) },
        label = stringResource(R.string.email),
        leadingIcon = ShoplyIcons.At,
        trailingIcon = ShoplyIcons.XCircle,
        onTrailingIconClicked = onClear,
        showSupportingText = showSupportingText,
        supportingText = errorMessage,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        shape = shape,
        colors = colors,
    )
}

@PreviewLightDark
@Composable
private fun FormEmailPreview() {
    ShoplyTheme {
        FormEmail(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            email = stringResource(R.string.user_gmail_com),
            onEmailChanged = {},
            onClear = {},
            errorMessage = null
        )
    }
}
