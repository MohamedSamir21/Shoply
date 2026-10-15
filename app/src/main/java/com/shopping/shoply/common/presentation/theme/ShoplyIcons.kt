package com.shopping.shoply.common.presentation.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Auth icon set, backed by the bundled Material icons so they always render.
 * Named after their role in the Shoply Remix design so call sites stay readable.
 */
object ShoplyIcons {
    val Back: ImageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft
    val ArrowRight: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
    val Check: ImageVector = Icons.Filled.Check
    val Chevron: ImageVector = Icons.Filled.KeyboardArrowDown
    val Mail: ImageVector = Icons.Outlined.Email
    val At: ImageVector = Icons.Outlined.Email
    val Lock: ImageVector = Icons.Outlined.Lock
    val User: ImageVector = Icons.Outlined.Person
    val Phone: ImageVector = Icons.Outlined.Phone
    val XCircle: ImageVector = Icons.Outlined.Clear
    val Shield: ImageVector = Icons.Outlined.Lock
}
