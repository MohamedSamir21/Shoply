package com.shopping.shoply.authentication.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MethodCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setMethodCard(
        kind: String = "Email",
        value: String = "user@gmail.com",
        selected: Boolean = false,
        onClick: () -> Unit = {}
    ) {
        composeRule.setContent {
            ShoplyTheme {
                MethodCard(
                    icon = Icons.Default.Info,
                    kind = kind,
                    value = value,
                    selected = selected,
                    onClick = onClick
                )
            }
        }
    }

    @Test
    fun `shows kind text in the method card`() {
        val emailText = "Email"
        setMethodCard(kind = emailText)

        composeRule.onNodeWithText(emailText).assertIsDisplayed()
    }

    @Test
    fun `shows value text in the method card`() {
        val valueText = "user@gmail.com"
        setMethodCard(value = valueText)

        composeRule.onNodeWithText(valueText).assertIsDisplayed()
    }

    @Test
    fun `ensures the card responds to the clicks`() {
        var clicks = 0
        setMethodCard(onClick = { clicks++ })

        composeRule.onNodeWithTag(METHOD_CARD_TAG).performClick()

        assertThat(clicks).isEqualTo(1)
    }

    @Test
    fun `reports itself as selected when the card is selected`() {
        setMethodCard(selected = true)

        composeRule.onNodeWithTag(METHOD_CARD_TAG).assertIsSelected()
    }

    @Test
    fun `reports itself as unselected when the card is not selected`() {
        setMethodCard(selected = false)

        composeRule.onNodeWithTag(METHOD_CARD_TAG).assertIsNotSelected()
    }
}
