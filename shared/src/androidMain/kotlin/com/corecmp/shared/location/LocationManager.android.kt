package com.corecmp.shared.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager as AndroidLocationManager
import android.os.Bundle
import android.os.Looper
import com.corecmp.shared.api.appContext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.channels.awaitClose

actual class LocationManager actual constructor() {
    private val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager

    @SuppressLint("MissingPermission")
    actual suspend fun getCurrentLocation(onResult: (LatLng?) -> Unit) {
        try {
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }
            if (bestLocation != null) {
                onResult(LatLng(bestLocation.latitude, bestLocation.longitude))
                return
            }

            val provider = if (locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER)) {
                AndroidLocationManager.GPS_PROVIDER
            } else if (locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)) {
                AndroidLocationManager.NETWORK_PROVIDER
            } else {
                onResult(null)
                return
            }

            locationManager.requestSingleUpdate(
                provider,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        onResult(LatLng(location.latitude, location.longitude))
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                },
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            onResult(null)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(null)
        }
    }

    @SuppressLint("MissingPermission")
    actual fun observeLocation(intervalMillis: Long): Flow<LatLng?> = callbackFlow {
        if (!LocationPolicy.requireForegroundForContinuousUpdates()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(LatLng(location.latitude, location.longitude))
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            val provider = if (locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER)) {
                AndroidLocationManager.GPS_PROVIDER
            } else {
                AndroidLocationManager.NETWORK_PROVIDER
            }

            locationManager.requestLocationUpdates(
                provider,
                intervalMillis,
                0f,
                listener,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            trySend(null)
        } catch (e: Exception) {
            e.printStackTrace()
            trySend(null)
        }

        awaitClose {
            locationManager.removeUpdates(listener)
        }
    }.distinctUntilChanged()
}
