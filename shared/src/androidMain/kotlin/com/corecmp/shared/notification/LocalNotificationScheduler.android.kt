package com.corecmp.shared.notification

actual class LocalNotificationScheduler actual constructor() {
    actual suspend fun schedule(request: ScheduledNotificationRequest) = Unit

    actual suspend fun cancel(id: String) = Unit

    actual suspend fun cancelAll() = Unit
}
