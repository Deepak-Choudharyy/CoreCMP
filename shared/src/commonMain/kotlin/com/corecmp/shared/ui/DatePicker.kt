package com.corecmp.shared.ui

import kotlinx.datetime.*
import kotlinx.datetime.format.*
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

internal val tz: TimeZone
    get() = TimeZone.currentSystemDefault()

@OptIn(FormatStringsInDatetimeFormats::class)
internal fun formatDateCMP(
    input: Any?,
    outputFormat: String
): String {
    if (input == null) return ""

    return try {
        when (input) {
            is Long -> {
                val dt = Instant.fromEpochMilliseconds(input).toLocalDateTime(tz)
                formatLocalDateTime(dt, outputFormat)
            }
            is Instant -> {
                val dt = input.toLocalDateTime(tz)
                formatLocalDateTime(dt, outputFormat)
            }
            is LocalDateTime -> {
                formatLocalDateTime(input, outputFormat)
            }
            is LocalDate -> {
                formatLocalDate(input, outputFormat)
            }
            else -> {
                parseFromString(input.toString(), outputFormat)
            }
        }
    } catch (_: Exception) {
        input.toString()
    }
}

// --------------------
// STRING PARSER
// --------------------

@OptIn(FormatStringsInDatetimeFormats::class)
private fun parseFromString(
    raw: String,
    outputFormat: String
): String {
    val text = raw.trim()
    if (text.isEmpty()) return ""

    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
        "dd-MM-yyyy",
        "dd/MM/yyyy",
        "yyyy/MM/dd",
        "yyyyMMdd"
    )

    patterns.forEach { pattern ->
        // try datetime
        try {
            val formatter = LocalDateTime.Format {
                byUnicodePattern(pattern)
            }
            val parsed = LocalDateTime.parse(text, formatter)
            return formatLocalDateTime(parsed, outputFormat)
        } catch (_: Exception) {}

        // try date
        try {
            val formatter = LocalDate.Format {
                byUnicodePattern(pattern)
            }
            val parsed = LocalDate.parse(text, formatter)
            return formatLocalDate(parsed, outputFormat)
        } catch (_: Exception) {}
    }

    text.toLongOrNull()?.let {
        val dt = Instant.fromEpochMilliseconds(it).toLocalDateTime(tz)
        return formatLocalDateTime(dt, outputFormat)
    }

    return text
}

// --------------------
// CUSTOM FORMATTER
// --------------------

private fun formatLocalDateTime(dateTime: LocalDateTime, pattern: String): String {
    val year = dateTime.year
    val month = dateTime.monthNumber
    val day = dateTime.dayOfMonth
    val hour24 = dateTime.hour
    val minute = dateTime.minute
    val second = dateTime.second

    val isPm = hour24 >= 12
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    val amPmStr = if (isPm) "PM" else "AM"

    val monthNamesAbbr = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val monthNamesFull = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val mNameAbbr = monthNamesAbbr.getOrNull(month - 1) ?: ""
    val mNameFull = monthNamesFull.getOrNull(month - 1) ?: ""

    val sb = StringBuilder()
    var i = 0
    while (i < pattern.length) {
        val c = pattern[i]
        
        // Handle literal text in single quotes
        if (c == '\'') {
            var j = i + 1
            while (j < pattern.length && pattern[j] != '\'') {
                sb.append(pattern[j])
                j++
            }
            i = if (j < pattern.length) j + 1 else j
            continue
        }

        // Count consecutive occurrences of the same character
        var count = 1
        while (i + count < pattern.length && pattern[i + count] == c) {
            count++
        }

        when (c) {
            'y' -> {
                val yStr = year.toString()
                if (count >= 4) {
                    sb.append(yStr.padStart(count, '0'))
                } else if (count == 2) {
                    sb.append(yStr.takeLast(2))
                } else {
                    sb.append(yStr)
                }
            }
            'M' -> {
                when (count) {
                    4 -> sb.append(mNameFull)
                    3 -> sb.append(mNameAbbr)
                    2 -> sb.append(month.toString().padStart(2, '0'))
                    else -> sb.append(month.toString())
                }
            }
            'd' -> {
                if (count >= 2) {
                    sb.append(day.toString().padStart(2, '0'))
                } else {
                    sb.append(day.toString())
                }
            }
            'H' -> {
                if (count >= 2) {
                    sb.append(hour24.toString().padStart(2, '0'))
                } else {
                    sb.append(hour24.toString())
                }
            }
            'h' -> {
                if (count >= 2) {
                    sb.append(hour12.toString().padStart(2, '0'))
                } else {
                    sb.append(hour12.toString())
                }
            }
            'm' -> {
                if (count >= 2) {
                    sb.append(minute.toString().padStart(2, '0'))
                } else {
                    sb.append(minute.toString())
                }
            }
            's' -> {
                if (count >= 2) {
                    sb.append(second.toString().padStart(2, '0'))
                } else {
                    sb.append(second.toString())
                }
            }
            'a' -> {
                sb.append(amPmStr)
            }
            else -> {
                for (k in 0 until count) {
                    sb.append(c)
                }
            }
        }
        i += count
    }
    return sb.toString()
}

private fun formatLocalDate(date: LocalDate, pattern: String): String {
    val dateTime = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 0, 0, 0, 0)
    return formatLocalDateTime(dateTime, pattern)
}

fun formatDateMillis(
    millis: Long?,
    format: String = "dd MMM yyyy"
): String {
    if (millis == null) return ""
    return formatDateCMP(
        millis,
        format
    )
}