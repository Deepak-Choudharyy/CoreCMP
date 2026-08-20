package com.corecmp.shared.permission

import androidx.compose.runtime.Composable

actual class PermissionManager actual constructor() {

    @Composable
    actual fun RegisterPermissionLauncher() {}

    actual suspend fun requestPermissions(
        permissions: List<AppPermission>,
        callback: PermissionCallback
    ) {
        val results = permissions.map {
            PermissionResult(
                permission = it,
                status = PermissionStatus.GRANTED
            )
        }
        callback.onResult(results)
    }
}