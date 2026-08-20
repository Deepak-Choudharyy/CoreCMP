package com.corecmp.shared.platform

actual fun readDeviceInfo(): DeviceInfoSnapshot =
    DeviceInfoSnapshot(
        appVersion = System.getProperty("java.version") ?: "unknown",
        osVersion = System.getProperty("os.name") ?: "JVM",
        model = System.getProperty("os.arch") ?: "desktop",
    )

actual class DeviceInfoProvider actual constructor() {
    actual fun snapshot(): DeviceInfoSnapshot = readDeviceInfo()
}
