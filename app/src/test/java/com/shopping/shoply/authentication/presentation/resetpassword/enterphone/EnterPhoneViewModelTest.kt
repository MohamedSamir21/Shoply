package com.shopping.shoply.authentication.presentation.resetpassword.enterphone

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.CountryInfo
import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.authentication.domain.usecase.auth.CheckIfPhoneNumberExistsUseCase
import com.shopping.shoply.authentication.domain.usecase.countryinfo.GetCountryInfoUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidatePhoneUseCase
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("EnterPhoneViewModel")
class EnterPhoneViewModelTest {
    private val validatePhoneUseCase = mockk<ValidatePhoneUseCase>()
    private val checkIfPhoneNumberExistsUseCase = mockk<CheckIfPhoneNumberExistsUseCase>()
    private val getCountryInfoUseCase = mockk<GetCountryInfoUseCase>()
    private val sut = EnterPhoneViewModel(
        validatePhoneUseCase = validatePhoneUseCase,
        checkIfPhoneNumberExistsUseCase = checkIfPhoneNumberExistsUseCase,
        getCountryInfoUseCase = getCountryInfoUseCase
    )

    private val egypt =
        CountryInfo(codeNumber = "+20", name = "Egypt", flag = "🇪🇬", format = "100 123 4567")
    private val usa =
        CountryInfo(codeNumber = "+1", name = "United States", flag = "🇺🇸", format = "201 555 0123")

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    @Nested
    @DisplayName("loadCountriesInfo")
    inner class LoadCountriesInfo {

        @Test
        @DisplayName("populates the country list and selects the first one")
        fun `populates the country list and selects the first one`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { getCountryInfoUseCase() } returns listOf(egypt, usa)

                sut.loadCountriesInfo()

                val state = sut.state.value
                assertThat(state.countriesInfo).containsExactly(egypt, usa)
                assertThat(state.countriesInfoByKeyword).containsExactly(egypt, usa)
                assertThat(state.selectedCountryInfo).isEqualTo(egypt)
                assertThat(state.isCountryCodeSearchButtonClickable).isTrue()
            }

        @Test
        @DisplayName("disables the search button when only one country is returned")
        fun `disables the search button when only one country is returned`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { getCountryInfoUseCase() } returns listOf(egypt)

                sut.loadCountriesInfo()

