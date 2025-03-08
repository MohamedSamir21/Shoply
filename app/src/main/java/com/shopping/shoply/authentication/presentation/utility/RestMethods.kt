package com.shopping.shoply.authentication.presentation.utility

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.reset_password.forgot_password.model.RestMethod

fun getRestMethods(): List<RestMethod> = listOf(
    RestMethod(
        name = UiText.StringResource(R.string.email),
        icon = Icons.Outlined.Email
    ),
    RestMethod(
        name = UiText.StringResource(R.string.phone_number),
        icon = Icons.Outlined.Phone
    )
)