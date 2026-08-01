package com.shopping.shoply.authentication.presentation.resetpassword.createpassword

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.PasswordStrength
import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.authentication.domain.usecase.auth.UpdatePasswordUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.MeasurePasswordStrengthUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateConfirmedPasswordUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidatePasswordUseCase
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

@DisplayName("CreateNewPasswordViewModel")
class CreateNewPasswordViewModelTest {
    private val validatePasswordUseCase = mockk<ValidatePasswordUseCase>()
    private val validateConfirmedPasswordUseCase = mockk<ValidateConfirmedPasswordUseCase>()
    private val measurePasswordStrengthUseCase = mockk<MeasurePasswordStrengthUseCase>()
    private val updatePasswordUseCase = mockk<UpdatePasswordUseCase>()
    private val sut = CreateNewPasswordViewModel(
        validatePasswordUseCase = validatePasswordUseCase,
        validateConfirmedPasswordUseCase = validateConfirmedPasswordUseCase,
        measurePasswordStrengthUseCase = measurePasswordStrengthUseCase,
        updatePasswordUseCase = updatePasswordUseCase
    )

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    @Nested
    @DisplayName("onIntent Change")
    inner class ChangeIntent {

        @Test
        @DisplayName("ChangePassword updates the password and clears its error")
        fun `ChangePassword updates the password and clears its error`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { measurePasswordStrengthUseCase("Abc12345") } returns PasswordStrength.GOOD
                sut.onIntent(CreateNewPasswordIntent.ChangePassword("Abc12345"))

                assertThat(sut.state.value.password).isEqualTo("Abc12345")
                assertThat(sut.state.value.passwordError).isNull()
            }

        @Test
        @DisplayName("ChangeConfirmedPassword updates the confirmed password and clears its error")
        fun `ChangeConfirmedPassword updates the confirmed password and clears its error`() =
            runTest(mainDispatcherExtension.dispatcher) {
                sut.onIntent(CreateNewPasswordIntent.ChangeConfirmedPassword("Abc12345"))

                assertThat(sut.state.value.confirmedPassword).isEqualTo("Abc12345")
                assertThat(sut.state.value.confirmedPasswordError).isNull()
            }

