package com.corecmp.shared.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

object CoreCmpAccessibility {
    const val BACK = "Go back"
    const val CLOSE = "Close"
    const val SUBMIT = "Submit"
    const val SEARCH = "Search"
    const val MENU = "Open menu"
    const val SHARE = "Share"
    const val DOWNLOAD = "Download"
    const val DELETE = "Delete"
    const val EDIT = "Edit"
    const val CAMERA = "Take photo"
    const val GALLERY = "Choose from gallery"
    const val DOCUMENT = "Choose document"
}

fun Modifier.coreCmpContentDescription(
    label: String,
    role: Role? = null,
): Modifier = semantics {
    contentDescription = label
    if (role != null) this.role = role
}
