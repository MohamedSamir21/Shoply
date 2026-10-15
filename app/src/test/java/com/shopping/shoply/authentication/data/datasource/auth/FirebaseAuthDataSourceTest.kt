package com.shopping.shoply.authentication.data.datasource.auth

import app.cash.turbine.test
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.shopping.shoply.authentication.data.dto.UserDto
import com.shopping.shoply.authentication.data.mapper.toUserDto
import com.shopping.shoply.authentication.domain.model.SendOtpEvent
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.platform.ActivityProvider
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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.IOException

private typealias SuccessResult = Result.Success<Unit, DataError>
private typealias ErrorResult = Result.Error<Unit, DataError>

@DisplayName("FirebaseAuthDataSource")
class FirebaseAuthDataSourceTest {

    // One TestDispatcher, injected into the SUT AND used to drive runTest, so the code under test and
    // the test share a single virtual clock (withTimeout uses virtual time, not real seconds).
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    // Collaborators are mocks. `relaxed = true` returns sensible defaults for any call we don't stub.
    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val auth: FirebaseAuth = mockk()
    private val activityProvider: ActivityProvider = mockk(relaxed = true)

    private val sut = FirebaseAuthDataSource(
        firestore = firestore,
        auth = auth,
        activityProvider = activityProvider,
        ioDispatcher = testDispatcher
    )

    @Nested
    @DisplayName("sendPasswordResetEmail")
    inner class SendPasswordResetEmail {

        @Test
        @DisplayName("returns Success when Firebase accepts the request")
        fun `returns Success when Firebase accepts the request`() = runTest(testDispatcher) {
            val email = "user@gmail.com"
            // A completed Task, so the method's internal .await() resolves with no exception.
            every { auth.sendPasswordResetEmail(email) } returns Tasks.forResult(null)

            val result = sut.sendPasswordResetEmail(email)

            assertThat(result).isEqualTo(SuccessResult(Unit))
        }

        @Test
        @DisplayName("returns an Unknown error when Firebase rejects the request")
        fun `returns Unknown error when Firebase fails`() = runTest(testDispatcher) {
            val email = "user@gmail.com"
            // A failed Task: .await() rethrows the exception, which the method maps to Unknown.
            every { auth.sendPasswordResetEmail(email) } returns Tasks.forException(IOException("network down"))

            val result = sut.sendPasswordResetEmail(email)

            assertThat(result).isEqualTo(ErrorResult(DataError.Unknown))
        }
    }

