package com.shopping.shoply.authentication.data.mapper

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.data.dto.UserDto
import com.shopping.shoply.authentication.domain.model.User
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Tests for the [User] <-> [UserDto] mappers ([toUserDto] / [toUser]).
 *
 * The round-trip tests confirm no field is lost or misplaced in either direction, paired with
 * known-value assertions on the intermediate object so a symmetric bug (both mappers wrong the same
 * way) cannot hide behind a green round trip. The remaining tests pin the `isPhoneVerified` <->
 * `phoneVerified` rename for both boolean values and confirm nullable fields survive as `null`.
 */
@DisplayName("UserMapper")
class UserMapperTest {

    @Test
    @DisplayName("[User -> UserDto -> User] round-trip")
    fun `maps User to UserDto and back to User`() {
        val originalUser = User(
            username = "username",
            email = "username@gmail.com",
            phoneNumber = "+2001207790612",
            isPhoneVerified = true
        )

        val mappedUserDto = originalUser.toUserDto()
        val mappedUser = mappedUserDto.toUser()

        assertThat(mappedUser).isEqualTo(originalUser)
        assertThat(mappedUserDto.username).isEqualTo(originalUser.username)
        assertThat(mappedUserDto.email).isEqualTo(originalUser.email)
        assertThat(mappedUserDto.phoneNumber).isEqualTo(originalUser.phoneNumber)
        assertThat(mappedUserDto.phoneVerified).isEqualTo(originalUser.isPhoneVerified)
    }

    @Test
    @DisplayName("[UserDto -> User -> UserDto] round-trip")
    fun `maps UserDto to User and back to UserDto`() {
        val originalUserDto = UserDto(
            username = "username",
            email = "username@gmail.com",
            phoneNumber = "+2001207790612",
            phoneVerified = true
        )

        val mappedUser = originalUserDto.toUser()
        val mappedUserDto = mappedUser.toUserDto()

        assertThat(mappedUserDto).isEqualTo(originalUserDto)
        assertThat(mappedUser.username).isEqualTo(originalUserDto.username)
        assertThat(mappedUser.email).isEqualTo(originalUserDto.email)
        assertThat(mappedUser.phoneNumber).isEqualTo(originalUserDto.phoneNumber)
        assertThat(mappedUser.isPhoneVerified).isEqualTo(originalUserDto.phoneVerified)
    }

    @Test
    @DisplayName("phoneVerified = false is carried through both mappers")
    fun `maps unverified phone through both directions`() {
        // A true-only test can't catch a hard-coded `true`, so pin the false value explicitly.
        val unverifiedUser = User(username = "username", isPhoneVerified = false)

        val dto = unverifiedUser.toUserDto()
        assertThat(dto.phoneVerified).isFalse()
        assertThat(dto.toUser().isPhoneVerified).isFalse()
    }

    @Test
    @DisplayName("null email and phone are carried through as null")
    fun `maps null email and phone as null`() {
        val userWithNulls = User(username = "username", email = null, phoneNumber = null)

        val dto = userWithNulls.toUserDto()
        assertThat(dto.email).isNull()
        assertThat(dto.phoneNumber).isNull()

        val back = dto.toUser()
        assertThat(back.email).isNull()
        assertThat(back.phoneNumber).isNull()
    }
}
