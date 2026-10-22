package com.shopping.shoply.help.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.components.ListGroup
import com.shopping.shoply.common.presentation.components.ListRow
import com.shopping.shoply.common.presentation.components.ShoplySearchField
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.help.domain.model.Faq
import com.shopping.shoply.help.domain.rules.filterFaqs
import com.shopping.shoply.help.presentation.components.FaqList

/**
 * The Help Center. Holds the typed query and the open question (both survive rotation and
 * process death); the first question starts open.
 *
 * Email opens the email app to support, Call opens the dialer; if the phone has no app for it,
 * nothing happens.
 */
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    onOpenChat: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var expandedId by rememberSaveable { mutableStateOf<String?>(helpFaqs.first().id) }
    val faqs = localizedHelpFaqs()
    val shown = remember(faqs, query) { filterFaqs(faqs, query) }
    val context = LocalContext.current
    val email = stringResource(R.string.help_email_address)
    val phone = stringResource(R.string.help_phone_number)

    HelpContent(
        faqs = shown,
        query = query,
        onQueryChange = { query = it },
        expandedId = expandedId,
        onToggle = { id -> expandedId = if (expandedId == id) null else id },
        onBack = onBack,
        onChat = onOpenChat,
        onEmail = { context.startIfPossible(Intent(Intent.ACTION_SENDTO, "mailto:$email".toUri())) },
        onCall = {
            val digits = phone.filter { it.isDigit() || it == '+' }
            context.startIfPossible(Intent(Intent.ACTION_DIAL, "tel:$digits".toUri()))
        },
    )
}

/**
 * The Help Center without state (previews and UI tests use this): top bar, intro, search field,
 * "Popular Questions" ([FaqList] of [faqs], already filtered), then the "Contact Us" rows.
 */
@Composable
fun HelpContent(
    faqs: List<Faq>,
    query: String,
    onQueryChange: (String) -> Unit,
    expandedId: String?,
    onToggle: (String) -> Unit,
    onBack: () -> Unit,
    onChat: () -> Unit,
    onEmail: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(title = stringResource(R.string.help_title), onBack = onBack)
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Column(modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = ShoplyDimens.space1)) {
                Text(
                    text = stringResource(R.string.help_intro),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = ShoplyDimens.space1, end = ShoplyDimens.space1, bottom = ShoplyDimens.space4),
                )
                ShoplySearchField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = stringResource(R.string.help_search_hint),
                )
                Text(
                    text = stringResource(R.string.help_popular_questions),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(top = ShoplyDimens.space6, bottom = ShoplyDimens.space3)
                        .semantics { heading() },
                )
                FaqList(faqs = faqs, expandedId = expandedId, onToggle = onToggle, query = query)
            }
            ListGroup(
                title = stringResource(R.string.help_contact_us),
                modifier = Modifier.padding(top = ShoplyDimens.space6),
            ) {
                ListRow(
                    icon = ShoplyAppIcons.Chat,
                    label = stringResource(R.string.help_chat),
                    sub = stringResource(R.string.help_chat_sub),
                    onClick = onChat,
                )
                ListRow(
                    icon = ShoplyAppIcons.Mail,
                    label = stringResource(R.string.help_email),
                    sub = stringResource(R.string.help_email_address),
                    onClick = onEmail,
                )
                ListRow(
                    icon = ShoplyAppIcons.Phone,
                    label = stringResource(R.string.help_call),
                    sub = stringResource(R.string.help_call_sub, stringResource(R.string.help_phone_number)),
                    onClick = onCall,
                    showDivider = false,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** No email or dialer app (e.g. a tablet without telephony) → do nothing instead of crashing. */
private fun Context.startIfPossible(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Nothing on the phone can open it.
    }
}

@Composable
private fun HelpContentPreview(query: String, expandedId: String?) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HelpContent(
                faqs = filterFaqs(localizedHelpFaqs(), query),
                query = query,
                onQueryChange = {},
                expandedId = expandedId,
                onToggle = {},
                onBack = {},
                onChat = {},
                onEmail = {},
                onCall = {},
            )
        }
    }
}

/** First visit: no search, first question open. */
@PreviewLightDark
@Composable
fun HelpContentDefaultPreview() = HelpContentPreview(query = "", expandedId = "order-status")

/** Searching "cash": only the payment question is left. */
@PreviewLightDark
@Composable
fun HelpContentFilteredPreview() = HelpContentPreview(query = "cash", expandedId = null)
