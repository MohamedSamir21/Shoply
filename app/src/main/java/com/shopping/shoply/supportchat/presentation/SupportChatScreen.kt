package com.shopping.shoply.supportchat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.components.ShoplyChip
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.components.SkeletonBone
import com.shopping.shoply.common.presentation.components.SkeletonPulse
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.supportchat.domain.model.ChatMessage
import com.shopping.shoply.supportchat.domain.rules.SupportTopic
import com.shopping.shoply.supportchat.domain.rules.cannedReply
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SupportChatScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    viewModel: SupportChatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SupportChatEvent.SendFailed -> launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(context.getString(R.string.snackbar_chat_failed), duration = SnackbarDuration.Short)
                }
            }
        }
    }

    SupportChatContent(state = state, onIntent = viewModel::onIntent, onBack = onBack)
}

/** Top bar, the conversation (newest at the bottom, auto-scrolled), quick-reply chips and the input. */
@Composable
fun SupportChatContent(
    state: SupportChatState,
    onIntent: (SupportChatIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val messages = state.messages
    val listState = rememberLazyListState()
    val lastIndex = (messages?.size ?: 0) + if (state.replying) 1 else 0
    LaunchedEffect(lastIndex) { if (lastIndex > 0) listState.animateScrollToItem(lastIndex) }

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        ShoplyTopAppBar(
            title = stringResource(R.string.screen_support_chat),
            subtitle = stringResource(R.string.chat_subtitle),
            onBack = onBack,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Box(modifier = Modifier.weight(1f)) {
            when {
                messages == null -> ChatSkeleton()
                messages.isEmpty() && !state.replying -> StateMessage(
                    icon = ShoplyAppIcons.Chat,
                    title = stringResource(R.string.chat_empty_title),
                    message = stringResource(R.string.chat_empty_message),
                    actionLabel = stringResource(R.string.chat_quick_start),
                    onAction = { onIntent(SupportChatIntent.Send(QUICK_START)) },
                )
                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item(key = "intro") {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.chat_intro),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, ShoplyShapes.pill)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                    items(messages, key = { it.id }) { ChatBubble(it) }
                    if (state.replying) {
                        item(key = "typing") {
                            Text(
                                stringResource(R.string.chat_typing),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                    }
                }
            }
        }
        QuickReplies(onSend = { onIntent(SupportChatIntent.Send(it)) })
        ChatInput(
            draft = state.draft,
            onDraftChange = { onIntent(SupportChatIntent.DraftChanged(it)) },
            onSend = { onIntent(SupportChatIntent.Send(state.draft)) },
        )
    }
}

/** User bubbles on the right in soft orange; replies on the left as cards. */
@Composable
private fun ChatBubble(message: ChatMessage) {
    val scheme = MaterialTheme.colorScheme
    val mine = message.fromUser
    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (mine) 18.dp else 6.dp,
        bottomEnd = if (mine) 6.dp else 18.dp,
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
        Column(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .then(if (mine) Modifier.background(scheme.primaryContainer, shape) else Modifier.shoplyCard(shape))
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 8.dp),
        ) {
            Text(
                text = message.text,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 21.sp,
                color = if (mine) scheme.onPrimaryContainer else scheme.onSurface,
            )
            Text(
                text = timeOf(message.sentAtMillis),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (mine) scheme.onPrimaryContainer.copy(alpha = 0.75f) else scheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End).padding(top = 3.dp),
            )
        }
    }
}

@Composable
private fun QuickReplies(onSend: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        stringArrayResource(R.array.chat_quick_replies).forEach { reply ->
            ShoplyChip(label = reply, onClick = { onSend(reply) })
        }
    }
}

@Composable
private fun ChatInput(draft: String, onDraftChange: (String) -> Unit, onSend: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val canSend = draft.isNotBlank()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BasicTextField(
            value = draft,
            onValueChange = onDraftChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = scheme.onSurface),
            cursorBrush = SolidColor(scheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(ShoplyShapes.field)
                        .background(scheme.surfaceVariant)
                        .border(1.4.dp, scheme.outlineVariant, ShoplyShapes.field)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (draft.isEmpty()) {
                        Text(stringResource(R.string.chat_hint), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = scheme.onSurfaceVariant)
                    }
                    inner()
                }
            },
        )
        val sendLabel = stringResource(R.string.cd_send_message)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (canSend) scheme.primary else scheme.surfaceVariant)
                .clickable(enabled = canSend, role = Role.Button, onClick = onSend)
                .semantics { contentDescription = sendLabel },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                ShoplyAppIcons.ChevronRight,
                contentDescription = null,
                tint = if (canSend) scheme.onPrimary else scheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun ChatSkeleton() {
    SkeletonPulse(modifier = Modifier.padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            listOf(0.6f to false, 0.45f to true, 0.7f to false).forEach { (width, mine) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                    SkeletonBone(modifier = Modifier.fillMaxWidth(width).height(54.dp), shape = RoundedCornerShape(18.dp))
                }
            }
        }
    }
}

// The empty state's button asks a general question so the conversation starts with a reply.
private const val QUICK_START = "Hi! I need some help."

private fun timeOf(millis: Long): String = SimpleDateFormat("h:mm a", Locale.US).format(Date(millis))

@Composable
private fun Preview(state: SupportChatState) {
    ShoplyTheme { Surface(color = MaterialTheme.colorScheme.background) { SupportChatContent(state = state, onIntent = {}, onBack = {}) } }
}

@PreviewLightDark
@Composable
fun SupportChatConversationPreview() = Preview(
    SupportChatState(
        messages = listOf(
            ChatMessage("1", "Where is my order?", fromUser = true, sentAtMillis = 0),
            ChatMessage("2", cannedReply(SupportTopic.ORDER), fromUser = false, sentAtMillis = 60_000),
            ChatMessage("3", "Thanks!", fromUser = true, sentAtMillis = 120_000),
        ),
        draft = "It says on the way",
        replying = true,
    ),
)

@PreviewLightDark
@Composable
fun SupportChatEmptyPreview() = Preview(SupportChatState(messages = emptyList()))

@PreviewLightDark
@Composable
fun SupportChatLoadingPreview() = Preview(SupportChatState())
