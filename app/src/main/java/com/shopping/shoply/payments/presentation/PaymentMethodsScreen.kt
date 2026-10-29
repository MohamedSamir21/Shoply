package com.shopping.shoply.payments.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.FormTextField
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.checkout.domain.model.PaymentCard
import com.shopping.shoply.checkout.presentation.components.cardLabel
import com.shopping.shoply.checkout.presentation.preview.CheckoutPreviewData
import com.shopping.shoply.common.presentation.components.ListGroup
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.components.SkeletonBone
import com.shopping.shoply.common.presentation.components.SkeletonPulse
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.components.TopBarIconButton
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.payments.domain.rules.CardField
import com.shopping.shoply.payments.domain.rules.CardForm
import com.shopping.shoply.payments.domain.rules.formatCardNumberInput
import com.shopping.shoply.payments.domain.rules.formatExpiryInput
import kotlinx.coroutines.launch

@Composable
fun PaymentMethodsScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    viewModel: PaymentMethodsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PaymentMethodsEvent.ShowMessage -> launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(context.getString(event.message.textRes()), duration = SnackbarDuration.Short)
                }
            }
        }
    }

    PaymentMethodsContent(state = state, onIntent = viewModel::onIntent, onBack = onBack)
}

/** The Cards group (default tagged, others "Set as Default", each removable), Add New Card, the note. */
@Composable
fun PaymentMethodsContent(
    state: PaymentMethodsState,
    onIntent: (PaymentMethodsIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cards = state.cards
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(title = stringResource(R.string.screen_payment_methods), onBack = onBack)
        when {
            cards == null -> SkeletonPulse(modifier = Modifier.padding(horizontal = ShoplyDimens.gutter, vertical = 8.dp)) {
                SkeletonBone(modifier = Modifier.fillMaxWidth().height(180.dp), shape = ShoplyShapes.card)
            }
            cards.isEmpty() -> StateMessage(
                icon = ShoplyAppIcons.Card,
                title = stringResource(R.string.payments_empty_title),
                message = stringResource(R.string.payments_empty_message),
                actionLabel = stringResource(R.string.payments_add_card),
                onAction = { onIntent(PaymentMethodsIntent.OpenAddCard) },
            )
            else -> Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(top = 8.dp)) {
                ListGroup(title = stringResource(R.string.payments_cards)) {
                    cards.forEach { card ->
                        CardRow(card = card, onIntent = onIntent)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clickable(role = Role.Button) { onIntent(PaymentMethodsIntent.OpenAddCard) }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Icon(ShoplyAppIcons.Plus, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text(stringResource(R.string.payments_add_card), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(
                    text = stringResource(R.string.payments_secure_note),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(ShoplyDimens.gutter),
                )
            }
        }
    }

    if (state.sheetOpen) AddCardSheet(state, onIntent)
}

@Composable
private fun CardRow(card: PaymentCard, onIntent: (PaymentMethodsIntent) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val label = cardLabel(card)
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(scheme.primaryContainer, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(ShoplyAppIcons.Card, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = scheme.onSurface)
            Text(
                stringResource(R.string.payments_expires, card.expiry),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurfaceVariant,
            )
        }
        if (card.primary) {
            Text(
                text = stringResource(R.string.payments_default),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimaryContainer,
                modifier = Modifier.background(scheme.primaryContainer, ShoplyShapes.pill).padding(horizontal = 8.dp, vertical = 2.dp),
            )
        } else {
            Text(
                text = stringResource(R.string.payments_set_default),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.primary,
                modifier = Modifier
                    .clickable(role = Role.Button) { onIntent(PaymentMethodsIntent.SetDefault(card.id)) }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
            )
        }
        TopBarIconButton(
            icon = ShoplyAppIcons.Trash,
            label = stringResource(R.string.cd_remove_card, label),
            onClick = { onIntent(PaymentMethodsIntent.Remove(card.id)) },
            tint = MaterialTheme.shoplyExtra.red,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCardSheet(state: PaymentMethodsState, onIntent: (PaymentMethodsIntent) -> Unit) {
    val form = state.form
    fun change(update: CardForm.() -> CardForm) = onIntent(PaymentMethodsIntent.Change(form.update()))
    fun error(field: CardField, @StringRes text: Int) = if (field in state.errors) text else null

    ModalBottomSheet(
        onDismissRequest = { onIntent(PaymentMethodsIntent.CloseAddCard) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ShoplyDimens.gutter, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(stringResource(R.string.payments_add_card), fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            FormTextField(
                value = form.name,
                onValueChange = { change { copy(name = it) } },
                label = stringResource(R.string.payments_field_name),
                leadingIcon = ShoplyAppIcons.User,
                supportingText = error(CardField.NAME, R.string.payments_error_name)?.let { stringResource(it) },
            )
            FormTextField(
                value = form.number,
                onValueChange = { change { copy(number = formatCardNumberInput(it)) } },
                label = stringResource(R.string.payments_field_number),
                leadingIcon = ShoplyAppIcons.Card,
                supportingText = error(CardField.NUMBER, R.string.payments_error_number)?.let { stringResource(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormTextField(
                    value = form.expiry,
                    onValueChange = { change { copy(expiry = formatExpiryInput(it)) } },
                    label = stringResource(R.string.payments_field_expiry),
                    supportingText = error(CardField.EXPIRY, R.string.payments_error_expiry)?.let { stringResource(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                FormTextField(
                    value = form.cvv,
                    onValueChange = { change { copy(cvv = it.filter(Char::isDigit).take(4)) } },
                    label = stringResource(R.string.payments_field_cvv),
                    supportingText = error(CardField.CVV, R.string.payments_error_cvv)?.let { stringResource(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.weight(1f),
                )
            }
            if (!state.cards.isNullOrEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().shoplyCard().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.payments_make_default), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Switch(
                        checked = form.makeDefault,
                        onCheckedChange = { change { copy(makeDefault = it) } },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                    )
                }
            }
            ShoplyButton(
                label = stringResource(R.string.payments_save_card),
                onClick = { onIntent(PaymentMethodsIntent.SaveCard) },
                enable = !state.saving,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 8.dp),
            )
        }
    }
}

@StringRes
private fun PaymentMessage.textRes(): Int = when (this) {
    PaymentMessage.CARD_SAVED -> R.string.snackbar_card_saved
    PaymentMessage.CARD_REMOVED -> R.string.snackbar_card_removed
    PaymentMessage.DEFAULT_SET -> R.string.snackbar_card_default
    PaymentMessage.FAILED -> R.string.snackbar_card_failed
}

@Composable
private fun Preview(state: PaymentMethodsState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PaymentMethodsContent(state = state, onIntent = {}, onBack = {})
        }
    }
}

@PreviewLightDark
@Composable
fun PaymentMethodsPreview() = Preview(PaymentMethodsState(cards = listOf(CheckoutPreviewData.visa, CheckoutPreviewData.mastercard)))

@PreviewLightDark
@Composable
fun PaymentMethodsEmptyPreview() = Preview(PaymentMethodsState(cards = emptyList()))

@PreviewLightDark
@Composable
fun PaymentMethodsLoadingPreview() = Preview(PaymentMethodsState())
