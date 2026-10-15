package com.shopping.shoply.authentication.presentation.resetpassword.createpassword

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
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
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp")
class CreateNewPasswordContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val intents = mutableListOf<CreateNewPasswordIntent>()

    private fun setCreateNewPasswordContent(state: CreateNewPasswordState = CreateNewPasswordState()) {
        composeRule.setContent {
            ShoplyTheme {
                // Close the loop for typed text: a field whose value never updates re-syncs to the
                // stale value (and emits it) when focus moves away.
                var currentState by remember { mutableStateOf(state) }

                CreateNewPasswordContent(
                    isExpandedScreen = false,
                    state = currentState,
                    onIntent = { intent ->
                        intents += intent
                        currentState = when (intent) {
                            is CreateNewPasswordIntent.ChangePassword ->
                                currentState.copy(password = intent.password)
                            is CreateNewPasswordIntent.ChangeConfirmedPassword ->
                                currentState.copy(confirmedPassword = intent.confirmedPassword)
                            else -> currentState
                        }
                    },
                    onBack = {},
                )
            }
        }
    }

    private fun string(id: Int) = composeRule.activity.getString(id)

    @Test
    fun `typing in each field sends its change intent`() {
        setCreateNewPasswordContent()

        composeRule.onNodeWithText(string(R.string.new_password)).performTextInput("a")
        composeRule.onNodeWithText(string(R.string.confirm_new_password)).performTextInput("b")

        assertThat(intents).containsExactly(
            CreateNewPasswordIntent.ChangePassword("a"),
            CreateNewPasswordIntent.ChangeConfirmedPassword("b"),
        ).inOrder()
    }

    @Test
    fun `the confirm field's visibility toggle reports the current visibility`() {
        setCreateNewPasswordContent(CreateNewPasswordState(showPassword = false))

        // Both fields carry the same toggle label; the second one is the confirm field.
        composeRule.onAllNodesWithContentDescription(string(R.string.visibility_off))[1].performClick()

        assertThat(intents).containsExactly(CreateNewPasswordIntent.ChangePasswordVisibility(false))
    }

    @Test
    fun `tapping reset password submits`() {
        setCreateNewPasswordContent()

        composeRule.onNodeWithText(string(R.string.reset_password_button)).performClick()

        assertThat(intents).containsExactly(CreateNewPasswordIntent.Submit)
    }

    @Test
    fun `a loading dialog replaces the reset button while processing`() {
        // The loading logo spins forever.
        composeRule.mainClock.autoAdvance = false
        setCreateNewPasswordContent(CreateNewPasswordState(isProcessing = true))

        composeRule.onNode(isDialog()).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.reset_password_button)).assertDoesNotExist()
    }

    @Test
    fun `shows the confirmed-password error`() {
        setCreateNewPasswordContent(
            CreateNewPasswordState(confirmedPasswordError = UiText.StringResource(R.string.unknown_error))
        )

        composeRule.onNodeWithText(string(R.string.unknown_error)).assertIsDisplayed()
    }
}
