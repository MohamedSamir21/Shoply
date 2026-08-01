package com.shopping.shoply.authentication.presentation.resetpassword.forgotpassword

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.authentication.domain.usecase.auth.GetUserInfoUseCase
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("ForgotPasswordViewModel")
class ForgotPasswordViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val getUserInfoUseCase: GetUserInfoUseCase = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("masks the email and phone number once the user info loads")
    fun `masks the email and phone number once the user info loads`() =
        runTest(mainDispatcherExtension.dispatcher) {
            coEvery { getUserInfoUseCase() } returns Result.Success(
                User(
                    username = "mohamed",
                    email = "mohamed@gmail.com",
                    phoneNumber = "+201001234567",
                    isPhoneVerified = true
                )
            )
            val sut = ForgotPasswordViewModel(getUserInfoUseCase)

            // state is built with SharingStarted.Lazily, so the onStart { getUserInfo() } load
            // only fires once something collects it — start a collector, drain virtual time,
            // then read the settled value.
            val collectJob = launch { sut.state.collect { } }
            advanceUntilIdle()

            val loaded = sut.state.value
            assertThat(loaded.email).isEqualTo("mohamed@gmail.com")
            assertThat(loaded.phoneNumber).isEqualTo("+201001234567")
            assertThat(loaded.isPhoneVerified).isTrue()
            assertThat(loaded.resetMethods[0].message).isEqualTo("***amed@gmail.com")
            assertThat(loaded.resetMethods[1].message).isEqualTo("******1234567")

            collectJob.cancel()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("leaves the rest methods unmasked when loading the user info fails")
    fun `leaves the rest methods unmasked when loading the user info fails`() =
        runTest(mainDispatcherExtension.dispatcher) {
            coEvery { getUserInfoUseCase() } returns Result.Error(DataError.Unknown)
            val sut = ForgotPasswordViewModel(getUserInfoUseCase)

            val collectJob = launch { sut.state.collect { } }
            advanceUntilIdle()

            val state = sut.state.value
            assertThat(state.email).isNull()
            assertThat(state.resetMethods[0].message).isNull()
            assertThat(state.resetMethods[1].message).isNull()

            collectJob.cancel()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("toggles between the two reset methods")
    fun `toggles between the two reset methods`() = runTest(mainDispatcherExtension.dispatcher) {
        coEvery { getUserInfoUseCase() } returns Result.Error(DataError.Unknown)
        val sut = ForgotPasswordViewModel(getUserInfoUseCase)
        val collectJob = launch { sut.state.collect { } }
        advanceUntilIdle()

        sut.onSelectResetMethod()
        assertThat(sut.state.value.selectedResetMethodIndex).isEqualTo(1)

        sut.onSelectResetMethod()
        assertThat(sut.state.value.selectedResetMethodIndex).isEqualTo(0)

        collectJob.cancel()
    }
}
