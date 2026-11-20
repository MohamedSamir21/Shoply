package com.shopping.shoply.changeemail.presentation

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.interfaces.validator.EmailValidator
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateEmailUseCase
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.changeemail.domain.EmailChangeStatus
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.fakes.FakeEmailChangeRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("ChangeEmailViewModelTest")
class ChangeEmailViewModelTest {
    private val repository = FakeEmailChangeRepository(email = "alex@mail.com")

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut(askForPassword: Boolean = false) = ChangeEmailViewModel(
        repository = repository,
        validateEmail = ValidateEmailUseCase(EmailValidator { it.contains("@") && it.contains(".") }),
        savedStateHandle = SavedStateHandle(mapOf(ChangeEmailViewModel.ARG_ASK_FOR_PASSWORD to askForPassword))
    )

    private fun ChangeEmailViewModel.submit(email: String, password: String) {
        onIntent(ChangeEmailIntent.ChangeNewEmail(email))
        onIntent(ChangeEmailIntent.ChangePassword(password))
        onIntent(ChangeEmailIntent.SendLink)
    }

    private fun UiText?.id() = (this as UiText.StringResource).id

    @Test
    @DisplayName("a valid email and password send the link and move to Check your inbox")
    fun `a valid email and password send the link and move to Check your inbox`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.submit(" sam@mail.com ", "secret")

        assertThat(repository.requests.single()).isEqualTo("sam@mail.com" to "secret")
        assertThat(sut.state.value.step).isEqualTo(ChangeEmailStep.CHECK_INBOX)
        assertThat(sut.state.value.password).isEmpty()
        assertThat(sut.state.value.resendSecondsLeft).isGreaterThan(0)
    }

    @Test
    @DisplayName("a wrong password shows under the password field and stays on the first step")
    fun `a wrong password shows under the password field and stays on the first step`() = runTest(main.dispatcher) {
        repository.requestResult = Result.Error(DataError.Authentication.WRONG_PASSWORD)
        val sut = createSut()

        sut.submit("sam@mail.com", "wrong")

        assertThat(sut.state.value.step).isEqualTo(ChangeEmailStep.ENTER)
        assertThat(sut.state.value.passwordError.id()).isEqualTo(R.string.wrong_password)
    }

    @Test
    @DisplayName("the current email is rejected without calling Firebase")
    fun `the current email is rejected without calling Firebase`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.submit("ALEX@mail.com", "secret")

        assertThat(repository.requests).isEmpty()
        assertThat(sut.state.value.emailError.id()).isEqualTo(R.string.new_email_same_as_current)
    }

    @Test
    @DisplayName("an invalid email and an empty password are both flagged")
    fun `an invalid email and an empty password are both flagged`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.submit("not-an-email", "")

        assertThat(repository.requests).isEmpty()
        assertThat(sut.state.value.emailError.id()).isEqualTo(R.string.enter_valid_email)
        assertThat(sut.state.value.passwordError.id()).isEqualTo(R.string.password_required)
    }

    @Test
    @DisplayName("a change already waiting opens on Check your inbox")
    fun `a change already waiting opens on Check your inbox`() = runTest(main.dispatcher) {
        repository.pending = "sam@mail.com"

        val state = createSut().state.value

        assertThat(state.step).isEqualTo(ChangeEmailStep.CHECK_INBOX)
        assertThat(state.newEmail).isEqualTo("sam@mail.com")
    }

    @Test
    @DisplayName("an expired password check opens on the password step with the pending email filled in")
    fun `an expired password check opens on the password step with the pending email filled in`() = runTest(main.dispatcher) {
        repository.pending = "sam@mail.com"

        val state = createSut(askForPassword = true).state.value

        assertThat(state.step).isEqualTo(ChangeEmailStep.ENTER)
        assertThat(state.newEmail).isEqualTo("sam@mail.com")
    }

    @Test
    @DisplayName("coming back after the link was clicked shows Email updated")
    fun `coming back after the link was clicked shows Email updated`() = runTest(main.dispatcher) {
        repository.pending = "sam@mail.com"
        repository.status = Result.Success(EmailChangeStatus.CONFIRMED)
        val sut = createSut()

        sut.onIntent(ChangeEmailIntent.CheckIfConfirmed)

        assertThat(sut.state.value.step).isEqualTo(ChangeEmailStep.UPDATED)
        assertThat(sut.state.value.mustSignInAgain).isFalse()
    }

    @Test
    @DisplayName("a change that ended the session asks to sign in again")
    fun `a change that ended the session asks to sign in again`() = runTest(main.dispatcher) {
        repository.pending = "sam@mail.com"
        repository.status = Result.Success(EmailChangeStatus.SIGNED_OUT)
        val sut = createSut()

        sut.onIntent(ChangeEmailIntent.CheckIfConfirmed)

        assertThat(sut.state.value.step).isEqualTo(ChangeEmailStep.UPDATED)
        assertThat(sut.state.value.mustSignInAgain).isTrue()
    }
}
