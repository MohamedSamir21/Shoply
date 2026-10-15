package com.shopping.shoply.authentication.presentation.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShoplyTopBarTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun setShoplyTopBar(
        title: String = "Title",
        onBack: (() -> Unit)? = null
    ) {
        composeRule.setContent {
            ShoplyTheme {
                ShoplyTopBar(
                    title = title,
                    onBack = onBack
                )
            }
        }
    }

    private fun backPressText() = composeRule.activity.getString(R.string.back)

    @Test
    fun `shows the title correctly`() {
        val title = "Fun"
        setShoplyTopBar(title = title)

        composeRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun `ensures the back button does not exist when the onBack is null`() {
        setShoplyTopBar()

        composeRule.onNodeWithContentDescription(backPressText()).assertDoesNotExist()
    }

    @Test
    fun `ensures the back button is displayed and clickable when the onBack is not null`() {
        var clicks = 0
        setShoplyTopBar(onBack = { clicks++ })

        composeRule.onNodeWithContentDescription(backPressText()).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(backPressText()).performClick()
        assertThat(clicks).isEqualTo(1)
    }
}