package com.corecmp.shared.permission

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

actual class PermissionManager actual constructor() {
    private var launcher: ActivityResultLauncher<Array<String>>? = null
    private var callback: PermissionCallback? = null

    @Composable
    actual fun RegisterPermissionLauncher() {
        launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val list = result.map {
                PermissionResult(
                    permission = it.key.toPermissionEnum(),
                    status = if (it.value) PermissionStatus.GRANTED
                    else PermissionStatus.DENIED
                )
            }
            callback?.onResult(list)
        }
    }

    actual suspend fun requestPermissions(
        permissions: List<AppPermission>,
        callback: PermissionCallback
    ) {
        val autoGranted = permissions
            .filter { it.isGrantedWithoutPrompt() }
            .map { PermissionResult(it, PermissionStatus.GRANTED) }

        val runtimePermissions = permissions.filter { !it.isGrantedWithoutPrompt() }

        if (runtimePermissions.isEmpty()) {
            callback.onResult(autoGranted)
            return
        }

        this.callback = { runtimeResults ->
            callback.onResult(autoGranted + runtimeResults)
        }
        launcher?.launch(
            runtimePermissions.map { it.toAndroidPermission() }.toTypedArray()
        )
    }
}

fun AppPermission.toAndroidPermission(): String {
    return when (this) {
        AppPermission.CAMERA -> Manifest.permission.CAMERA
        AppPermission.GALLERY, AppPermission.STORAGE -> error(
            "$this uses the system picker and does not require a runtime permission"
        )
        AppPermission.LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
        AppPermission.MICROPHONE -> Manifest.permission.RECORD_AUDIO
        AppPermission.NOTIFICATION -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.POST_NOTIFICATIONS
            } else {
                error("POST_NOTIFICATIONS is not required below Android 13")
            }
        }
        AppPermission.CONTACTS -> Manifest.permission.READ_CONTACTS
    }
}

fun String.toPermissionEnum(): AppPermission {
    return when (this) {
        Manifest.permission.CAMERA -> AppPermission.CAMERA
        Manifest.permission.ACCESS_FINE_LOCATION -> AppPermission.LOCATION
        Manifest.permission.RECORD_AUDIO -> AppPermission.MICROPHONE
        Manifest.permission.POST_NOTIFICATIONS -> AppPermission.NOTIFICATION
        Manifest.permission.READ_CONTACTS -> AppPermission.CONTACTS
        else -> AppPermission.STORAGE
    }
}

private fun AppPermission.isGrantedWithoutPrompt(): Boolean {
    return isPickerOnly ||
        (this == AppPermission.NOTIFICATION && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
}
