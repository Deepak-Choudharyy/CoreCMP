package com.corecmp.shared.platform

import android.os.Build
import com.corecmp.shared.api.appContext

actual fun readDeviceInfo(): DeviceInfoSnapshot {
    val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
    val version = packageInfo.versionName ?: "unknown"
    return DeviceInfoSnapshot(
        appVersion = version,
        osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        model = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
    )
}

actual class DeviceInfoProvider actual constructor() {
    actual fun snapshot(): DeviceInfoSnapshot = readDeviceInfo()
}
