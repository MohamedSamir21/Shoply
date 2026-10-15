package com.shopping.shoply.authentication.presentation.resetpassword.enterphone

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToKeyAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.CountryInfo
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnterPhoneContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    // Far more rows than fit on the Robolectric screen, so the picker's LazyColumn cannot compose
    // them all at once.
    private val countries = (1..40).map { i ->
        CountryInfo(codeNumber = "+$i", name = "Country $i", flag = "🏳", format = "")
    }
    private val emittedKeywords = mutableListOf<String>()
    private val selectedCountries = mutableListOf<CountryInfo>()

    private fun setEnterPhoneContent(
        initialShowMenu: Boolean = false,
        isCountryCodeSearchButtonClickable: Boolean = true,
        errorMessage: UiText? = null,
    ) {
        composeRule.setContent {
            ShoplyTheme {
                // The content is stateless; the test plays the ViewModel and closes the loop.
                var showMenu by remember { mutableStateOf(initialShowMenu) }
                var selected by remember { mutableStateOf(countries.first()) }
                var keyword by remember { mutableStateOf("") }

                EnterPhoneContent(
                    isExpandedScreen = false,
                    methodValue = "",
                    isVerifyingValue = false,
                    isCountryCodeSearchButtonClickable = isCountryCodeSearchButtonClickable,
                    selectedCountryInfo = selected,
                    countriesInfo = countries,
                    keyword = keyword,
                    showCountryInfoMenu = showMenu,
                    onKeywordChange = { newKeyword ->
                        keyword = newKeyword
                        emittedKeywords.add(newKeyword)
                    },
                    onPhoneNumberChange = {},
                    onToggleCountryMenu = { showMenu = !showMenu },
                    onSelectCountry = {
                        selectedCountries += it
                        selected = it
                        showMenu = false
                    },
                    onBack = {},
                    onNext = {},
                    errorMessage = errorMessage
                )
            }
        }
    }

    private fun downArrowText() = composeRule.activity.getString(R.string.down_arrow)
    private fun cancelText() = composeRule.activity.getString(R.string.cancel)
    private fun standardRatesText() = composeRule.activity.getString(R.string.standard_rates_apply)
    private fun searchBox() = composeRule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog()))
    private fun unknownErrorMessage() = composeRule.activity.getString(R.string.unknown_error)

    @Test
    fun `tapping the country code opens the picker dialog`() {
        setEnterPhoneContent()
        // Absence first, then presence with the SAME finder — proves isDialog() is a real selector.
        composeRule.onNode(isDialog()).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(downArrowText()).performClick()

        composeRule.onNode(isDialog()).assertIsDisplayed()
    }

    @Test
    fun `a country below the fold is selectable only after scrolling to it`() {
        setEnterPhoneContent(initialShowMenu = true)
        // Never composed yet: the lazy list only builds the rows in the viewport.
        composeRule.onNodeWithText("Country 40").assertDoesNotExist()

        composeRule.onNode(hasScrollToKeyAction()).performScrollToKey("Country 40")
        composeRule.onNodeWithText("Country 40").performClick()

        assertThat(selectedCountries).containsExactly(countries.last())
        composeRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun `tapping cancel closes the picker dialog`() {
        setEnterPhoneContent(
            initialShowMenu = true
        )

        composeRule.onNode(isDialog()).assertIsDisplayed()
        composeRule.onNodeWithText(cancelText()).performClick()

        composeRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun `typing in the search box emits keywords`() {
        setEnterPhoneContent(initialShowMenu = true)

        composeRule.onNode(isDialog()).assertIsDisplayed()
        searchBox().performTextInput("e")
        searchBox().performTextInput("g")

        assertThat(emittedKeywords).containsExactly("e", "eg").inOrder()
    }

    @Test
    fun `tapping the country code doesn't open the dialog when it isn't clickable`() {
        setEnterPhoneContent(
            isCountryCodeSearchButtonClickable = false
        )

        composeRule.onNodeWithText(countries.first().codeNumber).performClick()

        composeRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun `shows the standard rates apply message when error message is null`() {
        setEnterPhoneContent()

        composeRule.onNodeWithText(standardRatesText()).assertIsDisplayed()
    }

    @Test
    fun `shows the error message when it's filled`() {
        setEnterPhoneContent(
            errorMessage = UiText.StringResource(R.string.unknown_error)
        )

        composeRule.onNodeWithText(unknownErrorMessage())
            .assertIsDisplayed()
    }
}
