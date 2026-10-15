package com.shopping.shoply.authentication.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

/** Google sign-in affordance — the brand icon bound to a [SocialButton]. */
@Composable
fun GoogleButton(
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.google),
    compact: Boolean = false,
    onClick: () -> Unit
) {
    SocialButton(
        icon = ImageVector.vectorResource(id = R.drawable.ic_google),
        label = label,
        onClick = onClick,
        modifier = modifier,
        compact = compact,
    )
}

@PreviewLightDark
@Composable
private fun GoogleButtonPreview() {
    ShoplyTheme {
        GoogleButton(label = "Continue with Google") {}
    }
}
