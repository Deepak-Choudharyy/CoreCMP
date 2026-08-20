package com.corecmp.shared.security

private val PAN_REGEX = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")

fun maskPan(pan: String): String {
    val normalized = pan.uppercase().filter { it.isLetterOrDigit() }
    if (normalized.length != 10 || !PAN_REGEX.matches(normalized)) return pan
    return normalized.substring(0, 5) + "****" + normalized.last()
}

fun maskPhone(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    if (digits.length < 4) return phone
    val lastFour = digits.takeLast(4)
    val prefix = digits.dropLast(4).takeLast(2).ifEmpty { "**" }
    return "$prefix****$lastFour"
}

fun maskAadhaar(aadhaar: String): String {
    val digits = aadhaar.filter { it.isDigit() }
    if (digits.length != 12) return aadhaar
    return "XXXX XXXX ${digits.takeLast(4)}"
}

fun redactPiiFromLog(message: String): String {
    var result = message
    PAN_REGEX.findAll(message).forEach { match ->
        result = result.replace(match.value, maskPan(match.value))
    }
    Regex("\\b\\d{12}\\b").findAll(result).forEach { match ->
        result = result.replace(match.value, maskAadhaar(match.value))
    }
    Regex("\\b\\d{10}\\b").findAll(result).forEach { match ->
        result = result.replace(match.value, maskPhone(match.value))
    }
    return result
}
