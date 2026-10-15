package com.shopping.shoply

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.shopping.shoply.common.presentation.navigation.ShoplyNavGraph

@Composable
fun ShoplyAppContent() {
    Scaffold { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            // Warm off-white page so white cards/sheets read as raised above it.
            color = MaterialTheme.colorScheme.background
        ) {
            ShoplyNavGraph(
                navController = rememberNavController(),
                windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
            )
        }
    }
}