package com.corecmp.shared.domain

private val PAN_REGEX = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
private val AADHAAR_REGEX = Regex("^\\d{12}$")
private val GSTIN_REGEX = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")
private val IFSC_REGEX = Regex("^[A-Z]{4}0[A-Z0-9]{6}$")
private val PINCODE_REGEX = Regex("^[1-9][0-9]{5}$")
private val MOBILE_REGEX = Regex("^[6-9][0-9]{9}$")
private val VEHICLE_REGEX = Regex("^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$")

fun isValidPan(value: String): Boolean =
    PAN_REGEX.matches(value.uppercase().filter { it.isLetterOrDigit() })

fun isValidAadhaar(value: String): Boolean {
    val digits = value.filter { it.isDigit() }
    if (!AADHAAR_REGEX.matches(digits)) return false
    return verhoeffCheck(digits)
}

fun isValidGstin(value: String): Boolean =
    GSTIN_REGEX.matches(value.uppercase())

fun isValidIfsc(value: String): Boolean =
    IFSC_REGEX.matches(value.uppercase())

fun isValidPincode(value: String): Boolean =
    PINCODE_REGEX.matches(value.filter { it.isDigit() })

fun isValidIndianMobile(value: String): Boolean =
    MOBILE_REGEX.matches(value.filter { it.isDigit() }.takeLast(10))

fun isValidVehicleNumber(value: String): Boolean =
    VEHICLE_REGEX.matches(value.uppercase().replace(" ", ""))

fun normalizePan(value: String): String =
    value.uppercase().filter { it.isLetterOrDigit() }

fun normalizeIndianMobile(value: String): String =
    value.filter { it.isDigit() }.takeLast(10)

private fun verhoeffCheck(number: String): Boolean {
    val d = arrayOf(
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
    val p = arrayOf(
        intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
        intArrayOf(1, 5, 7, 6, 2, 8, 3, 0, 9, 4),
        intArrayOf(5, 8, 0, 3, 7, 9, 6, 1, 4, 2),
        intArrayOf(8, 9, 1, 6, 0, 4, 3, 5, 2, 7),
        intArrayOf(9, 4, 5, 3, 1, 2, 6, 8, 7, 0),
        intArrayOf(4, 2, 8, 6, 5, 7, 3, 9, 0, 1),
        intArrayOf(2, 7, 9, 3, 8, 0, 6, 4, 1, 5),
        intArrayOf(7, 0, 4, 6, 9, 1, 3, 2, 5, 8),
    )
    var c = 0
    number.reversed().forEachIndexed { index, char ->
        c = d[c][p[index % 8][char.digitToInt()]]
    }
    return c == 0
}
