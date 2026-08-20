package com.corecmp.shared.navigation

data class PushPayload(
    val route: String? = null,
    val params: Map<String, String> = emptyMap(),
)

class PushNotificationRouter {
    fun routeFromPayload(payload: PushPayload): String? {
        payload.route?.let { return it }
        val deepLink = payload.params["deep_link"] ?: payload.params["url"]
        return deepLink?.let { uri ->
            val path = uri.substringAfter("://").substringAfter('/', missingDelimiterValue = "")
            if (path.startsWith('/')) path else "/$path"
        }
    }

    fun handleTap(payload: PushPayload, navigate: (String) -> Unit): Boolean {
        val route = routeFromPayload(payload) ?: return false
        navigate(route)
        return true
    }
}
