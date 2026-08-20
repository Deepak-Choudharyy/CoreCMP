package com.corecmp.shared.platform

import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

actual fun readDeviceInfo(): DeviceInfoSnapshot {
    val bundle = NSBundle.mainBundle
    val version = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "unknown"
    val device = UIDevice.currentDevice
    return DeviceInfoSnapshot(
        appVersion = version,
        osVersion = "${device.systemName} ${device.systemVersion}",
        model = device.model,
    )
}

actual class DeviceInfoProvider actual constructor() {
    actual fun snapshot(): DeviceInfoSnapshot = readDeviceInfo()
}
