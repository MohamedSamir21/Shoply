package com.shopping.shoply.payments.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.checkout.domain.PaymentRepository
import com.shopping.shoply.checkout.domain.model.PaymentCard
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.time.NowProvider
import com.shopping.shoply.payments.domain.CardWalletRepository
import com.shopping.shoply.payments.domain.rules.CardField
import com.shopping.shoply.payments.domain.rules.CardForm
import com.shopping.shoply.payments.domain.rules.cardFrom
import com.shopping.shoply.payments.domain.rules.cardIdToPromote
import com.shopping.shoply.payments.domain.rules.cardIdsToDemote
import com.shopping.shoply.payments.domain.rules.invalidCardFields
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Payment Methods.
 *
 * @property cards null until the first list arrives (loading).
 * @property sheetOpen the Add Card sheet is up; [form] and [errors] belong to it.
 * @property errors only filled after a Save attempt.
 */
data class PaymentMethodsState(
    val cards: List<PaymentCard>? = null,
    val sheetOpen: Boolean = false,
    val form: CardForm = CardForm(),
    val errors: Set<CardField> = emptySet(),
    val saving: Boolean = false,
)

sealed interface PaymentMethodsIntent {
    data object OpenAddCard : PaymentMethodsIntent
    data object CloseAddCard : PaymentMethodsIntent
    data class Change(val form: CardForm) : PaymentMethodsIntent
    data object SaveCard : PaymentMethodsIntent
    data class SetDefault(val id: String) : PaymentMethodsIntent
    data class Remove(val id: String) : PaymentMethodsIntent
}

enum class PaymentMessage { CARD_SAVED, CARD_REMOVED, DEFAULT_SET, FAILED }

sealed interface PaymentMethodsEvent {
    data class ShowMessage(val message: PaymentMessage) : PaymentMethodsEvent
}

/** Saved cards (live), Add Card with validation, Set as Default and Remove. */
@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    paymentRepository: PaymentRepository,
    private val cardWalletRepository: CardWalletRepository,
    private val nowProvider: NowProvider,
) : ViewModel() {

    private val ui = MutableStateFlow(PaymentMethodsState())
    private var triedSave = false

    val state: StateFlow<PaymentMethodsState> =
        combine(paymentRepository.observeCards().map<List<PaymentCard>, List<PaymentCard>?> { it }.onStart { emit(null) }, ui) { cards, ui ->
            ui.copy(cards = cards)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, PaymentMethodsState())

    private val _events = Channel<PaymentMethodsEvent>(Channel.BUFFERED)
    val events: Flow<PaymentMethodsEvent> = _events.receiveAsFlow()

    fun onIntent(intent: PaymentMethodsIntent) {
        when (intent) {
            PaymentMethodsIntent.OpenAddCard -> {
                triedSave = false
                ui.update { it.copy(sheetOpen = true, form = CardForm(), errors = emptySet()) }
            }
            PaymentMethodsIntent.CloseAddCard -> ui.update { it.copy(sheetOpen = false) }
            is PaymentMethodsIntent.Change -> ui.update {
                it.copy(form = intent.form, errors = if (triedSave) invalid(intent.form) else emptySet())
            }
            PaymentMethodsIntent.SaveCard -> saveCard()
            is PaymentMethodsIntent.SetDefault -> setDefault(intent.id)
            is PaymentMethodsIntent.Remove -> remove(intent.id)
        }
    }

    private fun saveCard() {
        if (ui.value.saving) return
        triedSave = true
        val form = ui.value.form
        val errors = invalid(form)
        if (errors.isNotEmpty()) {
            ui.update { it.copy(errors = errors) }
            return
        }
        val cards = state.value.cards.orEmpty()
        val card = cardFrom(form, isFirst = cards.isEmpty())
        ui.update { it.copy(saving = true) }
        viewModelScope.launch {
            val result = cardWalletRepository.add(card, if (card.primary) cardIdsToDemote(cards, id = "") else emptyList())
            ui.update { it.copy(saving = false, sheetOpen = result is Result.Error) }
            send(if (result is Result.Success) PaymentMessage.CARD_SAVED else PaymentMessage.FAILED)
        }
    }

    private fun setDefault(id: String) {
        val cards = state.value.cards.orEmpty()
        if (cards.firstOrNull { it.id == id }?.primary != false) return
        viewModelScope.launch {
            val result = cardWalletRepository.setDefault(id, cardIdsToDemote(cards, id))
            send(if (result is Result.Success) PaymentMessage.DEFAULT_SET else PaymentMessage.FAILED)
        }
    }

    private fun remove(id: String) {
        val cards = state.value.cards.orEmpty()
        viewModelScope.launch {
            val result = cardWalletRepository.remove(id, cardIdToPromote(cards, id))
            send(if (result is Result.Success) PaymentMessage.CARD_REMOVED else PaymentMessage.FAILED)
        }
    }

    private fun invalid(form: CardForm): Set<CardField> {
        val now = Calendar.getInstance().apply { timeInMillis = nowProvider.nowMillis() }
        return invalidCardFields(form, now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1)
    }

    private suspend fun send(message: PaymentMessage) = _events.send(PaymentMethodsEvent.ShowMessage(message))
}
