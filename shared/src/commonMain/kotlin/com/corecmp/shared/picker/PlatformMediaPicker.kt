package com.corecmp.shared.picker

import androidx.compose.runtime.Composable

expect class PlatformMediaPicker() {
    @Composable
    fun RegisterLaunchers()
    fun launch(
        type: PickerType,
        documentConfig: DocumentConfig? = null,
        onResult: (PickedFile?) -> Unit
    )
}