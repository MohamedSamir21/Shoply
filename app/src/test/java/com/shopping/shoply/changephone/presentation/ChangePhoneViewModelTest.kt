package com.shopping.shoply.changephone.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.interfaces.repository.CountryInfoRepository
import com.shopping.shoply.authentication.domain.interfaces.validator.PhoneValidator
import com.shopping.shoply.authentication.domain.model.ISOCountryCode
import com.shopping.shoply.authentication.domain.usecase.countryinfo.GetCountryInfoUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidatePhoneUseCase
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.changephone.domain.PhoneCodeEvent
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.fakes.FakePhoneChangeRepository
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("ChangePhoneViewModelTest")
class ChangePhoneViewModelTest {
    private val repository = FakePhoneChangeRepository(phone = "+201001234567")

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    /** Egypt only (+20); any 12-digit E.164 number counts as valid. */
    private val egyptOnly = object : CountryInfoRepository {
        override suspend fun getISOCountryCodes() = listOf(ISOCountryCode("EG"))
        override suspend fun getCountryCode(isoCountryCode: ISOCountryCode) = 20
        override suspend fun getCountryName(isoCountryCode: ISOCountryCode) = "Egypt"
        override suspend fun getExamplePhoneNumber(isoCountryCode: ISOCountryCode) = "100 123 4567"
    }

    private fun createSut() = ChangePhoneViewModel(
        repository = repository,
        validatePhone = ValidatePhoneUseCase(PhoneValidator { it.filter(Char::isDigit).length == 12 }),
        getCountries = GetCountryInfoUseCase(egyptOnly)
    )

    private fun ChangePhoneViewModel.submit(number: String, password: String = "secret") {
        onIntent(ChangePhoneIntent.ChangeNumber(number))
        onIntent(ChangePhoneIntent.ChangePassword(password))
        onIntent(ChangePhoneIntent.SendCode)
    }

    private fun UiText?.id() = (this as UiText.StringResource).id

    @Test
    @DisplayName("a valid number and password send a code to the full number")
    fun `a valid number and password send a code to the full number`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.submit("1009876543")

