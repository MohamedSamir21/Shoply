package com.shopping.shoply.profile.data

import com.google.firebase.auth.FirebaseAuth
import com.shopping.shoply.profile.domain.SessionRepository
import javax.inject.Inject

class FirebaseSessionRepository @Inject constructor(
    private val auth: FirebaseAuth,
) : SessionRepository {

    override fun signOut() = auth.signOut()
}
