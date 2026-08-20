package com.corecmp.shared.network

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_get_status
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_monitor_set_queue
import platform.darwin.dispatch_get_main_queue

actual class ConnectivityObserver actual constructor() {

    actual val isOnline: Boolean
        get() {
            var online = false
            val monitor = nw_path_monitor_create()
            val sem = platform.darwin.dispatch_semaphore_create(0)
            nw_path_monitor_set_update_handler(monitor) { path ->
                online = nw_path_get_status(path) == nw_path_status_satisfied
                platform.darwin.dispatch_semaphore_signal(sem)
            }
            nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
            nw_path_monitor_start(monitor)
            platform.darwin.dispatch_semaphore_wait(sem, platform.darwin.dispatch_time(platform.darwin.DISPATCH_TIME_NOW, 100_000_000L))
            nw_path_monitor_cancel(monitor)
            return online
        }

    actual val connectivityFlow: Flow<Boolean>
        get() = callbackFlow {
            val monitor = nw_path_monitor_create()
            nw_path_monitor_set_update_handler(monitor) { path ->
                val online = nw_path_get_status(path) == nw_path_status_satisfied
                trySend(online)
            }
            nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
            nw_path_monitor_start(monitor)
            awaitClose {
                nw_path_monitor_cancel(monitor)
            }
        }.distinctUntilChanged()
}
