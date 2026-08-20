package com.corecmp.shared.security

import platform.Foundation.NSFileManager

actual fun isDeviceCompromised(): Boolean {
    val jailbreakPaths = listOf(
        "/Applications/Cydia.app",
        "/Library/MobileSubstrate/MobileSubstrate.dylib",
        "/bin/bash",
        "/usr/sbin/sshd",
    )
    val fm = NSFileManager.defaultManager
    return jailbreakPaths.any { fm.fileExistsAtPath(it) }
}
