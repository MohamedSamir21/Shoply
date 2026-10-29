package com.shopping.shoply.supportchat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.time.NowProvider
import com.shopping.shoply.supportchat.domain.SupportChatRepository
import com.shopping.shoply.supportchat.domain.model.ChatMessage
import com.shopping.shoply.supportchat.domain.rules.cannedReply
import com.shopping.shoply.supportchat.domain.rules.topicOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** How long "Shoply Support is typing…" shows before the automatic reply. */
const val REPLY_DELAY_MS = 1_500L

/**
 * @property messages null until the first list arrives (loading).
 * @property replying the automatic reply is on its way (the typing line shows).
 */
data class SupportChatState(
    val messages: List<ChatMessage>? = null,
    val draft: String = "",
    val replying: Boolean = false,
)

sealed interface SupportChatIntent {
    data class DraftChanged(val text: String) : SupportChatIntent

    /** Send [text] (the typed draft or a quick reply). Blank text is ignored. */
    data class Send(val text: String) : SupportChatIntent
}

sealed interface SupportChatEvent {
    data object SendFailed : SupportChatEvent
}

/**
 * Backs Support Chat. Each message is saved, then after [REPLY_DELAY_MS] the assistant answers with
 * [cannedReply] for the message's topic. A failed send keeps the draft so it can be retried.
 */
@HiltViewModel
class SupportChatViewModel @Inject constructor(
    private val repository: SupportChatRepository,
    private val nowProvider: NowProvider,
) : ViewModel() {

    private val draft = MutableStateFlow("")
    private val replying = MutableStateFlow(false)

    val state: StateFlow<SupportChatState> = combine(
        repository.observeMessages().map<List<ChatMessage>, List<ChatMessage>?> { it }.onStart { emit(null) },
        draft,
        replying,
    ) { messages, draft, replying -> SupportChatState(messages, draft, replying) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SupportChatState())

    private val _events = Channel<SupportChatEvent>(Channel.BUFFERED)
    val events: Flow<SupportChatEvent> = _events.receiveAsFlow()

    fun onIntent(intent: SupportChatIntent) {
        when (intent) {
            is SupportChatIntent.DraftChanged -> draft.value = intent.text
            is SupportChatIntent.Send -> send(intent.text)
        }
    }

    private fun send(text: String) {
        val message = text.trim()
        if (message.isEmpty()) return
        val fromDraft = message == draft.value.trim()
        if (fromDraft) draft.value = ""
        viewModelScope.launch {
            if (repository.send(message, fromUser = true, sentAtMillis = nowProvider.nowMillis()) is Result.Error) {
                if (fromDraft) draft.value = message
                _events.send(SupportChatEvent.SendFailed)
                return@launch
            }
            replying.value = true
            delay(REPLY_DELAY_MS)
            repository.send(cannedReply(topicOf(message)), fromUser = false, sentAtMillis = nowProvider.nowMillis())
            replying.value = false
        }
    }
}
