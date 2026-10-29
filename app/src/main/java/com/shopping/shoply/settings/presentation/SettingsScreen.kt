package com.shopping.shoply.settings.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.components.ListGroup
import com.shopping.shoply.common.presentation.components.SegmentedTab
import com.shopping.shoply.common.presentation.components.SegmentedTabs
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.settings.domain.model.AppSettings
import com.shopping.shoply.settings.domain.model.NotificationType
import com.shopping.shoply.settings.domain.model.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val text = when (event) {
                is SettingsEvent.ResetLinkSent -> context.getString(R.string.snackbar_reset_sent, event.maskedEmail)
                SettingsEvent.ResetFailed -> context.getString(R.string.snackbar_reset_failed)
            }
            launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(text, duration = SnackbarDuration.Short)
            }
        }
    }

    SettingsContent(state = state, onIntent = viewModel::onIntent, onBack = onBack)
}

/** Appearance (theme), Notifications (three switches) and Security (Change Password). */
@Composable
fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = state.settings
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(title = stringResource(R.string.screen_settings), onBack = onBack)
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(top = 4.dp, bottom = 24.dp)) {
            ListGroup(title = stringResource(R.string.settings_appearance)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(stringResource(R.string.settings_theme), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    SegmentedTabs(
                        tabs = ThemeMode.entries.map { SegmentedTab(stringResource(it.labelRes())) },
                        selectedIndex = settings.theme.ordinal,
                        onSelect = { onIntent(SettingsIntent.SelectTheme(ThemeMode.entries[it])) },
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(
                        text = if (settings.theme == ThemeMode.SYSTEM) {
                            stringResource(R.string.settings_theme_system_hint)
                        } else {
                            stringResource(R.string.settings_theme_fixed_hint, stringResource(settings.theme.labelRes()).lowercase())
                        },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
            ListGroup(title = stringResource(R.string.settings_notifications), modifier = Modifier.padding(top = 22.dp)) {
                NotificationType.entries.forEachIndexed { index, type ->
                    SwitchRow(
                        icon = type.icon(),
                        label = stringResource(type.labelRes()),
                        sub = stringResource(type.hintRes()),
                        checked = settings.isOn(type),
                        onChecked = { onIntent(SettingsIntent.ToggleNotification(type, it)) },
                    )
                    if (index < NotificationType.entries.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            ListGroup(title = stringResource(R.string.settings_security), modifier = Modifier.padding(top = 22.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .clickable(enabled = !state.sendingReset, role = Role.Button) { onIntent(SettingsIntent.ChangePassword) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(ShoplyAppIcons.Gear, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_change_password), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.settings_change_password_hint),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(ShoplyAppIcons.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(icon: ImageVector, label: String, sub: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(sub, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
        )
    }
}

@StringRes
private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
    ThemeMode.SYSTEM -> R.string.settings_theme_system
}

@StringRes
private fun NotificationType.labelRes(): Int = when (this) {
    NotificationType.ORDER_UPDATES -> R.string.settings_order_updates
    NotificationType.OFFERS -> R.string.settings_offers
    NotificationType.PRICE_DROPS -> R.string.settings_price_drops
}

@StringRes
private fun NotificationType.hintRes(): Int = when (this) {
    NotificationType.ORDER_UPDATES -> R.string.settings_order_updates_hint
    NotificationType.OFFERS -> R.string.settings_offers_hint
    NotificationType.PRICE_DROPS -> R.string.settings_price_drops_hint
}

private fun NotificationType.icon(): ImageVector = when (this) {
    NotificationType.ORDER_UPDATES -> ShoplyAppIcons.Truck
    NotificationType.OFFERS -> ShoplyAppIcons.Tag
    NotificationType.PRICE_DROPS -> ShoplyAppIcons.Heart
}

@Composable
private fun Preview(state: SettingsState) {
    ShoplyTheme { Surface(color = MaterialTheme.colorScheme.background) { SettingsContent(state = state, onIntent = {}, onBack = {}) } }
}

@PreviewLightDark
@Composable
fun SettingsSystemPreview() = Preview(SettingsState())

@PreviewLightDark
@Composable
fun SettingsDarkPreview() = Preview(
    SettingsState(settings = AppSettings(theme = ThemeMode.DARK, notifications = NotificationType.entries.associateWith { true })),
)
