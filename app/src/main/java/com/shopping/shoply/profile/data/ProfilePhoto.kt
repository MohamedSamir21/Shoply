package com.shopping.shoply.profile.data

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result

/** Rules for the custom profile photo stored in `users/{uid}` (spec FR-012 to FR-014). */
internal object ProfilePhoto {
    const val MAX_SIDE_PX = 256
    const val JPEG_QUALITY = 80

    /** Firestore documents are capped at 1 MiB; this keeps the photo well below it. */
    const val MAX_BASE64_LENGTH = 200 * 1024

    const val FIELD_BASE64 = "photoBase64"
    const val FIELD_SOURCE = "photoSource"
    const val SOURCE_CUSTOM = "custom"

    /** The photo fields to write for a newly picked photo, or [DataError.PhotoTooLarge]. */
    fun fields(base64: String): Result<Map<String, Any>, DataError> =
        if (base64.length > MAX_BASE64_LENGTH) {
            Result.Error(DataError.PhotoTooLarge)
        } else {
            Result.Success(mapOf(FIELD_BASE64 to base64, FIELD_SOURCE to SOURCE_CUSTOM))
        }
}

/**
 * The profile fields to write for one save. Photo fields appear only when [photoFields] is
 * non-null, so saving without a picked photo leaves the stored photo untouched.
 */
internal fun profileUpdates(
    name: String?,
    dateOfBirth: String?,
    photoFields: Map<String, Any>?
): Map<String, Any> = buildMap {
    name?.let {
        put("username", it)
        put("nameSource", "custom")
    }
    dateOfBirth?.let { put("dateOfBirth", it) }
    photoFields?.let { putAll(it) }
}
