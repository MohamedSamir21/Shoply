package com.shopping.shoply.notifications.presentation

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.components.SkeletonBone
import com.shopping.shoply.common.presentation.components.SkeletonPulse
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.notifications.domain.model.Notification
import com.shopping.shoply.notifications.presentation.components.NotificationRow
import com.shopping.shoply.notifications.presentation.preview.NotificationsPreviewData
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * The Notifications list. Snackbars go to [snackbarHostState] (from MainShell, so they sit above
 * the bottom navigation).
 */
@Composable
fun NotificationsScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNavigate: (NotificationDestination) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is NotificationsEvent.ShowMessage -> launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = context.getString(event.message.textRes()),
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    NotificationsContent(state = state, onIntent = viewModel::onIntent, onBack = onBack, onNavigate = onNavigate)
}

/**
 * Notifications without a ViewModel (previews and UI tests use this): top bar ("Mark all read"
 * while any is unread), then the body by [NotificationsState.body]:
 * - Loading → list skeleton
 * - Empty → "No Notifications Yet" + Back to Home → [NotificationDestination.Home]
 * - Content → "Today" and "Earlier" groups; a tap marks the row read and opens its [destination].
 */
@Composable
fun NotificationsContent(
    state: NotificationsState,
    onIntent: (NotificationsIntent) -> Unit,
    onBack: () -> Unit,
    onNavigate: (NotificationDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val body = state.body
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(
            title = stringResource(R.string.screen_notifications),
            onBack = onBack,
        ) {
            if (state.hasUnread) {
                TextButton(onClick = { onIntent(NotificationsIntent.MarkAllRead) }) {
                    Text(
                        text = stringResource(R.string.notifications_mark_all_read),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        when (body) {
            NotificationsBody.Loading -> NotificationsSkeleton()
            NotificationsBody.Empty -> StateMessage(
                icon = ShoplyAppIcons.Bell,
                title = stringResource(R.string.notifications_empty_title),
                message = stringResource(R.string.notifications_empty_message),
                actionLabel = stringResource(R.string.notifications_empty_action),
                onAction = { onNavigate(NotificationDestination.Home) },
            )
            is NotificationsBody.Content -> {
                val context = LocalContext.current
                val startOfToday = remember { startOfTodayMillis() }
                val label = { n: Notification -> timeLabel(context, n.createdAtMillis, startOfToday) }
                val open: (Notification) -> Unit = { n ->
                    onIntent(NotificationsIntent.Open(n))
                    n.destination?.let(onNavigate)
                }
                LazyColumn(contentPadding = PaddingValues(bottom = ShoplyDimens.space6)) {
                    group(R.string.notifications_today, body.today, label, open)
                    group(R.string.notifications_earlier, body.earlier, label, open)
                }
            }
        }
    }
}

private fun LazyListScope.group(
    title: Int,
    notifications: List<Notification>,
    label: (Notification) -> String,
    onOpen: (Notification) -> Unit,
) {
    if (notifications.isEmpty()) return
    item(key = "header_$title") { GroupHeader(stringResource(title)) }
    items(notifications, key = { it.id }) { notification ->
        NotificationRow(
            notification = notification,
            timeLabel = label(notification),
            onClick = { onOpen(notification) },
            modifier = Modifier.animateItem(),
        )
    }
}

@Composable
private fun GroupHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.1.em,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = ShoplyDimens.space4, bottom = 6.dp)
            .semantics { heading() },
    )
}

/** Five row-shaped placeholders: icon box, title, two body lines. */
@Composable
private fun NotificationsSkeleton() {
    SkeletonPulse(modifier = Modifier.padding(horizontal = ShoplyDimens.gutter, vertical = ShoplyDimens.space4)) {
        Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
            repeat(5) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    SkeletonBone(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp))
                    Column(modifier = Modifier.padding(top = 2.dp)) {
                        SkeletonBone(modifier = Modifier.fillMaxWidth(0.6f).height(14.dp))
                        SkeletonBone(modifier = Modifier.padding(top = 10.dp).fillMaxWidth().height(12.dp))
                        SkeletonBone(modifier = Modifier.padding(top = 6.dp).fillMaxWidth(0.7f).height(12.dp))
                    }
                }
            }
        }
    }
}

/** Today → "10:34 AM", yesterday → "Yesterday", older → "Oct 9", in the device's locale. */
private fun timeLabel(context: Context, millis: Long, startOfTodayMillis: Long): String = when {
    millis >= startOfTodayMillis -> DateFormat.getTimeFormat(context).format(Date(millis))
    millis >= startOfTodayMillis - DateUtils.DAY_IN_MILLIS -> context.getString(R.string.notifications_yesterday)
    else -> DateUtils.formatDateTime(
        context,
        millis,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_NO_YEAR,
    )
}

private fun startOfTodayMillis(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun NotificationsMessage.textRes(): Int = when (this) {
    NotificationsMessage.MARK_ALL_FAILED -> R.string.snackbar_notifications_mark_all_failed
}

@Composable
private fun NotificationsContentPreview(state: NotificationsState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            NotificationsContent(state = state, onIntent = {}, onBack = {}, onNavigate = {})
        }
    }
}

@PreviewLightDark
@Composable
fun NotificationsFilledPreview() = NotificationsContentPreview(
    NotificationsState(
        body = NotificationsBody.Content(today = NotificationsPreviewData.today, earlier = NotificationsPreviewData.earlier),
        hasUnread = true,
    ),
)

@PreviewLightDark
@Composable
fun NotificationsEmptyPreview() = NotificationsContentPreview(NotificationsState(body = NotificationsBody.Empty))

@PreviewLightDark
@Composable
fun NotificationsLoadingPreview() = NotificationsContentPreview(NotificationsState(body = NotificationsBody.Loading))
