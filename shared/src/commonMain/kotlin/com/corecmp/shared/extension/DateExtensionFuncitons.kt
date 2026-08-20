package com.corecmp.shared.extension

import com.corecmp.shared.ui.formatDateCMP
import com.corecmp.shared.ui.tz
import io.ktor.util.date.getTimeMillis
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

fun Any?.toDdMmmYyyy(
    format: String = "dd MMM yyyy"
) = formatDateCMP(this, format)

fun Any?.toYyyyMmDd(
    format: String = "yyyy/MM/dd"
) = formatDateCMP(this, format)

fun Any?.toServerDate(
    format: String = "yyyy-MM-dd"
) = formatDateCMP(this, format)

fun Any?.toDateTimeSeconds(
    format: String = "dd MMM yyyy hh:mm:ss a"
) = formatDateCMP(this, format)

fun Any?.toDateTime(
    format: String = "dd MMM yyyy hh:mm a"
) = formatDateCMP(this, format)

object DateUtils {

    private fun now(): LocalDateTime {
        val instant = Instant.fromEpochMilliseconds(
                getTimeMillis()
            )
        return instant.toLocalDateTime(tz)
    }

    fun currentDate(
        format: String = "dd MMM yyyy"
    ) = formatDateCMP(now(), format)

    fun currentDateTime(
        format: String = "dd MMM yyyy hh:mm a"
    ) = formatDateCMP(now(), format)

    fun currentYear() = now().year.toString()

    fun currentMonthNumber() = now().month.number
            .toString()
            .padStart(2, '0')


    fun currentMonthName() = now().month.name
            .lowercase()
            .replaceFirstChar { it.uppercase() }

    fun currentDay() = now().day
            .toString()
            .padStart(2, '0')
}

fun currentDate() = DateUtils.currentDate()

fun currentDateTime() = DateUtils.currentDateTime()

fun currentYear() = DateUtils.currentYear()

fun currentMonthNumber() = DateUtils.currentMonthNumber()

fun currentMonthName() = DateUtils.currentMonthName()

fun currentDay() = DateUtils.currentDay()