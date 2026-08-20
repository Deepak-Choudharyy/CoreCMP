package com.corecmp.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController

fun NavController.setNavResult(key: String, value: Any?) {
    previousBackStackEntry?.savedStateHandle?.set(key, value)
}

@Composable
fun <T> NavController.rememberNavResult(
    key: String,
    onResult: (T) -> Unit,
) {
    val savedStateHandle = currentBackStackEntry?.savedStateHandle ?: return
    LaunchedEffect(savedStateHandle, key) {
        savedStateHandle.getStateFlow<T?>(key, null).collect { value ->
            if (value != null) {
                onResult(value)
                savedStateHandle.remove<T>(key)
            }
        }
    }
}
