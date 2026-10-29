package com.shopping.shoply.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.authentication.domain.interfaces.repository.AuthRepository
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.profile.domain.ProfileRepository
import com.shopping.shoply.settings.domain.SettingsRepository
import com.shopping.shoply.settings.domain.model.AppSettings
import com.shopping.shoply.settings.domain.model.NotificationType
import com.shopping.shoply.settings.domain.model.ThemeMode
import com.shopping.shoply.settings.domain.rules.maskEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** @property sendingReset Change Password is waiting for the reset email to send. */
data class SettingsState(
    val settings: AppSettings = AppSettings(),
    val sendingReset: Boolean = false,
)

sealed interface SettingsIntent {
    data class SelectTheme(val mode: ThemeMode) : SettingsIntent
    data class ToggleNotification(val type: NotificationType, val enabled: Boolean) : SettingsIntent
    data object ChangePassword : SettingsIntent
}

sealed interface SettingsEvent {
    /** "We sent a reset link to **med@gmail.com". */
    data class ResetLinkSent(val maskedEmail: String) : SettingsEvent
    data object ResetFailed : SettingsEvent
}

/**
 * Backs Settings. Theme and notification choices save on the device at once (the theme applies to
 * the whole app right away); Change Password emails a reset link to the account's address.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val sendingReset = MutableStateFlow(false)

    val state: StateFlow<SettingsState> =
        combine(settingsRepository.observe().onStart { emit(AppSettings()) }, sendingReset) { settings, sending ->
            SettingsState(settings, sending)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsState())

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events: Flow<SettingsEvent> = _events.receiveAsFlow()

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SelectTheme -> viewModelScope.launch { settingsRepository.setTheme(intent.mode) }
            is SettingsIntent.ToggleNotification ->
                viewModelScope.launch { settingsRepository.setNotification(intent.type, intent.enabled) }
            SettingsIntent.ChangePassword -> sendReset()
        }
    }

    private fun sendReset() {
        if (sendingReset.value) return
        sendingReset.value = true
        viewModelScope.launch {
            val email = profileRepository.observeAccount().first().email
            val result = if (email.isBlank()) null else authRepository.sendPasswordResetEmail(email)
            sendingReset.value = false
            _events.send(if (result is Result.Success) SettingsEvent.ResetLinkSent(maskEmail(email)) else SettingsEvent.ResetFailed)
        }
    }
}
