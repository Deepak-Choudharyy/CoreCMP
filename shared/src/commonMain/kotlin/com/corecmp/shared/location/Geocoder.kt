package com.corecmp.shared.location

import com.corecmp.shared.api.CoreCmpLogger
import com.corecmp.shared.api.HttpClientProvider
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class PlaceResult(
    val name: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double
)

sealed class GeocoderResult<out T> {
    data class Success<T>(val data: T) : GeocoderResult<T>()
    data class Error(val message: String) : GeocoderResult<Nothing>()
}

object Geocoder {
    private val json = Json { ignoreUnknownKeys = true }

    var baseUrl: String = "https://nominatim.openstreetmap.org"
    var userAgent: String = "CoreCmp/1.0"
    var language: String = "en"

    suspend fun search(query: String): GeocoderResult<List<PlaceResult>> {
        if (query.isBlank()) return GeocoderResult.Success(emptyList())
        return try {
            val responseText = HttpClientProvider.client.get("$baseUrl/search") {
                header("User-Agent", userAgent)
                parameter("q", query)
                parameter("format", "json")
                parameter("limit", "5")
                parameter("addressdetails", "1")
                parameter("accept-language", language)
            }.bodyAsText()

            val array = json.parseToJsonElement(responseText).jsonArray
            val results = array.mapNotNull { item ->
                val obj = item.jsonObject
                val displayName = obj["display_name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val lat = obj["lat"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: return@mapNotNull null
                val lon = obj["lon"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: return@mapNotNull null
                if (lat == 0.0 && lon == 0.0) return@mapNotNull null
                val name = obj["name"]?.jsonPrimitive?.content ?: displayName.substringBefore(",")
                PlaceResult(
                    name = name,
                    displayName = displayName,
                    latitude = lat,
                    longitude = lon
                )
            }
            GeocoderResult.Success(results)
        } catch (e: Exception) {
            CoreCmpLogger.d("Geocoder search failed: ${e.message}")
            GeocoderResult.Error(e.message ?: "Search failed")
        }
    }

    suspend fun reverse(lat: Double, lon: Double): GeocoderResult<PlaceResult?> {
        if (lat == 0.0 && lon == 0.0) {
            return GeocoderResult.Error("Invalid coordinates")
        }
        return try {
            val responseText = HttpClientProvider.client.get("$baseUrl/reverse") {
                header("User-Agent", userAgent)
                parameter("lat", lat.toString())
                parameter("lon", lon.toString())
                parameter("format", "json")
                parameter("addressdetails", "1")
                parameter("accept-language", language)
            }.bodyAsText()

            val obj = json.parseToJsonElement(responseText).jsonObject
            val displayName = obj["display_name"]?.jsonPrimitive?.content
                ?: return GeocoderResult.Success(null)
            val name = obj["name"]?.jsonPrimitive?.content ?: displayName.substringBefore(",")
            GeocoderResult.Success(
                PlaceResult(
                    name = name,
                    displayName = displayName,
                    latitude = lat,
                    longitude = lon
                )
            )
        } catch (e: Exception) {
            CoreCmpLogger.d("Geocoder reverse failed: ${e.message}")
            GeocoderResult.Error(e.message ?: "Reverse geocoding failed")
        }
    }
}
