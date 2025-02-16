package com.shopping.shoply.authentication.presentation.model

import android.content.Context
import com.shopping.shoply.authentication.domain.model.DomainContext

data class ActivityContext(
    val context: Context
) : DomainContext