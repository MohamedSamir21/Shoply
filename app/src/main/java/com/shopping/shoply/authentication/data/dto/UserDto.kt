package com.shopping.shoply.authentication.data.dto

// Firestore serialization model, separate from the domain User so the domain isn't
// coupled to the persistence schema. No-arg defaults are required by Firestore's
// reflective deserialization. Note: no password field — credentials are never stored.
data class UserDto(
    val username: String = "",
    val email: String? = null,
    val phoneNumber: String? = null,
    // Persisted as "phoneVerified" (NOT "isPhoneVerified"): Firebase's serializer strips the
    // "is" prefix from Boolean getters, so naming it without the prefix keeps the read path
    // (toObject), the write path (set), and the manual update()/getBoolean() all on one field.
    // Missing on legacy docs -> deserializes to false (no backfill needed).
    val phoneVerified: Boolean = false
)