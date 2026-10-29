package com.shopping.shoply.supportchat.domain

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.supportchat.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

/** The signed-in user's support conversation. */
interface SupportChatRepository {

    /** Every message, oldest first. Emits right away, then on every change; never throws. */
    fun observeMessages(): Flow<List<ChatMessage>>

    /** Adds a message (from the user, or the support assistant's reply). Local-first. */
    suspend fun send(text: String, fromUser: Boolean, sentAtMillis: Long): Result<Unit, DataError>
}
