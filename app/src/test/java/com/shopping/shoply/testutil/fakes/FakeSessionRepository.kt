package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.profile.domain.SessionRepository

class FakeSessionRepository : SessionRepository {
    var signOutCalls = 0

    override fun signOut() {
        signOutCalls++
    }
}
