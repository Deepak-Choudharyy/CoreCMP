package com.corecmp.shared.permission

fun interface PermissionCallback {
    fun onResult(
        results: List<PermissionResult>
    )
}