package com.shopping.shoply.help.domain.model

/** A Help Center question with its [answer]. [id] keeps the open question stable while filtering. */
data class Faq(
    val id: String,
    val question: String,
    val answer: String,
)
