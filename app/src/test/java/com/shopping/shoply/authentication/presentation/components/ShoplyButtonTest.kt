package com.shopping.shoply.authentication.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShoplyButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setShoplyButton(
        label: String = "Sign In",
        onClick: () -> Unit = {},
        enabled: Boolean = true
    ) {
        composeRule.setContent {
            ShoplyTheme {
                ShoplyButton(
                    label = label,
                    onClick = onClick,
                    enable = enabled
                )
            }
        }
    }

    @Test
    fun `shows the label correctly`() {
        val signUpText = "Sign Up"
        setShoplyButton(label = signUpText)

        composeRule.onNodeWithText(signUpText).assertIsDisplayed()
    }

    @Test
    fun `ensures the button responds to the clicks`() {
        var clicks = 0
        setShoplyButton(
            onClick = { clicks++ }
        )

        composeRule.onNodeWithText("Sign In").assertIsEnabled()
        composeRule.onNodeWithText("Sign In").performClick()

        assertThat(clicks).isEqualTo(1)
    }

    @Test
    fun `ensures the buttons does not respond to the clicks when it is disabled`() {
        var clicks = 0
        setShoplyButton(
            onClick = { clicks++ },
            enabled = false
        )

        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.onNodeWithText("Sign In").assertIsNotEnabled()
        assertThat(clicks).isEqualTo(0)
    }
}