package com.shopping.shoply.notifications.domain.rules

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.notifications.domain.model.Notification
import com.shopping.shoply.notifications.domain.model.NotificationType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SplitByDayTest")
class SplitByDayTest {
    private val startOfToday = 1_000_000L

    private fun notification(id: String, createdAtMillis: Long) = Notification(
        id = id,
        type = NotificationType.GENERAL,
        title = "title $id",
        body = "body $id",
        createdAtMillis = createdAtMillis,
        read = false,
    )

    @Test
    @DisplayName("one created at the start of today is today")
    fun `one created at the start of today is today`() {
        val atMidnight = notification("a", startOfToday)

        assertThat(splitByDay(listOf(atMidnight), startOfToday))
            .isEqualTo(NotificationGroups(today = listOf(atMidnight), earlier = emptyList()))
    }

    @Test
    @DisplayName("one created just before today is earlier")
    fun `one created just before today is earlier`() {
        val lastNight = notification("a", startOfToday - 1)

        assertThat(splitByDay(listOf(lastNight), startOfToday))
            .isEqualTo(NotificationGroups(today = emptyList(), earlier = listOf(lastNight)))
    }

    @Test
    @DisplayName("each group keeps the input order")
    fun `each group keeps the input order`() {
        val today2 = notification("t2", startOfToday + 200)
        val today1 = notification("t1", startOfToday + 100)
        val earlier2 = notification("e2", startOfToday - 100)
        val earlier1 = notification("e1", startOfToday - 200)

        val groups = splitByDay(listOf(today2, today1, earlier2, earlier1), startOfToday)

        assertThat(groups.today).containsExactly(today2, today1).inOrder()
        assertThat(groups.earlier).containsExactly(earlier2, earlier1).inOrder()
    }

    @Test
    @DisplayName("an empty list gives two empty groups")
    fun `an empty list gives two empty groups`() {
        assertThat(splitByDay(emptyList(), startOfToday))
            .isEqualTo(NotificationGroups(today = emptyList(), earlier = emptyList()))
    }
}
