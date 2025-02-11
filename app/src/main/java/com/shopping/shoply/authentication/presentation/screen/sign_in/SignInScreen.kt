package com.shopping.shoply.authentication.presentation.screen.sign_in

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SignInScreen() {
    val currentUser = FirebaseAuth.getInstance().currentUser

    Column {
        Text("${currentUser?.email}")
        Text("isEmailVerified: ${currentUser?.isEmailVerified}")
    }
}