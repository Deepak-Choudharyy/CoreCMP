package com.corecmp.shared.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class LocationManager actual constructor() {
    actual suspend fun getCurrentLocation(onResult: (LatLng?) -> Unit) {
        onResult(null)
    }

    actual fun observeLocation(intervalMillis: Long): Flow<LatLng?> {
        return flowOf(null)
    }
}
