package com.corecmp.shared.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.Clock

data class AgeResult(val years: Int, val months: Int)

fun ageFromDob(dob: LocalDate, today: LocalDate = todayLocalDate()): AgeResult {
    var years = today.year - dob.year
    var months = today.month.number - dob.month.number
    if (today.day < dob.day) {
        months -= 1
    }
    if (months < 0) {
        years -= 1
        months += 12
    }
    return AgeResult(years = years.coerceAtLeast(0), months = months.coerceAtLeast(0))
}

fun ncbPercent(claimFreeYears: Int): Int = when {
    claimFreeYears <= 0 -> 0
    claimFreeYears == 1 -> 20
    claimFreeYears == 2 -> 25
    claimFreeYears == 3 -> 35
    claimFreeYears == 4 -> 45
    else -> 50
}

fun gstAmount(premiumExGst: Double, ratePercent: Double = 18.0): Double =
    (premiumExGst * ratePercent / 100.0 * 100.0).roundToInt() / 100.0

fun emiAmount(
    principal: Double,
    annualRatePercent: Double,
    tenureMonths: Int,
): Double {
    if (tenureMonths <= 0) return 0.0
    if (annualRatePercent <= 0.0) return (principal / tenureMonths * 100.0).roundToInt() / 100.0
    val monthlyRate = annualRatePercent / 12.0 / 100.0
    val factor = (1 + monthlyRate).pow(tenureMonths)
    val emi = principal * monthlyRate * factor / (factor - 1)
    return (emi * 100.0).roundToInt() / 100.0
}

fun rupeeInWords(amount: Long): String {
    if (amount == 0L) return "Zero Rupees Only"
    val sign = if (amount < 0) "Minus " else ""
    val value = kotlin.math.abs(amount)
    val words = buildIndianWords(value)
    return "$sign$words Rupees Only"
}

fun upiDeepLink(
    payeeAddress: String,
    payeeName: String? = null,
    amount: Double? = null,
    transactionNote: String? = null,
    transactionRef: String? = null,
): String = buildString {
    append("upi://pay?pa=")
    append(encodeUpi(payeeAddress))
    payeeName?.let {
        append("&pn=")
        append(encodeUpi(it))
    }
    amount?.let {
        append("&am=")
        append((it * 100.0).roundToInt() / 100.0)
    }
    transactionNote?.let {
        append("&tn=")
        append(encodeUpi(it))
    }
    transactionRef?.let {
        append("&tr=")
        append(encodeUpi(it))
    }
    append("&cu=INR")
}

fun formatPolicyNumber(raw: String, groupSizes: List<Int> = listOf(4, 4, 4)): String {
    val alnum = raw.filter { it.isLetterOrDigit() }.uppercase()
    if (alnum.isEmpty()) return raw
    val chunks = mutableListOf<String>()
    var index = 0
    for (size in groupSizes) {
        if (index >= alnum.length) break
        chunks += alnum.substring(index, (index + size).coerceAtMost(alnum.length))
        index += size
    }
    if (index < alnum.length) {
        chunks += alnum.substring(index)
    }
    return chunks.joinToString("-")
}

private fun todayLocalDate(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

private fun encodeUpi(value: String): String =
    value.replace(" ", "%20")

private fun buildIndianWords(number: Long): String {
    if (number == 0L) return "Zero"
    val parts = mutableListOf<String>()
    var remaining = number

    val crore = remaining / 1_00_00_000
    if (crore > 0) {
        parts += "${twoDigitWords(crore)} Crore"
        remaining %= 1_00_00_000
    }
    val lakh = remaining / 1_00_000
    if (lakh > 0) {
        parts += "${twoDigitWords(lakh)} Lakh"
        remaining %= 1_00_000
    }
    val thousand = remaining / 1_000
    if (thousand > 0) {
        parts += "${twoDigitWords(thousand)} Thousand"
        remaining %= 1_000
    }
    if (remaining > 0) {
        parts += threeDigitWords(remaining.toInt())
    }
    return parts.joinToString(" ")
}

private fun twoDigitWords(value: Long): String = threeDigitWords(value.toInt())

private fun threeDigitWords(value: Int): String {
    val ones = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen",
    )
    val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety",
    )
    val hundred = value / 100
    val remainder = value % 100
    return buildString {
        if (hundred > 0) {
            append(ones[hundred])
            append(" Hundred")
            if (remainder > 0) append(' ')
        }
        if (remainder in 1..19) {
            append(ones[remainder])
        } else if (remainder >= 20) {
            append(tens[remainder / 10])
            if (remainder % 10 != 0) {
                append(' ')
                append(ones[remainder % 10])
            }
        }
    }.trim()
}
