package com.shopping.shoply.authentication.data.datasource

import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

private typealias FacebookSuccess = Result.Success<Unit, DataError>
private typealias FacebookError = Result.Error<Unit, DataError>

@DisplayName("FacebookAuthDataSource")
class FacebookAuthDataSourceTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val loginManager: LoginManager = mockk(relaxed = true)
    private val callbackManager: CallbackManager = mockk()
    private val firebaseAuth: FirebaseAuth = mockk()

    private val sut = FacebookAuthDataSource(
        loginManager = loginManager,
        callbackManager = callbackManager,
        firebaseAuth = firebaseAuth
    )

    @BeforeEach
    fun setUp() {
        // FacebookAuthProvider.getCredential() is a genuine Java static.
        mockkStatic(FacebookAuthProvider::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    @DisplayName("returns UNKNOWN when the user cancels the Facebook login")
    fun `returns UNKNOWN when the user cancels the Facebook login`() = runTest(testDispatcher) {
        val callback = captureLoginCallback()

        val deferred = async { sut.signUpWithFacebook() }   // parks on the continuation
        callback.captured.onCancel()                         // unpark it by hand

        assertThat(deferred.await()).isEqualTo(FacebookError(DataError.Unknown))
    }

    @Test
    @DisplayName("returns Success when Firebase accepts the Facebook credential")
    fun `returns Success when Firebase accepts the Facebook credential`() =
        runTest(testDispatcher) {
            val callback = captureLoginCallback()
            stubTokenExchange(successful = true)

            val deferred = async { sut.signUpWithFacebook() }
            callback.captured.onSuccess(loginResultWithToken())

            assertThat(deferred.await()).isEqualTo(FacebookSuccess(Unit))
        }

    @Test
    @DisplayName("returns UNKNOWN error when something wrong happens")
    fun `returns UNKNOWN error when something wrong happens`() = runTest(testDispatcher) {
        val callback = captureLoginCallback()

        val deferred = async { sut.signUpWithFacebook() }
        callback.captured.onError(FacebookException("Something bad happened!"))

        assertThat(deferred.await()).isEqualTo(FacebookError(DataError.Authentication.UNKNOWN))
    }

    @Test
    @DisplayName("maps a firebase error code to its authentication error")
    fun `maps a firebase error code to its authentication error`() = runTest(testDispatcher) {
        val callback = captureLoginCallback()
        stubTokenExchange(
            successful = false, exception = FirebaseAuthException(
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL",
                ""
            )
        )

        val deferred = async { sut.signUpWithFacebook() }
        callback.captured.onSuccess(loginResultWithToken())

        assertThat(deferred.await()).isEqualTo(
            FacebookError(DataError.Authentication.ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL)
        )
    }

    @Test
    @DisplayName("returns Network error when firebase goes down")
    fun `returns Network error when firebase goes down`() = runTest(testDispatcher) {
        val callback = captureLoginCallback()
        stubTokenExchange(successful = false, exception = FirebaseNetworkException(""))

        val deferred = async { sut.signUpWithFacebook() }
        callback.captured.onSuccess(loginResultWithToken())

        assertThat(deferred.await()).isEqualTo(
            FacebookError(
                DataError.Network
            )
        )
    }

    @Test
    @DisplayName("verifies that the facebook callback is always unregistered")
    fun `verifies that the facebook callback is always unregistered`() = runTest(testDispatcher) {
        val callback = captureLoginCallback()

        val deferred = async { sut.signUpWithFacebook() }
        callback.captured.onCancel()

        assertThat(deferred.await()).isEqualTo(
            FacebookError(DataError.Unknown)
        )
        verify(exactly = 1) { loginManager.unregisterCallback(callbackManager) }
    }

    // Captures the FacebookCallback the SUT registers so the test can fire its methods by hand.
    private fun captureLoginCallback(): CapturingSlot<FacebookCallback<LoginResult>> {
        val callback = slot<FacebookCallback<LoginResult>>()
        every { loginManager.registerCallback(callbackManager, capture(callback)) } just Runs
        return callback
    }

    // The Firebase exchange: build a credential from the Facebook token, then fire the
    // signInWithCredential completion listener by hand. The SUT uses addOnCompleteListener (not
    // .await()), so we drive the listener rather than return a real Task — a real completed Task
    // would post to the absent main-thread executor and never fire in a host test.
    private fun stubTokenExchange(successful: Boolean, exception: Exception? = null) {
        every { FacebookAuthProvider.getCredential(any()) } returns mockk<AuthCredential>()
        val task = mockk<Task<AuthResult>> {
            every { isSuccessful } returns successful
            if (!successful) every { this@mockk.exception } returns exception
        }
        every { firebaseAuth.signInWithCredential(any()) } returns task
        every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
            firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
            task
        }
    }

    // A LoginResult whose accessToken exposes a non-null token string.
    private fun loginResultWithToken(): LoginResult =
        mockk { every { accessToken } returns mockk<AccessToken> { every { token } returns "fb-token" } }
}