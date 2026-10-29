package com.shopping.shoply.addresses.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.addresses.domain.rules.AddressField
import com.shopping.shoply.addresses.domain.rules.AddressForm
import com.shopping.shoply.addresses.domain.rules.AddressLabel
import com.shopping.shoply.authentication.presentation.components.FormTextField
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.authentication.presentation.components.ShoplyButtonVariant
import com.shopping.shoply.checkout.presentation.preview.CheckoutPreviewData
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
import com.shopping.shoply.orders.presentation.components.OrderActionStyle
import com.shopping.shoply.orders.presentation.components.SmallActionButton
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import kotlinx.coroutines.launch

// ── Addresses list ──

@Composable
fun AddressesScreen(
    snackbarHostState: SnackbarHostState,
    onNavigate: (AddressesDestination) -> Unit,
    viewModel: AddressesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AddressEvents(viewModel.events, snackbarHostState, onDone = {})
    AddressesContent(state = state, onIntent = viewModel::onIntent, onNavigate = onNavigate)
}

/** The saved addresses as cards (default outlined orange, first), and "Add New Address" at the bottom. */
@Composable
fun AddressesContent(
    state: AddressesState,
    onIntent: (AddressesIntent) -> Unit,
    onNavigate: (AddressesDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(title = stringResource(R.string.screen_addresses), onBack = { onNavigate(AddressesDestination.Back) })
        Box(modifier = Modifier.weight(1f)) {
            when {
                state.loading -> SkeletonPulse(modifier = Modifier.padding(horizontal = ShoplyDimens.gutter, vertical = 8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(2) { SkeletonBone(modifier = Modifier.fillMaxWidth().height(150.dp), shape = ShoplyShapes.card) }
                    }
                }
                state.addresses.isEmpty() -> StateMessage(
                    icon = ShoplyAppIcons.Pin,
                    title = stringResource(R.string.addresses_empty_title),
                    message = stringResource(R.string.addresses_empty_message),
                    actionLabel = stringResource(R.string.addresses_add),
                    onAction = { onNavigate(AddressesDestination.Add) },
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 8.dp, bottom = ShoplyDimens.gutter),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.addresses, key = { it.id }) { address ->
                        AddressCard(
                            address = address,
                            onEdit = { onNavigate(AddressesDestination.Edit(address.id)) },
                            onSetDefault = { onIntent(AddressesIntent.SetDefault(address.id)) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
        if (!state.loading && state.addresses.isNotEmpty()) {
            ShoplyButton(
                label = stringResource(R.string.addresses_add),
                onClick = { onNavigate(AddressesDestination.Add) },
                leadingIcon = ShoplyAppIcons.Plus,
                modifier = Modifier.fillMaxWidth().padding(horizontal = ShoplyDimens.gutter, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun AddressCard(address: DeliveryAddress, onEdit: () -> Unit, onSetDefault: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shoplyCard()
            .border(2.dp, if (address.primary) scheme.primary else scheme.surface.copy(alpha = 0f), ShoplyShapes.card)
            .padding(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(44.dp).background(scheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(labelIcon(address.label), contentDescription = null, tint = scheme.primary, modifier = Modifier.size(21.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(address.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                    if (address.primary) DefaultTag()
                }
                Text(
                    text = address.line1 + "\n" + address.line2,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = "${address.recipientName} · ${address.phone}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp), color = scheme.outlineVariant)
        Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallActionButton(label = stringResource(R.string.addresses_edit), style = OrderActionStyle.Soft, onClick = onEdit)
            if (!address.primary) {
                SmallActionButton(label = stringResource(R.string.addresses_set_default), style = OrderActionStyle.Ghost, onClick = onSetDefault)
            }
        }
    }
}

@Composable
private fun DefaultTag() {
    Text(
        text = stringResource(R.string.addresses_default),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, ShoplyShapes.pill)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

// ── Add / Edit Address ──

@Composable
fun AddressEditScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    viewModel: AddressEditViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AddressEvents(viewModel.events, snackbarHostState, onDone = onBack)
    AddressEditContent(state = state, onIntent = viewModel::onIntent, onBack = onBack)
}

/** "Save as" chips, the form fields, the default switch, Save, and Delete when editing. */
@Composable
fun AddressEditContent(
    state: AddressEditState,
    onIntent: (AddressEditIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val form = state.form
    fun change(update: AddressForm.() -> AddressForm) = onIntent(AddressEditIntent.Change(form.update()))

    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(
            title = stringResource(if (state.isEdit) R.string.addresses_edit_title else R.string.addresses_add),
            onBack = onBack,
        )
        if (state.loading) return@Column
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 4.dp, bottom = ShoplyDimens.gutter),
        ) {
            Text(stringResource(R.string.addresses_save_as), fontSize = 14.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AddressLabel.entries.forEach { label ->
                    ShoplyChip(
                        label = stringResource(label.textRes()),
                        icon = labelIcon(label),
                        selected = form.label == label,
                        onClick = { change { copy(label = label) } },
                    )
                }
            }
            Column(modifier = Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AddressTextField(form.recipientName, { change { copy(recipientName = it) } }, R.string.addresses_field_name, ShoplyAppIcons.User, AddressField.NAME, state.errors)
                AddressTextField(
                    form.phone, { change { copy(phone = it) } }, R.string.addresses_field_phone, ShoplyAppIcons.Phone, AddressField.PHONE, state.errors,
                    keyboardType = KeyboardType.Phone,
                )
                AddressTextField(form.street, { change { copy(street = it) } }, R.string.addresses_field_street, ShoplyAppIcons.Pin, AddressField.STREET, state.errors)
                AddressTextField(form.apartment, { change { copy(apartment = it) } }, R.string.addresses_field_apartment, ShoplyAppIcons.Store, null, state.errors)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AddressTextField(
                        form.city, { change { copy(city = it) } }, R.string.addresses_field_city, ShoplyAppIcons.Home, AddressField.CITY, state.errors,
                        modifier = Modifier.weight(1.4f),
                    )
                    AddressTextField(
                        form.zip, { change { copy(zip = it) } }, R.string.addresses_field_zip, ShoplyAppIcons.Tag, AddressField.ZIP, state.errors,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .shoplyCard()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.addresses_default_switch), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(R.string.addresses_default_switch_hint),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                // The only address is always the default, so its switch is locked on.
                Switch(
                    checked = form.primary || state.isFirst,
                    onCheckedChange = { change { copy(primary = it) } },
                    enabled = !state.isFirst,
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                )
            }
            if (state.isEdit) {
                ShoplyButton(
                    label = stringResource(R.string.addresses_delete),
                    onClick = { onIntent(AddressEditIntent.Delete) },
                    variant = ShoplyButtonVariant.Ghost,
                    leadingIcon = ShoplyAppIcons.Trash,
                    enable = !state.saving,
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                )
            }
        }
        ShoplyButton(
            label = stringResource(R.string.addresses_save),
            onClick = { onIntent(AddressEditIntent.Save) },
            enable = !state.saving,
            modifier = Modifier.fillMaxWidth().padding(horizontal = ShoplyDimens.gutter, vertical = 12.dp),
        )
    }
}

@Composable
private fun AddressTextField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes label: Int,
    icon: ImageVector,
    field: AddressField?,
    errors: Set<AddressField>,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val error = when {
        field == null || field !in errors -> null
        field == AddressField.PHONE && value.isNotBlank() -> stringResource(R.string.addresses_field_phone_invalid)
        else -> stringResource(R.string.addresses_field_required)
    }
    FormTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(label),
        leadingIcon = icon,
        supportingText = error,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
    )
}

/** Shared by both screens: snackbars, and [onDone] after the form saved or deleted. */
@Composable
private fun AddressEvents(
    events: kotlinx.coroutines.flow.Flow<AddressesEvent>,
    snackbarHostState: SnackbarHostState,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val done by rememberUpdatedState(onDone)
    LaunchedEffect(events) {
        events.collect { event ->
            val message = when (event) {
                is AddressesEvent.ShowMessage -> event.message
                is AddressesEvent.Done -> event.message.also { done() }
            }
            launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(context.getString(message.textRes()), duration = SnackbarDuration.Short)
            }
        }
    }
}

private fun labelIcon(label: String): ImageVector =
    labelIcon(AddressLabel.entries.firstOrNull { it.name.equals(label, ignoreCase = true) } ?: AddressLabel.OTHER)

private fun labelIcon(label: AddressLabel): ImageVector = when (label) {
    AddressLabel.HOME -> ShoplyAppIcons.Home
    AddressLabel.WORK -> ShoplyAppIcons.Bag
    AddressLabel.OTHER -> ShoplyAppIcons.Pin
}

@StringRes
private fun AddressLabel.textRes(): Int = when (this) {
    AddressLabel.HOME -> R.string.addresses_label_home
    AddressLabel.WORK -> R.string.addresses_label_work
    AddressLabel.OTHER -> R.string.addresses_label_other
}

@StringRes
private fun AddressMessage.textRes(): Int = when (this) {
    AddressMessage.SAVED -> R.string.snackbar_address_saved
    AddressMessage.UPDATED -> R.string.snackbar_address_updated
    AddressMessage.DELETED -> R.string.snackbar_address_deleted
    AddressMessage.DEFAULT_SET -> R.string.snackbar_address_default
    AddressMessage.FAILED -> R.string.snackbar_address_failed
}

@Composable
private fun Preview(content: @Composable () -> Unit) {
    ShoplyTheme { Surface(color = MaterialTheme.colorScheme.background) { content() } }
}

@PreviewLightDark
@Composable
fun AddressesListPreview() = Preview {
    AddressesContent(
        state = AddressesState(loading = false, addresses = listOf(CheckoutPreviewData.home, CheckoutPreviewData.work)),
        onIntent = {},
        onNavigate = {},
    )
}

@PreviewLightDark
@Composable
fun AddressesEmptyPreview() = Preview { AddressesContent(state = AddressesState(loading = false), onIntent = {}, onNavigate = {}) }

@PreviewLightDark
@Composable
fun AddressEditPreview() = Preview {
    AddressEditContent(
        state = AddressEditState(
            isEdit = true,
            form = AddressForm(AddressLabel.HOME, "Ahmed Hassan", "+1 555 0100", "24 Elm Street", "Apt 5B", "Brooklyn", "11201", primary = true),
        ),
        onIntent = {},
        onBack = {},
    )
}

@PreviewLightDark
@Composable
fun AddressAddErrorsPreview() = Preview {
    AddressEditContent(
        state = AddressEditState(errors = setOf(AddressField.NAME, AddressField.STREET, AddressField.CITY, AddressField.ZIP), isFirst = true),
        onIntent = {},
        onBack = {},
    )
}
