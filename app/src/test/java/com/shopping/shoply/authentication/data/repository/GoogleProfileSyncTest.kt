package com.shopping.shoply.authentication.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("GoogleProfileSyncTest")
class GoogleProfileSyncTest {

    @Test
    @DisplayName("a new user gets the Google name and photo")
    fun `a new user gets the Google name and photo`() {
        val updates = googleProfileUpdates(
            nameSource = null,
            photoSource = null,
            googleName = "Alex Morgan",
            googlePhotoUrl = "https://lh3.googleusercontent.com/a/photo"
        )

        assertThat(updates).isEqualTo(
            mapOf(
                "username" to "Alex Morgan",
                "nameSource" to "google",
                "photoUrl" to "https://lh3.googleusercontent.com/a/photo",
                "photoSource" to "google"
            )
        )
    }

    @Test
    @DisplayName("a custom name and photo are never overwritten")
    fun `a custom name and photo are never overwritten`() {
        val updates = googleProfileUpdates(
            nameSource = "custom",
            photoSource = "custom",
            googleName = "Alex Morgan",
            googlePhotoUrl = "https://lh3.googleusercontent.com/a/photo"
        )

        assertThat(updates).isEmpty()
    }

    @Test
    @DisplayName("missing Google values are skipped")
    fun `missing Google values are skipped`() {
        val updates = googleProfileUpdates(
            nameSource = "google",
            photoSource = "google",
            googleName = null,
            googlePhotoUrl = null
        )

        assertThat(updates).isEmpty()
    }
}
