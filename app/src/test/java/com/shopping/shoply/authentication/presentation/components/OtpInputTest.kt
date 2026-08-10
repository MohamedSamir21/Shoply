package com.shopping.shoply.authentication.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OtpInputTest {

    @get:Rule
    val composeRule = createComposeRule()

    // OtpInput is stateless: value in, onValueChange out. In production the ViewModel closes
    // that loop; here the test has to close it, or typing would never change what's rendered.
    // `emitted` records every value the component let through its digit/length filter.
    private val emitted = mutableListOf<String>()

    private fun setOtp(length: Int = 4, initialValue: String = "") {
        composeRule.setContent {
            ShoplyTheme {
                var value by remember { mutableStateOf(initialValue) }
                OtpInput(
                    value = value,
                    onValueChange = {
                        emitted += it
                        value = it // close the loop, exactly as the ViewModel does
                    },
                    length = length
                )
            }
        }
    }

    @Test
    fun `forwards each typed digit through onValueChange`() {
        setOtp()

        // performTextInput types into the node as the IME would — one call per "keystroke"
        // here, so the component's filter is exercised on every intermediate value.
        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("1")
        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("2")

        assertThat(emitted).containsExactly("1", "12").inOrder()
    }

    @Test
    fun `eliminates each typed letter from going through onValueChange`() {
        setOtp()

        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("a")

        assertThat(emitted).isEmpty()
    }

    @Test
    fun `eliminates each typed letter while forwarding each typed digit through onValueChange`() {
        setOtp()

        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("1")
        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("a")
        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("2")

        assertThat(emitted).containsExactly("1", "12").inOrder()
    }

    @Test
    fun `eliminates each extra typed digit from going through onValueChange when the length is exceeded`() {
        setOtp()

        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("1")
        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("2")
        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("3")
        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("4")
        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("5")

        assertThat(emitted).containsExactly("1", "12", "123", "1234").inOrder()
    }

    @Test
    fun `ensures the number of otp cells equals the length`() {
        setOtp()

        // BasicTextField merges its descendants, so the decorationBox's cells are absorbed
        // into the field's own node — they only exist as separate nodes in the unmerged tree.
        composeRule.onAllNodesWithTag(OTP_CELL_TAG, useUnmergedTree = true)
            .assertCountEquals(4)
    }

    @Test
    fun `ensures filled cells render their digits`() {
        setOtp()

        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("1")
        composeRule.onNodeWithTag(OTP_INPUT_TAG).performTextInput("2")

        // assertIsDisplayed(), not isDisplayed(): the latter RETURNS a Boolean that nothing
        // checks, so the test could never fail. Unmerged again, to assert on the cells
        // themselves rather than on the field's merged text.
        composeRule.onNodeWithText("1", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("2", useUnmergedTree = true).assertIsDisplayed()
    }
}
