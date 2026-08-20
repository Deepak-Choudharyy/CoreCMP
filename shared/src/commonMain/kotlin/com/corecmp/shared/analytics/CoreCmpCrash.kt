package com.corecmp.shared.analytics

interface CoreCmpCrash {
    fun recordException(throwable: Throwable, keys: Map<String, String> = emptyMap())
    fun log(message: String)
    fun setCustomKey(key: String, value: String)
}

object NoOpCoreCmpCrash : CoreCmpCrash {
    override fun recordException(throwable: Throwable, keys: Map<String, String>) = Unit
    override fun log(message: String) = Unit
    override fun setCustomKey(key: String, value: String) = Unit
}
