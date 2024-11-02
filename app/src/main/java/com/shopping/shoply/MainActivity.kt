package com.shopping.shoply

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.shopping.shoply.authentication.presentation.screen.OnboardingScreen
import com.shopping.shoply.common.ui.theme.ShoplyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoplyTheme {
                OnboardingScreen(navController = rememberNavController())
            }
        }
    }
}
