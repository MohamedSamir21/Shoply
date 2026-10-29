package com.shopping.shoply.addresses.presentation

import com.shopping.shoply.addresses.domain.rules.AddressField
import com.shopping.shoply.addresses.domain.rules.AddressForm
import com.shopping.shoply.profile.domain.model.DeliveryAddress

// State, intents, events and destinations of Addresses and Add / Edit Address.

/** The Addresses list; default first. */
data class AddressesState(
    val loading: Boolean = true,
    val addresses: List<DeliveryAddress> = emptyList(),
)

sealed interface AddressesIntent {
    data class SetDefault(val id: String) : AddressesIntent
}

/**
 * Add / Edit Address.
 *
 * @property isEdit false for a new address (no Delete button, "Add New Address" title).
 * @property isFirst the user has no other address, so this one is the default and the switch is locked on.
 * @property errors the fields to mark red; only filled after a Save attempt.
 * @property loading editing waits for the saved address before showing the form.
 */
data class AddressEditState(
    val isEdit: Boolean = false,
    val loading: Boolean = false,
    val form: AddressForm = AddressForm(),
    val isFirst: Boolean = false,
    val errors: Set<AddressField> = emptySet(),
    val saving: Boolean = false,
)

sealed interface AddressEditIntent {
    data class Change(val form: AddressForm) : AddressEditIntent
    data object Save : AddressEditIntent
    data object Delete : AddressEditIntent
}

/** Snackbar messages from both screens. */
enum class AddressMessage { SAVED, UPDATED, DELETED, DEFAULT_SET, FAILED }

sealed interface AddressesEvent {
    data class ShowMessage(val message: AddressMessage) : AddressesEvent

    /** The form saved or deleted: show [message] and go back to the list. */
    data class Done(val message: AddressMessage) : AddressesEvent
}

sealed interface AddressesDestination {
    data object Back : AddressesDestination
    data object Add : AddressesDestination
    data class Edit(val id: String) : AddressesDestination
}
