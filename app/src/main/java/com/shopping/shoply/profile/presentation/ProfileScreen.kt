package com.shopping.shoply.profile.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.BuildConfig
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.components.ListGroup
import com.shopping.shoply.common.presentation.components.ListRow
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.components.TopBarIconButton
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.profile.presentation.components.AccountCard
import com.shopping.shoply.profile.presentation.components.LogoutDialog

/**
 * The Profile tab. [onSignedOut] runs once the user confirms Log Out; the caller leaves the
 * signed-in app.
 */
@Composable
fun ProfileScreen(
    onNavigate: (ProfileDestination) -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val signedOut by rememberUpdatedState(onSignedOut)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileEvent.SignedOut -> signedOut()
            }
        }
    }

    ProfileContent(state = state, onIntent = viewModel::onIntent, onNavigate = onNavigate)
}

/**
 * Profile without a ViewModel (previews and UI tests use this): top bar with ⚙, [AccountCard],
 * then the Account, Preferences and Support groups and the app version. Shows [LogoutDialog]
 * while [ProfileState.showLogoutDialog].
 */
@Composable
fun ProfileContent(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    onNavigate: (ProfileDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(title = stringResource(R.string.profile_title), centered = false) {
            TopBarIconButton(
                icon = ShoplyAppIcons.Gear,
                label = stringResource(R.string.cd_settings),
                onClick = { onNavigate(ProfileDestination.Settings) },
            )
        }
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            AccountCard(
                name = state.name,
                initials = state.initials,
                email = state.email,
                phone = state.phone,
                onEdit = { onNavigate(ProfileDestination.EditProfile) },
                modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = ShoplyDimens.space1),
            )
            ListGroup(
                title = stringResource(R.string.profile_group_account),
                modifier = Modifier.padding(top = ShoplyDimens.space6),
            ) {
                ListRow(
                    icon = ShoplyAppIcons.Receipt,
                    label = stringResource(R.string.profile_my_orders),
                    onClick = { onNavigate(ProfileDestination.Orders) },
                )
                ListRow(
                    icon = ShoplyAppIcons.Heart,
                    label = stringResource(R.string.screen_wishlist),
                    value = state.wishlistCount.toString(),
                    onClick = { onNavigate(ProfileDestination.Wishlist) },
                )
                ListRow(
                    icon = ShoplyAppIcons.Store,
                    label = stringResource(R.string.screen_favorite_stores),
                    onClick = { onNavigate(ProfileDestination.FavoriteStores) },
                )
                ListRow(
                    icon = ShoplyAppIcons.Pin,
                    label = stringResource(R.string.screen_addresses),
                    onClick = { onNavigate(ProfileDestination.Addresses) },
                )
                ListRow(
                    icon = ShoplyAppIcons.Card,
                    label = stringResource(R.string.screen_payment_methods),
                    onClick = { onNavigate(ProfileDestination.Payments) },
                    showDivider = false,
                )
            }
            ListGroup(
                title = stringResource(R.string.profile_group_preferences),
                modifier = Modifier.padding(top = 22.dp),
            ) {
                ListRow(
                    icon = ShoplyAppIcons.Bell,
                    label = stringResource(R.string.screen_notifications),
                    value = state.unreadNotificationCount
                        .takeIf { it > 0 }
                        ?.let { stringResource(R.string.profile_notifications_new, it) },
                    onClick = { onNavigate(ProfileDestination.Notifications) },
                )
                ListRow(
                    icon = ShoplyAppIcons.Gear,
                    label = stringResource(R.string.screen_settings),
                    sub = stringResource(R.string.profile_settings_sub),
                    onClick = { onNavigate(ProfileDestination.Settings) },
                    showDivider = false,
                )
            }
            ListGroup(
                title = stringResource(R.string.profile_group_support),
                modifier = Modifier.padding(top = 22.dp),
            ) {
                ListRow(
                    icon = ShoplyAppIcons.Help,
                    label = stringResource(R.string.help_title),
                    onClick = { onNavigate(ProfileDestination.Help) },
                )
                ListRow(
                    icon = ShoplyAppIcons.Logout,
                    label = stringResource(R.string.profile_log_out),
                    danger = true,
                    onClick = { onIntent(ProfileIntent.LogOutClick) },
                    showDivider = false,
                )
            }
            Text(
                text = stringResource(R.string.profile_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.shoplyExtra.inkFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ShoplyDimens.space5, bottom = ShoplyDimens.space6),
            )
        }
    }

    if (state.showLogoutDialog) {
        LogoutDialog(
            onConfirm = { onIntent(ProfileIntent.LogOutConfirm) },
            onDismiss = { onIntent(ProfileIntent.LogOutDismiss) },
        )
    }
}

@Composable
private fun ProfileContentPreview(state: ProfileState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProfileContent(state = state, onIntent = {}, onNavigate = {})
        }
    }
}

/** Signed-in user with wishlist items and unread notifications. */
@PreviewLightDark
@Composable
fun ProfileContentDefaultPreview() = ProfileContentPreview(
    ProfileState(
        name = "Ahmed Mostafa",
        initials = "AM",
        email = "ahmed.mostafa@gmail.com",
        phone = "+20 100 555 0199",
        wishlistCount = 3,
        unreadNotificationCount = 2,
    ),
)

/** Before the account loads: no name, nothing saved, nothing unread. */
@PreviewLightDark
@Composable
fun ProfileContentEmptyPreview() = ProfileContentPreview(ProfileState())
