package com.shopping.shoply.authentication.presentation.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.shopping.shoply.R

@Composable
fun ShoplyButton(
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.button),
    shape: Shape = ButtonDefaults.shape,
    enable: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        shape = shape,
        enabled = enable,
        onClick = onClick
    ) {
        Text(text = label)
    }
}

@Preview
@Composable
private fun RoundedShoplyButtonPreview() {
    ShoplyButton { }
}