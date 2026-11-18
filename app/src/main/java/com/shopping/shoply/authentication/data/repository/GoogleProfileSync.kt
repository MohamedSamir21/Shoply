package com.shopping.shoply.authentication.data.repository

/**
 * The profile fields to copy from the Google account (spec FR-015): the name and photo are
 * copied only while the user hasn't set their own, and missing Google values are skipped.
 */
internal fun googleProfileUpdates(
    nameSource: String?,
    photoSource: String?,
    googleName: String?,
    googlePhotoUrl: String?
): Map<String, Any> = buildMap {
    if (nameSource != "custom" && !googleName.isNullOrBlank()) {
        put("username", googleName)
        put("nameSource", "google")
    }
    if (photoSource != "custom" && !googlePhotoUrl.isNullOrBlank()) {
        put("photoUrl", googlePhotoUrl)
        put("photoSource", "google")
    }
}
