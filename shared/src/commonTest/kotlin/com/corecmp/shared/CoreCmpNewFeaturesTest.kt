package com.corecmp.shared

import com.corecmp.shared.storage.SecureStorage
import com.corecmp.shared.network.ConnectivityObserver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class CoreCmpNewFeaturesTest {

    @Test
    fun testSecureStorage() {
        CoreCmp.init()
        val storage = SecureStorage()
        storage.clear()

        // Test string
        storage.putString("name", "CoreCmp")
        assertEquals("CoreCmp", storage.getString("name"))

        // Test int
        storage.putInt("version", 1)
        assertEquals(1, storage.getInt("version"))

        // Test boolean
        storage.putBoolean("isAwesome", true)
        assertTrue(storage.getBoolean("isAwesome"))

        // Test long
        storage.putLong("timestamp", 123456789L)
        assertEquals(123456789L, storage.getLong("timestamp"))

        // Test remove
        storage.remove("name")
        assertEquals("default", storage.getString("name", "default"))

        // Test clear
        storage.clear()
        assertEquals(0, storage.getInt("version"))
        assertFalse(storage.getBoolean("isAwesome"))
    }

    @Test
    fun testConnectivityObserver() {
        val observer = ConnectivityObserver()
        // Call properties to verify they resolve without crashing
        val online = observer.isOnline
        println("Is Online: $online")
    }
}
