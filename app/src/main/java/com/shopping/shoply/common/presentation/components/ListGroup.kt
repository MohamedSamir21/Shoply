package com.shopping.shoply.common.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard

/**
 * A titled card of [ListRow]s, inset by the 24 dp gutter. [title] shows uppercase above the card;
 * null leaves it out.
 */
@Composable
fun ListGroup(
    title: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.padding(horizontal = ShoplyDimens.gutter)) {
        if (title != null) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.1.em,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = ShoplyDimens.space1, end = ShoplyDimens.space1, bottom = 10.dp)
                    .semantics { heading() },
            )
        }
        Column(modifier = Modifier.fillMaxWidth().shoplyCard(), content = content)
    }
}

/** The Help Center's "Contact Us" group: three rows with sub-lines, no divider under the last. */
@PreviewLightDark
@Composable
fun ListGroupPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ListGroup(title = "Contact Us", modifier = Modifier.padding(vertical = ShoplyDimens.gutter)) {
                ListRow(icon = ShoplyAppIcons.Chat, label = "Chat with Us", sub = "Usually replies in 2 minutes", onClick = {})
                ListRow(icon = ShoplyAppIcons.Mail, label = "Email Support", sub = "support@shoply.app", onClick = {})
                ListRow(icon = ShoplyAppIcons.Phone, label = "Call Us", onClick = {}, showDivider = false)
            }
        }
    }
}
