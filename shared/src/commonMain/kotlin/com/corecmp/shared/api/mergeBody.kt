package com.corecmp.shared.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

fun mergeBody(
    baseName: String,
    body: Map<String, Any?>?
): Map<String, Any?> {

    val config: BaseConfig = ApiConfig.getConfig(baseName)

    val finalMap: MutableMap<String, Any?> = mutableMapOf()

    finalMap.putAll(
        config.defaultBodyParams
    )

    if (body != null) {
        finalMap.putAll(body)
    }

    return finalMap
}

@PublishedApi
internal inline fun <reified Req : Any> mergeRequestBody(base: String, body: Req?): Req? {
    val config = ApiConfig.getConfig(base)
    if (config.defaultBodyParams.isEmpty()) return body

    val mergedJson = buildJsonObject {
        config.defaultBodyParams.forEach { (key, value) ->
            put(
                key,
                when (value) {
                    null -> JsonNull
                    is JsonElement -> value
                    is Boolean -> JsonPrimitive(value)
                    is Number -> JsonPrimitive(value)
                    else -> JsonPrimitive(value.toString())
                }
            )
        }
        if (body != null) {
            json.parseToJsonElement(json.encodeToString(body)).jsonObject.forEach { (key, value) ->
                put(key, value)
            }
        }
    }

    return if (body == null && mergedJson.isEmpty()) {
        null
    } else {
        json.decodeFromString(mergedJson.toString())
    }
}