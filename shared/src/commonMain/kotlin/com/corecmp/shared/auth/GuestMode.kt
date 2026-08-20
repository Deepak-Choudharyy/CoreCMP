package com.corecmp.shared.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GuestModeManager {
    private val _isGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    fun enableGuest() {
        _isGuest.value = true
    }

    fun disableGuest() {
        _isGuest.value = false
    }

    fun requireLogin(onRequireLogin: () -> Unit, action: () -> Unit) {
        if (_isGuest.value) onRequireLogin() else action()
    }
}
