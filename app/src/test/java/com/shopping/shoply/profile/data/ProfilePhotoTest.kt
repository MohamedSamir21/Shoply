package com.shopping.shoply.profile.data

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ProfilePhotoTest")
class ProfilePhotoTest {

    @Test
    @DisplayName("a photo within the limit is stored as Base64 and marked custom")
    fun `a photo within the limit is stored as Base64 and marked custom`() {
        val base64 = "a".repeat(ProfilePhoto.MAX_BASE64_LENGTH)

        val result = ProfilePhoto.fields(base64)

        assertThat(result).isEqualTo(
            Result.Success<Map<String, Any>, DataError>(
                mapOf("photoBase64" to base64, "photoSource" to "custom")
            )
        )
    }

    @Test
    @DisplayName("a photo over the limit is rejected")
    fun `a photo over the limit is rejected`() {
        val base64 = "a".repeat(ProfilePhoto.MAX_BASE64_LENGTH + 1)

        val result = ProfilePhoto.fields(base64)

        assertThat(result).isEqualTo(Result.Error<Map<String, Any>, DataError>(DataError.PhotoTooLarge))
    }

    @Test
    @DisplayName("saving without a picked photo writes no photo fields")
    fun `saving without a picked photo writes no photo fields`() {
        val updates = profileUpdates(name = "Alex Morgan", dateOfBirth = "1999-04-02", photoFields = null)

        assertThat(updates).isEqualTo(
            mapOf("username" to "Alex Morgan", "nameSource" to "custom", "dateOfBirth" to "1999-04-02")
        )
    }

    @Test
    @DisplayName("saving with a picked photo writes its fields too")
    fun `saving with a picked photo writes its fields too`() {
        val photoFields = mapOf<String, Any>("photoBase64" to "abc", "photoSource" to "custom")

        val updates = profileUpdates(name = null, dateOfBirth = null, photoFields = photoFields)

        assertThat(updates).isEqualTo(photoFields)
    }
}
