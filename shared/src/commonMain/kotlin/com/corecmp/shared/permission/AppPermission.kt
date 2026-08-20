package com.corecmp.shared.permission

enum class AppPermission {
    CAMERA,
    GALLERY,
    LOCATION,
    MICROPHONE,
    STORAGE,
    NOTIFICATION,
    CONTACTS;

    /** Permissions satisfied by a system picker — no runtime prompt on Android/iOS. */
    val isPickerOnly: Boolean
        get() = this == GALLERY || this == STORAGE
}

data class PermissionResult(

    val permission: AppPermission,
    val status: PermissionStatus

)

enum class PermissionStatus {

    GRANTED,
    DENIED,
    PERMANENTLY_DENIED

}