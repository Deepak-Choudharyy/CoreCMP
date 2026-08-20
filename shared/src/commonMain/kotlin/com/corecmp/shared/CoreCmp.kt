package com.corecmp.shared

import com.corecmp.shared.analytics.CoreCmpAnalytics
import com.corecmp.shared.analytics.CoreCmpCrash
import com.corecmp.shared.analytics.NoOpCoreCmpAnalytics
import com.corecmp.shared.analytics.NoOpCoreCmpCrash
import com.corecmp.shared.api.ApiClient
import com.corecmp.shared.api.ApiConfig
import com.corecmp.shared.api.CoreCmpLogger
import com.corecmp.shared.auth.AppleAuth
import com.corecmp.shared.auth.GoogleAuth
import com.corecmp.shared.auth.GuestModeManager
import com.corecmp.shared.auth.MultiAccountManager
import com.corecmp.shared.deeplink.DeepLinkHandler
import com.corecmp.shared.display.DisplaySettingsManager
import com.corecmp.shared.haptic.HapticManager
import com.corecmp.shared.location.Geocoder
import com.corecmp.shared.location.LocationManager
import com.corecmp.shared.network.ApiResponseCache
import com.corecmp.shared.network.ConnectivityObserver
import com.corecmp.shared.network.OfflineQueueManager
import com.corecmp.shared.network.RequestDeduplicator
import com.corecmp.shared.notification.InAppNotificationStore
import com.corecmp.shared.notification.PushTokenManager
import com.corecmp.shared.permission.PermissionManager
import com.corecmp.shared.picker.PlatformMediaPicker
import com.corecmp.shared.platform.ClipboardManager
import com.corecmp.shared.platform.DeviceInfoProvider
import com.corecmp.shared.platform.QrGenerator
import com.corecmp.shared.platform.QrScanner
import com.corecmp.shared.security.AppLockManager
import com.corecmp.shared.security.BackgroundLockManager
import com.corecmp.shared.security.ConsentManager
import com.corecmp.shared.security.SessionTimeoutManager
import com.corecmp.shared.share.ShareManager
import com.corecmp.shared.network.CoreCmpSocketManager
import com.corecmp.shared.storage.ApiCacheStorage
import com.corecmp.shared.storage.SocketLogStorage
import com.corecmp.shared.storage.FormDraftManager
import com.corecmp.shared.storage.LocalDataStore
import com.corecmp.shared.storage.PreferencesStore
import com.corecmp.shared.storage.SecureStorage
import com.corecmp.shared.theme.ThemeManager
import com.corecmp.shared.ui.Placeholder
import com.corecmp.shared.update.UpdateChecker
import com.corecmp.shared.upload.UploadManager
import com.corecmp.shared.upload.UploadQueueManager
import org.koin.core.component.inject

import com.corecmp.shared.internal.CoreCmpBuildInfo

object CoreCmp {
    val VERSION: String get() = CoreCmpBuildInfo.VERSION

    // --- Core platform services ---
    val location: LocationManager by lazy { LocationManager() }
    val permission: PermissionManager by lazy { PermissionManager() }
    val media: PlatformMediaPicker by lazy { PlatformMediaPicker() }
    val network: ConnectivityObserver by lazy { ConnectivityObserver() }
    val storage: SecureStorage by lazy { SecureStorage() }
    val haptics: HapticManager by lazy { HapticManager() }
    val share: ShareManager by lazy { ShareManager() }
    val geocoder: Geocoder = Geocoder

    // --- Display & theme ---
    val display: DisplaySettingsManager by lazy { DisplaySettingsManager() }
    val theme: ThemeManager by lazy { ThemeManager() }

    // --- Security ---
    val appLock: AppLockManager by lazy { AppLockManager() }
    val sessionTimeout: SessionTimeoutManager by lazy { SessionTimeoutManager() }
    val backgroundLock: BackgroundLockManager by lazy { BackgroundLockManager() }
    val consent: ConsentManager by lazy { ConsentManager() }

    // --- Storage & drafts ---
    val formDrafts: FormDraftManager by lazy { FormDraftManager() }
    val preferences: PreferencesStore by lazy { PreferencesStore() }
    val apiCache: ApiCacheStorage by lazy { ApiCacheStorage() }
    val socketLogCache: SocketLogStorage by lazy { SocketLogStorage() }
    val localStore: LocalDataStore by lazy { LocalDataStore("default") }
    val responseCache: ApiResponseCache by lazy { ApiResponseCache() }

    // --- Network & Sockets ---
    val api: ApiClient by lazy { ApiClient() }
    val socket: CoreCmpSocketManager by lazy { CoreCmpSocketManager() }
    val offlineQueue: OfflineQueueManager by lazy { OfflineQueueManager() }
    val requestDeduplicator: RequestDeduplicator by lazy { RequestDeduplicator() }

