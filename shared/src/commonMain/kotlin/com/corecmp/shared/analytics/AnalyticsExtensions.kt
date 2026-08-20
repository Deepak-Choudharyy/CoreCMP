package com.corecmp.shared.analytics

import com.corecmp.shared.CoreCmp
import kotlin.time.Clock
import kotlin.time.Duration

object AnalyticsBreadcrumbs {
    private val trail = mutableListOf<String>()

    fun add(action: String) {
        trail += "${Clock.System.now().toEpochMilliseconds()}:$action"
        if (trail.size > 50) trail.removeAt(0)
    }

    fun snapshot(): List<String> = trail.toList()
}

class PerformanceTrace(private val name: String) {
    private val start = Clock.System.now()

    fun stop(): Duration {
        val duration = Clock.System.now() - start
        CoreCmp.analytics.logEvent(
            AnalyticsEvent(
                name = "perf_trace",
                params = mapOf("trace" to name, "duration_ms" to duration.inWholeMilliseconds.toString()),
            ),
        )
        return duration
    }
}

fun CoreCmpAnalytics.trackScreen(screenName: String) {
    logEvent(AnalyticsEvent("screen_view", mapOf("screen" to screenName)))
}

fun CoreCmpAnalytics.track(name: String, vararg params: Pair<String, String>) {
    logEvent(AnalyticsEvent(name, params.toMap()))
}

fun CoreCmpAnalytics.setExperimentVariant(experimentId: String, variant: String) {
    setUserProperty("exp_$experimentId", variant)
}

fun CoreCmpCrash.recordBreadcrumb(message: String) {
    AnalyticsBreadcrumbs.add(message)
    log("breadcrumb:$message")
}
