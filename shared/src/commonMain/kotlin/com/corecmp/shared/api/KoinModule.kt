package com.corecmp.shared.api

import com.corecmp.shared.CoreCmp
import com.corecmp.shared.network.EazySocketManager
import com.corecmp.shared.storage.SocketLogStorage
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule = module {
    single { json }
    single { provideSettings() }
    single { SharedViewModel(settings = get(), json = get()) }

    single { ApiClient() }
    single { SocketLogStorage() }
    single { CoreCmp.socket }
    single { com.corecmp.shared.db.CoreCmpDatabase(provideSqlDriver()) }
}
var SETTINGS_NAME = "app_settings"


fun eazyModule(): Module = coreModule

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    prettyPrint = true
    explicitNulls = false
    coerceInputValues = true
}