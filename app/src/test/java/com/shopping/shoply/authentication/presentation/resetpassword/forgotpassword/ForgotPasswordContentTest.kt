package com.shopping.shoply.authentication.presentation.resetpassword.forgotpassword

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.METHOD_CARD_TAG
import com.shopping.shoply.authentication.presentation.utility.getResetMethods
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp")
class ForgotPasswordContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var checkedCount = 0
    private var nextCount = 0
    private var backCount = 0

    private fun setForgotPasswordContent(selectedIndex: Int = 0) {
        composeRule.setContent {
            ShoplyTheme {
                ForgotPasswordContent(
                    isExpandedScreen = false,
                    resetMethods = getResetMethods(),
                    selectedResetMethodIndex = selectedIndex,
                    onResetMethodChecked = { checkedCount++ },
                    onNext = { nextCount++ },
                    onBack = { backCount++ },
                )
            }
        }
    }

    private fun string(id: Int) = composeRule.activity.getString(id)
    private fun cards() = composeRule.onAllNodesWithTag(METHOD_CARD_TAG)

    @Test
    fun `only the card at the selected index is selected`() {
        setForgotPasswordContent(selectedIndex = 1)

        cards()[0].assertIsNotSelected()
        cards()[1].assertIsSelected()
    }

    @Test
    fun `tapping a reset method reports the check`() {
        setForgotPasswordContent()

        cards()[1].performClick()

        assertThat(checkedCount).isEqualTo(1)
    }

    @Test
    fun `next and back hand off to their callbacks`() {
        setForgotPasswordContent()

        composeRule.onNodeWithText(string(R.string.next)).performClick()
        composeRule.onNodeWithContentDescription(string(R.string.back)).performClick()

        assertThat(nextCount).isEqualTo(1)
        assertThat(backCount).isEqualTo(1)
    }
}
