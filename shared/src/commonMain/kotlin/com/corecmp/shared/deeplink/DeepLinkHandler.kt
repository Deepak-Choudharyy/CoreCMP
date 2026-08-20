package com.corecmp.shared.deeplink

data class DeepLinkMatch(
    val path: String,
    val query: Map<String, String>,
    val pathParams: Map<String, String>,
)

typealias DeepLinkCallback = (DeepLinkMatch) -> Unit

class DeepLinkHandler {
    private val routes = linkedMapOf<String, DeepLinkCallback>()

    fun route(pattern: String, onMatch: DeepLinkCallback) {
        routes[pattern] = onMatch
    }

    fun handle(uri: String): Boolean {
        val parsed = parseUri(uri)
        routes.forEach { (pattern, callback) ->
            match(pattern, parsed)?.let {
                callback(it)
                return true
            }
        }
        return false
    }

    private data class ParsedUri(
        val scheme: String,
        val host: String,
        val path: String,
        val query: Map<String, String>,
        val segments: List<String>,
    )

    private fun parseUri(uri: String): ParsedUri {
        val noScheme = uri.substringAfter("://", uri)
        val pathAndQuery = noScheme.substringAfter('/', "")
        val host = noScheme.substringBefore('/', "")
        val path = "/" + pathAndQuery.substringBefore('?')
        val queryRaw = pathAndQuery.substringAfter('?', "")
        val query = if (queryRaw.isBlank()) {
            emptyMap()
        } else {
            queryRaw.split('&').mapNotNull { pair ->
                val key = pair.substringBefore('=')
                val value = pair.substringAfter('=', "")
                if (key.isNotBlank()) key to value else null
            }.toMap()
        }
        val segments = path.trim('/').split('/').filter { it.isNotBlank() }
        val scheme = if (uri.contains("://")) uri.substringBefore("://") else ""
        return ParsedUri(scheme, host, path, query, segments)
    }

    private fun match(pattern: String, parsed: ParsedUri): DeepLinkMatch? {
        val patternSegments = pattern.trim('/').split('/').filter { it.isNotBlank() }
        if (patternSegments.size != parsed.segments.size) return null

        val params = mutableMapOf<String, String>()
        patternSegments.forEachIndexed { index, segment ->
            val actual = parsed.segments[index]
            if (segment.startsWith("{") && segment.endsWith("}")) {
                params[segment.removeSurrounding("{", "}")] = actual
            } else if (segment != actual) {
                return null
            }
        }
        return DeepLinkMatch(parsed.path, parsed.query, params)
    }
}

expect fun readInitialDeepLink(): String?