        assertThat(repository.passwordsChecked.single()).isEqualTo("secret")
        assertThat(repository.sentTo.single()).isEqualTo("+201009876543")
        assertThat(sut.state.value.step).isEqualTo(ChangePhoneStep.CODE)
        assertThat(sut.state.value.password).isEmpty()
    }

    @Test
    @DisplayName("a wrong password stops before any code is sent")
    fun `a wrong password stops before any code is sent`() = runTest(main.dispatcher) {
        repository.passwordResult = Result.Error(DataError.Authentication.WRONG_PASSWORD)
        val sut = createSut()

        sut.submit("1009876543", password = "wrong")

        assertThat(repository.sentTo).isEmpty()
        assertThat(sut.state.value.passwordError.id()).isEqualTo(R.string.wrong_password)
    }

    @Test
    @DisplayName("the current number and an invalid number are rejected without texting")
    fun `the current number and an invalid number are rejected without texting`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.submit("1001234567")
        assertThat(sut.state.value.numberError.id()).isEqualTo(R.string.new_phone_same_as_current)

        sut.submit("123")
        assertThat(sut.state.value.numberError.id()).isEqualTo(R.string.enter_valid_phone_number)
        assertThat(repository.sentTo).isEmpty()
    }

    @Test
    @DisplayName("accounts without a password skip the password check")
    fun `accounts without a password skip the password check`() = runTest(main.dispatcher) {
        repository.hasPassword = false
        val sut = createSut()

        sut.submit("1009876543", password = "")

        assertThat(repository.passwordsChecked).isEmpty()
        assertThat(repository.sentTo.single()).isEqualTo("+201009876543")
    }

    @Test
    @DisplayName("the right code finishes the change")
    fun `the right code finishes the change`() = runTest(main.dispatcher) {
        val sut = createSut()
        sut.submit("1009876543")

        sut.onIntent(ChangePhoneIntent.ChangeCode("382911"))
        sut.onIntent(ChangePhoneIntent.Verify)

        assertThat(sut.state.value.step).isEqualTo(ChangePhoneStep.UPDATED)
    }

    @Test
    @DisplayName("a wrong code shows under the code and stays on the code step")
    fun `a wrong code shows under the code and stays on the code step`() = runTest(main.dispatcher) {
        repository.verifyResult = Result.Error(DataError.Authentication.INVALID_CODE)
        val sut = createSut()
        sut.submit("1009876543")

        sut.onIntent(ChangePhoneIntent.ChangeCode("000000"))
        sut.onIntent(ChangePhoneIntent.Verify)

        assertThat(sut.state.value.step).isEqualTo(ChangePhoneStep.CODE)
        assertThat(sut.state.value.codeError.id()).isEqualTo(R.string.invalid_code)
    }

    @Test
    @DisplayName("a number already on another account goes back to the number with an error")
    fun `a number already on another account goes back to the number with an error`() = runTest(main.dispatcher) {
        repository.verifyResult = Result.Error(DataError.Authentication.PHONE_NUMBER_ALREADY_IN_USE)
        val sut = createSut()
        sut.submit("1009876543")

        sut.onIntent(ChangePhoneIntent.ChangeCode("382911"))
        sut.onIntent(ChangePhoneIntent.Verify)

        assertThat(sut.state.value.step).isEqualTo(ChangePhoneStep.ENTER)
        assertThat(sut.state.value.numberError.id()).isEqualTo(R.string.phone_already_in_use)
    }

    @Test
    @DisplayName("an expired sign-in sends password accounts back to the password")
    fun `an expired sign-in sends password accounts back to the password`() = runTest(main.dispatcher) {
        repository.verifyResult = Result.Error(DataError.Authentication.REQUIRES_RECENT_LOGIN)
        val sut = createSut()
        sut.submit("1009876543")

        sut.events.test {
            sut.onIntent(ChangePhoneIntent.ChangeCode("382911"))
            sut.onIntent(ChangePhoneIntent.Verify)

            val message = awaitItem() as ChangePhoneEvent.Message
            assertThat(message.text.id()).isEqualTo(R.string.requires_recent_login)
        }
        assertThat(sut.state.value.step).isEqualTo(ChangePhoneStep.ENTER)
    }

    @Test
    @DisplayName("an expired sign-in asks accounts without a password to sign in again")
    fun `an expired sign-in asks accounts without a password to sign in again`() = runTest(main.dispatcher) {
        repository.hasPassword = false
        repository.verifyResult = Result.Error(DataError.Authentication.REQUIRES_RECENT_LOGIN)
        val sut = createSut()
        sut.submit("1009876543", password = "")

        sut.events.test {
            sut.onIntent(ChangePhoneIntent.ChangeCode("382911"))
            sut.onIntent(ChangePhoneIntent.Verify)

            val message = awaitItem() as ChangePhoneEvent.Message
            assertThat(message.text.id()).isEqualTo(R.string.sign_in_again_to_change_phone)
        }
    }

    @Test
    @DisplayName("resend waits 30 seconds, then texts the same number again")
    fun `resend waits 30 seconds, then texts the same number again`() = runTest(main.dispatcher) {
        val sut = createSut()
        sut.submit("1009876543")

        sut.onIntent(ChangePhoneIntent.ResendCode)
        assertThat(repository.sentTo).hasSize(1)

        advanceTimeBy(31_000)
        sut.onIntent(ChangePhoneIntent.ResendCode)
        assertThat(repository.sentTo).containsExactly("+201009876543", "+201009876543").inOrder()
    }

    @Test
    @DisplayName("Use a different number goes back to the number, ready to send again")
    fun `Use a different number goes back to the number, ready to send again`() = runTest(main.dispatcher) {
        val sut = createSut()
        sut.submit("1009876543")

        sut.onIntent(ChangePhoneIntent.UseDifferentNumber)

        assertThat(sut.state.value.step).isEqualTo(ChangePhoneStep.ENTER)
        assertThat(sut.state.value.isBusy).isFalse()
        assertThat(sut.state.value.resendSecondsLeft).isEqualTo(0)
    }

    @Test
    @DisplayName("a number Android verifies by itself finishes without typing a code")
    fun `a number Android verifies by itself finishes without typing a code`() = runTest(main.dispatcher) {
        repository.sendEvent = PhoneCodeEvent.Finished(Result.Success(Unit))
        val sut = createSut()

        sut.submit("1009876543")

        assertThat(sut.state.value.step).isEqualTo(ChangePhoneStep.UPDATED)
    }
}
