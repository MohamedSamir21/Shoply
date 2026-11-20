package com.shopping.shoply.launch.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.shopping.shoply.launch.domain.LaunchRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

private val Context.launchDataStore by preferencesDataStore(name = "launch")
private val ONBOARDING_SEEN_KEY = booleanPreferencesKey("onboarding_seen")

/** Firebase keeps the sign-in on the device; the onboarding flag lives in a Preferences DataStore. */
class DefaultLaunchRepository @Inject constructor(
    private val auth: FirebaseAuth,
    @param:ApplicationContext private val context: Context,
) : LaunchRepository {

    override fun isSignedIn(): Boolean {
        val user = auth.currentUser ?: return false
        // An email account that never verified its email quit sign-up half-way.
        val usesPassword = user.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }
        return !usesPassword || user.isEmailVerified
    }

    override suspend fun hasSeenOnboarding(): Boolean = try {
        context.launchDataStore.data.first()[ONBOARDING_SEEN_KEY] ?: false
    } catch (_: IOException) {
        false
    }

    override suspend fun markOnboardingSeen() {
        try {
            context.launchDataStore.edit { it[ONBOARDING_SEEN_KEY] = true }
        } catch (_: IOException) {
            // Worst case, onboarding shows once more.
        }
    }
}
