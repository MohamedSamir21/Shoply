package com.shopping.shoply.authentication.presentation.signin

import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
class SignInContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val intents = mutableListOf<SignInScreenIntent>()
    private var forgotPasswordCount = 0
    private var signUpCount = 0

    private fun setSignInContent(state: SignInScreenState = SignInScreenState()) {
        composeRule.setContent {
            ShoplyTheme {
                SignInContent(
                    state = state,
                    isExpandedScreen = false,
                    onChange = { intents += it },
                    onForgotPasswordClicked = { forgotPasswordCount++ },
                    onSignUpClicked = { signUpCount++ },
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
    fun `the social buttons send their sign-in intents`() {
        setSignInContent()

        composeRule.onNodeWithText(FACEBOOK).performClick()
        composeRule.onNodeWithText(GOOGLE).performClick()

        assertThat(intents).containsExactly(
            SignInScreenIntent.ContinueWithFacebook,
            SignInScreenIntent.ContinueWithGoogle,
        ).inOrder()
    }

    @Test
    fun `typing an email sends a change intent`() {
        setSignInContent()

        composeRule.onNodeWithText(string(R.string.email)).performTextInput("a")

        assertThat(intents).containsExactly(SignInScreenIntent.Change.Email("a"))
    }

    @Test
    fun `tapping sign in sends the sign-in intent when enabled`() {
        setSignInContent(SignInScreenState(enableSignIn = true))

        composeRule.onNodeWithText(string(R.string.sign_in)).performScrollTo().performClick()

        assertThat(intents).containsExactly(SignInScreenIntent.SignIn)
    }

    @Test
    fun `sign in is disabled until the form allows it`() {
        setSignInContent(SignInScreenState(enableSignIn = false))

        composeRule.onNodeWithText(string(R.string.sign_in)).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun `a progress indicator replaces sign in while signing in`() {
        // The indeterminate spinner never goes idle.
        composeRule.mainClock.autoAdvance = false
        setSignInContent(SignInScreenState(isSigningIn = true))

        composeRule.onNodeWithText(string(R.string.sign_in)).assertDoesNotExist()
    }

    @Test
    fun `shows the sign-in error`() {
        setSignInContent(SignInScreenState(signInError = UiText.StringResource(R.string.unknown_error)))

        composeRule.onNodeWithText(string(R.string.unknown_error)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `forgot password and register hand off to their callbacks`() {
        setSignInContent()

        composeRule.onNodeWithText(string(R.string.forgot_password) + "?").performScrollTo().performClick()
        composeRule.onNodeWithText(string(R.string.register)).performScrollTo().performClick()

        assertThat(forgotPasswordCount).isEqualTo(1)
        assertThat(signUpCount).isEqualTo(1)
    }

    private companion object {
        const val FACEBOOK = "Fake Facebook"
        const val GOOGLE = "Fake Google"
    }
}
