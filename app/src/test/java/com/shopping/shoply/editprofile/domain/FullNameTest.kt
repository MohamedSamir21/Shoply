package com.shopping.shoply.editprofile.domain

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.domain.Result
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("FullNameTest")
class FullNameTest {

    @Test
    @DisplayName("a valid name is trimmed")
    fun `a valid name is trimmed`() {
        assertThat(validateFullName("  Alex Morgan ")).isEqualTo(Result.Success<String, FullNameError>("Alex Morgan"))
    }

    @Test
    @DisplayName("a blank name is rejected")
    fun `a blank name is rejected`() {
        assertThat(validateFullName("   ")).isEqualTo(Result.Error<String, FullNameError>(FullNameError.BLANK))
    }

    @Test
    @DisplayName("a name over the limit is rejected, one at the limit is accepted")
    fun `a name over the limit is rejected, one at the limit is accepted`() {
        val atLimit = "a".repeat(MAX_FULL_NAME_LENGTH)

        assertThat(validateFullName(atLimit)).isEqualTo(Result.Success<String, FullNameError>(atLimit))
        assertThat(validateFullName(atLimit + "a"))
            .isEqualTo(Result.Error<String, FullNameError>(FullNameError.TOO_LONG))
    }
}
