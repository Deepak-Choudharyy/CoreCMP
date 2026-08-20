package com.corecmp.shared.upload

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.corecmp.shared.api.appContext

internal actual fun isOnWifi(): Boolean {
    val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}
