package com.shopping.shoply.authentication.presentation.reset_password.forgot_password.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.shopping.shoply.authentication.presentation.utility.UiText

data class RestMethod(
    val name: UiText,
    val message: String? = null,
    val icon: ImageVector
)
