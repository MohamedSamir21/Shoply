package com.shopping.shoply.authentication.presentation.signin

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.SignInData
import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.authentication.domain.usecase.auth.SignInUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpWithFacebookUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpWithGoogleUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.CheckInputsFilledUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateEmailUseCase
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

@DisplayName("SignInViewModel")
class SignInViewModelTest {
    private val continueWithFacebookUseCase = mockk<SignUpWithFacebookUseCase>()
    private val continueWithGoogleUseCase = mockk<SignUpWithGoogleUseCase>()
    private val checkInputsFilledUseCase = mockk<CheckInputsFilledUseCase>()
    private val validateEmailUseCase = mockk<ValidateEmailUseCase>()
    private val signInUseCase = mockk<SignInUseCase>()
    private val sut = SignInViewModel(
        continueWithFacebookUseCase = continueWithFacebookUseCase,
        continueWithGoogleUseCase = continueWithGoogleUseCase,
        checkInputsFilledUseCase = checkInputsFilledUseCase,
        validateEmailUseCase = validateEmailUseCase,
        signInUseCase = signInUseCase
    )
    private val email = "user@gmail.com"
    private val password = "password"

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    @Nested
    @DisplayName("ChangeIntent")
    inner class ChangeIntent {

        @Test
        @DisplayName("updates both email and password leading to enable the sign-in")
        fun `updates email and password leading to enable the sign-in`() =
            runTest(mainDispatcherExtension.dispatcher) {
                every { checkInputsFilledUseCase(any<SignInData>()) } returns true

                sut.onIntent(SignInScreenIntent.Change.Email(email))
                sut.onIntent(SignInScreenIntent.Change.Password(password))

                val signInState = sut.state.value
                assertThat(signInState.email).isEqualTo(email)
                assertThat(signInState.password).isEqualTo(password)
                assertThat(signInState.enableSignIn).isTrue()
            }

        @Test
        @DisplayName("updates only email leading to not enable the sign-in")
        fun `updates only email leading to not enable the sign-in`() =
            runTest(mainDispatcherExtension.dispatcher) {
                every { checkInputsFilledUseCase(any<SignInData>()) } returns false

                sut.onIntent(SignInScreenIntent.Change.Email(email))

                val signInState = sut.state.value
                assertThat(signInState.email).isEqualTo(email)
                assertThat(signInState.password).isEmpty()
                assertThat(signInState.enableSignIn).isFalse()
            }

        @Test
        @DisplayName("toggles the password visibility")
        fun `toggles the password visibility`() = runTest(mainDispatcherExtension.dispatcher) {
            val oldPasswordVisibility1 = sut.state.value.showPassword

            sut.onIntent(SignInScreenIntent.Change.PasswordVisibility)

            assertThat(sut.state.value.showPassword).isEqualTo(!oldPasswordVisibility1)

            val oldPasswordVisibility2 = sut.state.value.showPassword

            sut.onIntent(SignInScreenIntent.Change.PasswordVisibility)
            assertThat(sut.state.value.showPassword).isEqualTo(!oldPasswordVisibility2)
        }
    }

    @Nested
    @DisplayName("ClearIntent")
    inner class ClearIntent {

        @Test
        @DisplayName("clears the email")
        fun `clears the email`() = runTest(mainDispatcherExtension.dispatcher) {
            every { checkInputsFilledUseCase(any<SignInData>()) } returns false
            sut.onIntent(SignInScreenIntent.Change.Email("user@gmail.com"))

            sut.onIntent(SignInScreenIntent.Clear.Email)

            assertThat(sut.state.value.email).isEmpty()
        }

        @Test
        @DisplayName("clears the password")
        fun `clears the password`() = runTest(mainDispatcherExtension.dispatcher) {
            every { checkInputsFilledUseCase(any<SignInData>()) } returns false
            sut.onIntent(SignInScreenIntent.Change.Password("password"))

            sut.onIntent(SignInScreenIntent.Clear.Password)

            assertThat(sut.state.value.password).isEmpty()
        }
    }

