package com.shopping.shoply.authentication.presentation.resetpassword.forgotpassword.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.shopping.shoply.authentication.presentation.utility.UiText

data class ResetMethod(
    val name: UiText,
    val message: String? = null,
    val icon: ImageVector
)
