package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.shopping.shoply.R

@Composable
fun FormEmail(
    modifier: Modifier = Modifier,
    email: String,
    onEmailChanged: (String) -> Unit,
    showSupportingText: Boolean = true,
    errorMessage: String?,
    onClear: () -> Unit,
    shape: Shape = OutlinedTextFieldDefaults.shape
) {
    FormTextField(
        value = email,
        onValueChange = { onEmailChanged(it) },
        onTrailingIconClicked = onClear,
        label = stringResource(R.string.email),
        leadingIcon = ImageVector.vectorResource(id = R.drawable.ic_email),
        trailingIcon = Icons.Outlined.Clear,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        showSupportingText = showSupportingText,
        supportingText = errorMessage,
        modifier = modifier,
        shape = shape
    )
}

@Preview
@Composable
private fun FormEmailPreview() {
    FormEmail(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        email = stringResource(R.string.user_gmail_com),
        onEmailChanged = {},
        onClear = {},
        errorMessage = null
    )
}