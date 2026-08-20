package com.corecmp.shared.api

data class BaseConfig(
    val baseUrl: String,
    val token: String?,
    val tokenProvider: (() -> String?)? = null,
    val defaultHeaders: Map<String, String>,
    val defaultQueryParams: Map<String, String>,
    val defaultBodyParams: Map<String, Any?>
)

object ApiConfig {
    const val DEFAULT_BASE_NAME = "default"

    private val configs: MutableMap<String, BaseConfig> = mutableMapOf()
    private val mockResponses: MutableMap<String, String> = mutableMapOf()

    fun registerMockResponse(endpoint: String, json: String) {
        mockResponses[endpoint] = json
    }

    fun getMockResponse(endpoint: String): String? {
        return mockResponses[endpoint]
    }

    fun removeMockResponse(endpoint: String) {
        mockResponses.remove(endpoint)
    }

    fun clearMockResponses() {
        mockResponses.clear()
    }

    fun registerBaseUrl(
        name: String = DEFAULT_BASE_NAME,
        baseUrl: String,
        token: String? = null,
        tokenProvider: (() -> String?)? = null,
        defaultHeaders: Map<String, String> = emptyMap(),
        defaultQueryParams: Map<String, String> = emptyMap(),
        defaultBodyParams: Map<String, Any?> = emptyMap()
    ) {
        configs[name] = BaseConfig(
            baseUrl = baseUrl,
            token = token,
            tokenProvider = tokenProvider,
            defaultHeaders = defaultHeaders,
            defaultQueryParams = defaultQueryParams,
            defaultBodyParams = defaultBodyParams
        )
    }

    fun updateToken(
        name: String = DEFAULT_BASE_NAME,
        token: String
    ) {
        val config = configs[name] ?: return
        configs[name] = config.copy(
            token = token
        )
    }

    fun getConfig(name: String = DEFAULT_BASE_NAME): BaseConfig {
        val config = configs[name] ?: configs[DEFAULT_BASE_NAME]
        if (config != null) return config

        error(
            """
            Base URL not registered for key '$name'.
            Please call CoreCmp.configure { baseUrl = "https://api.example.com" } or CoreCmp.setBaseUrl(...) before making API requests.
            Registered bases: [${configs.keys.joinToString()}]
            """.trimIndent()
        )
    }

    fun isRegistered(name: String = DEFAULT_BASE_NAME): Boolean {
        return configs.containsKey(name)
    }

    fun clear(name: String = DEFAULT_BASE_NAME) {
        configs.remove(name)
    }

    fun clearAll() {
        configs.clear()
    }
}