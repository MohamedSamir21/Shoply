package com.shopping.shoply.authentication.domain.usecase.count_down

import java.util.Locale
import javax.inject.Inject

class FormatCountDownTimeUseCase @Inject constructor() {

    operator fun invoke(timeInMillis: Long): String {
        val timeInSeconds = timeInMillis / 1000
        val timeInMinutes = timeInSeconds / 60
        return if (timeInMinutes > 0) {
            String.format(
                Locale.getDefault(),
                "%02d:%02d",
                timeInMinutes, timeInSeconds
            )
        } else {
            String.format(
                Locale.getDefault(),
                "00:%02d",
                timeInSeconds
            )
        }
    }

}