package com.shopping.shoply.common.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.darkRing

/**
 * The user's [photo] in a circle; without one, a soft orange circle with their [initials], or a
 * person icon when there are none. [photo] is any Coil model: a URL, a content URI, or JPEG bytes.
 */
@Composable
fun Avatar(
    initials: String?,
    photo: Any? = null,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .darkRing(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = stringResource(R.string.avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (initials != null) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall,
                // The design scales the letters with the circle.
                fontSize = (size.value * 0.36f).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

        } else {
            Icon(
                imageVector = ShoplyAppIcons.User,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(size / 2),
            )
        }
    }
}

/** Initials and the no-name icon, at Home (44 dp) and Profile (60 dp) sizes. */
@PreviewLightDark
@Composable
fun AvatarPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(ShoplyDimens.gutter),
                horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(initials = "AM")
                Avatar(initials = null)
                Avatar(initials = "AM", size = 60.dp)
                Avatar(initials = null, size = 60.dp)
            }
        }
    }
}
