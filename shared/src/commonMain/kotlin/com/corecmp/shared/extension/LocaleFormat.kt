package com.corecmp.shared.extension

import com.corecmp.shared.ui.formatDateCMP

fun Number.toIndianCurrency(includePaisa: Boolean = true): String {
    val value = toDouble()
    val sign = if (value < 0) "-" else ""
    val abs = kotlin.math.abs(value)
    val whole = abs.toLong()
    val fraction = ((abs - whole) * 100).toInt()

    val grouped = whole.toString().let { raw ->
        if (raw.length <= 3) raw
        else {
            val last3 = raw.takeLast(3)
            val rest = raw.dropLast(3)
            val groupedRest = rest.reversed()
                .chunked(2)
                .joinToString(",") { it.reversed() }
                .reversed()
            "$groupedRest,$last3"
        }
    }

    return buildString {
        append(sign)
        append('₹')
        append(grouped)
        if (includePaisa) {
            append('.')
            append(fraction.toString().padStart(2, '0'))
        }
    }
}

fun Number.toIndianNumber(): String = toIndianCurrency(includePaisa = false).removePrefix("₹")

fun String.toIndianPhone(): String {
    val digits = filter { it.isDigit() }.takeLast(10)
    if (digits.length != 10) return this
    return "+91 ${digits.substring(0, 5)} ${digits.substring(5)}"
}

fun Any?.toIndianDate(format: String = "dd/MM/yyyy"): String =
    formatDateCMP(this, format)

fun Any?.toIndianDateTime(format: String = "dd/MM/yyyy hh:mm a"): String =
    formatDateCMP(this, format)

fun Double.toCompactIndianAmount(): String {
    val abs = kotlin.math.abs(this)
    val sign = if (this < 0) "-" else ""
    return when {
        abs >= 1_00_00_000 -> "$sign₹${compactUnit(abs / 1_00_00_000)}Cr"
        abs >= 1_00_000 -> "$sign₹${compactUnit(abs / 1_00_000)}L"
        abs >= 1_000 -> "$sign₹${compactUnit(abs / 1_000)}K"
        else -> toIndianCurrency()
    }
}

private fun compactUnit(value: Double): String {
    val scaled = (value * 10).toLong()
    val whole = scaled / 10
    val decimal = scaled % 10
    return if (decimal == 0L) whole.toString() else "$whole.$decimal"
}
