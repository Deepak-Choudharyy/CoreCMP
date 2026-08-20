package com.corecmp.shared.permission

/**
 * Maps CoreCmp features to permissions host apps must declare.
 * Library manifest does NOT merge sensitive permissions — avoids Play/App Store rejection
 * for permissions the host app does not use.
 */
object PermissionManifest {

    data class AndroidPermission(
        val permission: String,
        val maxSdkVersion: Int? = null,
        val feature: String,
    )

    data class IosUsageString(
        val key: String,
        val example: String,
        val feature: String,
    )

    fun androidPermissionsFor(features: Set<AppPermission>): List<AndroidPermission> =
        features.flatMap { it.androidManifestEntries() }.distinctBy { it.permission }

    fun iosUsageStringsFor(features: Set<AppPermission>): List<IosUsageString> =
        features.flatMap { it.iosUsageStrings() }.distinctBy { it.key }

    private fun AppPermission.androidManifestEntries(): List<AndroidPermission> = when (this) {
        AppPermission.CAMERA -> listOf(
            AndroidPermission("android.permission.CAMERA", feature = "Camera capture"),
        )
        AppPermission.LOCATION -> listOf(
            AndroidPermission("android.permission.ACCESS_FINE_LOCATION", feature = "GPS location"),
            AndroidPermission("android.permission.ACCESS_COARSE_LOCATION", feature = "Network location"),
        )
        AppPermission.MICROPHONE -> listOf(
            AndroidPermission("android.permission.RECORD_AUDIO", feature = "Audio recording"),
        )
        AppPermission.NOTIFICATION -> listOf(
            AndroidPermission(
                permission = "android.permission.POST_NOTIFICATIONS",
                feature = "Push notifications (Android 13+)",
            ),
        )
        AppPermission.CONTACTS -> listOf(
            AndroidPermission("android.permission.READ_CONTACTS", feature = "Contact picker"),
        )
        AppPermission.GALLERY, AppPermission.STORAGE -> emptyList()
    }

    private fun AppPermission.iosUsageStrings(): List<IosUsageString> = when (this) {
        AppPermission.CAMERA -> listOf(
            IosUsageString(
                key = "NSCameraUsageDescription",
                example = "We need camera access to capture KYC documents.",
                feature = "Camera capture",
            ),
        )
        AppPermission.MICROPHONE -> listOf(
            IosUsageString(
                key = "NSMicrophoneUsageDescription",
                example = "We need microphone access for voice notes.",
                feature = "Audio recording",
            ),
        )
        AppPermission.LOCATION -> listOf(
            IosUsageString(
                key = "NSLocationWhenInUseUsageDescription",
                example = "We use your location to find nearby branches.",
                feature = "Location while app is in use",
            ),
        )
        AppPermission.CONTACTS -> listOf(
            IosUsageString(
                key = "NSContactsUsageDescription",
                example = "We use contacts to help you invite nominees.",
                feature = "Contact access",
            ),
        )
        AppPermission.NOTIFICATION -> emptyList()
        AppPermission.GALLERY, AppPermission.STORAGE -> emptyList()
    }
}
