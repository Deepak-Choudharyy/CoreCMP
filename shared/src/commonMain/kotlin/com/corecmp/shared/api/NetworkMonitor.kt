package com.corecmp.shared.api

import com.corecmp.shared.picker.PickedFile

enum class ApiPriority {
    HIGH,
    NORMAL,
    LOW
}

data class RequestOptions(
    val priority: ApiPriority = ApiPriority.NORMAL,
    val retryOnConnection: Boolean = false,
    val useCacheFallback: Boolean = true
)



enum class BodyType {
    JSON,
    FORM_DATA,
    FORM_URLENCODED
}

data class FilePart(
    val name: String,
    val file: PickedFile?
)