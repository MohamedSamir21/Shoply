package com.shopping.shoply.authentication.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// JUnit4-style on purpose: createComposeRule() is a JUnit4 Rule and Robolectric (which runs
// this on the JVM) is JUnit4-only — the vintage engine runs it beside the Jupiter suites.
// See docs/testing-concepts.md § "Compose UI tests on the JVM".
@RunWith(AndroidJUnit4::class)
class SendAgainComponentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setSendAgain(
        countDownFinished: Boolean,
        showResendButtonWhileCountingDown: Boolean = true,
        onResendAction: () -> Unit = {}
    ) {
        composeRule.setContent {
            ShoplyTheme {
                SendAgainComponent(
                    sendText = "send verification link again",
                    count = "00:55",
                    countDownFinished = countDownFinished,
                    onResendAction = onResendAction,
                    resendLabel = "Resend",
                    showResendButtonWhileCountingDown = showResendButtonWhileCountingDown
                )
            }
        }
    }

    // --- default mode: resend label always composed, click-gated by the countdown ---

    @Test
    fun `shows the countdown appended to the send text while counting`() {
        setSendAgain(countDownFinished = false)

        // buildAnnotatedString renders as ONE node: send text + styled count, single string.
        composeRule.onNodeWithText("send verification link again (00:55)").assertIsDisplayed()
    }

    @Test
    fun `keeps the resend label visible but inert while counting`() {
        var clicks = 0
        setSendAgain(countDownFinished = false, onResendAction = { clicks++ })

        val resend = composeRule.onNodeWithText("Resend")
        resend.assertIsDisplayed()
        resend.assertIsNotEnabled() // clickable(enabled = false) keeps the node, disabled

        // The gesture lands but the disabled clickable must swallow it.
        resend.performClick()
        assertThat(clicks).isEqualTo(0)
    }

    @Test
    fun `drops the countdown from the send text once finished`() {
        setSendAgain(countDownFinished = true)

        composeRule.onNodeWithText("send verification link again").assertIsDisplayed()
        composeRule.onNodeWithText("send verification link again (00:55)").assertDoesNotExist()
    }

    @Test
    fun `fires the resend action once the countdown finishes`() {
        var clicks = 0
        setSendAgain(countDownFinished = true, onResendAction = { clicks++ })

        composeRule.onNodeWithText("Resend").performClick()

        assertThat(clicks).isEqualTo(1)
    }

    // --- OTP mode (showResendButtonWhileCountingDown = false): label and countdown swap ---

    @Test
    fun `hides the resend label entirely while counting in OTP mode`() {
        setSendAgain(countDownFinished = false, showResendButtonWhileCountingDown = false)

        // Not merely disabled — the branch never composes it. Different tree, same look.
        composeRule.onNodeWithText("Resend").assertDoesNotExist()
        composeRule.onNodeWithText("send verification link again (00:55)").assertIsDisplayed()
    }

    @Test
    fun `swaps the countdown for the resend label once finished in OTP mode`() {
        var clicks = 0
        setSendAgain(
            countDownFinished = true,
            showResendButtonWhileCountingDown = false,
            onResendAction = { clicks++ }
        )

        composeRule.onNodeWithText("send verification link again").assertDoesNotExist()
        composeRule.onNodeWithText("Resend").performClick()
        assertThat(clicks).isEqualTo(1)
    }
}
