package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shopping.shoply.R

@Composable
fun FacebookButton(
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.facebook),
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_facebook),
            contentDescription = label
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            color = textColor
        )
    }
}

@Preview
@Composable
private fun FacebookButtonPreview() {
    FacebookButton {

    }
}