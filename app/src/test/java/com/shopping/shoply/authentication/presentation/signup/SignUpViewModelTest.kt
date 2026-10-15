package com.shopping.shoply.authentication.presentation.signup

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.CountryInfo
import com.shopping.shoply.authentication.domain.model.PasswordStrength
import com.shopping.shoply.authentication.domain.model.SignUpData
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.authentication.domain.usecase.auth.CheckIfPhoneNumberExistsUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpWithFacebookUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpWithGoogleUseCase
import com.shopping.shoply.authentication.domain.usecase.countryinfo.GetCountryInfoUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.CheckInputsFilledUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.MeasurePasswordStrengthUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateConfirmedPasswordUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateEmailUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidatePasswordUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidatePhoneUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateUsernameUseCase
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("SignUpViewModel")
class SignUpViewModelTest {
    private val checkInputsFilledUseCase = mockk<CheckInputsFilledUseCase>()
    private val validateUsernameUseCase = mockk<ValidateUsernameUseCase>()
    private val validateEmailUseCase = mockk<ValidateEmailUseCase>()
    private val validatePhoneUseCase = mockk<ValidatePhoneUseCase>()
    private val validatePasswordUseCase = mockk<ValidatePasswordUseCase>()
    private val validateConfirmedPasswordUseCase = mockk<ValidateConfirmedPasswordUseCase>()
    private val signUpUseCase = mockk<SignUpUseCase>()
    private val signUpWithGoogleUseCase = mockk<SignUpWithGoogleUseCase>()
    private val signUpWithFacebookUseCase = mockk<SignUpWithFacebookUseCase>()
    private val getCountryInfoUseCase = mockk<GetCountryInfoUseCase>()
    private val checkIfPhoneNumberExistsUseCase = mockk<CheckIfPhoneNumberExistsUseCase>()
    private val measurePasswordStrengthUseCase = mockk<MeasurePasswordStrengthUseCase>()

    private val egypt =
        CountryInfo(codeNumber = "+20", name = "Egypt", flag = "🇪🇬", format = "100 123 4567")
    private val usa =
        CountryInfo(codeNumber = "+1", name = "United States", flag = "🇺🇸", format = "201 555 0123")

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    // SignUpViewModel's init{} eagerly calls loadCountriesInfo(), which touches viewModelScope
    // (i.e. Dispatchers.Main.immediate) right away — unlike SignIn/EnterEmail/EnterPhone. JUnit5
    // builds the test instance (running field initializers) BEFORE MainDispatcherExtension's
    // beforeEach() calls Dispatchers.setMain(), so a class-level `sut` field would try to resolve
    // Dispatchers.Main before it's been stubbed and blow up with "Main dispatcher had failed to
    // initialize" on every single test. Building it inside each test body sidesteps that.
    private fun createSut(countries: List<CountryInfo> = listOf(egypt, usa)): SignUpViewModel {
        coEvery { getCountryInfoUseCase() } returns countries
        return SignUpViewModel(
            checkInputsFilledUseCase = checkInputsFilledUseCase,
            validateUsernameUseCase = validateUsernameUseCase,
            validateEmailUseCase = validateEmailUseCase,
            validatePhoneUseCase = validatePhoneUseCase,
            validatePasswordUseCase = validatePasswordUseCase,
            validateConfirmedPasswordUseCase = validateConfirmedPasswordUseCase,
            signUpUseCase = signUpUseCase,
            signUpWithGoogleUseCase = signUpWithGoogleUseCase,
            signUpWithFacebookUseCase = signUpWithFacebookUseCase,
            getCountryInfoUseCase = getCountryInfoUseCase,
            checkIfPhoneNumberExistsUseCase = checkIfPhoneNumberExistsUseCase,
            measurePasswordStrengthUseCase = measurePasswordStrengthUseCase
        )
    }

