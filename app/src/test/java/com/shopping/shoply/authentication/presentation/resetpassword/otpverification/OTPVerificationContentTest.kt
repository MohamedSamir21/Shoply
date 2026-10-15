package com.shopping.shoply.authentication.presentation.resetpassword.otpverification

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.OTP_INPUT_TAG
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.util.maskPhoneNumberKeepingLast
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp")
class OTPVerificationContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val enteredCodes = mutableListOf<String>()
    private var confirmCount = 0
    private var skipCount = 0

    private fun setOtpContent(
        state: OTPVerificationState = OTPVerificationState(countryCode = "+1", phoneNumber = "5551234590"),
        showSkip: Boolean = false,
    ) {
        composeRule.setContent {
            ShoplyTheme {
                OTPVerificationContent(
                    expandedScreen = false,
                    state = state,
                    showSkip = showSkip,
                    onBack = {},
                    onCodeChange = { enteredCodes += it },
                    onResendCodeAction = {},
                    onConfirm = { confirmCount++ },
                    onSkip = { skipCount++ },
                )
            }
        }
    }

    private fun string(id: Int) = composeRule.activity.getString(id)

    @Test
    fun `shows the masked phone number the code was sent to`() {
        setOtpContent()
        val masked = maskPhoneNumberKeepingLast(countryCode = "+1", phoneNumber = "5551234590")

        // The guide and the number are one AnnotatedString, so one text node.
        composeRule.onNodeWithText(masked, substring = true).assertIsDisplayed()
    }

    @Test
    fun `typing the code forwards it`() {
        setOtpContent()

        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("1")

        assertThat(enteredCodes).containsExactly("1")
    }

    @Test
    fun `tapping confirm hands off to its callback`() {
        setOtpContent()

        composeRule.onNodeWithText(string(R.string.confirm)).performClick()

        assertThat(confirmCount).isEqualTo(1)
    }

    @Test
    fun `a loading dialog replaces confirm while verifying`() {
        // The loading logo spins forever.
        composeRule.mainClock.autoAdvance = false
        setOtpContent(OTPVerificationState(isLoading = true))

        composeRule.onNode(isDialog()).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.confirm)).assertDoesNotExist()
    }

    @Test
    fun `skip is hidden unless requested`() {
        setOtpContent(showSkip = false)

        composeRule.onNodeWithText(string(R.string.skip_for_now)).assertDoesNotExist()
    }

    @Test
    fun `skip hands off to its callback when shown`() {
        setOtpContent(showSkip = true)

        composeRule.onNodeWithText(string(R.string.skip_for_now)).performClick()

        assertThat(skipCount).isEqualTo(1)
    }
}
