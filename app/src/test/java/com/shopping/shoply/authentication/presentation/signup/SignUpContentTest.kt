package com.shopping.shoply.authentication.presentation.signup

import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
class SignUpContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val intents = mutableListOf<SignUpScreenIntent>()

    private fun setSignUpContent(
        state: SignUpScreenState = SignUpScreenState(),
        isExpandedScreen: Boolean = false,
    ) {
        composeRule.setContent {
            ShoplyTheme {
                SignUpContent(
                    isExpandedScreen = isExpandedScreen,
                    state = state,
                    onEvent = { intents += it },
                    // Plain stand-ins for the Hilt/SDK-backed social buttons.
                    socialButtons = { onFacebookClick, onGoogleClick ->
                        Button(onClick = onFacebookClick) { Text(FACEBOOK) }
                        Button(onClick = onGoogleClick) { Text(GOOGLE) }
                    },
                )
            }
        }
    }

    private fun string(id: Int) = composeRule.activity.getString(id)

    @Test
    fun `the social buttons send their sign-up intents`() {
        setSignUpContent()

        composeRule.onNodeWithText(FACEBOOK).performScrollTo().performClick()
        composeRule.onNodeWithText(GOOGLE).performScrollTo().performClick()

        assertThat(intents).containsExactly(
            SignUpScreenIntent.SignUp.Facebook,
            SignUpScreenIntent.SignUp.Google,
        ).inOrder()
    }

    @Test
    fun `the expanded layout has no social buttons`() {
        setSignUpContent(isExpandedScreen = true)

        // Same finder that matches in the compact layout above.
        composeRule.onNodeWithText(FACEBOOK).assertDoesNotExist()
    }

    @Test
    fun `typing a username sends a change intent`() {
        setSignUpContent()

        composeRule.onNodeWithText(string(R.string.username)).performTextInput("m")

        assertThat(intents).containsExactly(SignUpScreenIntent.Change.Username("m"))
    }

    @Test
    fun `the clear buttons send their clear intents`() {
        setSignUpContent()

        composeRule.onNodeWithContentDescription(string(R.string.clear_username)).performClick()
        composeRule.onNodeWithContentDescription(string(R.string.clear_email)).performClick()
        composeRule.onNodeWithContentDescription(string(R.string.clear_phone_number))
            .performScrollTo().performClick()

        assertThat(intents).containsExactly(
            SignUpScreenIntent.Clear.Username,
            SignUpScreenIntent.Clear.Email,
            SignUpScreenIntent.Clear.PhoneNumber,
        ).inOrder()
    }

    @Test
    fun `tapping sign up submits the form`() {
        setSignUpContent(SignUpScreenState(enableSignUp = true))

        composeRule.onNodeWithText(string(R.string.sign_up)).performScrollTo().performClick()

        assertThat(intents).containsExactly(SignUpScreenIntent.SignUp.SubmitForm)
    }

    @Test
    fun `shows the sign-up error`() {
        setSignUpContent(SignUpScreenState(signUpError = UiText.StringResource(R.string.unknown_error)))

        composeRule.onNodeWithText(string(R.string.unknown_error)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `tapping sign in sends the sign-in intent`() {
        setSignUpContent()

        composeRule.onNodeWithText(string(R.string.sign_in)).performScrollTo().performClick()

        assertThat(intents).containsExactly(SignUpScreenIntent.SignIn)
    }

    private companion object {
        const val FACEBOOK = "Fake Facebook"
        const val GOOGLE = "Fake Google"
    }
}
