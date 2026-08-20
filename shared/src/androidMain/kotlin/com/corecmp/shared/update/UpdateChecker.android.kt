package com.corecmp.shared.update

import android.content.Intent
import android.net.Uri
import com.corecmp.shared.api.appContext
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import androidx.activity.ComponentActivity

actual fun openAppUpdate(url: String) {
    if (url.isBlank()) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    appContext.startActivity(intent)
}

actual fun triggerNativeInAppUpdate() {
    val activity = appContext as? ComponentActivity ?: return
    val manager = AppUpdateManagerFactory.create(appContext)
    val info = manager.appUpdateInfo
    info.addOnSuccessListener { appUpdateInfo ->
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
        ) {
            manager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                activity,
                9001
            )
        }
    }
}
