package com.corecmp.shared.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lock app when it goes to background. Host calls [onBackground] / [onForeground].
 */
class BackgroundLockManager(
    private val onLockRequired: () -> Unit = {},
) {
    private val _isInBackground = MutableStateFlow(false)
    val isInBackground: StateFlow<Boolean> = _isInBackground.asStateFlow()

    fun onBackground() {
        _isInBackground.value = true
        onLockRequired()
    }

    fun onForeground() {
        _isInBackground.value = false
    }
}
