package com.corecmp.shared.permission

import androidx.compose.runtime.Composable

expect class PermissionManager() {
    suspend fun requestPermissions(
        permissions: List<AppPermission>,
        callback: PermissionCallback
    )

    @Composable
    fun RegisterPermissionLauncher()
}