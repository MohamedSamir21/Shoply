package com.shopping.shoply.authentication.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.shopping.shoply.R
import com.shopping.shoply.common.di.FacebookEntryPoint
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import dagger.hilt.android.EntryPointAccessors

@Composable
fun FacebookButton(
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.facebook),
    textColor: Color = Color.Unspecified,
    compact: Boolean = false,
    permissions: List<String> = listOf("email", "public_profile", "ads_read"),
    onClick: () -> Unit
) {
    val loginManager = remember { LoginManager.getInstance() }
    // The SAME CallbackManager the data source registers its callback on — otherwise the login
    // result would never reach it.
    val callbackManager = rememberFacebookCallbackManager()
    val fbLauncher = rememberLauncherForActivityResult(
        loginManager.createLogInActivityResultContract(callbackManager, null)
    ) {}

    DisposableEffect(loginManager) {
        onDispose { loginManager.unregisterCallback(callbackManager) }
    }

    SocialButton(
        icon = ImageVector.vectorResource(id = R.drawable.ic_facebook),
        label = label,
        onClick = {
            onClick()
            fbLauncher.launch(permissions)
        },
        modifier = modifier,
        compact = compact,
    )
}

/** Pulls the shared [CallbackManager] out of the Hilt graph — Compose cannot be constructor-injected. */
@Composable
private fun rememberFacebookCallbackManager(): CallbackManager {
    // Previews render without a Hilt Application, so the entry point is unavailable there.
    if (LocalInspectionMode.current) return remember { CallbackManager.Factory.create() }

    val appContext = LocalContext.current.applicationContext
    return remember(appContext) {
        EntryPointAccessors.fromApplication(appContext, FacebookEntryPoint::class.java)
            .callbackManager()
    }
}

@PreviewLightDark
@Composable
private fun FacebookButtonPreview() {
    ShoplyTheme {
        FacebookButton(label = "Continue with Facebook") { }
    }
}
