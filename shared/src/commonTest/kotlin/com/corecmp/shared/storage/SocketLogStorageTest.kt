package com.corecmp.shared.storage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SocketLogStorageTest {

    @Test
    fun testLogEventAndRetrieval() {
        val storage = SocketLogStorage(maxCacheSizeBytes = 10 * 1024 * 1024L)
        storage.clearAll()

        val url = "wss://api.example.com/chat"
        val reqData = """{"action":"subscribe","channel":"live"}"""
        val respData = """{"event":"message","text":"Hello World"}"""

        val itemSent = storage.logEvent(
            url = url,
            event = "subscribe",
            direction = "SENT",
            requestData = reqData
        )
        assertNotNull(itemSent)
        assertEquals("SENT", itemSent.direction)

        val itemReceived = storage.logEvent(
            url = url,
            event = "message",
            direction = "RECEIVED",
            responseData = respData
        )
        assertNotNull(itemReceived)
        assertEquals("RECEIVED", itemReceived.direction)

        assertEquals(2, storage.getLogCount())

        val logsForUrl = storage.getLogsForUrl(url)
        assertEquals(2, logsForUrl.size)

        val textReport = storage.exportLogsAsText()
        assertTrue(textReport.contains("wss://api.example.com/chat"))
        assertTrue(textReport.contains("subscribe"))
        assertTrue(textReport.contains("Hello World"))

        storage.clearAll()
    }

    @Test
    fun testEvictionWhenExceedingMax10MbSizeLimit() {
        val smallStorage = SocketLogStorage(maxCacheSizeBytes = 250L)
        smallStorage.clearAll()

        smallStorage.logEvent(
            url = "wss://test.com",
            event = "event1",
            direction = "SENT",
            requestData = "X".repeat(80)
        )

        smallStorage.logEvent(
            url = "wss://test.com",
            event = "event2",
            direction = "RECEIVED",
            responseData = "Y".repeat(80)
        )

        assertEquals(2, smallStorage.getLogCount())

        // Third event pushes total size over limit, oldest (event1) should be evicted
        smallStorage.logEvent(
            url = "wss://test.com",
            event = "event3",
            direction = "SENT",
            requestData = "Z".repeat(80)
        )

        val remainingLogs = smallStorage.getLogs()
        assertTrue(remainingLogs.none { it.event == "event1" })
        assertTrue(remainingLogs.any { it.event == "event2" })
        assertTrue(remainingLogs.any { it.event == "event3" })

        smallStorage.clearAll()
    }
}
