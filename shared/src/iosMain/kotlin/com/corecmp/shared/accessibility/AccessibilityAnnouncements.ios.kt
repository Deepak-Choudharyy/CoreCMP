package com.corecmp.shared.accessibility

import platform.UIKit.UIAccessibilityAnnouncementNotification
import platform.UIKit.UIAccessibilityPostNotification

actual object AccessibilityAnnouncements {
    actual fun announce(message: String) {
        UIAccessibilityPostNotification(UIAccessibilityAnnouncementNotification, message)
    }
}
