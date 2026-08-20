package com.corecmp.shared.validation

fun maskPhone(raw: String): String {
    val digits = raw.filter { it.isDigit() }.takeLast(10)
    if (digits.isEmpty()) return ""
    return buildString {
        append("+91 ")
        digits.forEachIndexed { index, char ->
            append(char)
            if (index == 4 && index != digits.lastIndex) append(' ')
        }
    }
}

fun maskPan(raw: String): String =
    raw.filter { it.isLetterOrDigit() }
        .uppercase()
        .take(10)

fun maskVehicle(raw: String): String {
    val cleaned = raw.filter { it.isLetterOrDigit() }.uppercase()
    if (cleaned.length <= 2) return cleaned
    if (cleaned.length <= 4) return "${cleaned.take(2)}-${cleaned.drop(2)}"
    if (cleaned.length <= 6) {
        return "${cleaned.take(2)}-${cleaned.substring(2, 4)}-${cleaned.drop(4)}"
    }
    return "${cleaned.take(2)}-${cleaned.substring(2, 4)}-${cleaned.substring(4, 6)}-${cleaned.drop(6).take(4)}"
}

fun maskCurrency(raw: String): String {
    val digits = raw.filter { it.isDigit() || it == '.' }
    if (digits.isEmpty()) return ""

    val parts = digits.split('.')
    val whole = parts.firstOrNull().orEmpty().trimStart('0').ifEmpty { "0" }
    val fraction = parts.getOrNull(1)?.take(2).orEmpty()

    val groupedWhole = whole.reversed()
        .chunked(3)
        .joinToString(",") { chunk -> chunk.reversed() }
        .reversed()

    return if (fraction.isNotEmpty() || digits.contains('.')) {
        "$groupedWhole.$fraction"
    } else {
        groupedWhole
    }
}

fun formatCurrencyDisplay(amount: Double, symbol: String = "₹"): String {
    val whole = amount.toLong()
    val fraction = ((amount - whole) * 100).toInt().coerceIn(0, 99)
    val grouped = maskCurrency(whole.toString())
    return if (fraction == 0) "$symbol$grouped" else "$symbol$grouped.${fraction.toString().padStart(2, '0')}"
}
