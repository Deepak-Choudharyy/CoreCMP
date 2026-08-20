package com.corecmp.shared.security

import java.io.File

actual fun isDeviceCompromised(): Boolean {
    val rootPaths = listOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
    )
    return rootPaths.any { File(it).exists() }
}
