package com.corecmp.shared.platform

expect class ClipboardManager() {
    fun copy(text: String, label: String = "text")
    fun paste(): String?
    fun hasText(): Boolean
}
