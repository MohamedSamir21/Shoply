package com.shopping.shoply.authentication.presentation.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp")
// The hero panel clips to a shape with only its bottom corners rounded; hit-testing that needs
// real Path support, which Robolectric's default (legacy) graphics stubs out.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OnboardingContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val swipedPages = mutableListOf<Int>()
    private var skipCount = 0
    private var finishCount = 0

    private fun setOnboardingContent() {
        composeRule.setContent {
            ShoplyTheme {
                // Plays the ViewModel: page changes from swipes and buttons feed back into state.
                var state by remember {
                    mutableStateOf(OnboardingScreenState(onboardings = generateOnboardings()))
                }

                OnboardingContent(
                    state = state,
                    onSwipe = {
                        swipedPages += it
                        state = state.copy(currentPageIndex = it)
                    },
                    onForward = {
                        state = state.copy(
                            currentPageIndex = (state.currentPageIndex + 1)
                                .coerceAtMost(state.onboardings.lastIndex)
                        )
                    },
                    onPrevious = {
                        state = state.copy(
                            currentPageIndex = (state.currentPageIndex - 1).coerceAtLeast(0)
                        )
                    },
                    onSkip = { skipCount++ },
                    onFinish = { finishCount++ },
                )
            }
        }
    }

    private fun string(id: Int) = composeRule.activity.getString(id)

    @Test
    fun `swiping left settles on the second page and reports it`() {
        setOnboardingContent()

        composeRule.onNode(hasScrollAction()).performTouchInput { swipeLeft() }

        composeRule.onNodeWithText(string(R.string.get_it_delivered)).assertIsDisplayed()
        // The settled-page effect also fires once for the initial page.
        assertThat(swipedPages).containsExactly(0, 1).inOrder()
    }

    @Test
    fun `the first page offers skip but not previous`() {
        setOnboardingContent()

        composeRule.onNodeWithText(string(R.string.previous)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.skip)).performClick()

        assertThat(skipCount).isEqualTo(1)
    }

    @Test
    fun `tapping next moves to the next page`() {
        setOnboardingContent()

        composeRule.onNodeWithContentDescription(string(R.string.next)).performClick()

        composeRule.onNodeWithText(string(R.string.get_it_delivered)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.previous)).assertIsDisplayed()
    }

    @Test
    fun `the last page swaps next for get started and drops skip`() {
        setOnboardingContent()

        composeRule.onNodeWithContentDescription(string(R.string.next)).performClick()
        composeRule.onNodeWithContentDescription(string(R.string.next)).performClick()

        composeRule.onNodeWithText(string(R.string.flexible_payment)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.skip)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(string(R.string.get_started)).performClick()
        assertThat(finishCount).isEqualTo(1)
    }
}
