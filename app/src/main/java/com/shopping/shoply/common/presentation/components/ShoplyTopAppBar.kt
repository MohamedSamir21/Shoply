package com.shopping.shoply.common.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

/**
 * The design's 56 dp top app bar: optional back button, a 19 sp bold title, and [actions] at the end.
 *
 * - [onBack] null hides the back button (tab roots have nowhere to go back to).
 * - [centered] true centres the title over the whole bar (M3 center-aligned); false puts it at the
 *   start, lined up with the 24 dp screen gutter when there is no back button.
 * - [subtitle] adds a small grey line under the title (e.g. "3 saved items").
 */
@Composable
fun ShoplyTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    centered: Boolean = true,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 10.dp),
    ) {
        if (centered) {
            TopBarTitle(
                title = title,
                subtitle = subtitle,
                textAlign = TextAlign.Center,
                // Clear of a 44 dp button on either side.
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 54.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                TopBarIconButton(icon = ShoplyAppIcons.Back, label = stringResource(R.string.cd_go_back), onClick = onBack)
            }
            if (centered) {
                Box(modifier = Modifier.weight(1f))
            } else {
                TopBarTitle(
                    title = title,
                    subtitle = subtitle,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (onBack != null) 4.dp else 14.dp),
                )
            }
            actions()
        }
    }
}

/** 44 dp plain icon button for [ShoplyTopAppBar]'s back and action slots. */
@Composable
fun TopBarIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun TopBarTitle(title: String, subtitle: String?, textAlign: TextAlign, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = textAlign,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() },
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = textAlign,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 1.dp),
            )
        }
    }
}

/** Tab root: title at the start, one action. */
@PreviewLightDark
@Composable
fun ShoplyTopAppBarStartPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ShoplyTopAppBar(title = "Categories", centered = false) {
                TopBarIconButton(icon = ShoplyAppIcons.Search, label = "Search", onClick = {})
            }
        }
    }
}

/** Pushed screen: back button, centred title. */
@PreviewLightDark
@Composable
fun ShoplyTopAppBarCenteredPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ShoplyTopAppBar(title = "Help Center", onBack = {})
        }
    }
}

/** Pushed screen with a subtitle under the centred title. */
@PreviewLightDark
@Composable
fun ShoplyTopAppBarSubtitlePreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ShoplyTopAppBar(title = "Wishlist", subtitle = "3 saved items", onBack = {})
        }
    }
}
