package com.corecmp.shared.state

/**
 * One-shot event wrapper for UI actions (navigation, snackbars, etc.).
 * Consumed events should not re-fire on configuration change.
 */
class Event<out T>(private val content: T) {
    private var handled = false

    fun getContentIfNotHandled(): T? =
        if (handled) null else content.also { handled = true }

    fun peekContent(): T = content
}

fun <T> T.asEvent(): Event<T> = Event(this)
