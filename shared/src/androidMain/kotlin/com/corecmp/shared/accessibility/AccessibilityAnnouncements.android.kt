package com.corecmp.shared.accessibility

import android.view.accessibility.AccessibilityManager
import com.corecmp.shared.api.appContext

actual object AccessibilityAnnouncements {
    actual fun announce(message: String) {
        val manager = appContext.getSystemService(AccessibilityManager::class.java) ?: return
        if (!manager.isEnabled) return
        val event = android.view.accessibility.AccessibilityEvent.obtain().apply {
            eventType = android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
            text.add(message)
        }
        manager.sendAccessibilityEvent(event)
    }
}
