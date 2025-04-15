package com.shopping.shoply.authentication.presentation.reset_password.otp_code_verification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

@Composable
fun OTPVerificationScreen() {

}

@Composable
private fun OTPVerificationContent(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp)
    ) {
        BackButton(onBack)
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.verification_code),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun BackButton(
    onBack: () -> Unit
) {
    IconButton(
        onClick = onBack,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back_arrow)
        )
    }
}

@PreviewLightDark
@Composable
private fun OTPVerificationContentPreview() {
    ShoplyTheme {
        OTPVerificationContent(
            onBack = {}
        )
    }
}