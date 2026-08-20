package com.corecmp.shared.api

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
expect fun provideHttpClient(): HttpClient

expect fun provideSettings() : Settings
expect fun provideSqlDriver() : app.cash.sqldelight.db.SqlDriver

fun initSettingsName(name: String) {
    SETTINGS_NAME = name
}

object HttpClientProvider {
    val client: HttpClient by lazy {

        provideHttpClient()
    }
}