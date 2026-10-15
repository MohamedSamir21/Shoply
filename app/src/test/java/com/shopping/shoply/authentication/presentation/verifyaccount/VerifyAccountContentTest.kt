package com.shopping.shoply.authentication.presentation.verifyaccount

import androidx.activity.ComponentActivity
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
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp")
class VerifyAccountContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var resendCount = 0
    private var forwardCount = 0

    private fun setVerifyAccountContent(countDownFinished: Boolean = false) {
        // The pulsing mail badge never goes idle.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            ShoplyTheme {
                VerifyAccountContent(
                    email = "username@gmail.com",
                    count = "00:40",
                    countDownFinished = countDownFinished,
                    onResend = { resendCount++ },
                    onForwardAction = { forwardCount++ },
                )
            }
        }
    }

    private fun string(id: Int) = composeRule.activity.getString(id)

    @Test
    fun `has no back button`() {
        setVerifyAccountContent()

        // Same finder that matches on SendResetLink, whose top bar does have one.
        composeRule.onNodeWithContentDescription(string(R.string.back)).assertDoesNotExist()
    }

    @Test
    fun `continue to home hands off to its callback`() {
        setVerifyAccountContent()

        composeRule.onNodeWithText(string(R.string.continue_to_home)).performClick()

        assertThat(forwardCount).isEqualTo(1)
    }

    @Test
    fun `resend works once the countdown finishes`() {
        setVerifyAccountContent(countDownFinished = true)

        composeRule.onNodeWithText(string(R.string.resend)).performClick()

        assertThat(resendCount).isEqualTo(1)
    }
}
