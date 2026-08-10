package com.shopping.shoply.authentication.presentation.components

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shopping.shoply.authentication.domain.model.PasswordStrength
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// One test per strength tier instead of a parameterized test: this runner slot is taken by
// AndroidJUnit4 (JUnit4 allows exactly one runner, and Robolectric needs it), and setContent
// may only be called once per test — so neither @ParameterizedTest nor an in-test loop fits.
@RunWith(AndroidJUnit4::class)
class PasswordStrengthMeterTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setMeter(strength: PasswordStrength) {
        composeRule.setContent {
            ShoplyTheme {
                PasswordStrengthMeter(strength = strength)
            }
        }
    }

    private fun ComposeContentTestRule.assertSegments(filled: Int) {
        onAllNodesWithTag(STRENGTH_SEGMENT_FILLED_TAG).assertCountEquals(filled)
        onAllNodesWithTag(STRENGTH_SEGMENT_EMPTY_TAG).assertCountEquals(4 - filled)
    }

    @Test
    fun `NONE composes no meter at all`() {
        setMeter(PasswordStrength.NONE)

        // AnimatedVisibility(visible = false): nothing enters the tree — no segments, no label.
        composeRule.onAllNodesWithTag(STRENGTH_SEGMENT_FILLED_TAG).assertCountEquals(0)
        composeRule.onAllNodesWithTag(STRENGTH_SEGMENT_EMPTY_TAG).assertCountEquals(0)
        composeRule.onNodeWithText("Weak").assertDoesNotExist()
    }

    @Test
    fun `WEAK fills one segment and labels it Weak`() {
        setMeter(PasswordStrength.WEAK)

        composeRule.assertSegments(filled = 1)
        composeRule.onNodeWithText("Weak").assertIsDisplayed()
    }

    @Test
    fun `FAIR fills two segments and labels it Fair`() {
        setMeter(PasswordStrength.FAIR)

        composeRule.assertSegments(filled = 2)
        composeRule.onNodeWithText("Fair").assertIsDisplayed()
    }

    @Test
    fun `GOOD fills three segments and labels it Good`() {
        setMeter(PasswordStrength.GOOD)

        composeRule.assertSegments(filled = 3)
        composeRule.onNodeWithText("Good").assertIsDisplayed()
    }

    @Test
    fun `STRONG fills all four segments and labels it Strong`() {
        setMeter(PasswordStrength.STRONG)

        composeRule.assertSegments(filled = 4)
        composeRule.onNodeWithText("Strong").assertIsDisplayed()
    }
}
