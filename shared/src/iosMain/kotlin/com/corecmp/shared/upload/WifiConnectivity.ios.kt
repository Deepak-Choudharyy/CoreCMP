package com.corecmp.shared.upload

import platform.Network.nw_interface_type_wifi
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_uses_interface_type
import platform.darwin.dispatch_get_main_queue

internal actual fun isOnWifi(): Boolean {
    var onWifi = false
    val monitor = nw_path_monitor_create()
    val sem = platform.darwin.dispatch_semaphore_create(0)
    nw_path_monitor_set_update_handler(monitor) { path ->
        onWifi = nw_path_get_status(path) == nw_path_status_satisfied &&
            nw_path_uses_interface_type(path, nw_interface_type_wifi)
        platform.darwin.dispatch_semaphore_signal(sem)
    }
    nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
    nw_path_monitor_start(monitor)
    platform.darwin.dispatch_semaphore_wait(
        sem,
        platform.darwin.dispatch_time(platform.darwin.DISPATCH_TIME_NOW, 100_000_000L),
    )
    nw_path_monitor_cancel(monitor)
    return onWifi
}
