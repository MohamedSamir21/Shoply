package com.shopping.shoply.authentication.presentation.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormPasswordTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val emitted = mutableListOf<String>()
    private val visibilityEvents = mutableListOf<Boolean>()

    private fun setFormPassword(
        password: String = "",
        showPassword: Boolean = false,
        label: String = "Password",
        showSupportingText: Boolean = true,
        errorMessage: String? = null,
    ) {
        composeRule.setContent {
            ShoplyTheme {
                var passwordValue by remember { mutableStateOf(password) }
                var showPasswordValue by remember { mutableStateOf(showPassword) }

                FormPassword(
                    password = passwordValue,
                    onPasswordChanged = {
                        passwordValue = it
                        emitted += it
                    },
                    showPassword = showPasswordValue,
                    onVisibilityChanged = {
                        visibilityEvents += it
                        showPasswordValue = !showPasswordValue
                    },
                    label = label,
                    showSupportingText = showSupportingText,
                    errorMessage = errorMessage
                )
            }
        }
    }

    private fun visibilityOn() =
        composeRule.activity.getString(
            R.string.visibility_on
        )

    private fun visibilityOff() =
        composeRule.activity.getString(
            R.string.visibility_off
        )

    private fun requiredText() =
        composeRule.activity.getString(
            R.string.required
        )

    @Test
    fun `forwards each typed password through onPasswordChanged`() {
        val label = "My Text field"
        setFormPassword(label = label)

        composeRule.onNodeWithText(label).performTextInput("pass")
        composeRule.onNodeWithText(label).performTextInput("word")

        assertThat(emitted).containsExactly("pass", "password").inOrder()
    }

    @Test
    fun `shows the label of the FormPassword`() {
        val label = "Label"
        setFormPassword(label = label)

        composeRule.onNodeWithText(label).assertIsDisplayed()
    }

    @Test
    fun `swaps the trailing icon to visibility-on after the icon is clicked`() {
        setFormPassword(
            showPassword = false,
        )

        composeRule.onNodeWithContentDescription(visibilityOff()).performClick()

        composeRule.onNodeWithContentDescription(visibilityOn()).assertIsDisplayed()
    }

    @Test
    fun `emits the current visibility value when the trailing icon is clicked`() {
        setFormPassword(showPassword = false)

        composeRule.onNodeWithContentDescription(visibilityOff()).performClick()

        // FormPassword forwards the CURRENT value, not its negation — inverting is the caller's job.
        assertThat(visibilityEvents).containsExactly(false)
    }

    @Test
    fun `shows star required supporting text when there is no error`() {
        setFormPassword(errorMessage = null)

        composeRule.onNodeWithText("*${requiredText()}").assertIsDisplayed()
    }

    @Test
    fun `shows error message supporting text when an error exists`() {
        val errorMessage = "Any error"
        setFormPassword(errorMessage = errorMessage)

        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun `hides supporting text when its visibility gets set to false`() {
        val errorMessage = "Any error"
        setFormPassword(showSupportingText = false, errorMessage = errorMessage)

        composeRule.onNodeWithText(errorMessage).assertDoesNotExist()
    }
}