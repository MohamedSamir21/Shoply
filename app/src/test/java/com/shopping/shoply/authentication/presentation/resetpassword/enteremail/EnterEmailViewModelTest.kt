package com.shopping.shoply.authentication.presentation.resetpassword.enteremail

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.authentication.domain.usecase.auth.CheckIfEmailExistsUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SendPasswordResetEmailUseCase
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

@DisplayName("EnterEmailViewModel")
class EnterEmailViewModelTest {
    private val validateEmailUseCase = mockk<ValidateEmailUseCase>()
    private val checkIfEmailExistsUseCase = mockk<CheckIfEmailExistsUseCase>()
    private val sendPasswordResetEmailUseCase = mockk<SendPasswordResetEmailUseCase>()
    private val sut = EnterEmailViewModel(
        validateEmailUseCase = validateEmailUseCase,
        checkIfEmailExistsUseCase = checkIfEmailExistsUseCase,
        sendPasswordResetEmailUseCase = sendPasswordResetEmailUseCase
    )
    private val email = "user@gmail.com"

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    @Test
    @DisplayName("onChangeEmail updates the email")
    fun `onChangeEmail updates the email`() = runTest(mainDispatcherExtension.dispatcher) {
        sut.onChangeEmail(email)

        assertThat(sut.state.value.email).isEqualTo(email)
    }

    @Nested
    @DisplayName("onNext")
    inner class OnNext {

        @Test
        @DisplayName("reports that the email isn't valid")
        fun `reports that the email isn't valid`() = runTest(mainDispatcherExtension.dispatcher) {
            sut.onChangeEmail("user")
            every { validateEmailUseCase(any()) } returns Result.Error(ValidationError.INVALID_EMAIL)

            sut.onNext()

            val actualUiText = sut.state.value.error as UiText.StringResource
            assertThat(actualUiText.id).isEqualTo(R.string.enter_valid_email)
            coVerify(exactly = 0) { checkIfEmailExistsUseCase(any()) }
        }

        @Test
        @DisplayName("reports an error when checking if the email exists fails")
        fun `reports an error when checking if the email exists fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                sut.onChangeEmail(email)
                every { validateEmailUseCase(any()) } returns Result.Success(Unit)
                coEvery { checkIfEmailExistsUseCase(email) } returns Result.Error(DataError.Unknown)

                sut.onNext()

                val actualUiText = sut.state.value.error as UiText.StringResource
                assertThat(actualUiText.id).isEqualTo(R.string.unknown_error)
                assertThat(sut.state.value.isProcessing).isFalse()
                coVerify(exactly = 0) { sendPasswordResetEmailUseCase(any()) }
            }

        @Test
        @DisplayName("reports an error when sending the reset email fails")
        fun `reports an error when sending the reset email fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                sut.onChangeEmail(email)
                every { validateEmailUseCase(any()) } returns Result.Success(Unit)
                coEvery { checkIfEmailExistsUseCase(email) } returns Result.Success(Unit)
                coEvery { sendPasswordResetEmailUseCase(email) } returns Result.Error(DataError.Unknown)

                sut.onNext()

                val actualUiText = sut.state.value.error as UiText.StringResource
                assertThat(actualUiText.id).isEqualTo(R.string.unknown_error)
                assertThat(sut.state.value.isProcessing).isFalse()
            }

        @Test
        @DisplayName("reports success once the reset email is sent")
        fun `reports success once the reset email is sent`() =
            runTest(mainDispatcherExtension.dispatcher) {
                sut.onChangeEmail(email)
                every { validateEmailUseCase(any()) } returns Result.Success(Unit)
                coEvery { checkIfEmailExistsUseCase(email) } returns Result.Success(Unit)
                coEvery { sendPasswordResetEmailUseCase(email) } returns Result.Success(Unit)

                sut.onNext()

                sut.emailVerificationResult.test {
                    assertThat(awaitItem()).isEqualTo(EnterEmailEvent.Success)
                }
                assertThat(sut.state.value.isProcessing).isFalse()
                assertThat(sut.state.value.error).isNull()
            }
    }
}
