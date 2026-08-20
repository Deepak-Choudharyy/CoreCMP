package com.corecmp.shared.validation

private val PAN_REGEX = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
private val GSTIN_REGEX = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")
private val IFSC_REGEX = Regex("^[A-Z]{4}0[A-Z0-9]{6}$")
private val VEHICLE_REGEX = Regex("^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$")
private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val INDIAN_PHONE_REGEX = Regex("^[6-9][0-9]{9}$")
private val PINCODE_REGEX = Regex("^[1-9][0-9]{5}$")

private val VERHOEFF_D = arrayOf(
    intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
    intArrayOf(1, 2, 3, 4, 0, 6, 7, 8, 9, 5),
    intArrayOf(2, 3, 4, 0, 1, 7, 8, 9, 5, 6),
    intArrayOf(3, 4, 0, 1, 2, 8, 9, 5, 6, 7),
    intArrayOf(4, 0, 1, 2, 3, 9, 5, 6, 7, 8),
    intArrayOf(5, 9, 8, 7, 6, 0, 4, 3, 2, 1),
    intArrayOf(6, 5, 9, 8, 7, 1, 0, 4, 3, 2),
    intArrayOf(7, 6, 5, 9, 8, 2, 1, 0, 4, 3),
    intArrayOf(8, 7, 6, 5, 9, 3, 2, 1, 0, 4),
    intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0),
)

private val VERHOEFF_P = arrayOf(
    intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
    intArrayOf(1, 5, 7, 6, 2, 8, 3, 0, 9, 4),
    intArrayOf(5, 8, 0, 3, 7, 9, 6, 1, 4, 2),
    intArrayOf(8, 9, 1, 6, 0, 4, 3, 5, 2, 7),
    intArrayOf(9, 4, 5, 3, 1, 2, 6, 8, 7, 0),
    intArrayOf(4, 2, 8, 6, 5, 7, 3, 9, 0, 1),
    intArrayOf(2, 7, 9, 3, 8, 0, 6, 4, 1, 5),
    intArrayOf(7, 0, 4, 6, 9, 1, 3, 2, 5, 8),
)

private val VERHOEFF_INV = intArrayOf(0, 4, 3, 2, 1, 5, 6, 7, 8, 9)

fun isValidPan(value: String): Boolean {
    val normalized = value.trim().uppercase()
    return PAN_REGEX.matches(normalized)
}

fun isValidAadhaar(value: String): Boolean {
    val digits = value.filter { it.isDigit() }
    if (digits.length != 12) return false
    if (digits.all { it == digits.first() }) return false
    return verhoeffCheck(digits)
}

fun isValidGstin(value: String): Boolean {
    val normalized = value.trim().uppercase()
    return GSTIN_REGEX.matches(normalized)
}

fun isValidIfsc(value: String): Boolean {
    val normalized = value.trim().uppercase()
    return IFSC_REGEX.matches(normalized)
}

fun isValidVehicleNumber(value: String): Boolean {
    val normalized = value.filter { it.isLetterOrDigit() }.uppercase()
    return VEHICLE_REGEX.matches(normalized)
}

fun isValidEmail(value: String): Boolean {
    val normalized = value.trim()
    if (normalized.isEmpty()) return false
    return EMAIL_REGEX.matches(normalized)
}

fun isValidIndianPhone(value: String): Boolean {
    val digits = value.filter { it.isDigit() }
    val normalized = when {
        digits.length == 12 && digits.startsWith("91") -> digits.drop(2)
        digits.length == 11 && digits.startsWith("0") -> digits.drop(1)
        else -> digits
    }
    return INDIAN_PHONE_REGEX.matches(normalized)
}

fun isValidPincode(value: String): Boolean {
    val digits = value.filter { it.isDigit() }
    return PINCODE_REGEX.matches(digits)
}

private fun verhoeffCheck(number: String): Boolean {
    var checksum = 0
    val reversed = number.reversed()
    for (i in reversed.indices) {
        val digit = reversed[i].digitToIntOrNull() ?: return false
        checksum = VERHOEFF_D[checksum][VERHOEFF_P[i % 8][digit]]
    }
    return checksum == 0
}
