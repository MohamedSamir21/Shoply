package com.shopping.shoply.supportchat.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.common.data.firestore.liveSnapshots
import com.shopping.shoply.common.data.firestore.long
import com.shopping.shoply.common.data.firestore.string
import com.shopping.shoply.common.data.firestore.writeForCurrentUser
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.supportchat.domain.SupportChatRepository
import com.shopping.shoply.supportchat.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Messages in `users/{uid}/supportChat/{id}`: text, fromUser, sentAtMillis. Sorted in the app. */
class FirestoreSupportChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : SupportChatRepository {

    override fun observeMessages(): Flow<List<ChatMessage>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return chat(uid).liveSnapshots().map { snapshot ->
            snapshot.documents.map {
                ChatMessage(
                    id = it.id,
                    text = it.string("text"),
                    fromUser = it.getBoolean("fromUser") == true,
                    sentAtMillis = it.long("sentAtMillis"),
                )
            }.sortedBy { it.sentAtMillis }
        }
    }

    override suspend fun send(text: String, fromUser: Boolean, sentAtMillis: Long): Result<Unit, DataError> =
        auth.writeForCurrentUser { uid ->
            chat(uid).document().set(mapOf("text" to text, "fromUser" to fromUser, "sentAtMillis" to sentAtMillis))
        }

    private fun chat(uid: String) = firestore.collection("users").document(uid).collection("supportChat")
}
