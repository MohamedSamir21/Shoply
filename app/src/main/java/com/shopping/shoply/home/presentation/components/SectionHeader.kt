package com.shopping.shoply.home.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

/**
 * Section title (17 sp bold) with an optional orange "See all" on the right.
 * [onSeeAll] null hides "See all" (Recommended has none); [actionLabel] replaces its text. Includes the screen gutter and the
 * design's 26 dp above / 14 dp below.
 */
@Composable
fun SectionHeader(
    title: String,
    onSeeAll: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ShoplyDimens.gutter)
            .padding(top = 26.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (onSeeAll != null) {
            Text(
                text = actionLabel ?: stringResource(R.string.common_see_all),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                // Compose stretches a small clickable's touch area to 48 dp, so the header keeps
                // the design's height instead of growing to fit a 48 dp box.
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onSeeAll)
                    .padding(vertical = 6.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SectionHeaderPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                SectionHeader(title = "Featured Stores", onSeeAll = {})
                SectionHeader(title = "Recommended for You", onSeeAll = null)
            }
        }
    }
}
