package com.shopping.shoply.authentication.domain.usecase.validate

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.domain.model.SignInData
import com.shopping.shoply.authentication.domain.model.SignUpData
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@DisplayName("CheckInputsFilledUseCase")
class CheckInputsFilledUseCaseTest {
    private val checkFilling = CheckInputsFilledUseCase()

    @ParameterizedTest(name = "rejects {0}")
    @MethodSource("provideSignUpTestData")
    @DisplayName("returns false if not all sign up data aren't filled")
    fun `returns if not all sign up data aren't filled`(
        signUpData: SignUpData
    ) = runTest {
        assertThat(checkFilling(signUpData)).isFalse()
    }

    @Test
    @DisplayName("returns true if all sign up data are filled")
    fun `returns true if all sign up data are filled`() = runTest {
        val signUpData = SignUpData(
            username = "username",
            email = "user@gmail.com",
            phoneNumber = "01234",
            password = "password",
            confirmedPassword = "password"
        )

        assertThat(checkFilling(signUpData)).isTrue()
    }

    @ParameterizedTest(name = "rejects {0}")
    @MethodSource("provideSignInTestData")
    @DisplayName("returns false if not all sign in data aren't filled")
    fun `returns false if not all sign in data aren't filled`(
        signInData: SignInData
    ) = runTest {
        assertThat(checkFilling(signInData)).isFalse()
    }

    @Test
    @DisplayName("returns true if all sign in data are filled")
    fun `returns true if all sign in data are filled`() = runTest {
        val signInData = SignInData(
            email = "user@gmail.com",
            password = "password"
        )

        assertThat(checkFilling(signInData)).isTrue()
    }

    companion object {

        @JvmStatic
        fun provideSignUpTestData(): List<SignUpData> = listOf(
            SignUpData(
                username = " ",
                email = " ",
                phoneNumber = " ",
                password = " ",
                confirmedPassword = " ",
            ),
            SignUpData(
                username = "",
                email = "",
                phoneNumber = "",
                password = "",
                confirmedPassword = "",
            ),
            SignUpData(
                username = " ",
                email = "user@gmail.com",
                phoneNumber = "01234",
                password = "password",
                confirmedPassword = "password",
            ),
        )

        @JvmStatic
        fun provideSignInTestData(): List<SignInData> = listOf(
            SignInData(
                email = " ",
                password = " "
            ),
            SignInData(
                email = "",
                password = ""
            ),
            SignInData(
                email = " ",
                password = "password"
            )
        )
    }
}