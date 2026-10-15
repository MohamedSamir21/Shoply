package com.shopping.shoply.authentication.presentation.resetpassword.sendresetlink

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
import com.shopping.shoply.common.presentation.util.hidePartOfEmailWithStars
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp")
class SendResetLinkContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var backCount = 0
    private var resendCount = 0
    private var forwardCount = 0

    private fun setSendResetLinkContent(countDownFinished: Boolean = false) {
        composeRule.setContent {
            ShoplyTheme {
                SendResetLinkContent(
                    email = EMAIL,
                    count = "00:45",
                    countDownFinished = countDownFinished,
                    onBack = { backCount++ },
                    onResend = { resendCount++ },
                    onForwardAction = { forwardCount++ },
                )
            }
        }
    }

    private fun string(id: Int) = composeRule.activity.getString(id)

    @Test
    fun `shows the masked email the link was sent to`() {
        setSendResetLinkContent()

        composeRule.onNodeWithText(hidePartOfEmailWithStars(EMAIL)).assertIsDisplayed()
    }

    @Test
    fun `back and the forward button hand off to their callbacks`() {
        setSendResetLinkContent()

        composeRule.onNodeWithContentDescription(string(R.string.back)).performClick()
        composeRule.onNodeWithText(string(R.string.ok_i_will_see)).performClick()

        assertThat(backCount).isEqualTo(1)
        assertThat(forwardCount).isEqualTo(1)
    }

    @Test
    fun `resend works once the countdown finishes`() {
        setSendResetLinkContent(countDownFinished = true)

        composeRule.onNodeWithText(string(R.string.resend)).performClick()

        assertThat(resendCount).isEqualTo(1)
    }

    private companion object {
        const val EMAIL = "username@gmail.com"
    }
}
