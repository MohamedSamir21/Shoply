package com.shopping.shoply.authentication.data.mapper

import com.shopping.shoply.authentication.data.dto.UserDto
import com.shopping.shoply.authentication.domain.model.User

/**
 * Maps this domain [User] to its Firestore [UserDto].
 *
 * Keeps the persistence schema out of the domain layer — the DTO, not [User], knows how a profile is
 * shaped in Firestore. Note the deliberate field rename [User.isPhoneVerified] to
 * [UserDto.phoneVerified] (see [UserDto] for why the stored field drops the `is` prefix). The user's
 * password is intentionally absent: credentials are never persisted.
 */
fun User.toUserDto(): UserDto = UserDto(
    username = username,
    email = email,
    phoneNumber = phoneNumber,
    phoneVerified = isPhoneVerified
)

/**
 * Maps this Firestore [UserDto] back to the domain [User]; the inverse of [toUserDto].
 *
 * Used when reading a stored profile into the domain entity. [UserDto.phoneVerified] becomes
 * [User.isPhoneVerified].
 */
fun UserDto.toUser(): User = User(
    username = username,
    email = email,
    phoneNumber = phoneNumber,
    isPhoneVerified = phoneVerified
)