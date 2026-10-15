package com.shopping.shoply.authentication.presentation.splash

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `tapping the splash skips it`() {
        var tapCount = 0
        // The bouncing dots never go idle.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            ShoplyTheme { SplashContent(onTap = { tapCount++ }) }
        }

        composeRule.onNode(hasClickAction()).performClick()

        assertThat(tapCount).isEqualTo(1)
    }
}
