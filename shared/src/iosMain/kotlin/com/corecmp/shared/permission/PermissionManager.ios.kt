package com.corecmp.shared.permission

import androidx.compose.runtime.Composable
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Contacts.CNAuthorizationStatusAuthorized
import platform.Contacts.CNAuthorizationStatusDenied
import platform.Contacts.CNAuthorizationStatusNotDetermined
import platform.Contacts.CNAuthorizationStatusRestricted
import platform.Contacts.CNContactStore
import platform.Contacts.CNEntityType
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class PermissionManager actual constructor() {

    private val locationRequester = IosLocationPermissionRequester()

    @Composable
    actual fun RegisterPermissionLauncher() {}

    actual suspend fun requestPermissions(
        permissions: List<AppPermission>,
        callback: PermissionCallback
    ) {
        val results = permissions.map { requestPermission(it) }
        callback.onResult(results)
    }

    private suspend fun requestPermission(permission: AppPermission): PermissionResult {
        val status = when (permission) {
            AppPermission.GALLERY, AppPermission.STORAGE -> PermissionStatus.GRANTED
            AppPermission.CAMERA -> requestAvPermission(AVMediaTypeVideo ?: "vide")
            AppPermission.MICROPHONE -> requestAvPermission(AVMediaTypeAudio ?: "soun")
            AppPermission.LOCATION -> locationRequester.request()
            AppPermission.NOTIFICATION -> requestNotificationPermission()
            AppPermission.CONTACTS -> requestContactsPermission()
        }
        return PermissionResult(permission, status)
    }

    private suspend fun requestAvPermission(mediaType: String): PermissionStatus {
        return when (val status = AVCaptureDevice.authorizationStatusForMediaType(mediaType)) {
            AVAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
            AVAuthorizationStatusDenied -> PermissionStatus.PERMANENTLY_DENIED
            AVAuthorizationStatusRestricted -> PermissionStatus.DENIED
            AVAuthorizationStatusNotDetermined -> suspendCancellableCoroutine { cont ->
                AVCaptureDevice.requestAccessForMediaType(mediaType) { granted ->
                    cont.resume(
                        if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
                    )
                }
            }
            else -> PermissionStatus.DENIED
        }
    }

    private suspend fun requestNotificationPermission(): PermissionStatus {
        return suspendCancellableCoroutine { cont ->
            UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            ) { granted, _ ->
                cont.resume(if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED)
            }
        }
    }

    private suspend fun requestContactsPermission(): PermissionStatus {
        val entityType = CNEntityType.CNEntityTypeContacts
        return when (val status = CNContactStore.authorizationStatusForEntityType(entityType)) {
            CNAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
            CNAuthorizationStatusDenied -> PermissionStatus.PERMANENTLY_DENIED
            CNAuthorizationStatusRestricted -> PermissionStatus.DENIED
            CNAuthorizationStatusNotDetermined -> suspendCancellableCoroutine { cont ->
                CNContactStore().requestAccessForEntityType(entityType) { granted, _ ->
                    cont.resume(
                        if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
                    )
                }
            }
            else -> PermissionStatus.DENIED
        }
    }
}

private class IosLocationPermissionRequester : NSObject(), CLLocationManagerDelegateProtocol {
    private var manager: CLLocationManager? = null
    private var continuation: ((PermissionStatus) -> Unit)? = null

    suspend fun request(): PermissionStatus = suspendCancellableCoroutine { cont ->
        val current = CLLocationManager.authorizationStatus()
        when (current) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> cont.resume(PermissionStatus.GRANTED)
            kCLAuthorizationStatusDenied -> cont.resume(PermissionStatus.PERMANENTLY_DENIED)
            kCLAuthorizationStatusRestricted -> cont.resume(PermissionStatus.DENIED)
            kCLAuthorizationStatusNotDetermined -> {
                continuation = { status -> cont.resume(status) }
                val locationManager = CLLocationManager()
                manager = locationManager
                locationManager.delegate = this
                locationManager.requestWhenInUseAuthorization()
            }
            else -> cont.resume(PermissionStatus.DENIED)
        }
    }

    override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: Int) {
        deliverStatus()
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        deliverStatus()
    }

    private fun deliverStatus() {
        val callback = continuation ?: return
        continuation = null
        callback(mapLocationStatus(CLLocationManager.authorizationStatus()))
        this.manager?.stopUpdatingLocation()
        this.manager = null
    }

    private fun mapLocationStatus(status: Int): PermissionStatus = when (status) {
        kCLAuthorizationStatusAuthorizedWhenInUse,
        kCLAuthorizationStatusAuthorizedAlways -> PermissionStatus.GRANTED
        kCLAuthorizationStatusDenied -> PermissionStatus.PERMANENTLY_DENIED
        kCLAuthorizationStatusRestricted -> PermissionStatus.DENIED
        else -> PermissionStatus.DENIED
    }
}
