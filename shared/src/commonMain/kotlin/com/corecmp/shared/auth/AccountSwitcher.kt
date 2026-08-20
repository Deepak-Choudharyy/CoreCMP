package com.corecmp.shared.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class PosAccount(
    val id: String,
    val name: String,
    val role: String = "agent",
)

class MultiAccountManager {
    private val _accounts = MutableStateFlow<List<PosAccount>>(emptyList())
    val accounts: StateFlow<List<PosAccount>> = _accounts.asStateFlow()

    private val _activeAccountId = MutableStateFlow<String?>(null)
    val activeAccountId: StateFlow<String?> = _activeAccountId.asStateFlow()

    val activeAccount: PosAccount?
        get() = _accounts.value.find { it.id == _activeAccountId.value }

    fun setAccounts(accounts: List<PosAccount>, activeId: String? = accounts.firstOrNull()?.id) {
        _accounts.value = accounts
        _activeAccountId.value = activeId
    }

    fun switchTo(accountId: String): Boolean {
        if (_accounts.value.none { it.id == accountId }) return false
        _activeAccountId.value = accountId
        return true
    }
}
