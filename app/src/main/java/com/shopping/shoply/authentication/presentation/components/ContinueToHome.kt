package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shopping.shoply.R

@Composable
fun ContinueToHome(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    shape: Shape = RoundedCornerShape(10.dp)
) {
    Button(
        modifier = modifier.padding(horizontal = 20.dp),
        onClick = onClick,
        colors = colors.copy(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = shape
    ) {
        Text(text = stringResource(R.string.continue_to_home))
    }
}