package com.corecmp.shared.analytics

interface CoreCmpAnalytics {
    fun logEvent(event: AnalyticsEvent)
    fun setUserId(userId: String?)
    fun setUserProperty(key: String, value: String?)
}

object NoOpCoreCmpAnalytics : CoreCmpAnalytics {
    override fun logEvent(event: AnalyticsEvent) = Unit
    override fun setUserId(userId: String?) = Unit
    override fun setUserProperty(key: String, value: String?) = Unit
}
