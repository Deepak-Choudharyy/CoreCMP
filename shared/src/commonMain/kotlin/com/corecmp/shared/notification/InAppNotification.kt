package com.corecmp.shared.notification

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.time.Clock

enum class InAppNotificationLevel {
    INFO, SUCCESS, WARNING, ERROR,
}

data class InAppNotification(
    val id: String,
    val title: String,
    val message: String,
    val level: InAppNotificationLevel = InAppNotificationLevel.INFO,
    val createdAtEpochMs: Long = Clock.System.now().toEpochMilliseconds(),
)

class InAppNotificationStore {
    private val _notifications = mutableStateListOf<InAppNotification>()
    val notifications: List<InAppNotification> get() = _notifications

    var unreadCount by mutableStateOf(0)
        private set

    fun push(notification: InAppNotification) {
        _notifications.add(0, notification)
        unreadCount++
    }

    fun markRead(id: String) {
        if (_notifications.any { it.id == id }) {
            unreadCount = (unreadCount - 1).coerceAtLeast(0)
        }
    }

    fun dismiss(id: String) {
        _notifications.removeAll { it.id == id }
    }

    fun clearAll() {
        _notifications.clear()
        unreadCount = 0
    }
}
