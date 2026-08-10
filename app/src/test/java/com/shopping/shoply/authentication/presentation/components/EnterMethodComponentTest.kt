package com.shopping.shoply.authentication.presentation.components

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnterMethodComponentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()
    private val emittedMethodValues = mutableListOf<String>()

    private fun setEnterMethodComponent(
        isExpandedScreen: Boolean = false,
        methodName: String = "Email",
        methodMessage: String = "Enter your email",
        isVerifyingValue: Boolean = false,
        errorMessage: UiText? = null,
        inputLabel: String? = null,
        onNext: () -> Unit = {},
        onBack: () -> Unit = {},
        menuButton: @Composable () -> Unit = {}
    ) {
        composeRule.setContent {
            ShoplyTheme {
                var currentMethodValue by remember { mutableStateOf("") }

                EnterMethodComponent(
                    isExpandedScreen = isExpandedScreen,
                    methodName = methodName,
                    methodMessage = methodMessage,
                    methodValue = currentMethodValue,
                    isVerifyingValue = isVerifyingValue,
                    errorMessage = errorMessage,
                    inputLabel = inputLabel,
                    onMethodValueChange = {
                        emittedMethodValues += it
                        currentMethodValue = it
                    },
                    onBack = onBack,
                    onNext = onNext,
                    menuButton = menuButton
                )
            }
        }
    }

    private fun nextText() = composeRule.activity.getString(R.string.next)
    private fun enterText() = composeRule.activity.getString(R.string.enter)
    private fun expandedNextText() = composeRule.activity.getString(R.string.expanded_next)
    private fun linearProgressText() = composeRule.activity.getString(R.string.linear_progress)
    private fun backPress() = composeRule.activity.getString(R.string.back)
    private fun requiredHint() = "*${composeRule.activity.getString(R.string.required)}"

    @Test
    fun `shows only the next arrow button if the screen is expanded`() {
        setEnterMethodComponent(isExpandedScreen = true)

        composeRule.onNodeWithContentDescription(expandedNextText()).assertIsDisplayed()
        composeRule.onNodeWithText(nextText()).assertDoesNotExist()
    }

    @Test
    fun `shows only the next text button if the screen is not expanded`() {
        setEnterMethodComponent(isExpandedScreen = false)

        composeRule.onNodeWithContentDescription(expandedNextText()).assertDoesNotExist()
        composeRule.onNodeWithText(nextText()).assertIsDisplayed()
    }

    @Test
    fun `ensures the method name is displayed at the top bar`() {
        val methodName = "Email"
        setEnterMethodComponent(methodName = methodName)

        composeRule.onNodeWithText(enterText() + " $methodName").assertIsDisplayed()
    }

    @Test
    fun `ensures the method message is displayed`() {
        val methodMessage = "Enter your email"
        setEnterMethodComponent(methodMessage = methodMessage)

        composeRule.onNodeWithText(methodMessage).assertIsDisplayed()
    }

    @Test
    fun `forwards each typed method value through onMethodValueChange`() {
        val label = "Email"
        setEnterMethodComponent(inputLabel = label)

        composeRule.onNodeWithText(label).performTextInput("user")
        composeRule.onNodeWithText(label).performTextInput("@")
        composeRule.onNodeWithText(label).performTextInput("gmail")
        composeRule.onNodeWithText(label).performTextInput(".com")

        assertThat(emittedMethodValues).containsExactly(
            "user",
            "user@",
            "user@gmail",
            "user@gmail.com"
        ).inOrder()
    }

    @Test
    fun `shows the linear progress indicator instead of the next button while verifying`() {
        setEnterMethodComponent(isVerifyingValue = true)

        composeRule.onNodeWithContentDescription(linearProgressText()).assertIsDisplayed()
        // The progress indicator replaces the button rather than sitting beside it — that is
        // what stops a second submit while the first is still in flight.
        composeRule.onNodeWithText(nextText()).assertDoesNotExist()
    }

    @Test
    fun `shows the error message when there's an error`() {
        val errorMessage = "Whatever error"
        setEnterMethodComponent(errorMessage = UiText.DynamicString(errorMessage))

        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun `shows no supporting text when there is no error`() {
        setEnterMethodComponent(errorMessage = null)

        // Unlike the standalone fields, this component suppresses the supporting slot entirely
        // when there is nothing to report — no "*required" hint under the input.
        composeRule.onNodeWithText(requiredHint()).assertDoesNotExist()
    }

    @Test
    fun `shows label when it's provided`() {
        val label = "Email"
        setEnterMethodComponent(inputLabel = label)

        composeRule.onNodeWithText(label).assertIsDisplayed()
    }

    @Test
    fun `triggers onBack when the back button is clicked`() {
        var clicked = 0

        setEnterMethodComponent(
            onBack = {
                clicked++
            }
        )

        composeRule.onNodeWithContentDescription(backPress()).performClick()

        assertThat(clicked).isEqualTo(1)
    }

    @Test
    fun `triggers onNext when the arrow button is clicked`() {
        var clicked = 0

        setEnterMethodComponent(
            isExpandedScreen = true,
            onNext = {
                clicked++
            }
        )

        composeRule.onNodeWithContentDescription(expandedNextText()).performClick()

        assertThat(clicked).isEqualTo(1)
    }

    @Test
    fun `triggers onNext when the next button is clicked`() {
        var clicked = 0

        setEnterMethodComponent(
            isExpandedScreen = false,
            onNext = {
                clicked++
            }
        )

        composeRule.onNodeWithText(nextText()).performClick()

        assertThat(clicked).isEqualTo(1)
    }

    @Test
    fun `appends the menuButton to the EnterMethodComponent`() {
        val menuButtonText = "Menu Button Text"
        setEnterMethodComponent(
            menuButton = {
                Text(menuButtonText)
            }
        )

        composeRule.onNodeWithText(menuButtonText).assertIsDisplayed()
    }
}