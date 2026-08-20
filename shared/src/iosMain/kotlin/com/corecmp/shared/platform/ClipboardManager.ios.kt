package com.corecmp.shared.platform

import platform.UIKit.UIPasteboard

actual class ClipboardManager actual constructor() {
    actual fun copy(text: String, label: String) {
        UIPasteboard.generalPasteboard.string = text
    }

    actual fun paste(): String? =
        UIPasteboard.generalPasteboard.string

    actual fun hasText(): Boolean =
        !UIPasteboard.generalPasteboard.string.isNullOrBlank()
}