    @Nested
    @DisplayName("SignInIntent")
    inner class SignInIntent {

        @Test
        @DisplayName("reports that the email isn't valid")
        fun `reports that the email isn't valid`() = runTest(mainDispatcherExtension.dispatcher) {
            stubEmailWithSpecificResult(
                email = "user",
                emailValidationResult = Result.Error<Unit, ValidationError>(
                    error = ValidationError.INVALID_EMAIL
                )
            )

            sut.onIntent(SignInScreenIntent.SignIn)
            val actualUiText = sut.state.value.emailValidationError.let { uiText ->
                (uiText as UiText.StringResource)
            }
            val expectedUiText = UiText.StringResource(R.string.enter_valid_email)

            assertThat(actualUiText.id).isEqualTo(expectedUiText.id)
            coVerify(exactly = 0) { signInUseCase(any(), any()) }
        }

        @Test
        @DisplayName("reports success when the user is signed in successfully")
        fun `reports success when the user is signed in successfully`() =
            runTest(mainDispatcherExtension.dispatcher) {
                stubEmailWithSpecificResult(
                    email = email,
                    emailValidationResult = Result.Success<Unit, ValidationError>(Unit)
                )
                coEvery {
                    signInUseCase(
                        any(),
                        any()
                    )
                } returns Result.Success<Unit, DataError>(Unit)

                sut.onIntent(SignInScreenIntent.SignIn)

                sut.signInResult.test {
                    assertThat(awaitItem()).isEqualTo(SignInScreenEvent.SignInResult.Success)
                }
                assertThat(sut.state.value.isSigningIn).isFalse()
            }

        @Test
        @DisplayName("reports error when the sign-in process fails")
        fun `reports error when the sign-in process fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                stubEmailWithSpecificResult(
                    email = email,
                    emailValidationResult = Result.Success<Unit, ValidationError>(Unit)
                )
                coEvery {
                    signInUseCase(any(), any())
                } returns Result.Error<Unit, DataError>(
                    error = DataError.Unknown
                )

                sut.onIntent(SignInScreenIntent.SignIn)

                sut.signInResult.test {
                    val expectedUiText = UiText.StringResource(R.string.unknown_error)
                    val actualUiText =
                        awaitItem().let { result ->
                            val uiText = (result as SignInScreenEvent.SignInResult.Failure).uiText
                            uiText as UiText.StringResource
                        }
                    assertThat(actualUiText.id).isEqualTo(expectedUiText.id)
                }
                assertThat(sut.state.value.isSigningIn).isFalse()
            }

        private fun stubEmailWithSpecificResult(
            email: String,
            emailValidationResult: Result<Unit, ValidationError>
        ) {
            every { checkInputsFilledUseCase(any<SignInData>()) } returns true
            sut.onIntent(SignInScreenIntent.Change.Email(email))
            sut.onIntent(SignInScreenIntent.Change.Password(password))
            every { validateEmailUseCase(any()) } returns emailValidationResult
        }
    }

    @Nested
    @DisplayName("ContinueWithFacebookIntent")
    inner class ContinueWithFacebookIntent {

        @Test
        @DisplayName("reports success when facebook authentication succeeds")
        fun `reports success when facebook authentication succeeds`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { continueWithFacebookUseCase() } returns Result.Success<Unit, DataError>(
                    Unit
                )

                sut.onIntent(SignInScreenIntent.ContinueWithFacebook)

                sut.signInResult.test {
                    assertThat(awaitItem()).isEqualTo(SignInScreenEvent.SignInResult.Success)
                }
            }

        @Test
        @DisplayName("reports failure when facebook authentication fails")
        fun `reports failure when facebook authentication fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { continueWithFacebookUseCase() } returns Result.Error<Unit, DataError>(
                    error = DataError.Unknown
                )

                sut.onIntent(SignInScreenIntent.ContinueWithFacebook)

                val expectedUiText = UiText.StringResource(
                    R.string.unknown_error
                )
                sut.signInResult.test {
                    val signInResult = awaitItem()
                    val actualUiText = signInResult.let { result ->
                        val uiText = (result as SignInScreenEvent.SignInResult.Failure).uiText
                        uiText as UiText.StringResource
                    }

                    assertThat(actualUiText.id).isEqualTo(expectedUiText.id)
                }
            }
    }

    @Nested
    @DisplayName("ContinueWithGoogleIntent")
    inner class ContinueWithGoogleIntent {
        @Test
        @DisplayName("reports success when google authentication succeeds")
        fun `reports success when google authentication succeeds`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { continueWithGoogleUseCase() } returns Result.Success<Unit, DataError>(Unit)

                sut.onIntent(SignInScreenIntent.ContinueWithGoogle)

                sut.signInResult.test {
                    assertThat(awaitItem()).isEqualTo(SignInScreenEvent.SignInResult.Success)
                }
            }

        @Test
        @DisplayName("reports failure when google authentication fails")
        fun `reports failure when google authentication fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { continueWithGoogleUseCase() } returns Result.Error<Unit, DataError>(
                    error = DataError.Unknown
                )

                sut.onIntent(SignInScreenIntent.ContinueWithGoogle)

                val expectedUiText = UiText.StringResource(
                    R.string.unknown_error
                )
                sut.signInResult.test {
                    val signInResult = awaitItem()
                    val actualUiText = signInResult.let { result ->
                        val uiText = (result as SignInScreenEvent.SignInResult.Failure).uiText
                        uiText as UiText.StringResource
                    }
                    assertThat(actualUiText.id).isEqualTo(expectedUiText.id)
                }
            }
    }

    @Nested
    @DisplayName("SignInFailureEvent")
    inner class SignInFailureEvent {
        @Test
        @DisplayName("reports error message when any authentication fails")
        fun `reports error message when any authentication fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val originalUiText = UiText.StringResource(
                    R.string.unknown_error
                )
                sut.onEvent(
                    SignInScreenEvent.SignInResult.Failure(
                        uiText = originalUiText
                    )
                )

                val actualUiText =
                    sut.state.value.signInError.let { uiText -> uiText as UiText.StringResource }
                assertThat(actualUiText.id).isEqualTo(originalUiText.id)
            }
    }
}