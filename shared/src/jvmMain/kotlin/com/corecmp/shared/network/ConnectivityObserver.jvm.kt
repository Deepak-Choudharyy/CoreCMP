package com.corecmp.shared.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.net.InetSocketAddress
import java.net.Socket

actual class ConnectivityObserver actual constructor() {

    actual val isOnline: Boolean
        get() = checkConnection()

    actual val connectivityFlow: Flow<Boolean>
        get() = flow {
            while (true) {
                emit(checkConnection())
                delay(5000) // Poll every 5 seconds
            }
        }.distinctUntilChanged()

    private fun checkConnection(): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
