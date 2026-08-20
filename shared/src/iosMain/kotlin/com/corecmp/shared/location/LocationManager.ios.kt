package com.corecmp.shared.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject

actual class LocationManager actual constructor() {

    private var currentLocationDelegate: NSObject? = null
    private var observeDelegate: NSObject? = null
    private var observeManager: CLLocationManager? = null

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getCurrentLocation(onResult: (LatLng?) -> Unit) {
        val manager = CLLocationManager()
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val location = didUpdateLocations.firstOrNull() as? CLLocation
                if (location != null) {
                    val lat = location.coordinate.useContents { latitude }
                    val lng = location.coordinate.useContents { longitude }
                    onResult(LatLng(lat, lng))
                } else {
                    onResult(null)
                }
                manager.stopUpdatingLocation()
                currentLocationDelegate = null
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                onResult(null)
                manager.stopUpdatingLocation()
                currentLocationDelegate = null
            }
        }
        currentLocationDelegate = delegate
        manager.delegate = delegate
        manager.requestWhenInUseAuthorization()
        manager.startUpdatingLocation()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun observeLocation(intervalMillis: Long): Flow<LatLng?> = callbackFlow {
        if (!LocationPolicy.requireForegroundForContinuousUpdates()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val manager = CLLocationManager()
        observeManager = manager
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val location = didUpdateLocations.firstOrNull() as? CLLocation
                if (location != null) {
                    val lat = location.coordinate.useContents { latitude }
                    val lng = location.coordinate.useContents { longitude }
                    trySend(LatLng(lat, lng))
                }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                trySend(null)
            }
        }
        observeDelegate = delegate
        manager.delegate = delegate
        manager.distanceFilter = 10.0
        manager.desiredAccuracy = platform.CoreLocation.kCLLocationAccuracyBest
        manager.requestWhenInUseAuthorization()
        manager.startUpdatingLocation()

        val throttleJob = launch {
            while (isActive) {
                delay(intervalMillis.coerceAtLeast(1000L))
            }
        }

        awaitClose {
            throttleJob.cancel()
            manager.stopUpdatingLocation()
            observeDelegate = null
            observeManager = null
        }
    }.distinctUntilChanged()
}
