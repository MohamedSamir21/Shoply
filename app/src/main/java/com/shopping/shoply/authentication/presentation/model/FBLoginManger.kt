package com.shopping.shoply.authentication.presentation.model

import com.facebook.login.LoginManager
import com.shopping.shoply.authentication.domain.model.FacebookLoginManger

data class FBLoginManger(
    val loginManager: LoginManager
) : FacebookLoginManger
