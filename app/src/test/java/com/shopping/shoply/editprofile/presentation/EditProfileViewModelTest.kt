package com.shopping.shoply.editprofile.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.profile.domain.model.Account
import com.shopping.shoply.profile.domain.model.UserInfo
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.fakes.FakeProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("EditProfileViewModelTest")
class EditProfileViewModelTest {
    private val fakeProfileRepository = FakeProfileRepository().apply {
        account.value = Account(
            name = "Alex Morgan",
            email = "alex@mail.com",
            phone = null,
            dateOfBirth = "1999-04-02"
        )
    }

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut() = EditProfileViewModel(profileRepository = fakeProfileRepository)

    @Test
    @DisplayName("loading ends once the profile arrives")
    fun `loading ends once the profile arrives`() = runTest(main.dispatcher) {
        val state = createSut().state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.fullName).isEqualTo("Alex Morgan")
    }

    @Test
    @DisplayName("saving without changes writes nothing")
    fun `saving without changes writes nothing`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(EditProfileIntent.SaveUserInfo)

        assertThat(fakeProfileRepository.savedUserInfo).isEmpty()
    }

    @Test
    @DisplayName("only the changed name is sent, trimmed")
    fun `only the changed name is sent, trimmed`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(EditProfileIntent.ChangeFullName("  Sam Lee "))
        sut.onIntent(EditProfileIntent.ChangeDateOfBirth("1999-04-02"))
        sut.onIntent(EditProfileIntent.SaveUserInfo)

        assertThat(fakeProfileRepository.savedUserInfo.single())
            .isEqualTo(UserInfo(name = "Sam Lee", pickedPhotoUri = null, dateOfBirth = null))
    }

    @Test
    @DisplayName("a blank name is not saved and shows an error")
    fun `a blank name is not saved and shows an error`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(EditProfileIntent.ChangeFullName("   "))
        sut.onIntent(EditProfileIntent.SaveUserInfo)

        assertThat(fakeProfileRepository.savedUserInfo).isEmpty()
        assertThat((sut.state.value.nameError as UiText.StringResource).id)
            .isEqualTo(R.string.full_name_required)
    }

    @Test
    @DisplayName("saving after picking a photo sends that photo")
    fun `saving after picking a photo sends that photo`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(EditProfileIntent.ChangeAvatar("content://media/picker/0/42"))
        sut.onIntent(EditProfileIntent.SaveUserInfo)

        assertThat(fakeProfileRepository.savedUserInfo.single().pickedPhotoUri)
            .isEqualTo("content://media/picker/0/42")
    }

    @Test
    @DisplayName("a photo that is too large shows the photo-too-large error")
    fun `a photo that is too large shows the photo-too-large error`() = runTest(main.dispatcher) {
        fakeProfileRepository.updateResult = Result.Error(DataError.PhotoTooLarge)
        val sut = createSut()

        sut.events.test {
            sut.onIntent(EditProfileIntent.ChangeAvatar("content://media/picker/0/42"))
            sut.onIntent(EditProfileIntent.SaveUserInfo)

            val event = awaitItem() as EditProfileEvent.ProfileUpdateError
            assertThat((event.error as UiText.StringResource).id).isEqualTo(R.string.photo_too_large)
        }
    }
}
