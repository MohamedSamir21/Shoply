package com.shopping.shoply.notifications.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.time.StartOfDayProvider
import com.shopping.shoply.notifications.domain.model.Notification
import com.shopping.shoply.notifications.domain.model.NotificationType
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.fakes.FakeNotificationsRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("NotificationsViewModelTest")
class NotificationsViewModelTest {
    val fakeNotificationsRepository = FakeNotificationsRepository()
    val startOfToday = 1_000_000L

    // Newest first, like the repository: one today (unread), one earlier (read).
    val today = notification("today", createdAtMillis = startOfToday + 10, read = false)
    val earlier = notification("earlier", createdAtMillis = startOfToday - 10, read = true)

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut() = NotificationsViewModel(
        notificationsRepository = fakeNotificationsRepository,
        startOfDayProvider = StartOfDayProvider { startOfToday },
    )

    private fun notification(id: String, createdAtMillis: Long, read: Boolean) = Notification(
        id = id,
        type = NotificationType.GENERAL,
        title = "title $id",
        body = "body $id",
        createdAtMillis = createdAtMillis,
        read = read,
    )

    @Test
    @DisplayName("state is Loading until the first list arrives")
    fun `state is Loading until the first list arrives`() = runTest(main.dispatcher) {
        val sut = createSut()

        assertThat(sut.state.value).isEqualTo(NotificationsState(body = NotificationsBody.Loading, hasUnread = false))
    }

    @Test
    @DisplayName("an empty list is Empty")
    fun `an empty list is Empty`() = runTest(main.dispatcher) {
        fakeNotificationsRepository.notifications.value = emptyList()
        val sut = createSut()

        assertThat(sut.state.value).isEqualTo(NotificationsState(body = NotificationsBody.Empty, hasUnread = false))
    }

    @Test
    @DisplayName("the list is split into today and earlier")
    fun `the list is split into today and earlier`() = runTest(main.dispatcher) {
        fakeNotificationsRepository.notifications.value = listOf(today, earlier)
        val sut = createSut()

        assertThat(sut.state.value.body)
            .isEqualTo(NotificationsBody.Content(today = listOf(today), earlier = listOf(earlier)))
    }

    @Test
    @DisplayName("hasUnread follows the live list")
    fun `hasUnread follows the live list`() = runTest(main.dispatcher) {
        fakeNotificationsRepository.notifications.value = listOf(today, earlier)
        val sut = createSut()
        assertThat(sut.state.value.hasUnread).isTrue()

        fakeNotificationsRepository.notifications.value = listOf(today.copy(read = true), earlier)

        assertThat(sut.state.value.hasUnread).isFalse()
    }

    @Test
    @DisplayName("Open an unread one marks it read once")
    fun `Open an unread one marks it read once`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(NotificationsIntent.Open(today))

        assertThat(fakeNotificationsRepository.markReadCalls).containsExactly("today")
    }

    @Test
    @DisplayName("Open a read one doesn't mark it again")
    fun `Open a read one doesn't mark it again`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(NotificationsIntent.Open(earlier))

        assertThat(fakeNotificationsRepository.markReadCalls).isEmpty()
    }

    @Test
    @DisplayName("MarkAllRead marks all read with no message")
    fun `MarkAllRead marks all read with no message`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.events.test {
            sut.onIntent(NotificationsIntent.MarkAllRead)

            assertThat(fakeNotificationsRepository.markAllReadCalls).isEqualTo(1)
            expectNoEvents()
        }
    }

    @Test
    @DisplayName("a failed MarkAllRead shows MARK_ALL_FAILED")
    fun `a failed MarkAllRead shows MARK_ALL_FAILED`() = runTest(main.dispatcher) {
        fakeNotificationsRepository.result = Result.Error(DataError.Network)
        val sut = createSut()

        sut.events.test {
            sut.onIntent(NotificationsIntent.MarkAllRead)

            assertThat(awaitItem()).isEqualTo(NotificationsEvent.ShowMessage(NotificationsMessage.MARK_ALL_FAILED))
        }
    }
}