                assertThat(sut.state.value.isCountryCodeSearchButtonClickable).isFalse()
            }
    }

    @Test
    @DisplayName("onChangePhone updates the phone number")
    fun `onChangePhone updates the phone number`() = runTest(mainDispatcherExtension.dispatcher) {
        sut.onChangePhone("1001234567")

        assertThat(sut.state.value.phoneNumber).isEqualTo("1001234567")
    }

    @Nested
    @DisplayName("onIntent")
    inner class OnIntent {

        @Test
        @DisplayName("ToggleCountryInfoMenuVisibility flips the menu visibility")
        fun `ToggleCountryInfoMenuVisibility flips the menu visibility`() =
            runTest(mainDispatcherExtension.dispatcher) {
                sut.onIntent(EnterPhoneIntent.ToggleCountryInfoMenuVisibility)
                assertThat(sut.state.value.showCountryInfoMenu).isTrue()

                sut.onIntent(EnterPhoneIntent.ToggleCountryInfoMenuVisibility)
                assertThat(sut.state.value.showCountryInfoMenu).isFalse()
            }

        @Test
        @DisplayName("SelectCountryInfo selects the country and closes the menu")
        fun `SelectCountryInfo selects the country and closes the menu`() =
            runTest(mainDispatcherExtension.dispatcher) {
                sut.onIntent(EnterPhoneIntent.ToggleCountryInfoMenuVisibility)

                sut.onIntent(EnterPhoneIntent.SelectCountryInfo(usa))

                assertThat(sut.state.value.selectedCountryInfo).isEqualTo(usa)
                assertThat(sut.state.value.showCountryInfoMenu).isFalse()
            }

        // ChangeSearchKeyword title-cases the keyword, then filters the already-loaded
        // countriesInfo by startsWith — new logic (not seen in SignIn/EnterEmail), so it
        // gets its own coverage rather than being folded into the repetitive setter tests.
        @Nested
        @DisplayName("ChangeSearchKeyword")
        inner class ChangeSearchKeyword {

            @Test
            @DisplayName("filters case-insensitively to the matching country")
            fun `filters case-insensitively to the matching country`() =
                runTest(mainDispatcherExtension.dispatcher) {
                    coEvery { getCountryInfoUseCase() } returns listOf(egypt, usa)
                    sut.loadCountriesInfo()

                    sut.onIntent(EnterPhoneIntent.ChangeSearchKeyword("egy"))

                    assertThat(sut.state.value.countriesInfoByKeyword).containsExactly(egypt)
                }

            @Test
            @DisplayName("returns an empty list when nothing matches")
            fun `returns an empty list when nothing matches`() =
                runTest(mainDispatcherExtension.dispatcher) {
                    coEvery { getCountryInfoUseCase() } returns listOf(egypt, usa)
                    sut.loadCountriesInfo()

                    sut.onIntent(EnterPhoneIntent.ChangeSearchKeyword("zzz"))

                    assertThat(sut.state.value.countriesInfoByKeyword).isEmpty()
                }

            @Test
            @DisplayName("restores the full list once the keyword is cleared")
            fun `restores the full list once the keyword is cleared`() =
                runTest(mainDispatcherExtension.dispatcher) {
                    coEvery { getCountryInfoUseCase() } returns listOf(egypt, usa)
                    sut.loadCountriesInfo()
                    sut.onIntent(EnterPhoneIntent.ChangeSearchKeyword("egy"))

                    sut.onIntent(EnterPhoneIntent.ChangeSearchKeyword(""))

                    assertThat(sut.state.value.countriesInfoByKeyword).containsExactly(egypt, usa)
                }
        }
    }

    @Nested
    @DisplayName("onNext")
    inner class OnNext {

        // onNext builds the E.164 number from selectedCountryInfo.codeNumber + phoneNumber
        // before validating/looking it up, so the mocks must be stubbed against that
        // constructed value, not the raw phone digits the user typed.
        private val phoneNumber = "1001234567"
        private val e164 = "+201001234567"

        private fun enterPhoneNumber() {
            sut.onIntent(EnterPhoneIntent.SelectCountryInfo(egypt))
            sut.onChangePhone(phoneNumber)
        }

        @Test
        @DisplayName("reports that the phone number isn't valid")
        fun `reports that the phone number isn't valid`() =
            runTest(mainDispatcherExtension.dispatcher) {
                enterPhoneNumber()
                every { validatePhoneUseCase(e164) } returns Result.Error(ValidationError.INVALID_PHONE_NUMBER)

                sut.onNext()

                val actualUiText = sut.state.value.error as UiText.StringResource
                assertThat(actualUiText.id).isEqualTo(R.string.enter_valid_phone_number)
                coVerify(exactly = 0) { checkIfPhoneNumberExistsUseCase(any()) }
            }

        @Test
        @DisplayName("reports an error when checking if the phone number exists fails")
        fun `reports an error when checking if the phone number exists fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                enterPhoneNumber()
                every { validatePhoneUseCase(e164) } returns Result.Success(Unit)
                coEvery { checkIfPhoneNumberExistsUseCase(e164) } returns Result.Error(DataError.Unknown)

                sut.onNext()

                val actualUiText = sut.state.value.error as UiText.StringResource
                assertThat(actualUiText.id).isEqualTo(R.string.unknown_error)
                assertThat(sut.state.value.isProcessing).isFalse()
            }

        @Test
        @DisplayName("reports success once the phone number is verified")
        fun `reports success once the phone number is verified`() =
            runTest(mainDispatcherExtension.dispatcher) {
                enterPhoneNumber()
                every { validatePhoneUseCase(e164) } returns Result.Success(Unit)
                coEvery { checkIfPhoneNumberExistsUseCase(e164) } returns Result.Success(Unit)

                sut.onNext()

                sut.phoneVerificationResult.test {
                    assertThat(awaitItem()).isEqualTo(
                        EnterPhoneEvent.Success(countryCode = "+20", phoneNumber = "201001234567")
                    )
                }
                assertThat(sut.state.value.isProcessing).isFalse()
                assertThat(sut.state.value.error).isNull()
            }
    }
}
