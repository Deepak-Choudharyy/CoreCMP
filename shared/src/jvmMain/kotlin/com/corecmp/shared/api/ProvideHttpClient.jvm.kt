package com.corecmp.shared.api

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.util.prefs.Preferences

import io.ktor.client.plugins.websocket.WebSockets

actual fun provideHttpClient(): HttpClient {

    return HttpClient(CIO) {

        install(WebSockets)

        install(ContentNegotiation) {

            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }

        install(Logging) {
            level = if (CoreCmpLogger.isDebugEnabled) LogLevel.INFO else LogLevel.NONE
            logger = object : Logger {
                override fun log(message: String) {
                    CoreCmpLogger.d("KTOR → $message")
                }
            }
        }
    }

}

actual fun provideSettings() : Settings {
    val delegate = Preferences.userRoot().node(SETTINGS_NAME)
    return PreferencesSettings(delegate)
}

actual fun provideSqlDriver(): app.cash.sqldelight.db.SqlDriver {
    return com.corecmp.shared.db.DatabaseDriverFactory().createDriver()
}