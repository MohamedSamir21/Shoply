package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shopping.shoply.R

@Composable
fun ContinueWithRow(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(vertical = 20.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HorizontalDivider(modifier = Modifier.width(100.dp))
        Text(
            text = stringResource(R.string.or_continue_with),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.width(100.dp))
    }
}

@Preview
@Composable
private fun ContinueWithRowPreview() {
    ContinueWithRow()
}