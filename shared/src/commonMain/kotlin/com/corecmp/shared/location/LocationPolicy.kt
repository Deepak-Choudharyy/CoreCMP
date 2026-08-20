package com.corecmp.shared.location

/**
 * Play Store / App Store policy: continuous location must run only while app is foreground.
 * Host app sets [isInForeground] via lifecycle observer.
 */
object LocationPolicy {
    var isInForeground: Boolean = true

    fun requireForegroundForContinuousUpdates(): Boolean = isInForeground
}
