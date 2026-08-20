package com.corecmp.shared.api

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

import io.ktor.client.plugins.websocket.WebSockets

actual fun provideHttpClient(): HttpClient {

    return HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            }
        }

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
            logger = Logger.DEFAULT
            level = if (CoreCmpLogger.isDebugEnabled) LogLevel.INFO else LogLevel.NONE
        }
    }
}
lateinit var appContext: Context

fun isAppContextInitialized(): Boolean = ::appContext.isInitialized

fun initCoreCmp(context: Context, name : String) {
    appContext = context
    SETTINGS_NAME = name

    try {
        val pref = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        if (!pref.contains("core_cmp_initialized")) {
            pref.edit().putString("core_cmp_initialized", "true").apply()
        }
    } catch (_: Exception) {}
}


actual fun provideSettings(): Settings {
    if (!::appContext.isInitialized) {
        error("Call CoreCmp.init(context) in Application.onCreate() before starting Koin on Android.")
    }

    val pref = appContext.getSharedPreferences(
        SETTINGS_NAME,
        Context.MODE_PRIVATE
    )

    return SharedPreferencesSettings(pref)
}

actual fun provideSqlDriver(): app.cash.sqldelight.db.SqlDriver {
    if (!::appContext.isInitialized) {
        error("Call CoreCmp.init(context) in Application.onCreate() before using DB on Android.")
    }
    return com.corecmp.shared.db.DatabaseDriverFactory(appContext).createDriver()
}