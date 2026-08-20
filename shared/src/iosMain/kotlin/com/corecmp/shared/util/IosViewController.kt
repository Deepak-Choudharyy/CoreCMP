package com.corecmp.shared.util

import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

fun topViewController(): UIViewController? {
    val application = UIApplication.sharedApplication
    val window = (application.keyWindow as? UIWindow)
        ?: application.windows.firstOrNull() as? UIWindow
    var controller = window?.rootViewController ?: return null
    while (controller.presentedViewController != null) {
        controller = controller.presentedViewController!!
    }
    return controller
}
