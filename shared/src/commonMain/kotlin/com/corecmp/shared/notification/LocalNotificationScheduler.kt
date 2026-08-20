package com.corecmp.shared.notification

import kotlin.time.Instant

data class ScheduledNotificationRequest(
    val id: String,
    val title: String,
    val body: String,
    val fireAt: Instant,
)

expect class LocalNotificationScheduler() {
    suspend fun schedule(request: ScheduledNotificationRequest)
    suspend fun cancel(id: String)
    suspend fun cancelAll()
}