    @Test
    @DisplayName("loads the countries and selects the first one on init")
    fun `loads the countries and selects the first one on init`() =
        runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut(listOf(egypt, usa))

            advanceUntilIdle()

            val state = sut.state.value
            assertThat(state.countriesInfo).containsExactly(egypt, usa)
            assertThat(state.selectedCountryInfo).isEqualTo(egypt)
            assertThat(state.isCountryCodeSearchButtonClickable).isTrue()
        }

    @Nested
    @DisplayName("ChangeIntent")
    inner class ChangeIntent {

        @Test
        @DisplayName("updates the username and recomputes enableSignUp")
        fun `updates the username and recomputes enableSignUp`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut()
                every { checkInputsFilledUseCase(any<SignUpData>()) } returns true

                sut.onIntent(SignUpScreenIntent.Change.Username("mohamed"))

                assertThat(sut.state.value.form.username).isEqualTo("mohamed")
                assertThat(sut.state.value.enableSignUp).isTrue()
            }

        @Test
        @DisplayName("updates the email")
        fun `updates the email`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            every { checkInputsFilledUseCase(any<SignUpData>()) } returns false

            sut.onIntent(SignUpScreenIntent.Change.Email("user@gmail.com"))

            assertThat(sut.state.value.form.email).isEqualTo("user@gmail.com")
        }

        @Test
        @DisplayName("updates the phone number")
        fun `updates the phone number`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            every { checkInputsFilledUseCase(any<SignUpData>()) } returns false

            sut.onIntent(SignUpScreenIntent.Change.PhoneNumber("1001234567"))

            assertThat(sut.state.value.form.phoneNumber).isEqualTo("1001234567")
        }

        @Test
        @DisplayName("updates the confirmed password")
        fun `updates the confirmed password`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            every { checkInputsFilledUseCase(any<SignUpData>()) } returns false

            sut.onIntent(SignUpScreenIntent.Change.ConfirmedPassword("Abc12345"))

            assertThat(sut.state.value.form.confirmedPassword).isEqualTo("Abc12345")
        }

        @Test
        @DisplayName("toggles the password visibility")
        fun `toggles the password visibility`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            every { checkInputsFilledUseCase(any<SignUpData>()) } returns false

            sut.onIntent(SignUpScreenIntent.Change.PasswordVisibility(true))

            assertThat(sut.state.value.showPassword).isFalse()
        }
    }

    // PasswordStrengthDebouncer debounces zxcvbn behind a 200ms delay and cancels any in-flight
    // measurement on the next keystroke. This is the new concept for this ViewModel: testing
    // debounce + cancellation with the test dispatcher's virtual clock. advanceTimeBy(ms) moves
    // the clock forward by an exact amount (simulating "the user paused for exactly this long"),
    // as opposed to advanceUntilIdle() which just runs everything to completion.
    @Nested
    @DisplayName("Password strength debounce")
    inner class PasswordStrengthDebounce {

        @Test
        @DisplayName("measures the strength once the 200ms debounce elapses")
        fun `measures the strength once the 200ms debounce elapses`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut()
                every { checkInputsFilledUseCase(any<SignUpData>()) } returns false
                coEvery { measurePasswordStrengthUseCase("Abc12345") } returns PasswordStrength.GOOD

                sut.onIntent(SignUpScreenIntent.Change.Password("Abc12345"))
                // Not measured yet: the 200ms debounce hasn't elapsed.
                assertThat(sut.state.value.passwordStrength).isEqualTo(PasswordStrength.NONE)

                advanceTimeBy(200)
                advanceUntilIdle()

                assertThat(sut.state.value.passwordStrength).isEqualTo(PasswordStrength.GOOD)
            }

        @Test
        @DisplayName("cancels the previous measurement when the password changes again before 200ms")
        fun `cancels the previous measurement when the password changes again before 200ms`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut()
                every { checkInputsFilledUseCase(any<SignUpData>()) } returns false
                coEvery { measurePasswordStrengthUseCase("Ab") } returns PasswordStrength.WEAK
                coEvery { measurePasswordStrengthUseCase("Abc12345") } returns PasswordStrength.GOOD

                sut.onIntent(SignUpScreenIntent.Change.Password("Ab"))
                advanceTimeBy(100) // halfway through the first debounce window
                sut.onIntent(SignUpScreenIntent.Change.Password("Abc12345")) // cancels the first job

                advanceUntilIdle()

                assertThat(sut.state.value.passwordStrength).isEqualTo(PasswordStrength.GOOD)
                coVerify(exactly = 0) { measurePasswordStrengthUseCase("Ab") }
            }

        @Test
        @DisplayName("resets the strength to NONE immediately when the password is cleared")
        fun `resets the strength to NONE immediately when the password is cleared`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut()
                every { checkInputsFilledUseCase(any<SignUpData>()) } returns false
                coEvery { measurePasswordStrengthUseCase("Abc12345") } returns PasswordStrength.GOOD

                sut.onIntent(SignUpScreenIntent.Change.Password("Abc12345"))
                advanceTimeBy(100) // still mid-debounce — nothing measured yet

                sut.onIntent(SignUpScreenIntent.Clear.Password)

                assertThat(sut.state.value.passwordStrength).isEqualTo(PasswordStrength.NONE)
                assertThat(sut.state.value.form.password).isEmpty()
                advanceUntilIdle()
                coVerify(exactly = 0) { measurePasswordStrengthUseCase("Abc12345") }
            }
    }

    @Nested
    @DisplayName("ClearIntent")
    inner class ClearIntent {

        @Test
        @DisplayName("clears the username")
        fun `clears the username`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            every { checkInputsFilledUseCase(any<SignUpData>()) } returns false
            sut.onIntent(SignUpScreenIntent.Change.Username("mohamed"))

            sut.onIntent(SignUpScreenIntent.Clear.Username)

            assertThat(sut.state.value.form.username).isEmpty()
        }

        @Test
        @DisplayName("clears the email")
        fun `clears the email`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            every { checkInputsFilledUseCase(any<SignUpData>()) } returns false
            sut.onIntent(SignUpScreenIntent.Change.Email("user@gmail.com"))

            sut.onIntent(SignUpScreenIntent.Clear.Email)

            assertThat(sut.state.value.form.email).isEmpty()
        }

        @Test
        @DisplayName("clears the phone number")
        fun `clears the phone number`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            every { checkInputsFilledUseCase(any<SignUpData>()) } returns false
            sut.onIntent(SignUpScreenIntent.Change.PhoneNumber("1001234567"))

            sut.onIntent(SignUpScreenIntent.Clear.PhoneNumber)

            assertThat(sut.state.value.form.phoneNumber).isEmpty()
        }

        @Test
        @DisplayName("clears the confirmed password")
        fun `clears the confirmed password`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            every { checkInputsFilledUseCase(any<SignUpData>()) } returns false
            sut.onIntent(SignUpScreenIntent.Change.ConfirmedPassword("Abc12345"))

            sut.onIntent(SignUpScreenIntent.Clear.ConfirmedPassword)

            assertThat(sut.state.value.form.confirmedPassword).isEmpty()
        }
    }

    @Nested
    @DisplayName("CountryIntent")
    inner class CountryIntent {

        @Test
        @DisplayName("ToggleMenu flips the menu visibility")
        fun `ToggleMenu flips the menu visibility`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()

            sut.onIntent(SignUpScreenIntent.Country.ToggleMenu)

            assertThat(sut.state.value.showCountryInfoMenu).isTrue()
        }

        @Test
        @DisplayName("Select selects the country and closes the menu")
        fun `Select selects the country and closes the menu`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut()
                sut.onIntent(SignUpScreenIntent.Country.ToggleMenu)

                sut.onIntent(SignUpScreenIntent.Country.Select(usa))

                assertThat(sut.state.value.selectedCountryInfo).isEqualTo(usa)
                assertThat(sut.state.value.showCountryInfoMenu).isFalse()
            }

        @Test
        @DisplayName("ChangeKeyword filters the countries by name")
        fun `ChangeKeyword filters the countries by name`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut(listOf(egypt, usa))
                advanceUntilIdle() // countriesInfo must be loaded before it can be filtered

                sut.onIntent(SignUpScreenIntent.Country.ChangeKeyword("egy"))

                assertThat(sut.state.value.countriesInfoByKeyword).containsExactly(egypt)
            }
    }

    @Nested
    @DisplayName("SignUp.SubmitForm")
    inner class SubmitForm {

        private fun fillValidForm(sut: SignUpViewModel) {
            every { checkInputsFilledUseCase(any<SignUpData>()) } returns true
            coEvery { measurePasswordStrengthUseCase("Abc12345") } returns PasswordStrength.GOOD
            sut.onIntent(SignUpScreenIntent.Change.Username("mohamed"))
            sut.onIntent(SignUpScreenIntent.Change.Email("user@gmail.com"))
            sut.onIntent(SignUpScreenIntent.Change.PhoneNumber("1001234567"))
            sut.onIntent(SignUpScreenIntent.Change.Password("Abc12345"))
            sut.onIntent(SignUpScreenIntent.Change.ConfirmedPassword("Abc12345"))
            every { validateUsernameUseCase(any()) } returns Result.Success(Unit)
            every { validateEmailUseCase(any()) } returns Result.Success(Unit)
            every { validatePhoneUseCase(any()) } returns Result.Success(Unit)
            every { validatePasswordUseCase(any()) } returns Result.Success(Unit)
            every { validateConfirmedPasswordUseCase(any(), any()) } returns Result.Success(Unit)
        }

        @Test
        @DisplayName("reports a field's validation error and never signs up")
        fun `reports a field's validation error and never signs up`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut(listOf(egypt))
                advanceUntilIdle() // selectedCountryInfo = egypt, needed to build the E.164 number
                every { checkInputsFilledUseCase(any<SignUpData>()) } returns true
                sut.onIntent(SignUpScreenIntent.Change.Username(""))
                every { validateUsernameUseCase("") } returns Result.Error(ValidationError.EMPTY_USERNAME)
                every { validateEmailUseCase(any()) } returns Result.Success(Unit)
                every { validatePhoneUseCase(any()) } returns Result.Success(Unit)
                every { validatePasswordUseCase(any()) } returns Result.Success(Unit)
                every { validateConfirmedPasswordUseCase(any(), any()) } returns Result.Success(Unit)

                sut.onIntent(SignUpScreenIntent.SignUp.SubmitForm)

                val usernameError = sut.state.value.userError.usernameError as UiText.StringResource
                assertThat(usernameError.id).isEqualTo(R.string.username_cannot_be_empty)
                coVerify(exactly = 0) { signUpUseCase(any(), any()) }
            }

        @Test
        @DisplayName("reports that the phone number is already registered")
        fun `reports that the phone number is already registered`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut(listOf(egypt))
                advanceUntilIdle()
                fillValidForm(sut)
                coEvery { checkIfPhoneNumberExistsUseCase("+201001234567") } returns Result.Success(Unit)

                sut.onIntent(SignUpScreenIntent.SignUp.SubmitForm)

                val phoneError = sut.state.value.userError.phoneNumberError as UiText.StringResource
                assertThat(phoneError.id).isEqualTo(R.string.phone_already_registered)
                assertThat(sut.state.value.isSigningUp).isFalse()
                coVerify(exactly = 0) { signUpUseCase(any(), any()) }
            }

        @Test
        @DisplayName("reports success once the account is created")
        fun `reports success once the account is created`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut(listOf(egypt))
                advanceUntilIdle()
                fillValidForm(sut)
                coEvery {
                    checkIfPhoneNumberExistsUseCase("+201001234567")
                } returns Result.Error(DataError.Unknown)
                coEvery {
                    signUpUseCase(
                        User(username = "mohamed", email = "user@gmail.com", phoneNumber = "+201001234567"),
                        "Abc12345"
                    )
                } returns Result.Success(Unit)

                sut.onIntent(SignUpScreenIntent.SignUp.SubmitForm)

                sut.signUpResult.test {
                    assertThat(awaitItem()).isEqualTo(SignUpScreenEvent.SignUpResult.Success.NotVerified)
                }
                assertThat(sut.state.value.isSigningUp).isFalse()
            }

        @Test
        @DisplayName("reports failure when creating the account fails")
        fun `reports failure when creating the account fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut(listOf(egypt))
                advanceUntilIdle()
                fillValidForm(sut)
                coEvery {
                    checkIfPhoneNumberExistsUseCase("+201001234567")
                } returns Result.Error(DataError.Unknown)
                coEvery { signUpUseCase(any(), any()) } returns Result.Error(DataError.Unknown)

                sut.onIntent(SignUpScreenIntent.SignUp.SubmitForm)

                sut.signUpResult.test {
                    val failure = awaitItem() as SignUpScreenEvent.SignUpResult.Failure
                    val uiText = failure.uiText as UiText.StringResource
                    assertThat(uiText.id).isEqualTo(R.string.unknown_error)
                }
            }
    }

    @Nested
    @DisplayName("SignUp.Google / Facebook")
    inner class SocialSignUp {

        @Test
        @DisplayName("Google reports Verified on success")
        fun `Google reports Verified on success`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            coEvery { signUpWithGoogleUseCase() } returns Result.Success(Unit)

            sut.onIntent(SignUpScreenIntent.SignUp.Google)

            sut.signUpResult.test {
                assertThat(awaitItem()).isEqualTo(SignUpScreenEvent.SignUpResult.Success.Verified)
            }
        }

        @Test
        @DisplayName("Google reports failure")
        fun `Google reports failure`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            coEvery { signUpWithGoogleUseCase() } returns Result.Error(DataError.Unknown)

            sut.onIntent(SignUpScreenIntent.SignUp.Google)

            sut.signUpResult.test {
                val failure = awaitItem() as SignUpScreenEvent.SignUpResult.Failure
                assertThat((failure.uiText as UiText.StringResource).id).isEqualTo(R.string.unknown_error)
            }
        }

        @Test
        @DisplayName("Facebook reports Verified on success")
        fun `Facebook reports Verified on success`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            coEvery { signUpWithFacebookUseCase() } returns Result.Success(Unit)

            sut.onIntent(SignUpScreenIntent.SignUp.Facebook)

            sut.signUpResult.test {
                assertThat(awaitItem()).isEqualTo(SignUpScreenEvent.SignUpResult.Success.Verified)
            }
        }

        @Test
        @DisplayName("Facebook reports failure")
        fun `Facebook reports failure`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            coEvery { signUpWithFacebookUseCase() } returns Result.Error(DataError.Unknown)

            sut.onIntent(SignUpScreenIntent.SignUp.Facebook)

            sut.signUpResult.test {
                val failure = awaitItem() as SignUpScreenEvent.SignUpResult.Failure
                assertThat((failure.uiText as UiText.StringResource).id).isEqualTo(R.string.unknown_error)
            }
        }
    }

    @Test
    @DisplayName("onEvent(SignUpResult.Failure) sets the sign-up error")
    fun `onEvent SignUpResult Failure sets the sign-up error`() =
        runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            val uiText = UiText.StringResource(R.string.unknown_error)

            sut.onEvent(SignUpScreenEvent.SignUpResult.Failure(uiText))

            assertThat(sut.state.value.signUpError).isEqualTo(uiText)
        }
}