        @Test
        @DisplayName("ChangePasswordVisibility toggles the password visibility")
        fun `ChangePasswordVisibility toggles the password visibility`() =
            runTest(mainDispatcherExtension.dispatcher) {
                sut.onIntent(CreateNewPasswordIntent.ChangePasswordVisibility(true))

                assertThat(sut.state.value.showPassword).isFalse()
            }
    }

    // Same debounce + cancellation shape as SignUpViewModel — both now share PasswordStrengthDebouncer, see
    @OptIn(ExperimentalCoroutinesApi::class)
    @Nested
    @DisplayName("Password strength debounce")
    inner class PasswordStrengthDebounce {

        @Test
        @DisplayName("measures the strength once the 200ms debounce elapses")
        fun `measures the strength once the 200ms debounce elapses`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { measurePasswordStrengthUseCase("Abc12345") } returns PasswordStrength.GOOD

                sut.onIntent(CreateNewPasswordIntent.ChangePassword("Abc12345"))
                assertThat(sut.state.value.passwordStrength).isEqualTo(PasswordStrength.NONE)

                advanceTimeBy(200)
                advanceUntilIdle()

                assertThat(sut.state.value.passwordStrength).isEqualTo(PasswordStrength.GOOD)
            }

        @Test
        @DisplayName("cancels the previous measurement when the password changes again before 200ms")
        fun `cancels the previous measurement when the password changes again before 200ms`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { measurePasswordStrengthUseCase("Ab") } returns PasswordStrength.WEAK
                coEvery { measurePasswordStrengthUseCase("Abc12345") } returns PasswordStrength.GOOD

                sut.onIntent(CreateNewPasswordIntent.ChangePassword("Ab"))
                advanceTimeBy(100)
                sut.onIntent(CreateNewPasswordIntent.ChangePassword("Abc12345"))

                advanceUntilIdle()

                assertThat(sut.state.value.passwordStrength).isEqualTo(PasswordStrength.GOOD)
                coVerify(exactly = 0) { measurePasswordStrengthUseCase("Ab") }
            }

        @Test
        @DisplayName("resets the strength to NONE immediately when the password becomes empty")
        fun `resets the strength to NONE immediately when the password becomes empty`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { measurePasswordStrengthUseCase("Abc12345") } returns PasswordStrength.GOOD

                sut.onIntent(CreateNewPasswordIntent.ChangePassword("Abc12345"))
                advanceTimeBy(100) // still mid-debounce — nothing measured yet

                sut.onIntent(CreateNewPasswordIntent.ChangePassword(""))

                assertThat(sut.state.value.passwordStrength).isEqualTo(PasswordStrength.NONE)
                advanceUntilIdle()
                coVerify(exactly = 0) { measurePasswordStrengthUseCase("Abc12345") }
            }
    }

    @Nested
    @DisplayName("Submit")
    inner class Submit {

        @Test
        @DisplayName("reports the password error and never updates the password")
        fun `reports the password error and never updates the password`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { measurePasswordStrengthUseCase("short") } returns PasswordStrength.WEAK
                sut.onIntent(CreateNewPasswordIntent.ChangePassword("short"))
                sut.onIntent(CreateNewPasswordIntent.ChangeConfirmedPassword("short"))
                every { validatePasswordUseCase("short") } returns
                        Result.Error(ValidationError.PASSWORD_TOO_SHORT)
                every { validateConfirmedPasswordUseCase("short", "short") } returns Result.Success(
                    Unit
                )

                sut.onIntent(CreateNewPasswordIntent.Submit)

                val passwordError = sut.state.value.passwordError as UiText.StringResource
                assertThat(passwordError.id).isEqualTo(R.string.password_too_short)
                assertThat(sut.state.value.confirmedPasswordError).isNull()
                coVerify(exactly = 0) { updatePasswordUseCase(any()) }
            }

        @Test
        @DisplayName("reports the confirmed-password error and never updates the password")
        fun `reports the confirmed-password error and never updates the password`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { measurePasswordStrengthUseCase("Abc12345") } returns PasswordStrength.GOOD
                sut.onIntent(CreateNewPasswordIntent.ChangePassword("Abc12345"))
                sut.onIntent(CreateNewPasswordIntent.ChangeConfirmedPassword("different"))
                every { validatePasswordUseCase("Abc12345") } returns Result.Success(Unit)
                every { validateConfirmedPasswordUseCase("Abc12345", "different") } returns
                        Result.Error(ValidationError.PASSWORDS_DO_NOT_MATCH)

                sut.onIntent(CreateNewPasswordIntent.Submit)

                val confirmedError = sut.state.value.confirmedPasswordError as UiText.StringResource
                assertThat(confirmedError.id).isEqualTo(R.string.passwords_do_not_match)
                assertThat(sut.state.value.passwordError).isNull()
                coVerify(exactly = 0) { updatePasswordUseCase(any()) }
            }

        @Test
        @DisplayName("reports success once the password is updated")
        fun `reports success once the password is updated`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { measurePasswordStrengthUseCase("Abc12345") } returns PasswordStrength.GOOD
                sut.onIntent(CreateNewPasswordIntent.ChangePassword("Abc12345"))
                sut.onIntent(CreateNewPasswordIntent.ChangeConfirmedPassword("Abc12345"))
                every { validatePasswordUseCase("Abc12345") } returns Result.Success(Unit)
                every {
                    validateConfirmedPasswordUseCase(
                        "Abc12345",
                        "Abc12345"
                    )
                } returns Result.Success(Unit)
                coEvery { updatePasswordUseCase("Abc12345") } returns Result.Success(Unit)

                sut.onIntent(CreateNewPasswordIntent.Submit)

                assertThat(sut.state.value.resetSucceeded).isTrue()
                assertThat(sut.state.value.isProcessing).isFalse()
            }

        @Test
        @DisplayName("reports an error when updating the password fails")
        fun `reports an error when updating the password fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { measurePasswordStrengthUseCase("Abc12345") } returns PasswordStrength.GOOD
                sut.onIntent(CreateNewPasswordIntent.ChangePassword("Abc12345"))
                sut.onIntent(CreateNewPasswordIntent.ChangeConfirmedPassword("Abc12345"))
                every { validatePasswordUseCase("Abc12345") } returns Result.Success(Unit)
                every {
                    validateConfirmedPasswordUseCase(
                        "Abc12345",
                        "Abc12345"
                    )
                } returns Result.Success(Unit)
                coEvery { updatePasswordUseCase("Abc12345") } returns Result.Error(DataError.Unknown)

                sut.onIntent(CreateNewPasswordIntent.Submit)

                val error = sut.state.value.error as UiText.StringResource
                assertThat(error.id).isEqualTo(R.string.unknown_error)
                assertThat(sut.state.value.isProcessing).isFalse()
                assertThat(sut.state.value.resetSucceeded).isFalse()
            }
    }

    @Test
    @DisplayName("resetError clears the error")
    fun `resetError clears the error`() = runTest(mainDispatcherExtension.dispatcher) {
        coEvery { measurePasswordStrengthUseCase("Abc12345") } returns PasswordStrength.GOOD
        sut.onIntent(CreateNewPasswordIntent.ChangePassword("Abc12345"))
        sut.onIntent(CreateNewPasswordIntent.ChangeConfirmedPassword("Abc12345"))
        every { validatePasswordUseCase("Abc12345") } returns Result.Success(Unit)
        every { validateConfirmedPasswordUseCase("Abc12345", "Abc12345") } returns Result.Success(
            Unit
        )
        coEvery { updatePasswordUseCase("Abc12345") } returns Result.Error(DataError.Unknown)
        sut.onIntent(CreateNewPasswordIntent.Submit)
        assertThat(sut.state.value.error).isNotNull()

        sut.resetError()

        assertThat(sut.state.value.error).isNull()
    }
}
