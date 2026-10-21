package com.shopping.shoply.home.domain.rules

fun greetingFor(hour: Int): Greeting = when (hour) {
    in 5..11 -> Greeting.MORNING
    in 12..16 -> Greeting.AFTERNOON
    in 17..23 -> Greeting.EVENING
    in 0..4 -> Greeting.EVENING
    else -> Greeting.NONE
}

enum class Greeting {
    MORNING,
    AFTERNOON,
    EVENING,
    NONE
}