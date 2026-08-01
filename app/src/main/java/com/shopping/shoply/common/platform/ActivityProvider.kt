package com.shopping.shoply.common.platform

import android.app.Activity

// Lets the data layer obtain the current Activity WITHOUT a Context being passed
// down through the domain. Domain stays Android-free; data depends only on this
// neutral-kernel abstraction.
interface ActivityProvider {
    fun current(): Activity?
}