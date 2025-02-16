package com.shopping.shoply.authentication.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.shopping.shoply.R

@Composable
fun FacebookButton(
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.facebook),
    textColor: Color = Color.Black,
    permissions: List<String> = listOf("email", "public_profile"),
    onClick: (LoginManager, CallbackManager) -> Unit
) {
    val loginManager = remember {
        LoginManager.getInstance()
    }
    val callbackManager = remember {
        CallbackManager.Factory.create()
    }
    val fbLauncher = rememberLauncherForActivityResult(
        loginManager.createLogInActivityResultContract(callbackManager, null)
    ) {}

    DisposableEffect(loginManager) {
        onDispose {
            loginManager.unregisterCallback(callbackManager)
        }
    }

    OutlinedButton(
        modifier = modifier,
        onClick = {
            onClick(loginManager, callbackManager)
            fbLauncher.launch(permissions)
        }
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
    FacebookButton { _, _ -> }
}