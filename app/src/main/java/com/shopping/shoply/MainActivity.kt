package com.shopping.shoply

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.settings.domain.SettingsRepository
import com.shopping.shoply.settings.domain.model.AppSettings
import com.shopping.shoply.settings.domain.rules.isDarkTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // The theme choice from Settings; read here so it applies to every screen.
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsFlow = remember { settingsRepository.observe() }
            val settings by settingsFlow.collectAsStateWithLifecycle(AppSettings())
            ShoplyTheme(darkTheme = isDarkTheme(settings.theme, isSystemInDarkTheme())) {
                ShoplyAppContent()
            }
        }
    }
}
