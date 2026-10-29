package com.shopping.shoply.supportchat

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.time.NowProvider
import com.shopping.shoply.supportchat.domain.SupportChatRepository
import com.shopping.shoply.supportchat.domain.model.ChatMessage
import com.shopping.shoply.supportchat.domain.rules.SupportTopic
import com.shopping.shoply.supportchat.domain.rules.cannedReply
import com.shopping.shoply.supportchat.domain.rules.topicOf
import com.shopping.shoply.supportchat.presentation.REPLY_DELAY_MS
import com.shopping.shoply.supportchat.presentation.SupportChatEvent
import com.shopping.shoply.supportchat.presentation.SupportChatIntent
import com.shopping.shoply.supportchat.presentation.SupportChatViewModel
import com.shopping.shoply.testutil.MainDispatcherExtension
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("SupportChatTest")
class SupportChatTest {
    val fakeRepository = FakeSupportChatRepository()

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut() = SupportChatViewModel(fakeRepository, NowProvider { 1_000L })

    @Test
    @DisplayName("topicOf: keywords by priority, any case; anything else is GENERAL")
    fun `topic rules`() {
        assertThat(topicOf("Where is my ORDER?")).isEqualTo(SupportTopic.ORDER)
        assertThat(topicOf("I want a refund for my order")).isEqualTo(SupportTopic.REFUND)
        assertThat(topicOf("The courier is late")).isEqualTo(SupportTopic.DELIVERY)
        assertThat(topicOf("My card was charged")).isEqualTo(SupportTopic.PAYMENT)
        assertThat(topicOf("Hello")).isEqualTo(SupportTopic.GENERAL)
    }

    @Test
    @DisplayName("Send saves the message and clears the draft; the topic's reply follows after the delay")
    fun `send then canned reply`() = runTest(main.dispatcher) {
        val sut = createSut()
        sut.onIntent(SupportChatIntent.DraftChanged("Where is my order?"))

        sut.onIntent(SupportChatIntent.Send("Where is my order?"))
        runCurrent()
        assertThat(sut.state.value.draft).isEmpty()
        assertThat(sut.state.value.replying).isTrue()
        assertThat(fakeRepository.messages.value.map { it.fromUser }).containsExactly(true)

        advanceTimeBy(REPLY_DELAY_MS + 1)
        assertThat(sut.state.value.replying).isFalse()
        assertThat(fakeRepository.messages.value.last().text).isEqualTo(cannedReply(SupportTopic.ORDER))
    }

    @Test
    @DisplayName("a failed send keeps the draft and says SendFailed; blank is ignored")
    fun `failed send`() = runTest(main.dispatcher) {
        fakeRepository.error = DataError.Unknown
        val sut = createSut()
        sut.onIntent(SupportChatIntent.DraftChanged("Hello"))

        sut.events.test {
            sut.onIntent(SupportChatIntent.Send("Hello"))
            assertThat(awaitItem()).isEqualTo(SupportChatEvent.SendFailed)
        }
        assertThat(sut.state.value.draft).isEqualTo("Hello")

        sut.onIntent(SupportChatIntent.Send("   "))
        assertThat(fakeRepository.sendCalls).isEqualTo(1)
    }

    class FakeSupportChatRepository : SupportChatRepository {
        val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
        var error: DataError? = null
        var sendCalls = 0

        override fun observeMessages() = messages

        override suspend fun send(text: String, fromUser: Boolean, sentAtMillis: Long): Result<Unit, DataError> {
            sendCalls++
            error?.let { return Result.Error(it) }
            messages.update { it + ChatMessage("m${it.size}", text, fromUser, sentAtMillis) }
            return Result.Success(Unit)
        }
    }
}