    // --- Upload (compress + fast upload) ---
    val upload: UploadManager by lazy { UploadManager() }
    val uploadQueue: UploadQueueManager by lazy { UploadQueueManager() }

    // --- Navigation & deep links ---
    val deepLinks: DeepLinkHandler by lazy { DeepLinkHandler() }

    // --- Updates ---
    val updates: UpdateChecker by lazy { UpdateChecker() }

    // --- Platform utilities ---
    val clipboard: ClipboardManager by lazy { ClipboardManager() }
    val deviceInfo: DeviceInfoProvider by lazy { DeviceInfoProvider() }
    val qrGenerator: QrGenerator by lazy { QrGenerator() }
    val qrScanner: QrScanner by lazy { QrScanner() }

    // --- Auth ---
    val googleAuth: GoogleAuth by lazy { GoogleAuth() }
    val appleAuth: AppleAuth by lazy { AppleAuth() }
    val accounts: MultiAccountManager by lazy { MultiAccountManager() }
    val guestMode: GuestModeManager by lazy { GuestModeManager() }

    // --- Notifications ---
    val pushToken: PushTokenManager by lazy { PushTokenManager() }
    val notifications: InAppNotificationStore by lazy { InAppNotificationStore() }

    // --- Analytics (host provides implementation) ---
    var analytics: CoreCmpAnalytics = NoOpCoreCmpAnalytics
    var crashReporter: CoreCmpCrash = NoOpCoreCmpCrash

    // --- Placeholders ---
    var defaultImagePlaceholder: Placeholder = Placeholder.LottieUrl(
        "https://lottie.host/a9be1300-ee73-471a-969d-6ebe32a5fb64/NT7azVsdv1.json"
    )
    var defaultApiLoadingPlaceholder: Placeholder = Placeholder.LottieUrl(
        "https://letterhead.ajmonic.com/loading.json"
    )

    fun setDefaultApiLoadingPlaceholder(source: Any) {
        Placeholder.from(source)?.let {
            defaultApiLoadingPlaceholder = it
        }
    }

    fun setDefaultImagePlaceholder(source: Any) {
        Placeholder.from(source)?.let {
            defaultImagePlaceholder = it
        }
    }

    fun setBaseUrl(
        baseUrl: String,
        token: String? = null,
        tokenProvider: (() -> String?)? = null,
        name: String = ApiConfig.DEFAULT_BASE_NAME,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        bodyParams: Map<String, Any?> = emptyMap()
    ) {
        ApiConfig.registerBaseUrl(
            name = name,
            baseUrl = baseUrl,
            token = token,
            tokenProvider = tokenProvider,
            defaultHeaders = headers,
            defaultQueryParams = queryParams,
            defaultBodyParams = bodyParams
        )
    }

    fun setAuthToken(token: String, name: String = ApiConfig.DEFAULT_BASE_NAME) {
        ApiConfig.updateToken(name, token)
    }

    fun setDebug(enabled: Boolean) {
        isDebugEnabled = enabled
    }

    fun configure(block: CoreCmpConfigBuilder.() -> Unit) {
        val builder = CoreCmpConfigBuilder().apply(block)

        if (builder.baseUrl.isNotBlank()) {
            setBaseUrl(
                baseUrl = builder.baseUrl,
                token = builder.token,
                tokenProvider = builder.tokenProvider,
                headers = builder.headers,
                queryParams = builder.queryParams,
                bodyParams = builder.bodyParams
            )
        }

        builder.apiLoadingPlaceholder?.let { setDefaultApiLoadingPlaceholder(it) }
        builder.imagePlaceholder?.let { setDefaultImagePlaceholder(it) }
        builder.socketUrl?.let { socket.connect(it) }
        isDebugEnabled = builder.isDebugEnabled
    }

    var isDebugEnabled: Boolean
        get() = CoreCmpLogger.isDebugEnabled
        set(value) { CoreCmpLogger.isDebugEnabled = value }

    fun init(context: Any? = null, settingsName: String = "core_cmp_prefs") {
        platformInit(context, settingsName)
    }
}

class CoreCmpConfigBuilder {
    var baseUrl: String = ""
    var token: String? = null
    var tokenProvider: (() -> String?)? = null
    var headers: Map<String, String> = emptyMap()
    var queryParams: Map<String, String> = emptyMap()
    var bodyParams: Map<String, Any?> = emptyMap()

    var socketUrl: String? = null
    var isDebugEnabled: Boolean = false

    var apiLoadingPlaceholder: Any? = null
    var imagePlaceholder: Any? = null
}

internal expect fun platformInit(context: Any?, settingsName: String)
internal expect fun getCacheDir(): String
