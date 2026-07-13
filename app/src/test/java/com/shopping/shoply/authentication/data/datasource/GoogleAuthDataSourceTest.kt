package com.shopping.shoply.authentication.data.datasource

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.platform.ActivityProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

private typealias GoogleSuccess = Result.Success<Unit, DataError>
private typealias GoogleError = Result.Error<Unit, DataError>

@DisplayName("GoogleAuthDataSource")
class GoogleAuthDataSourceTest {

    // One dispatcher for BOTH the injected main + io seams and runTest, so the code under test
    // and the test share a single virtual clock. UnconfinedTestDispatcher runs eagerly.
    private val testDispatcher = UnconfinedTestDispatcher()
    private val credentialManager: CredentialManager = mockk()
    private val firebaseAuth: FirebaseAuth = mockk()
    private val activityProvider: ActivityProvider = mockk()

    private val sut = GoogleAuthDataSource(
        credentialManager = credentialManager,
        firebaseAuth = firebaseAuth,
        activityProvider = activityProvider,
        mainDispatcher = testDispatcher,
        ioDispatcher = testDispatcher
    )

    @BeforeEach
    fun setUp() {
        // createFrom() is a companion-object function (not a JVM static), so mock the companion;
        // GoogleAuthProvider.getCredential() is a genuine Java static, so mock the static.
        mockkObject(GoogleIdTokenCredential.Companion)
        mockkStatic(GoogleAuthProvider::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    @DisplayName("returns UNKNOWN when no Activity is available")
    fun `returns UNKNOWN when no Activity is available`() = runTest(testDispatcher) {
        every { activityProvider.current() } returns null

        val result = sut.signUpWithGoogle()

        assertThat(result).isEqualTo(GoogleError(DataError.Unknown))
    }

    @Test
    @DisplayName("returns Success when Firebase accepts the Google credential")
    fun `returns Success when Firebase accepts the Google credential`() = runTest(testDispatcher) {
        stubGoogleCredentialResponse()
        stubTokenExchange(user = mockk())

        val result = sut.signUpWithGoogle()

        assertThat(result).isEqualTo(GoogleSuccess(Unit))
    }

    @Test
    @DisplayName("returns UNKNOWN authentication error when Firebase returns no user")
    fun `returns UNKNOWN authentication error when Firebase returns no user`() =
        runTest(testDispatcher) {
            stubGoogleCredentialResponse()
            stubTokenExchange(user = null)

            val result = sut.signUpWithGoogle()

            assertThat(result).isEqualTo(GoogleError(DataError.Authentication.UNKNOWN))
        }

    @Test
    @DisplayName("returns UNKNOWN authentication error when the credential is not a Google ID token")
    fun `returns UNKNOWN authentication error when the credential is not a Google ID token`() =
        runTest(testDispatcher) {
            every { activityProvider.current() } returns mockk<Activity>()
            val credential = CustomCredential("some.other.credential.type", mockk<Bundle>())
            val response =
                mockk<GetCredentialResponse> { every { this@mockk.credential } returns credential }
            coEvery {
                credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
            } returns response

            val result = sut.signUpWithGoogle()

            assertThat(result).isEqualTo(GoogleError(DataError.Authentication.UNKNOWN))
        }

    @Test
    @DisplayName("returns UNKNOWN authentication error when the google credential parsing fails")
    fun `returns UNKNOWN authentication error when the google credential parsing fails`() =
        runTest(testDispatcher) {
            stubGoogleCredentialResponse()
            every { GoogleIdTokenCredential.createFrom(any()) } throws GoogleIdTokenParsingException(
                Throwable("Parsing failed!")
            )

            val result = sut.signUpWithGoogle()

            assertThat(result).isEqualTo(GoogleError(DataError.Authentication.UNKNOWN))
        }

    @Test
    @DisplayName("returns UNKNOWN when the user cancels the account picker")
    fun `returns UNKNOWN when the user cancels the account picker`() = runTest(testDispatcher) {
        every { activityProvider.current() } returns mockk<Activity>()
        coEvery {
            credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
        } throws GetCredentialCancellationException()

        val result = sut.signUpWithGoogle()

        assertThat(result).isEqualTo(GoogleError(DataError.Unknown))
    }

    @Test
    @DisplayName("maps a Firebase error code to its authentication error")
    fun `maps a Firebase error code to its authentication error`() = runTest(testDispatcher) {
        stubGoogleCredentialResponse()
        every { GoogleIdTokenCredential.createFrom(any()) } returns mockk<GoogleIdTokenCredential>(
            relaxed = true
        )
        every { GoogleAuthProvider.getCredential(any(), any()) } returns mockk<AuthCredential>()
        val errorCode = "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL"
        every { firebaseAuth.signInWithCredential(any()) } returns Tasks.forException(
            FirebaseAuthException(errorCode, "")
        )

        val result = sut.signUpWithGoogle()

        assertThat(result).isEqualTo(
            GoogleError(
                DataError.Authentication.ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL
            )
        )
    }


    @Test
    @DisplayName("returns Network error when Firebase goes down")
    fun `returns Network error when Firebase goes down`() = runTest(testDispatcher) {
        stubGoogleCredentialResponse()
        every { GoogleIdTokenCredential.createFrom(any()) } returns mockk<GoogleIdTokenCredential>(
            relaxed = true
        )
        every { GoogleAuthProvider.getCredential(any(), any()) } returns mockk<AuthCredential>()
        every { firebaseAuth.signInWithCredential(any()) } returns Tasks.forException(
            FirebaseNetworkException("Firebase went down")
        )

        val result = sut.signUpWithGoogle()

        assertThat(result).isEqualTo(GoogleError(DataError.Network))
    }

    @Test
    @DisplayName("returns UNKNOWN when there's no credential")
    fun `returns UNKNOWN when there's no credential`() = runTest(testDispatcher) {
        every { activityProvider.current() } returns mockk<Activity>()
        coEvery {
            credentialManager.getCredential(
                any<Context>(),
                any<GetCredentialRequest>()
            )
        } throws NoCredentialException()

        val result = sut.signUpWithGoogle()

        assertThat(result).isEqualTo(GoogleError(DataError.Unknown))
    }

    // getCredential() returns a valid Google ID-token credential response.
    private fun stubGoogleCredentialResponse() {
        every { activityProvider.current() } returns mockk<Activity>()
        val credential = CustomCredential(
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
            mockk<Bundle>()
        )
        val response =
            mockk<GetCredentialResponse> { every { this@mockk.credential } returns credential }
        coEvery {
            credentialManager.getCredential(any<Context>(), any<GetCredentialRequest>())
        } returns response
    }

    // The token exchange: parse the ID token, build a Firebase credential, sign in with `user`.
    private fun stubTokenExchange(user: FirebaseUser?) {
        every { GoogleIdTokenCredential.createFrom(any()) } returns
                mockk { every { idToken } returns "id-token" }
        every { GoogleAuthProvider.getCredential(any(), any()) } returns mockk()
        val authResult = mockk<AuthResult> { every { this@mockk.user } returns user }
        every { firebaseAuth.signInWithCredential(any()) } returns Tasks.forResult(authResult)
    }
}
