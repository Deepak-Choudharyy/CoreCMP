package com.corecmp.shared.haptic

actual class HapticManager actual constructor() {
    actual fun performClickFeedback() {
        // No-op for JVM/Desktop
    }

    actual fun performSuccessFeedback() {
        // No-op for JVM/Desktop
    }

    actual fun performErrorFeedback() {
        // No-op for JVM/Desktop
    }
}
