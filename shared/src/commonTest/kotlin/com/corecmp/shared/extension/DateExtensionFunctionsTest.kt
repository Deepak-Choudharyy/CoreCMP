package com.corecmp.shared.extension

import kotlin.test.Test
import kotlin.test.assertEquals

class DateExtensionFunctionsTest {

    @Test
    fun testToDdMmmYyyy() {
        val dateStr = "2026-06-03T12:34:56"
        val formatted = dateStr.toDdMmmYyyy()
        assertEquals("03 Jun 2026", formatted)
    }

    @Test
    fun testToYyyyMmDd() {
        val dateStr = "2026-06-03"
        val formatted = dateStr.toYyyyMmDd()
        assertEquals("2026/06/03", formatted)
    }

    @Test
    fun testToServerDate() {
        val dateStr = "03-06-2026"
        val formatted = dateStr.toServerDate()
        assertEquals("2026-06-03", formatted)
    }

    @Test
    fun testToDateTimeSeconds() {
        val dateStr = "2026-06-03T15:04:05"
        val formatted = dateStr.toDateTimeSeconds()
        assertEquals("03 Jun 2026 03:04:05 PM", formatted)
    }

    @Test
    fun testToDateTime() {
        val dateStr = "2026-06-03T09:05:00"
        val formatted = dateStr.toDateTime()
        assertEquals("03 Jun 2026 09:05 AM", formatted)
    }

    @Test
    fun testDateUtils() {
        // Assert year is 4 digits
        val yr = currentYear()
        assertEquals(4, yr.length)
        
        // Assert month number is 2 digits
        val mn = currentMonthNumber()
        assertEquals(2, mn.length)
        
        // Assert day is 2 digits
        val dy = currentDay()
        assertEquals(2, dy.length)
        
        // Assert month name is not empty
        val monthName = currentMonthName()
        kotlin.test.assertTrue(monthName.isNotEmpty())
    }
}
