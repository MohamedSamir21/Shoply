package com.shopping.shoply.addresses.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.addresses.domain.AddressBookRepository
import com.shopping.shoply.addresses.domain.rules.addressFrom
import com.shopping.shoply.addresses.domain.rules.formOf
import com.shopping.shoply.addresses.domain.rules.idToPromote
import com.shopping.shoply.addresses.domain.rules.idsToDemote
import com.shopping.shoply.addresses.domain.rules.invalidFields
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.profile.domain.ProfileRepository
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Edit Address's nav arg; absent or blank means Add New Address. */
const val ADDRESS_ID_ARG = "addressId"

/** Backs Addresses: the live list, and Set as Default. */
@HiltViewModel
class AddressesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val addressBookRepository: AddressBookRepository,
) : ViewModel() {

    val state: StateFlow<AddressesState> = profileRepository.observeAddresses()
        .map { AddressesState(loading = false, addresses = it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AddressesState())

    private val _events = Channel<AddressesEvent>(Channel.BUFFERED)
    val events: Flow<AddressesEvent> = _events.receiveAsFlow()

    fun onIntent(intent: AddressesIntent) {
        when (intent) {
            is AddressesIntent.SetDefault -> setDefault(intent.id)
        }
    }

    private fun setDefault(id: String) {
        val addresses = state.value.addresses
        val address = addresses.firstOrNull { it.id == id } ?: return
        if (address.primary) return
        val saved = address.copy(primary = true)
        viewModelScope.launch {
            val message = when (addressBookRepository.save(saved, idsToDemote(addresses, saved))) {
                is Result.Success -> AddressMessage.DEFAULT_SET
                is Result.Error -> AddressMessage.FAILED
            }
            _events.send(AddressesEvent.ShowMessage(message))
        }
    }
}

/**
 * Backs Add / Edit Address. Save checks the form first (errors show only after a Save attempt);
 * a valid save or a delete sends [AddressesEvent.Done]. Deleting the default makes the next
 * address the default.
 */
@HiltViewModel
class AddressEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val addressBookRepository: AddressBookRepository,
) : ViewModel() {

    private val addressId: String = savedStateHandle.get<String>(ADDRESS_ID_ARG).orEmpty()
    private var addresses: List<DeliveryAddress> = emptyList()
    private var triedSave = false

    private val _state = MutableStateFlow(AddressEditState(isEdit = addressId.isNotBlank(), loading = addressId.isNotBlank()))
    val state: StateFlow<AddressEditState> = _state.asStateFlow()

    private val _events = Channel<AddressesEvent>(Channel.BUFFERED)
    val events: Flow<AddressesEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            addresses = profileRepository.observeAddresses().first()
            val existing = addresses.firstOrNull { it.id == addressId }
            val others = addresses.filterNot { it.id == addressId }
            _state.update {
                it.copy(
                    loading = false,
                    form = existing?.let(::formOf) ?: it.form,
                    isFirst = others.isEmpty(),
                )
            }
        }
    }

    fun onIntent(intent: AddressEditIntent) {
        when (intent) {
            is AddressEditIntent.Change -> _state.update {
                it.copy(form = intent.form, errors = if (triedSave) invalidFields(intent.form) else emptySet())
            }
            AddressEditIntent.Save -> save()
            AddressEditIntent.Delete -> delete()
        }
    }

    private fun save() {
        val current = _state.value
        if (current.saving || current.loading) return
        triedSave = true
        val errors = invalidFields(current.form)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        val saved = addressFrom(current.form, addressId, isFirst = current.isFirst)
        _state.update { it.copy(saving = true, errors = emptySet()) }
        viewModelScope.launch {
            val result = addressBookRepository.save(saved, idsToDemote(addresses, saved))
            _state.update { it.copy(saving = false) }
            when (result) {
                is Result.Success -> _events.send(AddressesEvent.Done(if (current.isEdit) AddressMessage.UPDATED else AddressMessage.SAVED))
                is Result.Error -> _events.send(AddressesEvent.ShowMessage(AddressMessage.FAILED))
            }
        }
    }

    private fun delete() {
        if (!_state.value.isEdit || _state.value.saving) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val result = addressBookRepository.delete(addressId, idToPromote(addresses, addressId))
            _state.update { it.copy(saving = false) }
            when (result) {
                is Result.Success -> _events.send(AddressesEvent.Done(AddressMessage.DELETED))
                is Result.Error -> _events.send(AddressesEvent.ShowMessage(AddressMessage.FAILED))
            }
        }
    }
}
