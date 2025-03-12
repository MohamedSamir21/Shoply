package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

@Composable
fun EnterMethodComponent(
    modifier: Modifier = Modifier,
    isExpandedScreen: Boolean,
    methodName: String,
    methodMessage: String,
    methodValue: String,
    isVerifyingValue: Boolean,
    errorMessage: UiText? = null,
    inputLabel: String,
    onMethodValueChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        BackButton(onBack = onBack)
        Spacer(Modifier.height(20.dp))
        EnterMethodText(methodName)
        Spacer(Modifier.height(40.dp))
        GuidingMessageText(methodMessage)
        Spacer(Modifier.height(30.dp))
        FormTextField(
            modifier = if (!isExpandedScreen) Modifier
                .fillMaxWidth()
                .padding(10.dp) else Modifier.padding(10.dp),
            value = methodValue,
            label = inputLabel,
            onValueChange = { onMethodValueChange(it) },
            showSupportingText = errorMessage != null,
            supportingText = errorMessage?.asString(),
            keyboardOptions = keyboardOptions
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isVerifyingValue) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator()
            } else {
                ShoplyButton(
                    modifier = if (!isExpandedScreen) Modifier
                        .fillMaxWidth()
                        .padding(10.dp) else Modifier,
                    shape = RoundedCornerShape(10.dp),
                    label = stringResource(R.string.next),
                    onClick = onNext
                )
            }
        }
    }
}

@Composable
fun GuidingMessageText(
    message: String
) {
    Text(
        text = message,
        modifier = Modifier.padding(start = 10.dp),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.tertiary
    )
}

@Composable
fun EnterMethodText(
    methodName: String
) {
    Text(
        text = stringResource(R.string.enter) + " $methodName",
        modifier = Modifier.padding(start = 10.dp),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun BackButton(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(
            onClick = onBack
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back_arrow),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EnterMethodComponentContentPreview() {
    ShoplyTheme {
        EnterMethodComponent(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            isExpandedScreen = false,
            methodName = stringResource(R.string.method_name),
            methodMessage = stringResource(R.string.method_message),
            methodValue = "",
            isVerifyingValue = false,
            inputLabel = stringResource(R.string.label),
            onMethodValueChange = {},
            onBack = {},
            onNext = {}
        )
    }
}