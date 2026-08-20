package com.corecmp.shared.haptic

expect class HapticManager() {
    fun performClickFeedback()
    fun performSuccessFeedback()
    fun performErrorFeedback()
}
