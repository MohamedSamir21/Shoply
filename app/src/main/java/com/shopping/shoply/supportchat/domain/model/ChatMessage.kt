package com.shopping.shoply.supportchat.domain.model

/** One chat bubble. [fromUser] false is the support assistant. */
data class ChatMessage(
    val id: String,
    val text: String,
    val fromUser: Boolean,
    val sentAtMillis: Long,
)
