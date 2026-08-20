package com.corecmp.shared.api

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

import io.ktor.client.plugins.websocket.WebSockets

actual fun provideHttpClient(): HttpClient {

    return HttpClient(Darwin) {

        install(WebSockets)

        install(ContentNegotiation) {

            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                }
            )
        }

        install(Logging) {
            level = if (CoreCmpLogger.isDebugEnabled) LogLevel.INFO else LogLevel.NONE
        }

    }
}

actual fun provideSettings(): Settings {
    val delegate = NSUserDefaults(suiteName = SETTINGS_NAME) ?: NSUserDefaults.standardUserDefaults
    return NSUserDefaultsSettings(delegate)
}

actual fun provideSqlDriver(): app.cash.sqldelight.db.SqlDriver {
    return com.corecmp.shared.db.DatabaseDriverFactory().createDriver()
}