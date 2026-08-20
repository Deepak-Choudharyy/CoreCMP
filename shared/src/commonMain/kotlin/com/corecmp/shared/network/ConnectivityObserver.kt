package com.corecmp.shared.network

import kotlinx.coroutines.flow.Flow

expect class ConnectivityObserver() {
    val isOnline: Boolean
    val connectivityFlow: Flow<Boolean>
}
