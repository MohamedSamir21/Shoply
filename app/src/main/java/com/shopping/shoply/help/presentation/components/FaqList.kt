package com.shopping.shoply.help.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.help.domain.model.Faq
import com.shopping.shoply.help.presentation.localizedHelpFaqs

/**
 * One card of questions; tapping a question opens its answer (the chevron turns orange and flips).
 *
 * - [expandedId] is the open FAQ's id, or null when all are closed. [onToggle] gets the tapped id.
 * - An empty [faqs] shows "No answers match “[query]”. Try contacting us below."
 */
@Composable
fun FaqList(
    faqs: List<Faq>,
    expandedId: String?,
    onToggle: (String) -> Unit,
    query: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().shoplyCard()) {
        if (faqs.isEmpty()) {
            Text(
                text = stringResource(R.string.help_no_match, query.trim()),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(18.dp),
            )
        }
        faqs.forEachIndexed { index, faq ->
            FaqItem(faq = faq, expanded = faq.id == expandedId, onClick = { onToggle(faq.id) })
            if (index < faqs.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun FaqItem(faq: Faq, expanded: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "chevron")
    val state = stringResource(if (expanded) R.string.cd_expanded else R.string.cd_collapsed)
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { stateDescription = state }
                .padding(horizontal = ShoplyDimens.space4, vertical = ShoplyDimens.space3),
            horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = faq.question,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = ShoplyAppIcons.ChevronDown,
                contentDescription = null,
                tint = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotation),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                text = faq.answer,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.5.sp,
                lineHeight = 21.6.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = ShoplyDimens.space4, end = ShoplyDimens.space4, bottom = ShoplyDimens.space4),
            )
        }
    }
}

@Composable
private fun FaqListPreviewFrame(content: @Composable () -> Unit) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(ShoplyDimens.gutter)) { content() }
        }
    }
}

/** The first question open. */
@PreviewLightDark
@Composable
fun FaqListPreview() = FaqListPreviewFrame {
    FaqList(faqs = localizedHelpFaqs(), expandedId = "order-status", onToggle = {}, query = "")
}

/** A search with no matches. */
@PreviewLightDark
@Composable
fun FaqListNoMatchPreview() = FaqListPreviewFrame {
    FaqList(faqs = emptyList(), expandedId = null, onToggle = {}, query = "crypto")
}