    @Nested
    @DisplayName("checkIfEmailExists")
    inner class CheckIfEmailExists {

        private val email = "user@gmail.com"
        private val snapshot: QuerySnapshot = mockk()

        // Stub the whole Firestore fluent chain in one shot; MockK auto-creates the intermediate
        // CollectionReference/Query mocks. The terminal .get() must return a REAL completed Task —
        // a relaxed mock Task would never invoke await()'s listener, hanging the test forever.
        private fun stubQueryReturns(email: String, task: Task<QuerySnapshot>) {
            every {
                firestore.collection(FirebaseAuthDataSource.USER_COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
            } returns task
        }

        @Test
        @DisplayName("returns Success when a matching document exists")
        fun `returns Success when a matching document exists`() = runTest(testDispatcher) {
            stubQueryReturns(email, task = Tasks.forResult(snapshot))
            every { snapshot.isEmpty } returns false

            val result = sut.checkIfEmailExists(email)

            assertThat(result).isEqualTo(SuccessResult(Unit))
        }

        @Test
        @DisplayName("returns EMAIL_DOES_NOT_EXIST when no document matches")
        fun `returns EMAIL_DOES_NOT_EXIST when no document matches`() = runTest(testDispatcher) {
            stubQueryReturns(email, task = Tasks.forResult(snapshot))
            every { snapshot.isEmpty } returns true

            val result = sut.checkIfEmailExists(email)

            assertThat(result).isEqualTo(
                ErrorResult(DataError.Authentication.EMAIL_DOES_NOT_EXIST)
            )
        }

        @Test
        @DisplayName("returns an Unknown error when the query fails")
        fun `returns Unknown error when the query fails`() = runTest(testDispatcher) {
            stubQueryReturns(email, task = Tasks.forException(IOException("firestore down")))

            val result = sut.checkIfEmailExists(email)

            assertThat(result).isEqualTo(ErrorResult(DataError.Unknown))
        }
    }

    @Nested
    @DisplayName("markPhoneNumberVerified")
    inner class MarkPhoneNumberVerified {

        private val uid = "uid-123"
        private val firebaseUser: FirebaseUser = mockk()

        // Stubbed link-by-link (rather than one chained every) so we hold a named `document` mock to
        // verify the write against.
        private val collection: CollectionReference = mockk()
        private val document: DocumentReference = mockk()

        @Test
        @DisplayName("flags the phone as verified in Firestore and returns Success")
        fun `flags the phone verified in Firestore and returns Success`() =
            runTest(testDispatcher) {
                every { auth.currentUser } returns firebaseUser
                every { firebaseUser.uid } returns uid
                every { firestore.collection(FirebaseAuthDataSource.USER_COLLECTION_NAME) } returns collection
                every { collection.document(uid) } returns document
                every { document.update("phoneVerified", true) } returns Tasks.forResult(null)

                val result = sut.markPhoneNumberVerified()

                assertThat(result).isEqualTo(SuccessResult(Unit))
                // This method exists to perform the write — assert it happened, once, with the right args.
                verify(exactly = 1) { document.update("phoneVerified", true) }
            }

        @Test
        @DisplayName("returns UNKNOWN and writes nothing when no user is signed in")
        fun `returns UNKNOWN and writes nothing when no user is signed in`() =
            runTest(testDispatcher) {
                every { auth.currentUser } returns null

                val result = sut.markPhoneNumberVerified()

                assertThat(result).isEqualTo(
                    ErrorResult(DataError.Authentication.UNKNOWN)
                )
                // The guard clause must short-circuit before Firestore is ever touched.
                verify(exactly = 0) { firestore.collection(any()) }
            }

        @Test
        @DisplayName("returns Unknown when the Firestore write fails")
        fun `returns Unknown when the Firestore write fails`() = runTest(testDispatcher) {
            every { auth.currentUser } returns firebaseUser
            every { firebaseUser.uid } returns uid
            every { firestore.collection(FirebaseAuthDataSource.USER_COLLECTION_NAME) } returns collection
            every { collection.document(uid) } returns document
            every { document.update("phoneVerified", true) } returns Tasks.forException(
                IOException(
                    "write failed"
                )
            )

            val result = sut.markPhoneNumberVerified()

            assertThat(result).isEqualTo(ErrorResult(DataError.Unknown))
            // The write path is the one under test, so the update must actually have been attempted.
            verify(exactly = 1) { document.update("phoneVerified", true) }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        @DisplayName("returns Network when the write times out")
        fun `returns Network when the write times out`() = runTest(testDispatcher) {
            every { auth.currentUser } returns firebaseUser
            every { firebaseUser.uid } returns uid
            every { firestore.collection(FirebaseAuthDataSource.USER_COLLECTION_NAME) } returns collection
            every { collection.document(uid) } returns document
            // A Task that never completes, so await() suspends until withTimeout(5000L) fires.
            every {
                document.update(
                    "phoneVerified",
                    true
                )
            } returns TaskCompletionSource<Void>().task

            // Run the call concurrently so we can advance the virtual clock while it's suspended.
            val deferred = async { sut.markPhoneNumberVerified() }
            advanceTimeBy(5_001) // push virtual time just past the 5s timeout
            runCurrent()         // let the timeout fire and the error mapping run

            assertThat(deferred.await()).isEqualTo(ErrorResult(DataError.Network))
        }
    }

    @Nested
    @DisplayName("signIn")
    inner class SignIn {

        private val email = "user@gmail.com"
        private val password = "p4ssword"

        // signIn doesn't await() a Task — it registers listeners and resumes from the first one that
        // fires. So we mock the Task itself and, via answers { }, invoke the exact listener we want.
        private val task: Task<AuthResult> = mockk()

        @Test
        @DisplayName("returns Success when the completion listener reports success")
        fun `returns Success when the completion listener reports success`() =
            runTest(testDispatcher) {
                every { auth.signInWithEmailAndPassword(email, password) } returns task
                every { task.isSuccessful } returns true
                // Fire the completion listener the SUT registers; return the Task so `.addOnFailureListener`
                // still has something to chain onto.
                every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
                    firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
                    task
                }
                every { task.addOnFailureListener(any<OnFailureListener>()) } returns task

                val result = sut.signIn(email, password)

                assertThat(result).isEqualTo(SuccessResult(Unit))
            }

        @Test
        @DisplayName("returns UNKNOWN when the failure listener fires")
        fun `returns UNKNOWN when the failure listener fires`() = runTest(testDispatcher) {
            every { auth.signInWithEmailAndPassword(email, password) } returns task
            // Do NOT fire the completion listener — only the failure path, so this branch is exercised
            // in isolation (a real failed Task would fire onComplete first and shadow it).
            every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } returns task
            every { task.addOnFailureListener(any<OnFailureListener>()) } answers {
                firstArg<OnFailureListener>().onFailure(Exception("network down"))
                task
            }

            val result = sut.signIn(email, password)

            assertThat(result).isEqualTo(ErrorResult(DataError.Authentication.UNKNOWN))
        }

        @Test
        @DisplayName("returns INVALID_CREDENTIALS when the completion listener reports failure")
        fun `returns INVALID_CREDENTIALS when the completion listener reports failure`() =
            runTest(testDispatcher) {
                every { auth.signInWithEmailAndPassword(email, password) } returns task
                every { task.isSuccessful } returns false
                every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
                    firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
                    task
                }
                every { task.addOnFailureListener(any<OnFailureListener>()) } returns task

                val result = sut.signIn(email, password)

                assertThat(result).isEqualTo(ErrorResult(DataError.Authentication.INVALID_CREDENTIALS))
            }
    }

    @Nested
    @DisplayName("sendOTPCode")
    inner class SendOTPCode {

        private val countryCode = "20"
        private val phoneNumber = "1001234567"
        private val credential: PhoneAuthCredential = mockk()
        private val task: Task<AuthResult> = mockk()

        // sendOTPCode builds a PhoneAuthOptions and calls the STATIC PhoneAuthProvider.verifyPhoneNumber.
        // We can't await anything here — the SUT hands Firebase a callbacks object and Firebase calls
        // back later. So we capture that callbacks object and fire its methods ourselves to drive the flow.
        private val builder: PhoneAuthOptions.Builder = mockk()
        private val callbacks = slot<PhoneAuthProvider.OnVerificationStateChangedCallbacks>()

        @BeforeEach
        fun stubPhoneAuth() {
            // mockkStatic replaces the static methods on these classes for the duration of the test.
            mockkStatic(PhoneAuthOptions::class, PhoneAuthProvider::class)
            // Each builder step returns the same mock, so the whole fluent chain lands on one object
            // we control — and setCallbacks captures the argument into `callbacks`.
            every { PhoneAuthOptions.newBuilder(auth) } returns builder
            every { builder.setPhoneNumber(any()) } returns builder
            every { builder.setCallbacks(capture(callbacks)) } returns builder
            every { builder.setTimeout(any(), any()) } returns builder
            every { builder.setActivity(any()) } returns builder
            every { builder.build() } returns mockk(relaxed = true)
            // Static, returns void: the real SDK would fire off an SMS — we make it a no-op.
            every { PhoneAuthProvider.verifyPhoneNumber(any<PhoneAuthOptions>()) } just Runs
        }

        @AfterEach
        fun releaseStaticMocks() = unmockkAll()

        @Test
        @DisplayName("emits CodeSent once Firebase reports the SMS was sent")
        fun `emits CodeSent when onCodeSent fires`() = runTest(testDispatcher) {
            sut.sendOTPCode(countryCode, phoneNumber, link = false).test {
                // Collection has started, so the SUT has registered its callbacks with Firebase.
                // Simulate Firebase delivering the code by firing onCodeSent ourselves.
                callbacks.captured.onCodeSent("verification-id", mockk())

                assertThat(awaitItem()).isEqualTo(SendOtpEvent.CodeSent)
                // The callbackFlow stays open (awaitClose), so end the collection explicitly.
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        @DisplayName("emits AutoVerified when Firebase auto-verifies the user without linking")
        fun `emits AutoVerified when onVerificationCompleted fires without linking`() =
            runTest(testDispatcher) {
                every { auth.signInWithCredential(credential) } returns task
                every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
                    firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
                    task
                }
                every { task.isSuccessful } returns true

                sut.sendOTPCode(countryCode, phoneNumber, link = false).test {
                    callbacks.captured.onVerificationCompleted(credential)

                    assertThat(awaitItem()).isEqualTo(
                        SendOtpEvent.AutoVerified(
                            SuccessResult(Unit)
                        )
                    )
                    cancelAndIgnoreRemainingEvents()
                }
            }
    }

    @Nested
    @DisplayName("verifyOTPCode")
    inner class VerifyOTPCode {

        private val code = "123456"
        private val credential: PhoneAuthCredential = mockk()
        private val task: Task<AuthResult> = mockk()

        @AfterEach
        fun releaseStaticMocks() = unmockkAll()

        @Test
        @DisplayName("returns Success when the credential sign-in completes")
        fun `returns Success when the credential sign-in completes`() = runTest(testDispatcher) {
            // Seed the id through the @VisibleForTesting seam, so this test is solitary — no need to
            // run sendOTPCode first just to populate verificationId.
            sut.verificationId = "verification-id"
            // getCredential is a STATIC PhoneAuthProvider call.
            mockkStatic(PhoneAuthProvider::class)
            every { PhoneAuthProvider.getCredential("verification-id", code) } returns credential
            every { auth.signInWithCredential(credential) } returns task
            every { task.isSuccessful } returns true
            // singInWithCredential() registers a completion listener — fire it ourselves.
            every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
                firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
                task
            }

            val result = sut.verifyOTPCode(code)

            assertThat(result).isEqualTo(SuccessResult(Unit))
        }

        @Test
        @DisplayName("returns UNKNOWN when the verificationId is null")
        fun `returns UNKNOWN when the verificationId is null`() = runTest(testDispatcher) {
            sut.verificationId = null

            val result = sut.verifyOTPCode(code)

            assertThat(result).isEqualTo(ErrorResult(DataError.Authentication.UNKNOWN))
            verify(exactly = 0) { auth.signInWithCredential(any()) }
        }

        @Test
        @DisplayName("returns UNKNOWN when the credential sign-in is rejected")
        fun `returns UNKNOWN when the credential sign-in is rejected`() = runTest(testDispatcher) {
            sut.verificationId = "verification-id"
            mockkStatic(PhoneAuthProvider::class)
            every { PhoneAuthProvider.getCredential("verification-id", code) } returns credential
            every { auth.signInWithCredential(credential) } returns task
            every { task.isSuccessful } returns false
            every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
                firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
                task
            }
            every { task.exception } returns null

            val result = sut.verifyOTPCode(code)

            assertThat(result).isEqualTo(ErrorResult(DataError.Authentication.UNKNOWN))
        }
    }

    @Nested
    @DisplayName("linkPhoneNumber")
    inner class LinkPhoneNumber {

        private val code = "123456"
        private val credential: PhoneAuthCredential = mockk()
        private val currentUser: FirebaseUser = mockk()
        private val task: Task<AuthResult> = mockk()

        @AfterEach
        fun releaseStaticMocks() = unmockkAll()

        @Test
        @DisplayName("returns PHONE_NUMBER_ALREADY_IN_USE when the number is linked to another account")
        fun `returns PHONE_NUMBER_ALREADY_IN_USE on a collision`() = runTest(testDispatcher) {
            sut.verificationId = "verification-id"
            mockkStatic(PhoneAuthProvider::class)
            every { PhoneAuthProvider.getCredential("verification-id", code) } returns credential
            // linkWithCredential attaches the credential to the signed-in user, so it must be non-null.
            every { auth.currentUser } returns currentUser
            every { currentUser.linkWithCredential(credential) } returns task
            every { task.isSuccessful } returns false
            // The failure branch does `when (task.exception) { is FirebaseAuthUserCollisionException -> ... }`;
            // a relaxed mock of that type satisfies the `is` check AND the `?.message` log without extra stubs.
            every { task.exception } returns mockk<FirebaseAuthUserCollisionException>(relaxed = true)
            every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
                firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
                task
            }

            val result = sut.linkPhoneNumber(code)

            assertThat(result).isEqualTo(
                ErrorResult(DataError.Authentication.PHONE_NUMBER_ALREADY_IN_USE)
            )
        }

        @Test
        @DisplayName("returns Success when the phone number is linked")
        fun `returns Success when the phone number is linked`() = runTest(testDispatcher) {
            sut.verificationId = "verification-id"
            mockkStatic(PhoneAuthProvider::class)
            every { PhoneAuthProvider.getCredential("verification-id", code) } returns credential
            every { auth.currentUser } returns currentUser
            every { currentUser.linkWithCredential(credential) } returns task
            every { task.addOnCompleteListener(any<OnCompleteListener<AuthResult>>()) } answers {
                firstArg<OnCompleteListener<AuthResult>>().onComplete(task)
                task
            }
            every { task.isSuccessful } returns true

            val result = sut.linkPhoneNumber(code)

            assertThat(result).isEqualTo(SuccessResult(Unit))
        }

        @Test
        @DisplayName("returns UNKNOWN when the user isn't signed in")
        fun `returns UNKNOWN when the user isn't signed in`() = runTest(testDispatcher) {
            sut.verificationId = "verification-id"
            mockkStatic(PhoneAuthProvider::class)
            every { PhoneAuthProvider.getCredential("verification-id", code) } returns credential
            every { auth.currentUser } returns null

            val result = sut.linkPhoneNumber(code)

            assertThat(result).isEqualTo(ErrorResult(DataError.Authentication.UNKNOWN))

        }
    }


    @Nested
    @DisplayName("checkIfPhoneNumberExists")
    inner class CheckIfPhoneNumberExists {
        private val phoneNumber = "+2001208210613"
        private val documentsQuery: QuerySnapshot = mockk()
        private val documentSnapshot: DocumentSnapshot = mockk()

        @Test
        @DisplayName("returns PHONE_NUMBER_DOES_NOT_EXIST when no document matches")
        fun `returns PHONE_NUMBER_DOES_NOT_EXIST when no document matches`() =
            runTest(testDispatcher) {
                stubQueryReturns(task = Tasks.forResult(documentsQuery))
                every { documentsQuery.documents } returns emptyList()

                val result = sut.checkIfPhoneNumberExists(phoneNumber)

                assertThat(result).isEqualTo(
                    ErrorResult(
                        DataError.Authentication.PHONE_NUMBER_DOES_NOT_EXIST
                    )
                )
            }

        @Test
        @DisplayName("returns Success when the phone number is linked")
        fun `returns Success when the phone number is linked`() = runTest(testDispatcher) {
            stubQueryReturns(task = Tasks.forResult(documentsQuery))
            every { documentsQuery.documents } returns listOf(documentSnapshot)
            every { documentSnapshot.getBoolean("phoneVerified") } returns true

            val result = sut.checkIfPhoneNumberExists(phoneNumber)

            assertThat(result).isEqualTo(
                SuccessResult(Unit)
            )
        }

        @Test
        @DisplayName("returns PHONE_NUMBER_NOT_LINKED when the phone number is not linked")
        fun `returns PHONE_NUMBER_NOT_LINKED when the phone number is not linked`() =
            runTest(testDispatcher) {
                stubQueryReturns(task = Tasks.forResult(documentsQuery))
                every { documentsQuery.documents } returns listOf(documentSnapshot)
                every { documentSnapshot.getBoolean("phoneVerified") } returns false

                val result = sut.checkIfPhoneNumberExists(phoneNumber)

                assertThat(result).isEqualTo(
                    ErrorResult(DataError.Authentication.PHONE_NUMBER_NOT_LINKED)
                )
            }

        @Test
        @DisplayName("returns Unknown error when Firebase fails")
        fun `returns Unknown error when Firebase fails`() = runTest(testDispatcher) {
            stubQueryReturns(Tasks.forException(IOException("Firestore went down")))

            val result = sut.checkIfPhoneNumberExists(phoneNumber)

            assertThat(result).isEqualTo(
                ErrorResult(DataError.Unknown)
            )
        }

        private fun stubQueryReturns(task: Task<QuerySnapshot>) {
            every {
                firestore.collection(FirebaseAuthDataSource.USER_COLLECTION_NAME)
                    .whereEqualTo("phoneNumber", phoneNumber)
                    .limit(1)
                    .get()
            } returns task
        }
    }

    @Nested
    @DisplayName("updatePassword")
    inner class UpdatePassword {
        private val newPassword = "ffs92M.LMf+92"
        private val firebaseUser: FirebaseUser = mockk()

        @Test
        @DisplayName("returns UNKNOWN when the user isn't signed-in")
        fun `returns UNKNOWN when the user isn't signed-in`() = runTest(testDispatcher) {
            every { auth.currentUser } returns null

            val result = sut.updatePassword(newPassword)

            assertThat(result).isEqualTo(
                ErrorResult(DataError.Authentication.UNKNOWN)
            )
        }

        @Test
        @DisplayName("returns Success when the password is updated successfully")
        fun `returns Success when the password is updated successfully`() =
            runTest(testDispatcher) {
                every { auth.currentUser } returns firebaseUser
                every { firebaseUser.updatePassword(newPassword) } returns Tasks.forResult(null)

                val result = sut.updatePassword(newPassword)

                assertThat(result).isEqualTo(
                    SuccessResult(Unit)
                )
            }

        @Test
        @DisplayName("returns WEAK_PASSWORD when the password is weak")
        fun `returns WEAK_PASSWORD when the password is weak`() = runTest(testDispatcher) {
            stubUpdatePasswordFail(exception = mockk<FirebaseAuthWeakPasswordException>(relaxed = true))

            val result = sut.updatePassword(newPassword)

            assertThat(result).isEqualTo(ErrorResult(DataError.Authentication.WEAK_PASSWORD))
        }

        @Test
        @DisplayName("returns INVALID_CREDENTIALS when there are invalid credentials")
        fun `returns INVALID_CREDENTIALS when there are invalid credentials`() =
            runTest(testDispatcher) {
                stubUpdatePasswordFail(mockk<FirebaseAuthRecentLoginRequiredException>(relaxed = true))

                val result = sut.updatePassword(newPassword)

                assertThat(result).isEqualTo(
                    ErrorResult(DataError.Authentication.INVALID_CREDENTIALS)
                )
            }

        @Test
        @DisplayName("maps a Firebase error code to its authentication error")
        fun `maps a Firebase error code to its authentication error`() = runTest(testDispatcher) {
            val exception = mockk<FirebaseAuthException>(relaxed = true)
            stubUpdatePasswordFail(exception)
            every { exception.errorCode } returns "ERROR_INVALID_EMAIL"

            val result = sut.updatePassword(newPassword)

            assertThat(result).isEqualTo(
                ErrorResult(DataError.Authentication.INVALID_EMAIL)
            )
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        @DisplayName("returns Network error when the time is out")
        fun `returns Network error when the time is out`() = runTest(testDispatcher) {
            every { auth.currentUser } returns firebaseUser
            every { firebaseUser.updatePassword(newPassword) } returns TaskCompletionSource<Void>().task

            val result = async { sut.updatePassword(newPassword) }
            advanceTimeBy(20001L)
            runCurrent()

            assertThat(result.await()).isEqualTo(
                ErrorResult(DataError.Network)
            )
        }

        @Test
        @DisplayName("returns UNKNOWN error for uncommon errors")
        fun `returns UNKNOWN error for uncommon errors`() = runTest(testDispatcher) {
            stubUpdatePasswordFail(Exception("Unknown error"))

            val result = sut.updatePassword(newPassword)

            assertThat(result).isEqualTo(
                ErrorResult(DataError.Unknown)
            )
        }

        private fun stubUpdatePasswordFail(exception: Exception) {
            every { auth.currentUser } returns firebaseUser
            every { firebaseUser.updatePassword(newPassword) } returns Tasks.forException(exception)
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("getUserInfo")
    inner class GetUserInfo {
        private val firebaseUser = mockk<FirebaseUser>()
        private val email = "user@gmail.com"
        private val phoneNumber = "+2001208210613"
        private val user = User(
            email = email,
            phoneNumber = phoneNumber
        )
        private val uid = "uid-123"
        private val documentSnapshot = mockk<DocumentSnapshot>()

        @Test
        @DisplayName("returns UNKNOWN when the user is not signed")
        fun `returns UNKNOWN when the user is not signed`() = runTest(testDispatcher) {
            every { auth.currentUser } returns null

            val result = sut.getUserInfo()

            assertThat(result).isEqualTo(
                ErrorResult(DataError.Authentication.UNKNOWN)
            )
        }

        @Test
        @DisplayName("returns UNKNOWN when the user info isn't in firestore")
        fun `returns UNKNOWN when the user info isn't in firestore`() = runTest(testDispatcher) {
            every { auth.currentUser } returns firebaseUser
            every { firebaseUser.email } returns null
            every { firebaseUser.phoneNumber } returns null
            stubUserInfo(null)

            val result = sut.getUserInfo()

            assertThat(result).isEqualTo(
                ErrorResult(DataError.Authentication.UNKNOWN)
            )
        }

        @Test
        @DisplayName("returns Success when the user info is available only in firebase user")
        fun `returns Success when the user info is available only in firebase user`() =
            runTest(testDispatcher) {
                every { auth.currentUser } returns firebaseUser
                every { firebaseUser.email } returns email
                every { firebaseUser.phoneNumber } returns phoneNumber
                stubUserInfo(null)

                val result = sut.getUserInfo()

                assertThat(result).isEqualTo(
                    Result.Success<User, DataError>(
                        user
                    )
                )
            }

        @Test
        @DisplayName("returns Success when the user info is available only in firestore")
        fun `returns Success when the user info is available only in firestore`() =
            runTest(testDispatcher) {
                every { auth.currentUser } returns firebaseUser
                every { firebaseUser.email } returns null
                every { firebaseUser.phoneNumber } returns null
                stubUserInfo(user.toUserDto())

                val result = sut.getUserInfo()

                assertThat(result).isEqualTo(
                    Result.Success<User, DataError>(
                        user
                    )
                )
            }

        @Test
        @DisplayName("prefers the firebase user info over firestore when both are present")
        fun `prefers the firebase user info over firestore when both are present`() =
            runTest(testDispatcher) {
                every { auth.currentUser } returns firebaseUser
                every { firebaseUser.email } returns email
                every { firebaseUser.phoneNumber } returns phoneNumber
                stubUserInfo(
                    user.copy(email = "firestore@gmail.com", phoneNumber = "+1472389274")
                        .toUserDto()
                )

                val result = sut.getUserInfo()

                assertThat(result).isEqualTo(
                    Result.Success<User, DataError>(
                        user
                    )
                )
            }

        @Test
        @DisplayName("returns Network error when the firestore goes down")
        fun `returns Network error when the firestore goes down`() = runTest(testDispatcher) {
            every { auth.currentUser } returns firebaseUser
            every { firebaseUser.email } returns email
            every { firebaseUser.phoneNumber } returns phoneNumber
            stubUserInfo(
                task = Tasks.forException(FirebaseNetworkException("network down"))
            )

            val result = sut.getUserInfo()

            assertThat(result).isEqualTo(
                ErrorResult(DataError.Network)
            )
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        @DisplayName("when the firestore read times out")
        fun `when the firestore read times out`() = runTest(testDispatcher) {
            every { auth.currentUser } returns firebaseUser
            every { firebaseUser.email } returns email
            every { firebaseUser.phoneNumber } returns phoneNumber
            stubUserInfo(
                task = TaskCompletionSource<DocumentSnapshot>().task
            )

            val deferred = async { sut.getUserInfo() }
            advanceTimeBy(5001L)
            runCurrent()

            assertThat(deferred.await()).isEqualTo(
                ErrorResult(DataError.Network)
            )
        }

        @Test
        @DisplayName("returns UNKNOWN error for uncommon errors")
        fun `returns UNKNOWN error for uncommon errors`() = runTest(testDispatcher) {
            every { auth.currentUser } returns firebaseUser
            every { firebaseUser.email } returns email
            every { firebaseUser.phoneNumber } returns phoneNumber
            stubUserInfo(
                task = Tasks.forException(Exception("unknown error"))
            )

            val result = sut.getUserInfo()

            assertThat(result).isEqualTo(
                ErrorResult(DataError.Unknown)
            )
        }

        private fun stubUserInfo(
            userDto: UserDto? = null,
            task: Task<DocumentSnapshot> = Tasks.forResult(documentSnapshot)
        ) {
            every { firebaseUser.uid } returns uid
            every {
                firestore.collection(FirebaseAuthDataSource.USER_COLLECTION_NAME)
                    .document(uid)
                    .get()
            } returns task
            every {
                documentSnapshot.toObject(UserDto::class.java)
            } returns userDto
        }
    }

    @Nested
    @DisplayName("refreshVerificationStatus")
    inner class RefreshVerificationStatus {
        private val firebaseUser = mockk<FirebaseUser>()
        private val task = mockk<Task<Void>>()

        @Test
        @DisplayName("returns UNKNOWN error when the user isn't signed in")
        fun `returns UNKNOWN error when the user isn't signed in`() = runTest(testDispatcher) {
            every { auth.currentUser } returns null

            val result = sut.refreshVerificationStatus()

            assertThat(result).isEqualTo(ErrorResult(DataError.Authentication.UNKNOWN))
        }

        @ParameterizedTest(name = "reports email verification {0}")
        @ValueSource(
            booleans = [
                true,
                false
            ]
        )
        @DisplayName("returns Success with flag indicating whether the email is verified")
        fun `returns Success with flag indicating whether the email is verified`(
            isEmailVerified: Boolean
        ) = runTest(testDispatcher) {
            every { auth.currentUser } returns firebaseUser
            every { firebaseUser.reload() } returns task
            every { firebaseUser.isEmailVerified } returns isEmailVerified
            every { task.isSuccessful } returns true
            every {
                task.addOnCompleteListener(any())
            } answers {
                firstArg<OnCompleteListener<Void>>().onComplete(task)
                task
            }

            val result = sut.refreshVerificationStatus()

            assertThat(result).isEqualTo(Result.Success<Boolean, DataError>(isEmailVerified))
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        @DisplayName("returns Network error when the time is out")
        fun `returns Network error when the time is out`() = runTest(testDispatcher) {
            every { auth.currentUser } returns firebaseUser
            every { firebaseUser.reload() } returns TaskCompletionSource<Void>().task

            val deferred = async { sut.refreshVerificationStatus() }
            advanceTimeBy(20001L)
            runCurrent()

            assertThat(deferred.await()).isEqualTo(Result.Error<Boolean, DataError>(DataError.Network))
        }

        @Test
        @DisplayName("returns UNKNOWN error when uncommon error happens")
        fun `returns UNKNOWN error when uncommon error happens`() = runTest(testDispatcher) {
            every { auth.currentUser } returns firebaseUser
            every { firebaseUser.reload() } returns task
            every { task.isSuccessful } returns false
            every { task.exception } returns Exception("Firebase went down!")
            every { task.addOnCompleteListener(any()) } answers {
                firstArg<OnCompleteListener<Void>>().onComplete(task)
                task
            }

            val result = sut.refreshVerificationStatus()

            assertThat(result).isEqualTo(Result.Error<Boolean, DataError>(DataError.Unknown))
        }
    }

    @Nested
    @DisplayName("resendVerificationEmailLink")
    inner class ResendVerificationEmailLink {
        private val firebaseUser = mockk<FirebaseUser>()
        private val reloadTask = mockk<Task<Void>>()
        private val sendTask = mockk<Task<Void>>()

        @Test
        @DisplayName("returns UNKNOWN error when the user isn't signed in")
        fun `returns UNKNOWN error when the user isn't signed in`() = runTest(testDispatcher) {
            every { auth.currentUser } returns null

            val result = sut.resendVerificationEmailLink()

            assertThat(result).isEqualTo(ErrorResult(DataError.Unknown))
        }

        @Test
        @DisplayName("returns ALREADY_VERIFIED when the verification is done before")
        fun `returns ALREADY_VERIFIED when the verification is done before`() =
            runTest(testDispatcher) {
                stubReload(isSuccessful = true, isEmailVerified = true)

                val result = sut.resendVerificationEmailLink()

                assertThat(result).isEqualTo(ErrorResult(DataError.Authentication.ALREADY_VERIFIED))
            }

        @Test
        @DisplayName("returns Success indicating that the email verification link is sent")
        fun `returns Success indicating that the email verification link is sent`() =
            runTest(testDispatcher) {
                stubReload(isSuccessful = true, isEmailVerified = false)
                stubSend(isSuccessful = true)

                val result = sut.resendVerificationEmailLink()

                assertThat(result).isEqualTo(SuccessResult(Unit))
            }

        @Test
        @DisplayName("returns UNKNOWN error indicating that the email verification link isn't sent")
        fun `returns UNKNOWN error indicating that the email verification link isn't sent`() =
            runTest(testDispatcher) {
                stubReload(isSuccessful = true, isEmailVerified = false)
                stubSend(isSuccessful = false)

                val result = sut.resendVerificationEmailLink()

                assertThat(result).isEqualTo(ErrorResult(DataError.Unknown))
            }

        @Test
        @DisplayName("returns UNKNOWN when the firebase went down")
        fun `returns UNKNOWN when the firebase went down`() = runTest(testDispatcher) {
            stubReload(isSuccessful = false, isEmailVerified = true)
            every { reloadTask.exception } returns Exception("Firebase went down")

            val result = sut.resendVerificationEmailLink()

            assertThat(result).isEqualTo(ErrorResult(DataError.Unknown))
        }

        private fun stubReload(isSuccessful: Boolean, isEmailVerified: Boolean) {
            every { auth.currentUser } returns firebaseUser
            every { firebaseUser.reload() } returns reloadTask
            every { reloadTask.addOnCompleteListener(any()) } answers {
                firstArg<OnCompleteListener<Void>>().onComplete(reloadTask)
                reloadTask
            }
            every { reloadTask.isSuccessful } returns isSuccessful
            every { firebaseUser.isEmailVerified } returns isEmailVerified
        }

        private fun stubSend(isSuccessful: Boolean) {
            every { auth.currentUser } returns firebaseUser
            every { firebaseUser.sendEmailVerification() } returns sendTask
            every { sendTask.addOnCompleteListener(any()) } answers {
                firstArg<OnCompleteListener<Void>>().onComplete(sendTask)
                sendTask
            }
            every { sendTask.isSuccessful } returns isSuccessful
        }
    }

    @Nested
    @DisplayName("signUp")
    inner class SignUp {
        private val firebaseUser = mockk<FirebaseUser>()
        private val authResult = mockk<AuthResult>()
        private val sendTask = mockk<Task<Void>>()
        private val email = "user@gmail.com"
        private val password = "p4ssword"
        private val uid = "uid-123"
        private val user = User(email = email)
        private val documentReference = mockk<DocumentReference>()

        @Test
        @DisplayName("returns UNKNOWN when it fails to create a new user")
        fun `returns UNKNOWN when it fails to create a new user`() = runTest(testDispatcher) {
            stubCreateUser()

            val result = sut.signUp(user, password)

            assertThat(result).isEqualTo(ErrorResult(DataError.Authentication.UNKNOWN))
            verify(exactly = 0) { firebaseUser.delete() }
        }

        @Test
        @DisplayName("returns Success when the signup succeeds with email verification sent successfully")
        fun `returns Success when the signup succeeds with email verification sent successfully`() =
            runTest(testDispatcher) {
                stubCreateUser(fibUser = firebaseUser)
                stubSavingUserInfo()
                stubSend(isSuccessful = true)

                val result = sut.signUp(user, password)

                assertThat(result).isEqualTo(SuccessResult(Unit))
                verify(exactly = 0) { firebaseUser.delete() }
            }

        @Test
        @DisplayName("returns Success even when the verification email fails to send")
        fun `returns Success even when the verification email fails to send`() =
            runTest(testDispatcher) {
                stubCreateUser(fibUser = firebaseUser)
                stubSavingUserInfo()
                stubSend(isSuccessful = false)

                val result = sut.signUp(user, password)

                assertThat(result).isEqualTo(SuccessResult(Unit))
                verify(exactly = 0) { firebaseUser.delete() }
            }

        @Test
        @DisplayName("maps a Firebase auth error code to its authentication error")
        fun `maps a Firebase auth error code to its authentication error`() =
            runTest(testDispatcher) {
                val exception = mockk<FirebaseAuthException>(relaxed = true)
                every { exception.errorCode } returns "ERROR_INVALID_EMAIL"
                stubCreateUser(task = Tasks.forException(exception))

                val result = sut.signUp(user, password)

                assertThat(result).isEqualTo(ErrorResult(DataError.Authentication.INVALID_EMAIL))
            }

        @Test
        @DisplayName("returns Network error when the account creation fails with a network error")
        fun `returns Network error when the account creation fails with a network error`() =
            runTest(testDispatcher) {
                stubCreateUser(task = Tasks.forException(FirebaseNetworkException("network down")))

                val result = sut.signUp(user, password)

                assertThat(result).isEqualTo(ErrorResult(DataError.Network))
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        @DisplayName("returns Network error when the account creation times out")
        fun `returns Network error when the account creation times out`() =
            runTest(testDispatcher) {
                stubCreateUser(task = TaskCompletionSource<AuthResult>().task)

                val deferred = async { sut.signUp(user, password) }
                advanceTimeBy(20001L)
                runCurrent()

                assertThat(deferred.await()).isEqualTo(ErrorResult(DataError.Network))
            }

        @Test
        @DisplayName("returns UNKNOWN error for uncommon errors")
        fun `returns UNKNOWN error for uncommon errors`() = runTest(testDispatcher) {
            stubCreateUser(task = Tasks.forException(Exception("boom")))

            val result = sut.signUp(user, password)

            assertThat(result).isEqualTo(ErrorResult(DataError.Unknown))
        }

        @Test
        @DisplayName("returns UNKNOWN error when saving the profile to Firestore fails")
        fun `returns UNKNOWN error when saving the profile to Firestore fails`() =
            runTest(testDispatcher) {
                stubCreateUser(fibUser = firebaseUser)
                stubSavingUserInfo(task = Tasks.forException(Exception("firestore down")))
                every { firebaseUser.delete() } returns Tasks.forResult(null)

                val result = sut.signUp(user, password)

                assertThat(result).isEqualTo(ErrorResult(DataError.Unknown))
                verify(exactly = 1) { firebaseUser.delete() }
            }

        @Test
        @DisplayName("still returns UNKNOWN when the rollback delete also fails")
        fun `still returns UNKNOWN when the rollback delete also fails`() =
            runTest(testDispatcher) {
                stubCreateUser(fibUser = firebaseUser)
                stubSavingUserInfo(task = Tasks.forException(Exception("firestore down")))
                every { firebaseUser.delete() } returns Tasks.forException(Exception("delete a user failed!"))

                val result = sut.signUp(user, password)

                assertThat(result).isEqualTo(ErrorResult(DataError.Unknown))
                verify(exactly = 1) { firebaseUser.delete() }
            }

        private fun stubCreateUser(
            task: Task<AuthResult> = Tasks.forResult(authResult),
            fibUser: FirebaseUser? = null
        ) {
            every { auth.createUserWithEmailAndPassword(email, password) } returns task
            every { authResult.user } returns fibUser
        }

        private fun stubSavingUserInfo(task: Task<Void> = Tasks.forResult(null)) {
            every { firebaseUser.uid } returns uid
            every {
                firestore.collection(FirebaseAuthDataSource.USER_COLLECTION_NAME)
                    .document(uid)
            } returns documentReference
            // A REAL completed Task: saveAdditionalUserInfo now .await()s the write.
            every { documentReference.set(user.toUserDto()) } returns task
        }

        private fun stubSend(isSuccessful: Boolean) {
            every { firebaseUser.sendEmailVerification() } returns sendTask
            every { sendTask.addOnCompleteListener(any()) } answers {
                firstArg<OnCompleteListener<Void>>().onComplete(sendTask)
                sendTask
            }
            every { sendTask.isSuccessful } returns isSuccessful
        }
    }
}
